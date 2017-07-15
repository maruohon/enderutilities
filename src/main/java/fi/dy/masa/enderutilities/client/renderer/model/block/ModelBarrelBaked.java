package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockBarrel;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelBarrelBaked implements IBakedModel
{
    public static final String TEX_NORMAL_FRONT   = Reference.MOD_ID + ":blocks/barrel_normal_front";
    public static final String TEX_NORMAL_SIDE    = Reference.MOD_ID + ":blocks/barrel_normal_side";
    public static final String TEX_NORMAL_TOP     = Reference.MOD_ID + ":blocks/barrel_normal_top";
    public static final String TEX_CREATIVE_FRONT = Reference.MOD_ID + ":blocks/barrel_creative_front";
    public static final String TEX_CREATIVE_SIDE  = Reference.MOD_ID + ":blocks/barrel_creative_side";
    public static final String TEX_CREATIVE_TOP   = Reference.MOD_ID + ":blocks/barrel_creative_top";

    private static final Map<IBlockState, IBakedModel> MODEL_CACHE = new HashMap<IBlockState, IBakedModel>();
    private final Map<String, String> textures;
    private final IModel baseModel;
    private final VertexFormat format;
    private final IBakedModel bakedBaseModel;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private final TextureAtlasSprite particle;

    public ModelBarrelBaked(ModelBarrelBase modelBarrel, IModel baseModel, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.textures = modelBarrel.getTextureMapping();
        this.baseModel = baseModel;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.bakedBaseModel = baseModel.bake(state, format, bakedTextureGetter);
        this.particle = bakedTextureGetter.apply(new ResourceLocation(this.textures.get("particle")));
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return this.bakedBaseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return this.bakedBaseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return this.bakedBaseModel.isBuiltInRenderer();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.bakedBaseModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return this.bakedBaseModel.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return this.particle;
    }

    @Override
    synchronized public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        // Item model
        if (state == null)
        {
            state = EnderUtilitiesBlocks.BARREL.getDefaultState().withProperty(BlockBarrel.LABEL_FRONT, true);
        }

        IBakedModel bakedModel = MODEL_CACHE.get(state);

        if (bakedModel == null)
        {
            IModel model = ((IRetexturableModel) this.baseModel).retexture(this.getTextures(state, this.textures));
            bakedModel = model.bake(new TRSRTransformation(state.getValue(BlockBarrel.FACING_H)), this.format, this.bakedTextureGetter);
            MODEL_CACHE.put(state, bakedModel);
        }

        return bakedModel.getQuads(state, side, rand);
    }

    private ImmutableMap<String, String> getTextures(IBlockState state, Map<String, String> texturesIn)
    {
        Map<String, String> texturesOut = new HashMap<String, String>();

        String texFront = texturesIn.get("front");
        String texSide  = texturesIn.get("side");
        String texUp    = texturesIn.get("up");

        texturesOut.put("particle", texUp);

        texturesOut.put("up",    state.getValue(BlockBarrel.LABEL_UP)    ? texFront : texUp);
        texturesOut.put("down",  state.getValue(BlockBarrel.LABEL_DOWN)  ? texFront : texUp);
        texturesOut.put("north", state.getValue(BlockBarrel.LABEL_FRONT) ? texFront : texSide);
        texturesOut.put("south", state.getValue(BlockBarrel.LABEL_BACK)  ? texFront : texSide);
        texturesOut.put("west",  state.getValue(BlockBarrel.LABEL_RIGHT) ? texFront : texSide);
        texturesOut.put("east",  state.getValue(BlockBarrel.LABEL_LEFT)  ? texFront : texSide);

        return ImmutableMap.copyOf(texturesOut);
    }

    private static abstract class ModelBarrelBase implements IModel
    {
        private static final ResourceLocation BASE_MODEL = new ResourceLocation(Reference.MOD_ID, "block/barrel_base");

        protected final Map<String, String> textures = new HashMap<String, String>();

        @Override
        public IModelState getDefaultState()
        {
            return ModelRotation.X0_Y0;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Lists.newArrayList(BASE_MODEL);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            List<ResourceLocation> textures = Lists.newArrayList();

            for (String name : this.getTextureMapping().values())
            {
                textures.add(new ResourceLocation(name));
            }

            return textures;
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            IModel baseModel = null;

            try
            {
                baseModel = ModelLoaderRegistry.getModel(BASE_MODEL);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for the Barrel!");
            }

            return new ModelBarrelBaked(this, baseModel, state, format, bakedTextureGetter);
        }

        public ImmutableMap<String, String> getTextureMapping()
        {
            return ImmutableMap.copyOf(this.textures);
        }
    }

    private static class ModelBarrelNormal extends ModelBarrelBase
    {
        private ModelBarrelNormal()
        {
            this.textures.put("particle",   TEX_NORMAL_TOP);
            this.textures.put("front",      TEX_NORMAL_FRONT);
            this.textures.put("side",       TEX_NORMAL_SIDE);
            this.textures.put("up",         TEX_NORMAL_TOP);
        }
    }

    private static class ModelBarrelCreative extends ModelBarrelBase
    {
        private ModelBarrelCreative()
        {
            this.textures.put("particle",   TEX_CREATIVE_TOP);
            this.textures.put("front",      TEX_CREATIVE_FRONT);
            this.textures.put("side",       TEX_CREATIVE_SIDE);
            this.textures.put("up",         TEX_CREATIVE_TOP);
        }
    }

    public static class ModelLoaderBarrel implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION_NORMAL   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/barrel_normal");
        private static final ResourceLocation FAKE_LOCATION_CREATIVE = new ResourceLocation(Reference.MOD_ID, "models/block/custom/barrel_creative");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION_NORMAL) || modelLocation.equals(FAKE_LOCATION_CREATIVE);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if (modelLocation.equals(FAKE_LOCATION_CREATIVE))
            {
                return new ModelBarrelCreative();
            }
            else
            {
                return new ModelBarrelNormal();
            }
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            MODEL_CACHE.clear();
        }
    }

    public static class StateMapper extends StateMapperBase
    {
        private static final ModelResourceLocation LOCATION_NORMAL   = new ModelResourceLocation(Reference.MOD_ID + ":barrel", "creative=false");
        private static final ModelResourceLocation LOCATION_CREATIVE = new ModelResourceLocation(Reference.MOD_ID + ":barrel", "creative=true");

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state)
        {
            return state.getValue(BlockBarrel.CREATIVE) ? LOCATION_CREATIVE : LOCATION_NORMAL;
        }
    }
}
