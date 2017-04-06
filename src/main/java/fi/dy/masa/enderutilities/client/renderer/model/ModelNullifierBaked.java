package fi.dy.masa.enderutilities.client.renderer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.ItemType;

public class ModelNullifierBaked implements IBakedModel, IPerspectiveAwareModel
{
    private static final Map<NullifierState, IBakedModel> NULLIFIER_MODEL_CACHE = new HashMap<NullifierState, IBakedModel>();
    private static final Map<ItemType, IBakedModel> ITEM_MODEL_CACHE = new HashMap<ItemType, IBakedModel>();
    private static final Map<Integer, IBakedModel>  TEXT_MODEL_CACHE = new HashMap<Integer, IBakedModel>();
    private static final ImmutableList<BakedQuad> EMPTY_LIST = ImmutableList.of();
    private final IBakedModel modelBase;
    private final IBakedModel modelLocked;
    private final IModelState modelState;
    private final VertexFormat format;
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
        this.particle = bakedTextureGetter.apply(new ResourceLocation(ModelNullifier.TEX_BASE));
        this.transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);
    }

    private ModelNullifierBaked(ModelNullifierBaked nullifierModel, boolean locked, ItemStack containedStack)
    {
        this.modelState = nullifierModel.modelState;
        this.modelBase = nullifierModel.modelBase;
        this.modelLocked = nullifierModel.modelLocked;
        this.format = nullifierModel.format;
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
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, this.transformMap, cameraTransformType);
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

        if (containedStack != null)
        {
            ItemType type = new ItemType(containedStack);
            itemModel = ITEM_MODEL_CACHE.get(type);

            if (itemModel == null)
            {
                //TRSRTransformation tr = new TRSRTransformation(new Vector3f(mt.tx, mt.ty, mt.tz), null, new Vector3f(mt.sx, mt.sy, mt.sz), null);
                //new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(tr));
                itemModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(containedStack, null, null);
                ITEM_MODEL_CACHE.put(type, itemModel);
            }

            /* TODO Add/enable in 1.11 using the Forge font model renderer
            int stackSize = containedStack.getCount();

            if (stackSize > 1)
            {
                textModel = TEXT_MODEL_CACHE.get(stackSize);

                if (textModel == null)
                {
                    textModel = todo;
                    TEXT_MODEL_CACHE.put(stackSize, model);
                }
            }
            */
        }

        this.addQuadsForSide(null, nullifierModel, itemModel, textModel, locked);

        for (EnumFacing side : EnumFacing.values())
        {
            this.addQuadsForSide(side, nullifierModel, itemModel, textModel, locked);
        }
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

        if (textModel != null)
        {
            builder.addAll(textModel.getQuads(null, side, 0));
        }

        this.quads.put(side, builder.build());
        //System.out.printf("ModelNullifierBaked#buildQuadsForSide() - side: %s - count: %d\n", side, this.quads.get(side).size());
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
                //System.out.printf("ModelNullifierBakedOverrideHandler#handleItemState() - creating a new model\n");
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
        private final int stackSize;

        public NullifierState(ItemStack nullifierStack)
        {
            this.locked = ItemNullifier.isNullifierEnabled(nullifierStack) == false;
            ItemStack containedStack = ItemNullifier.getSelectedStack(nullifierStack);
            this.containedItem = containedStack != null ? new ItemType(containedStack) : null;
            this.stackSize = containedStack != null ? containedStack.stackSize : 0;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((containedItem == null) ? 0 : containedItem.hashCode());
            result = prime * result + (locked ? 1231 : 1237);
            result = prime * result + stackSize;
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
            if (stackSize != other.stackSize)
                return false;
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
                lockedModel = ((IRetexturableModel) lockedModel).retexture(ImmutableMap.of("layer0", TEX_LOCKED));
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for the Nullifier!");
            }

            //System.out.printf("ModelNullifier#bake()\n");
            return new ModelNullifierBaked(baseModel, lockedModel, state, format, bakedTextureGetter);
        }
    }

    public static class ModelLoader implements ICustomModelLoader
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
            //System.out.printf("ModelLoader#loadModel(): %s\n", modelLocation);
            return new ModelNullifier();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            NULLIFIER_MODEL_CACHE.clear();
            ITEM_MODEL_CACHE.clear();
            TEXT_MODEL_CACHE.clear();
        }
    }
}
