package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.Collection;
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
import fi.dy.masa.enderutilities.client.renderer.model.block.BakedModelBarrel.ModelLoaderBarrel;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class ModelCamouflageBlock
{
    public static class ModelCamouflageBlockBase implements IModel
    {
        protected final ResourceLocation baseModelLocation;
        @Nullable
        protected final ResourceLocation overlayModelLocation;
        protected final ImmutableMap<String, String> textures;
        protected IModel baseModel;
        @Nullable
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

        protected ModelCamouflageBlockBase(ResourceLocation baseModelLocation, @Nullable ResourceLocation overlayModelLocation)
        {
            this.baseModelLocation = baseModelLocation;
            this.overlayModelLocation = overlayModelLocation;
            this.textures = ImmutableMap.of();
        }

        protected ModelCamouflageBlockBase(ResourceLocation baseModelLocation, @Nullable ResourceLocation overlayModelLocation,
                IModel baseModel, @Nullable IModel overlayModel, ImmutableMap<String, String> textures)
        {
            this.baseModelLocation = baseModelLocation;
            this.overlayModelLocation = overlayModelLocation;
            this.baseModel = baseModel;
            this.overlayModel = overlayModel;
            this.textures = textures;
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
            IModel baseModel    = ModelLoaderRegistry.getMissingModel();
            IModel overlayModel = null;

            try
            {
                baseModel = ModelLoaderRegistry.getModel(this.baseModelLocation);
                baseModel = baseModel.retexture(textures);

                if (this.overlayModelLocation != null)
                {
                    overlayModel = ModelLoaderRegistry.getModel(this.overlayModelLocation);
                    overlayModel = overlayModel.retexture(textures);
                }
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for a camouflage block", e);
            }

            return new ModelCamouflageBlockBase(this.baseModelLocation, this.overlayModelLocation, baseModel, overlayModel, textures);
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            return new BakedModelCamouflageBlock(this.baseModel, this.overlayModel, this.textures, state, format, bakedTextureGetter);
        }
    }

    private static class ModelElevator extends ModelCamouflageBlockBase
    {
        public ModelElevator(IBlockState defaultState, String variant)
        {
            super(new ResourceLocation(Reference.MOD_ID, "block/ender_elevator" + variant),
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
        private static final ResourceLocation LOC_BARREL_NORMAL         = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_barrel_normal");
        private static final ResourceLocation LOC_BARREL_CREATIVE       = new ResourceLocation(Reference.MOD_ID, "models/block/custom/camo_barrel_creative");

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
                return new ModelCamouflageBlockBase(new ResourceLocation("minecraft:block/cube_all"), null);
            }
            else if (modelLocation.equals(LOC_DRAW_BRIDGE_N) || modelLocation.equals(LOC_DRAW_BRIDGE_A))
            {
                ResourceLocation baseModelLocation = new ResourceLocation(Reference.MOD_ID, "block/orientable_directional_individual");
                return new ModelCamouflageBlockBase(baseModelLocation, null);
            }
            else if (modelLocation.equals(LOC_BARREL_NORMAL))
            {
                // The Barrel handles both normal and overlay models with the same custom model
                return new ModelCamouflageBlockBase(ModelLoaderBarrel.LOCATION_NORMAL, ModelLoaderBarrel.LOCATION_NORMAL);
            }
            else if (modelLocation.equals(LOC_BARREL_CREATIVE))
            {
                // The Barrel handles both normal and overlay models with the same custom model
                return new ModelCamouflageBlockBase(ModelLoaderBarrel.LOCATION_CREATIVE, ModelLoaderBarrel.LOCATION_CREATIVE);
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
