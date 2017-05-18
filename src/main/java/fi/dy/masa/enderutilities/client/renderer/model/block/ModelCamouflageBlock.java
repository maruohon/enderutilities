package fi.dy.masa.enderutilities.client.renderer.model.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
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

    public static class ModelLoaderCamouflageBlocks implements ICustomModelLoader
    {
        private static final ResourceLocation LOC_ELEVATOR_NORMAL = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator");
        private static final ResourceLocation LOC_ELEVATOR_SLAB   = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_slab");
        private static final ResourceLocation LOC_ELEVATOR_LAYER  = new ResourceLocation(Reference.MOD_ID, "models/block/custom/ender_elevator_layer");
        private static final ResourceLocation LOC_PORTAL_FRAME    = new ResourceLocation(Reference.MOD_ID, "models/block/custom/frame");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(LOC_ELEVATOR_NORMAL) ||
                   modelLocation.equals(LOC_ELEVATOR_SLAB) ||
                   modelLocation.equals(LOC_ELEVATOR_LAYER) ||
                   modelLocation.equals(LOC_PORTAL_FRAME);
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

            return ModelLoaderRegistry.getMissingModel();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            ModelCamouflageBlockBaked.QUAD_CACHE.clear();
        }
    }
}
