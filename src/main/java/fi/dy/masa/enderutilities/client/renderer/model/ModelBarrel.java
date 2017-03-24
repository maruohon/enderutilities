package fi.dy.masa.enderutilities.client.renderer.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockBarrel;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelBarrel implements IBakedModel
{
    public static final ResourceLocation BASE_MODEL = new ResourceLocation(Reference.MOD_ID, "block/barrel_base");
    public static final String TEX_NORMAL_FRONT   = Reference.MOD_ID + ":blocks/barrel_normal_front";
    public static final String TEX_NORMAL_SIDE    = Reference.MOD_ID + ":blocks/barrel_normal_side";
    public static final String TEX_NORMAL_TOP     = Reference.MOD_ID + ":blocks/barrel_normal_top";
    public static final String TEX_CREATIVE_FRONT = Reference.MOD_ID + ":blocks/barrel_creative_front";
    public static final String TEX_CREATIVE_SIDE  = Reference.MOD_ID + ":blocks/barrel_creative_side";
    public static final String TEX_CREATIVE_TOP   = Reference.MOD_ID + ":blocks/barrel_creative_top";

    private static final Map<IBlockState, IBakedModel> MODEL_CACHE = new HashMap<IBlockState, IBakedModel>();
    private final IModel baseModel;
    private final IModelState modelState;
    private final VertexFormat format;
    private final IBakedModel bakedBaseModel;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

    public ModelBarrel(IModel baseModel, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.baseModel = baseModel;
        this.modelState = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.bakedBaseModel = baseModel.bake(state, format, bakedTextureGetter);
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
        return this.bakedBaseModel.getParticleTexture();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        if (state == null)
        {
            state = EnderUtilitiesBlocks.BARREL.getDefaultState().withProperty(BlockBarrel.LABEL_FRONT, true);
        }

        IBakedModel bakedModel = MODEL_CACHE.get(state);

        if (bakedModel == null)
        {
            IModel model = ((IRetexturableModel) this.baseModel).retexture(this.getTextures(state));
            bakedModel = model.bake(this.modelState, this.format, this.bakedTextureGetter);
            MODEL_CACHE.put(state, bakedModel);
        }

        return bakedModel.getQuads(state, side, rand);
    }

    private ImmutableMap<String, String> getTextures(IBlockState state)
    {
        Map<String, String> textures = new HashMap<String, String>();

        if (state.getValue(BlockBarrel.CREATIVE))
        {
            textures.put("particle", TEX_CREATIVE_TOP);
            if (state.getValue(BlockBarrel.LABEL_UP))    { textures.put("up",    TEX_CREATIVE_FRONT); } else { textures.put("up",    TEX_CREATIVE_TOP);  }
            if (state.getValue(BlockBarrel.LABEL_DOWN))  { textures.put("down",  TEX_CREATIVE_FRONT); } else { textures.put("down",  TEX_CREATIVE_TOP);  }
            if (state.getValue(BlockBarrel.LABEL_FRONT)) { textures.put("north", TEX_CREATIVE_FRONT); } else { textures.put("north", TEX_CREATIVE_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_BACK))  { textures.put("south", TEX_CREATIVE_FRONT); } else { textures.put("south", TEX_CREATIVE_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_LEFT))  { textures.put("east",  TEX_CREATIVE_FRONT); } else { textures.put("east",  TEX_CREATIVE_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_RIGHT)) { textures.put("west",  TEX_CREATIVE_FRONT); } else { textures.put("west",  TEX_CREATIVE_SIDE); }
        }
        else
        {
            textures.put("particle", TEX_NORMAL_TOP);
            if (state.getValue(BlockBarrel.LABEL_UP))    { textures.put("up",    TEX_NORMAL_FRONT); } else { textures.put("up",    TEX_NORMAL_TOP);  }
            if (state.getValue(BlockBarrel.LABEL_DOWN))  { textures.put("down",  TEX_NORMAL_FRONT); } else { textures.put("down",  TEX_NORMAL_TOP);  }
            if (state.getValue(BlockBarrel.LABEL_FRONT)) { textures.put("north", TEX_NORMAL_FRONT); } else { textures.put("north", TEX_NORMAL_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_BACK))  { textures.put("south", TEX_NORMAL_FRONT); } else { textures.put("south", TEX_NORMAL_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_LEFT))  { textures.put("east",  TEX_NORMAL_FRONT); } else { textures.put("east",  TEX_NORMAL_SIDE); }
            if (state.getValue(BlockBarrel.LABEL_RIGHT)) { textures.put("west",  TEX_NORMAL_FRONT); } else { textures.put("west",  TEX_NORMAL_SIDE); }
        }

        return ImmutableMap.copyOf(textures);
    }

    public static class Model implements IModel
    {
        public Model()
        {
        }

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
            textures.add(new ResourceLocation(TEX_NORMAL_FRONT));
            textures.add(new ResourceLocation(TEX_NORMAL_SIDE));
            textures.add(new ResourceLocation(TEX_NORMAL_TOP));
            textures.add(new ResourceLocation(TEX_CREATIVE_FRONT));
            textures.add(new ResourceLocation(TEX_CREATIVE_SIDE));
            textures.add(new ResourceLocation(TEX_CREATIVE_TOP));
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
                EnderUtilities.logger.warn("Failed to load the base model for the Barrel!");
            }

            return new ModelBarrel(baseModel, state, format, bakedTextureGetter);
        }
    }

    public static class ModelLoader implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION = new ResourceLocation(Reference.MOD_ID, "models/block/custom/barrel");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            return new Model();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            MODEL_CACHE.clear();
        }
    }

    public static class StateMapper extends StateMapperBase
    {
        private static final Map<EnumFacing, ModelResourceLocation> LOCATIONS = Maps.newHashMap();

        public StateMapper()
        {
            LOCATIONS.clear();

            for (EnumFacing facing : EnumFacing.values())
            {
                LOCATIONS.put(facing, new ModelResourceLocation(Reference.MOD_ID + ":barrel", "facing=" + facing.toString().toLowerCase()));
            }
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state)
        {
            return state != null ? LOCATIONS.get(state.getValue(BlockBarrel.FACING_H)) : LOCATIONS.get(EnumFacing.NORTH);
        }
    }
}
