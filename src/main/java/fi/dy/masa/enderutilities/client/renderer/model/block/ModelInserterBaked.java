package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockInserter;
import fi.dy.masa.enderutilities.reference.Reference;

public class ModelInserterBaked implements IBakedModel
{
    private static final String TEX_NORMAL_IN                   = Reference.MOD_ID + ":blocks/inserter_normal_in";
    private static final String TEX_NORMAL_OUT                  = Reference.MOD_ID + ":blocks/inserter_normal_out";
    private static final String TEX_NORMAL_SIDE                 = Reference.MOD_ID + ":blocks/inserter_normal_side";
    private static final String TEX_NORMAL_SIDE_OUT_INVALID     = Reference.MOD_ID + ":blocks/inserter_normal_side_out_invalid";
    private static final String TEX_NORMAL_SIDE_OUT_VALID       = Reference.MOD_ID + ":blocks/inserter_normal_side_out_valid";
    private static final String TEX_FILTERED_IN                 = Reference.MOD_ID + ":blocks/inserter_filtered_in";
    private static final String TEX_FILTERED_OUT                = Reference.MOD_ID + ":blocks/inserter_filtered_out";
    private static final String TEX_FILTERED_SIDE               = Reference.MOD_ID + ":blocks/inserter_filtered_side";
    private static final String TEX_FILTERED_SIDE_OUT_VALID     = Reference.MOD_ID + ":blocks/inserter_filtered_side_out_valid";
    private static final String TEX_FILTERED_SIDE_OUT_INVALID   = Reference.MOD_ID + ":blocks/inserter_filtered_side_out_invalid";

    private static final Map<IBlockState, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE = new HashMap<IBlockState, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>>();
    private final IModel baseModel;
    private final IModel sideModelValid;
    private final IModel sideModelInvalid;
    private final VertexFormat format;
    private final IBakedModel bakedBaseModel;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private final TextureAtlasSprite particle;

    private ModelInserterBaked(ModelInserter inserterModel, IModel baseModel, IModel sideModel, IModelState modelState, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<String, String> textures = inserterModel.getTextureMapping();
        this.baseModel        = baseModel.retexture(textures);
        this.sideModelValid   = sideModel.retexture(ImmutableMap.of("side", textures.get("side_valid")));
        this.sideModelInvalid = sideModel.retexture(ImmutableMap.of("side", textures.get("side_invalid")));
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.bakedBaseModel = this.baseModel.bake(modelState, format, bakedTextureGetter);
        this.particle = bakedTextureGetter.apply(new ResourceLocation(textures.get("particle")));
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
            return this.bakedBaseModel.getQuads(state, side, rand);
        }

        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> quads = QUAD_CACHE.get(state);

        if (quads == null)
        {
            //IModelState modelState = new ModelStateComposition(new TRSRTransformation(state.getValue(BlockInserter.FACING)), this.modelState);
            IModelState modelState = new TRSRTransformation(state.getValue(BlockInserter.FACING));
            IBakedModel bakedBaseModel = this.baseModel.bake(modelState, this.format, this.bakedTextureGetter);

            quads = this.bakeFullModel(bakedBaseModel, state, side);
            QUAD_CACHE.put(state, quads);

            return quads.get(Optional.ofNullable(side));
        }

