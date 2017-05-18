package fi.dy.masa.enderutilities.client.renderer.model.block;

import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockDrawbridge;
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelCamouflageBlockBaked.ModelCamouflageBlockBase;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelCamouflageBlock
{
    private static class ModelElevator extends ModelCamouflageBlockBase
    {
        public ModelElevator(IBlockState defaultState, String variant)
        {
            super(defaultState,
                    new ResourceLocation(Reference.MOD_ID, "block/ender_elevator" + variant),
                    new ResourceLocation(Reference.MOD_ID, "block/ender_elevator" + variant + "_overlay"));

            this.textures.put("particle",   "enderutilities:blocks/enderelevator_side");
            this.textures.put("side",       "enderutilities:blocks/enderelevator_side");
            this.textures.put("overlay",    "enderutilities:blocks/enderelevator_top_overlay");
        }
    }

    private static class ModelPortalFrame extends ModelCamouflageBlockBase
    {
        public ModelPortalFrame(IBlockState defaultState)
        {
            super(defaultState, new ResourceLocation("minecraft:block/cube_all"), null);

            this.textures.put("particle",   "enderutilities:blocks/frame");
            this.textures.put("all",        "enderutilities:blocks/frame");
        }
    }

    private static class ModelDrawbridge extends ModelCamouflageBlockBase
    {
        public ModelDrawbridge(IBlockState defaultState, boolean advanced)
        {
            super(defaultState, new ResourceLocation(Reference.MOD_ID, "block/orientable_directional_individual"), null);

            if (advanced)
            {
                this.textures.put("particle",   "enderutilities:blocks/drawbridge_front_advanced");
                this.textures.put("front",      "enderutilities:blocks/drawbridge_front_advanced");
            }
            else
            {
                this.textures.put("particle",   "enderutilities:blocks/drawbridge_front_normal");
                this.textures.put("front",      "enderutilities:blocks/drawbridge_front_normal");
            }

            this.textures.put("top",        "enderutilities:blocks/drawbridge_side");
            this.textures.put("bottom",     "enderutilities:blocks/drawbridge_side");
            this.textures.put("left",       "enderutilities:blocks/drawbridge_side");
            this.textures.put("right",      "enderutilities:blocks/drawbridge_side");
            this.textures.put("back",       "enderutilities:blocks/drawbridge_back");
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            IModel baseModel = null;
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
                EnderUtilities.logger.warn("Failed to load a model for a Drawbridge", e);
            }

            return new ModelDrawbridgeBaked(this, baseModel, overlayModel, this.defaultState, state, format, bakedTextureGetter);
        }
    }

    public static class ModelLoaderCamouflageBlocks implements ICustomModelLoader
    {
        private static final ResourceLocation LOC_ELEVATOR_NORMAL = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator");
        private static final ResourceLocation LOC_ELEVATOR_SLAB   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_slab");
        private static final ResourceLocation LOC_ELEVATOR_LAYER  = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_layer");
        private static final ResourceLocation LOC_PORTAL_FRAME    = new ResourceLocation(Reference.MOD_ID, "models/block/custom/frame");
        private static final ResourceLocation LOC_DRAW_BRIDGE_N   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/draw_bridge_normal");
        private static final ResourceLocation LOC_DRAW_BRIDGE_A   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/draw_bridge_advanced");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(LOC_ELEVATOR_NORMAL) ||
                   modelLocation.equals(LOC_ELEVATOR_SLAB) ||
                   modelLocation.equals(LOC_ELEVATOR_LAYER) ||
                   modelLocation.equals(LOC_PORTAL_FRAME) ||
                   modelLocation.equals(LOC_DRAW_BRIDGE_N) ||
                   modelLocation.equals(LOC_DRAW_BRIDGE_A);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if (modelLocation.equals(LOC_ELEVATOR_NORMAL))
            {
                return new ModelElevator(EnderUtilitiesBlocks.blockElevator.getDefaultState(), "");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_SLAB))
            {
                return new ModelElevator(EnderUtilitiesBlocks.blockElevatorSlab.getDefaultState(), "_slab");
            }
            else if (modelLocation.equals(LOC_ELEVATOR_LAYER))
            {
                return new ModelElevator(EnderUtilitiesBlocks.blockElevatorLayer.getDefaultState(), "_layer");
            }
            else if (modelLocation.equals(LOC_PORTAL_FRAME))
            {
                return new ModelPortalFrame(EnderUtilitiesBlocks.blockPortalFrame.getDefaultState());
            }
            else if (modelLocation.equals(LOC_DRAW_BRIDGE_N))
            {
                return new ModelDrawbridge(EnderUtilitiesBlocks.DRAWBRIDGE.getDefaultState(), false);
            }
            else if (modelLocation.equals(LOC_DRAW_BRIDGE_A))
            {
                return new ModelDrawbridge(EnderUtilitiesBlocks.DRAWBRIDGE.getDefaultState().withProperty(BlockDrawbridge.ADVANCED, true), true);
            }

            return ModelLoaderRegistry.getMissingModel();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            ModelCamouflageBlockBaked.QUAD_CACHE.clear();
        }
    }
}
