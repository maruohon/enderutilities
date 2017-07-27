package fi.dy.masa.enderutilities.client.renderer.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IRegistryDelegate;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.ItemType;
import gnu.trove.map.hash.TIntObjectHashMap;

@EventBusSubscriber(Side.CLIENT)
public class ModelNullifierBaked implements IBakedModel
{
    private static ModelLoader MODEL_LOADER;
    private static Map<ModelResourceLocation, IModel> STATE_MODELS;
    private static Map<IRegistryDelegate<Item>, TIntObjectHashMap<ModelResourceLocation>> LOCATIONS;
    private static Map<Item, ItemMeshDefinition> SHAPERS;

    private static final Map<NullifierState, IBakedModel> NULLIFIER_MODEL_CACHE = new HashMap<NullifierState, IBakedModel>();
    private static final Map<ItemType, IBakedModel> ITEM_MODEL_CACHE = new HashMap<ItemType, IBakedModel>();
    private static final ImmutableList<BakedQuad> EMPTY_LIST = ImmutableList.of();
    private final IBakedModel modelBase;
    private final IBakedModel modelLocked;
    private final IModelState modelState;
    private final VertexFormat format;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<TransformType, TRSRTransformation> transformMap;
    private final Map<EnumFacing, ImmutableList<BakedQuad>> quads = new HashMap<EnumFacing, ImmutableList<BakedQuad>>();

    private ModelNullifierBaked(IModel baseModel, IModel lockedModel, IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.modelState = state;
        this.modelBase = baseModel.bake(state, format, bakedTextureGetter);
        this.modelLocked = lockedModel.bake(state, format, bakedTextureGetter);
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.particle = bakedTextureGetter.apply(new ResourceLocation(ModelNullifier.TEX_BASE));
        this.transformMap = PerspectiveMapWrapper.getTransforms(state);
    }

