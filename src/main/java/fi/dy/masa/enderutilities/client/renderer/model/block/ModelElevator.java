package fi.dy.masa.enderutilities.client.renderer.model.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelCamouflageBlockBaked.ModelCamouflageBlockBase;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelElevator extends ModelCamouflageBlockBase
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
                return new ModelElevator(EnderUtilitiesBlocks.blockElevator.getDefaultState(), "");
            }
            else if (modelLocation.equals(FAKE_LOCATION_SLAB))
            {
                return new ModelElevator(EnderUtilitiesBlocks.blockElevatorSlab.getDefaultState(), "_slab");
            }
            else
            {
                return new ModelElevator(EnderUtilitiesBlocks.blockElevatorLayer.getDefaultState(), "_layer");
            }
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            ModelCamouflageBlockBaked.QUAD_CACHE.clear();
        }
    }
}