        return QUAD_CACHE.get(state).get(Optional.ofNullable(side));
    }

    private ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> bakeFullModel(IBakedModel baseModel, IBlockState state, @Nullable EnumFacing side)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();
        List<IBakedModel> sideModels = this.getSideModels(state);

        for (EnumFacing face : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
            quads.addAll(baseModel.getQuads(state, face, 0));

            for (IBakedModel bakedPart : sideModels)
            {
                quads.addAll(bakedPart.getQuads(state, face, 0));
            }

            builder.put(Optional.of(face), quads.build());
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        quads.addAll(baseModel.getQuads(state, null, 0));

        for (IBakedModel bakedPart : sideModels)
        {
            quads.addAll(bakedPart.getQuads(state, null, 0));
        }

        builder.put(Optional.empty(), quads.build());

        return builder.build();
    }

    private List<IBakedModel> getSideModels(IBlockState state)
    {
        List<IBakedModel> models = new ArrayList<IBakedModel>();

        for (EnumFacing side : EnumFacing.values())
        {
            BlockInserter.Connection conn = state.getValue(BlockInserter.CONNECTIONS.get(side.getIndex()));

            if (conn == BlockInserter.Connection.VALID)
            {
                models.add(this.sideModelValid.bake(new TRSRTransformation(side), this.format, this.bakedTextureGetter));
            }
            else if (conn == BlockInserter.Connection.INVALID)
            {
                models.add(this.sideModelInvalid.bake(new TRSRTransformation(side), this.format, this.bakedTextureGetter));
            }
        }

        return models;
    }

    private static abstract class ModelInserter implements IModel
    {
        private static final ResourceLocation BASE_MODEL = new ResourceLocation(Reference.MOD_ID, "block/inserter_base");
        private static final ResourceLocation SIDE_MODEL = new ResourceLocation(Reference.MOD_ID, "block/inserter_side");

        protected final Map<String, String> textures = new HashMap<String, String>();

        @Override
        public IModelState getDefaultState()
        {
            return ModelRotation.X0_Y0;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Lists.newArrayList(BASE_MODEL, SIDE_MODEL);
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
            IModel sideModel = null;

            try
            {
                baseModel = ModelLoaderRegistry.getModel(BASE_MODEL);
                sideModel = ModelLoaderRegistry.getModel(SIDE_MODEL);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for the Inserter!");
            }

            return new ModelInserterBaked(this, baseModel, sideModel, state, format, bakedTextureGetter);
        }

        public ImmutableMap<String, String> getTextureMapping()
        {
            return ImmutableMap.copyOf(this.textures);
        }
    }

    private static class ModelInserterNormal extends ModelInserter
    {
        private ModelInserterNormal()
        {
            this.textures.put("particle",       TEX_NORMAL_IN);
            this.textures.put("base_in",        TEX_NORMAL_IN);
            this.textures.put("base_out",       TEX_NORMAL_OUT);
            this.textures.put("base_side",      TEX_NORMAL_SIDE);
            this.textures.put("side_valid",     TEX_NORMAL_SIDE_OUT_VALID);
            this.textures.put("side_invalid",   TEX_NORMAL_SIDE_OUT_INVALID);
        }
    }

    private static class ModelInserterFiltered extends ModelInserter
    {
        private ModelInserterFiltered()
        {
            this.textures.put("particle",       TEX_FILTERED_IN);
            this.textures.put("base_in",        TEX_FILTERED_IN);
            this.textures.put("base_out",       TEX_FILTERED_OUT);
            this.textures.put("base_side",      TEX_FILTERED_SIDE);
            this.textures.put("side_valid",     TEX_FILTERED_SIDE_OUT_VALID);
            this.textures.put("side_invalid",   TEX_FILTERED_SIDE_OUT_INVALID);
        }
    }

    public static class ModelLoaderInserter implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION_NORMAL   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/inserter_normal");
        private static final ResourceLocation FAKE_LOCATION_FILTERED = new ResourceLocation(Reference.MOD_ID, "models/block/custom/inserter_filtered");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION_NORMAL) || modelLocation.equals(FAKE_LOCATION_FILTERED);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if (modelLocation.equals(FAKE_LOCATION_FILTERED))
            {
                return new ModelInserterFiltered();
            }
            else
            {
                return new ModelInserterNormal();
            }
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            QUAD_CACHE.clear();
        }
    }

    public static class StateMapper extends StateMapperBase
    {
        private static final ModelResourceLocation LOCATION_NORMAL   = new ModelResourceLocation(Reference.MOD_ID + ":inserter", "type=normal");
        private static final ModelResourceLocation LOCATION_FILTERED = new ModelResourceLocation(Reference.MOD_ID + ":inserter", "type=filtered");

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state)
        {
            return state.getValue(BlockInserter.TYPE) == BlockInserter.InserterType.FILTERED ? LOCATION_FILTERED : LOCATION_NORMAL;
        }
    }
}
