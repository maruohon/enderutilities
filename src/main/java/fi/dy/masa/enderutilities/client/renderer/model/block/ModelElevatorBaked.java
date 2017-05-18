package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelElevatorBaked implements IBakedModel
{
    private static final Map<Long, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE =
            new HashMap<Long, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>>();
    private final ImmutableMap<String, String> textures;
    private final IBlockState defaultState;
    private final IBakedModel bakedBaseModel;
    private final IBakedModel bakedOverlayModel;
    private final TextureAtlasSprite particle;

    public ModelElevatorBaked(ModelElevatorBase elevatorModel, IModel baseModel, IModel overlayModel, IBlockState defaultState,
            IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.textures = ImmutableMap.copyOf(elevatorModel.getTextureMapping());
        this.defaultState = defaultState;
        this.bakedBaseModel = ((IRetexturableModel) baseModel).retexture(this.textures).bake(state, format, bakedTextureGetter);
        this.bakedOverlayModel = ((IRetexturableModel) overlayModel).retexture(this.textures).bake(state, format, bakedTextureGetter);
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
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        // Item model
        if (state == null)
        {
            return this.bakedBaseModel.getQuads(state, side, rand);
        }

        IBlockState camoState = ((IExtendedBlockState) state).getValue(BlockElevator.CAMOBLOCK);
        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> quads;
        long id = 0;

        if (camoState.getBlock() != Blocks.AIR)
        {
            id = ((long) Block.getStateId(state)) << 32 | Block.getStateId(camoState);
            quads = QUAD_CACHE.get(id);
        }
        else
        {
            id = ((long) Block.getStateId(this.defaultState)) << 32;
            quads = QUAD_CACHE.get(id);
        }

        if (quads == null)
        {
            if (camoState.getBlock() != Blocks.AIR)
            {
                IBakedModel camoModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camoState);
                quads = this.getCombinedQuads(this.bakedOverlayModel, state, camoModel, camoState);
                QUAD_CACHE.put(id, quads);
            }
            else
            {
                quads = this.getCombinedQuads(this.bakedBaseModel, state, null, null);
                QUAD_CACHE.put(id, quads);
            }
        }

        return quads.get(Optional.fromNullable(side));
    }

    private ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> getCombinedQuads(
            @Nonnull IBakedModel overlayModel, @Nonnull IBlockState stateElevator,
            @Nullable IBakedModel camoModel, @Nullable IBlockState stateCamo)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();

        for (EnumFacing face : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            if (camoModel != null)
            {
                quads.addAll(camoModel.getQuads(stateCamo, face, 0));
            }

            quads.addAll(overlayModel.getQuads(stateElevator, face, 0));
            builder.put(Optional.of(face), quads.build());
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        if (camoModel != null)
        {
            quads.addAll(camoModel.getQuads(stateCamo, null, 0));
        }

        quads.addAll(overlayModel.getQuads(stateElevator, null, 0));
        builder.put(Optional.<EnumFacing>absent(), quads.build());

        return builder.build();
    }

    private static abstract class ModelElevatorBase implements IModel
    {
        private final IBlockState defaultState;
        private final ResourceLocation baseModel;
        private final ResourceLocation overlayModel;
        private final Map<String, String> textures = new HashMap<String, String>();

        private ModelElevatorBase(IBlockState defaultState, ResourceLocation baseModel, ResourceLocation overlayModel)
        {
            this.defaultState = defaultState;
            this.baseModel = baseModel;
            this.overlayModel = overlayModel;
            this.textures.put("particle",   "enderutilities:blocks/ender_elevator_side");
            this.textures.put("side",       "enderutilities:blocks/ender_elevator_side");
            this.textures.put("overlay",    "enderutilities:blocks/ender_elevator_top_overlay");
        }

        @Override
        public IModelState getDefaultState()
        {
            return ModelRotation.X0_Y0;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Lists.newArrayList(this.baseModel, this.overlayModel);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            return Lists.newArrayList(
                    new ResourceLocation(this.textures.get("side")),
                    new ResourceLocation(this.textures.get("overlay")));
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            IModel baseModel = null;
            IModel overlayModel = null;

            try
            {
                baseModel    = ModelLoaderRegistry.getModel(this.baseModel);
                overlayModel = ModelLoaderRegistry.getModel(this.overlayModel);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for the Ender Elevator!");
            }

            return new ModelElevatorBaked(this, baseModel, overlayModel, this.defaultState, state, format, bakedTextureGetter);
        }

        public ImmutableMap<String, String> getTextureMapping()
        {
            return ImmutableMap.copyOf(this.textures);
        }
    }

    private static class ModelElevatorNormal extends ModelElevatorBase
    {
        private ModelElevatorNormal()
        {
            super(EnderUtilitiesBlocks.ELEVATOR.getDefaultState(),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator"),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator_overlay"));
        }
    }

    private static class ModelElevatorSlab extends ModelElevatorBase
    {
        private ModelElevatorSlab()
        {
            super(EnderUtilitiesBlocks.ELEVATOR_SLAB.getDefaultState(),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator_slab"),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator_slab_overlay"));
        }
    }

    private static class ModelElevatorLayer extends ModelElevatorBase
    {
        private ModelElevatorLayer()
        {
            super(EnderUtilitiesBlocks.ELEVATOR_LAYER.getDefaultState(),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator_layer"),
                  new ResourceLocation(Reference.MOD_ID, "block/ender_elevator_layer_overlay"));
        }
    }

    public static class ModelLoaderElevator implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION_NORMAL = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator");
        private static final ResourceLocation FAKE_LOCATION_SLAB   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_slab");
        private static final ResourceLocation FAKE_LOCATION_LAYER  = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_layer");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION_NORMAL) ||
                   modelLocation.equals(FAKE_LOCATION_SLAB) ||
                   modelLocation.equals(FAKE_LOCATION_LAYER);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if (modelLocation.equals(FAKE_LOCATION_NORMAL))
            {
                return new ModelElevatorNormal();
            }
            else if (modelLocation.equals(FAKE_LOCATION_SLAB))
            {
                return new ModelElevatorSlab();
            }
            else
            {
                return new ModelElevatorLayer();
            }
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            QUAD_CACHE.clear();
        }
    }
}