    private ModelNullifierBaked(ModelNullifierBaked nullifierModel, boolean locked, ItemStack containedStack)
    {
        this.modelState = nullifierModel.modelState;
        this.modelBase = nullifierModel.modelBase;
        this.modelLocked = nullifierModel.modelLocked;
        this.format = nullifierModel.format;
        this.bakedTextureGetter = nullifierModel.bakedTextureGetter;
        this.particle = nullifierModel.particle;
        this.transformMap = nullifierModel.transformMap;

        this.addQuads(nullifierModel, locked, containedStack);
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return this.modelBase.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return this.modelBase.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return this.modelBase.isBuiltInRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.modelBase.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return ModelNullifierBakedOverrideHandler.INSTANCE;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return this.modelBase.getParticleTexture();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
    {
        return PerspectiveMapWrapper.handlePerspective(this, this.transformMap, cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
    {
        ImmutableList<BakedQuad> list = this.quads.get(side);
        return list != null ? list : EMPTY_LIST;
    }

    private void addQuads(ModelNullifierBaked nullifierModel, boolean locked, ItemStack containedStack)
    {
        IBakedModel itemModel = null;
        IBakedModel textModel = null;

        if (containedStack.isEmpty() == false)
        {
            ItemType type = new ItemType(containedStack, true);
            itemModel = ITEM_MODEL_CACHE.get(type);

            if (itemModel == null)
            {
                IModel iModel = this.getItemModel(containedStack);

                if (iModel != null && iModel.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel") == false)
                {
                    TRSRTransformation trn = new TRSRTransformation(new javax.vecmath.Vector3f(-0.5f, -0.5f, -0.5f), null, null, null);
                    TRSRTransformation trr = new TRSRTransformation(ModelRotation.X0_Y180);
                    TRSRTransformation trp = new TRSRTransformation(new javax.vecmath.Vector3f( 0.5f,  0.5f,  0.5f), null, null, null);
                    TRSRTransformation trs = new TRSRTransformation(null, null, new javax.vecmath.Vector3f(0.6f, 0.6f, 0.6f), null);
                    TRSRTransformation tr = trn.compose(trr).compose(trp).compose(trs);

                    IModelState state = new ModelStateComposition(this.modelState, TRSRTransformation.blockCenterToCorner(tr));
                    itemModel = iModel.bake(state, this.format, this.bakedTextureGetter);
                }
                else
                {
                    Minecraft mc = Minecraft.getMinecraft();
                    itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(containedStack);
                    itemModel = itemModel.getOverrides().handleItemState(itemModel, containedStack, mc.world, mc.player);
                }

                ITEM_MODEL_CACHE.put(type, itemModel);
            }
        }

        this.addQuadsForSide(null, nullifierModel, itemModel, textModel, locked);

        for (EnumFacing side : EnumFacing.values())
        {
            this.addQuadsForSide(side, nullifierModel, itemModel, textModel, locked);
        }
    }

    @Nullable
    private IModel getItemModel(ItemStack stack)
    {
        // Unfortunately this can't be done before the init phase...
        this.reflectMaps();

        Item item = stack.getItem();
        ModelResourceLocation mrl = null;
        TIntObjectHashMap<ModelResourceLocation> map = LOCATIONS.get(item.delegate);

        if (map != null)
        {
            mrl = map.get(stack.getMetadata());
        }

        if (mrl == null)
        {
            ItemMeshDefinition mesh = SHAPERS.get(item);

            if (mesh != null)
            {
                mrl = mesh.getModelLocation(stack);
            }
        }

        if (mrl != null)
        {
            try
            {
                return ModelLoaderRegistry.getModel(mrl);
            }
            catch (Exception e)
            {
                return STATE_MODELS.get(mrl);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void reflectMaps()
    {
        if (LOCATIONS == null || SHAPERS == null)
        {
            try
            {
                ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

                Field locs = ReflectionHelper.findField(ItemModelMesherForge.class, "locations");
                LOCATIONS = (Map<IRegistryDelegate<Item>, TIntObjectHashMap<ModelResourceLocation>>) locs.get(mesher);

                Field shapers = ReflectionHelper.findField(ItemModelMesher.class, "field_178092_c", "shapers");
                SHAPERS = (Map<Item, ItemMeshDefinition>) shapers.get(mesher);

                Field models = ReflectionHelper.findField(ModelLoader.class, "stateModels");
                STATE_MODELS = (Map<ModelResourceLocation, IModel>) models.get(MODEL_LOADER);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("ModelNullifierBaked: Failed to reflect model maps", e);
            }
        }
    }

    @SubscribeEvent
    public static void onModelbakeEvent(ModelBakeEvent event)
    {
        MODEL_LOADER = event.getModelLoader();
    }

    private void addQuadsForSide(EnumFacing side, ModelNullifierBaked nullifierModel, IBakedModel itemModel, IBakedModel textModel, boolean locked)
    {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        builder.addAll(nullifierModel.modelBase.getQuads(null, side, 0));

        if (locked)
        {
            builder.addAll(nullifierModel.modelLocked.getQuads(null, side, 0));
        }

        if (itemModel != null)
        {
            builder.addAll(itemModel.getQuads(null, side, 0));
        }

        this.quads.put(side, builder.build());
    }

    private static final class ModelNullifierBakedOverrideHandler extends ItemOverrideList
    {
        public static final ModelNullifierBakedOverrideHandler INSTANCE = new ModelNullifierBakedOverrideHandler();

        private ModelNullifierBakedOverrideHandler()
        {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModelIn, @Nonnull ItemStack stack, World world, EntityLivingBase entity)
        {
            ModelNullifierBaked originalModel = (ModelNullifierBaked) originalModelIn;
            NullifierState state = new NullifierState(stack);
            IBakedModel model = NULLIFIER_MODEL_CACHE.get(state);

            if (model == null)
            {
                model = new ModelNullifierBaked(originalModel, state.locked, ItemNullifier.getSelectedStack(stack));
                NULLIFIER_MODEL_CACHE.put(state, model);
            }

            return model;
        }
    }

    private static class NullifierState
    {
        private final ItemType containedItem;
        private final boolean locked;
        //private final int stackSize;

        public NullifierState(ItemStack nullifierStack)
        {
            this.locked = ItemNullifier.isNullifierEnabled(nullifierStack) == false;
            ItemStack containedStack = ItemNullifier.getSelectedStack(nullifierStack);
            this.containedItem = containedStack.isEmpty() ? null : new ItemType(containedStack, true);
            //this.stackSize = containedStack.getCount();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((containedItem == null) ? 0 : containedItem.hashCode());
            result = prime * result + (locked ? 1231 : 1237);
            //result = prime * result + stackSize;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NullifierState other = (NullifierState) obj;
            if (containedItem == null)
            {
                if (other.containedItem != null)
                    return false;
            }
            else if (!containedItem.equals(other.containedItem))
                return false;
            if (locked != other.locked)
                return false;
            //if (stackSize != other.stackSize)
            //    return false;
            return true;
        }
    }

    private static class ModelNullifier implements IModel
    {
        public static final ResourceLocation BASE_MODEL     = new ResourceLocation(Reference.MOD_ID, "item/nullifier_base");
        public static final ResourceLocation LOCKED_MODEL   = new ResourceLocation(Reference.MOD_ID, "item/standard_item");
        public static final String TEX_BASE     = Reference.MOD_ID + ":items/nullifier";
        public static final String TEX_LOCKED   = Reference.MOD_ID + ":items/item_overlay_locked";

        @Override
        public IModelState getDefaultState()
        {
            return TRSRTransformation.identity();
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Lists.newArrayList(BASE_MODEL, LOCKED_MODEL);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            List<ResourceLocation> textures = Lists.newArrayList();

            textures.add(new ResourceLocation(TEX_BASE));
            textures.add(new ResourceLocation(TEX_LOCKED));

            return textures;
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            IModel baseModel = null;
            IModel lockedModel = null;

            try
            {
                baseModel   = ModelLoaderRegistry.getModel(BASE_MODEL);
                lockedModel = ModelLoaderRegistry.getModel(LOCKED_MODEL);
                lockedModel = lockedModel.retexture(ImmutableMap.of("layer0", TEX_LOCKED));
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for the Nullifier!");
            }

            return new ModelNullifierBaked(baseModel, lockedModel, state, format, bakedTextureGetter);
        }
    }

    public static class ModelLoaderNullifier implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION = new ResourceLocation(Reference.MOD_ID, "models/block/custom/nullifier");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            return new ModelNullifier();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            NULLIFIER_MODEL_CACHE.clear();
            ITEM_MODEL_CACHE.clear();
        }
    }
}
