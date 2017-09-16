package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockDrawbridge;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelCamouflageBlock
{
    public static class ModelCamouflageBlockBase implements IModel
    {
        protected final IBlockState defaultState;
        protected final ResourceLocation baseModelLocation;
        @Nullable
        protected final ResourceLocation overlayModelLocation;
        protected final Map<String, String> textures = new HashMap<String, String>();
        protected IModel baseModel;
        protected IModel overlayModel;
        protected static ImmutableList<ResourceLocation> texture_deps = ImmutableList.of();

        // FIXME is there a way to get these from the blockstate json somehow (before retexture() is called)?
        static
        {
            ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
            builder.add(new ResourceLocation("enderutilities:blocks/ender_elevator_side"));
            builder.add(new ResourceLocation("enderutilities:blocks/ender_elevator_top_overlay"));
            builder.add(new ResourceLocation("enderutilities:blocks/ender_elevator_side_layer"));
            builder.add(new ResourceLocation("enderutilities:blocks/ender_elevator_side_slab"));
            builder.add(new ResourceLocation("enderutilities:blocks/draw_bridge_back"));
            builder.add(new ResourceLocation("enderutilities:blocks/draw_bridge_front_advanced"));
            builder.add(new ResourceLocation("enderutilities:blocks/draw_bridge_front_normal"));
            builder.add(new ResourceLocation("enderutilities:blocks/draw_bridge_side_advanced"));
            builder.add(new ResourceLocation("enderutilities:blocks/draw_bridge_side_normal"));
            builder.add(new ResourceLocation("enderutilities:blocks/frame"));
            texture_deps = builder.build();
        }

        public ModelCamouflageBlockBase(IBlockState defaultState, ResourceLocation baseModelLocation, @Nullable ResourceLocation overlayModelLocation)
        {
            this.defaultState = defaultState;
            this.baseModelLocation = baseModelLocation;
            this.overlayModelLocation = overlayModelLocation;

            IModel baseModel    = ModelLoaderRegistry.getMissingModel();
            IModel overlayModel = null;

            try
            {
                baseModel = ModelLoaderRegistry.getModel(this.baseModelLocation);

                if (this.overlayModelLocation != null)
                {
                    overlayModel = ModelLoaderRegistry.getModel(this.overlayModelLocation);
                }
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for a camouflage block", e);
            }

            this.baseModel = baseModel;
            this.overlayModel = overlayModel;
        }

        @Override
        public IModelState getDefaultState()
        {
            return ModelRotation.X0_Y0;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            if (this.overlayModelLocation != null)
            {
                return ImmutableList.of(this.baseModelLocation, this.overlayModelLocation);
            }
            else
            {
                return ImmutableList.of(this.baseModelLocation);
            }
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            return texture_deps;
        }

        @Override
        public IModel retexture(ImmutableMap<String, String> textures)
        {
            this.textures.putAll(textures);
            this.baseModel = this.baseModel.retexture(textures);

            if (this.overlayModel != null)
            {
                this.overlayModel = this.overlayModel.retexture(textures);
            }

            return this;
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            return new BakedModelCamouflageBlock(ImmutableMap.copyOf(this.textures), this.baseModel, this.overlayModel, this.defaultState, state, format, bakedTextureGetter);
        }
    }

    private static class ModelElevator extends ModelCamouflageBlockBase
    {
        public ModelElevator(IBlockState defaultState, String variant)
        {
            super(defaultState,
                    new ResourceLocation(Reference.MOD_ID, "block/ender_elevator" + variant),
                    new ResourceLocation(Reference.MOD_ID, "block/ender_elevator" + variant + "_overlay"));
        }
    }

    public static class ModelLoaderCamouflageBlocks implements ICustomModelLoader
    {
        private static final ResourceLocation LOC_ELEVATOR_NORMAL       = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_ender_elevator");
        private static final ResourceLocation LOC_ELEVATOR_SLAB_TOP     = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_ender_elevator_slab_top");
        private static final ResourceLocation LOC_ELEVATOR_SLAB_BOTTOM  = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_ender_elevator_slab_bottom");
        private static final ResourceLocation LOC_ELEVATOR_LAYER_TOP    = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_ender_elevator_layer_top");
        private static final ResourceLocation LOC_ELEVATOR_LAYER_BOTTOM = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_ender_elevator_layer_bottom");
        private static final ResourceLocation LOC_PORTAL_FRAME          = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_frame");
        private static final ResourceLocation LOC_DRAW_BRIDGE_N         = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_draw_bridge_normal");
        private static final ResourceLocation LOC_DRAW_BRIDGE_A         = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_draw_bridge_advanced");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.getResourceDomain().equals(Reference.MOD_ID) &&
                   modelLocation.getResourcePath().startsWith("models/block/custom/camo_");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if (modelLocation.equals(LOC_ELEVATOR_NORMAL))
            {
                return new ModelElevator(EnderUtilitiesBlocks.ELEVATOR.getDefaultState(), "_full");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_SLAB_TOP))
            {
                return new ModelElevator(EnderUtilitiesBlocks.ELEVATOR_SLAB.getDefaultState(), "_slab_top");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_SLAB_BOTTOM))
            {
                return new ModelElevator(EnderUtilitiesBlocks.ELEVATOR_SLAB.getDefaultState(), "_slab_bottom");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_LAYER_TOP))
            {
                return new ModelElevator(EnderUtilitiesBlocks.ELEVATOR_LAYER.getDefaultState(), "_layer_top");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_LAYER_BOTTOM))
            {
                return new ModelElevator(EnderUtilitiesBlocks.ELEVATOR_LAYER.getDefaultState(), "_layer_bottom");
            }
            else if (modelLocation.equals(LOC_PORTAL_FRAME))
            {
                IBlockState defaultState = EnderUtilitiesBlocks.PORTAL_FRAME.getDefaultState();
                return new ModelCamouflageBlockBase(defaultState, new ResourceLocation("minecraft:block/cube_all"), null);
            }
            else if (modelLocation.equals(LOC_DRAW_BRIDGE_N) || modelLocation.equals(LOC_DRAW_BRIDGE_A))
            {
                IBlockState defaultState = EnderUtilitiesBlocks.DRAWBRIDGE.getDefaultState()
                        .withProperty(BlockDrawbridge.ADVANCED, modelLocation.equals(LOC_DRAW_BRIDGE_A));
                ResourceLocation baseModelLocation = new ResourceLocation(Reference.MOD_ID, "block/orientable_directional_individual");
                return new ModelCamouflageBlockBase(defaultState, baseModelLocation, null);
            }

            return ModelLoaderRegistry.getMissingModel();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            for (Map.Entry<BlockRenderLayer, Map<ImmutablePair<IBlockState, IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>>> entry : BakedModelCamouflageBlock.QUAD_CACHE.entrySet())
            {
                entry.getValue().clear();
            }
        }
    }
}
