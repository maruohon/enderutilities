package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockBarrel;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class BakedModelBarrel implements IBakedModel
{
    private static final Map<Optional<IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE_NORMAL = new HashMap<>();
    private static final Map<Optional<IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE_CAMO = new HashMap<>();
    private static final ImmutableList<BakedQuad> EMPTY_QUADS = ImmutableList.<BakedQuad>of();
    private final ImmutableMap<String, String> textures;
    private final IModel baseModel;
    private final IModel overlayModel;
    private final VertexFormat format;
    private final IBakedModel bakedBaseModel;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private final TextureAtlasSprite particle;
    private final ImmutableList<BakedQuad> itemQuads;

    private BakedModelBarrel(IModel baseModel, IModel overlayModel, IModelState state, VertexFormat format,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, ImmutableMap<String, String> textures)
    {
        IBlockState defaultState = EnderUtilitiesBlocks.BARREL.getDefaultState().withProperty(BlockBarrel.LABEL_FRONT, true);
        this.baseModel = baseModel.retexture(textures);
        this.overlayModel = overlayModel.retexture(textures);
        this.bakedBaseModel = this.baseModel.retexture(this.getTexturesBaseModel(defaultState, textures)).bake(state, format, bakedTextureGetter);
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.textures = textures;
        String particleName = this.textures.get("particle");
        this.particle = particleName != null ? bakedTextureGetter.apply(new ResourceLocation(particleName)) : this.bakedBaseModel.getParticleTexture();
        this.itemQuads = BakedModelCamouflageBlock.buildItemModel(this.bakedBaseModel, null);
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
            return this.itemQuads;
        }

        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        IBlockState actualState = extendedState.getClean();
        IBlockState camoState = extendedState.getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCKSTATE);
        boolean validCamo = camoState != null && camoState.getBlock() != Blocks.AIR;

        Optional<IBlockState> key = Optional.of(actualState);
        Map<Optional<IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> cache = validCamo ? QUAD_CACHE_CAMO : QUAD_CACHE_NORMAL;
        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> map = cache.get(key);

        if (map == null)
        {
            IBakedModel bakedModel = validCamo ? this.getBakedOverlayModel(actualState) : this.getBakedBaseModel(actualState);
            map = this.getQuadsForState(bakedModel, extendedState, rand, validCamo);
            cache.put(key, map);
        }

        return map.get(Optional.ofNullable(side));
    }

    private IBakedModel getBakedBaseModel(IBlockState actualState)
    {
        IModel model = this.baseModel.retexture(this.getTexturesBaseModel(actualState, this.textures));
        return model.bake(new TRSRTransformation(actualState.getValue(BlockBarrel.FACING_H)), this.format, this.bakedTextureGetter);
    }

    private IBakedModel getBakedOverlayModel(IBlockState actualState)
    {
        return this.overlayModel.bake(new TRSRTransformation(actualState.getValue(BlockBarrel.FACING_H)), this.format, this.bakedTextureGetter);
    }

    private ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> getQuadsForState(
            IBakedModel bakedModel, IExtendedBlockState extendedState, long rand, boolean validCamo)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> mapBuilder = ImmutableMap.builder();

        for (EnumFacing side : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            // Camo model, only add the quads if there is a label on this side
            if (validCamo)
            {
                quads.addAll(this.getQuadsForCamoModelSide(side, extendedState, bakedModel, rand));
            }
            // Not a camo model, always return the quads of the normal model
            else
            {
                quads.addAll(bakedModel.getQuads(extendedState, side, rand));
            }

            mapBuilder.put(Optional.ofNullable(side), quads.build());
        }

        mapBuilder.put(Optional.ofNullable(null), ImmutableList.copyOf(bakedModel.getQuads(extendedState, null, rand)));

        return mapBuilder.build();
    }

    private List<BakedQuad> getQuadsForCamoModelSide(EnumFacing side, IBlockState state, IBakedModel bakedModel, long rand)
    {
        EnumFacing relativeSide = PositionUtils.getRelativeFacing(state.getValue(BlockBarrel.FACING_H), side);

        switch (relativeSide)
        {
            case DOWN:
                return state.getValue(BlockBarrel.LABEL_DOWN)  ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            case UP:
                return state.getValue(BlockBarrel.LABEL_UP)    ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            case NORTH:
                return state.getValue(BlockBarrel.LABEL_FRONT) ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            case SOUTH:
                return state.getValue(BlockBarrel.LABEL_BACK)  ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            case WEST:
                return state.getValue(BlockBarrel.LABEL_RIGHT) ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            case EAST:
                return state.getValue(BlockBarrel.LABEL_LEFT)  ? bakedModel.getQuads(state, side, rand) : EMPTY_QUADS;
            default:
        }

        return EMPTY_QUADS;
    }

    private ImmutableMap<String, String> getTexturesBaseModel(IBlockState state, Map<String, String> texturesIn)
    {
        ImmutableMap.Builder<String, String> texturesOut = ImmutableMap.builder();

        String texFront = texturesIn.get("front_normal");
        String texSide  = texturesIn.get("side");
        String texUp    = texturesIn.get("top");

        texturesOut.put("particle", texturesIn.get("top"));
        texturesOut.put("up",    state.getValue(BlockBarrel.LABEL_UP)    ? texFront : texUp);
        texturesOut.put("down",  state.getValue(BlockBarrel.LABEL_DOWN)  ? texFront : texUp);
        texturesOut.put("north", state.getValue(BlockBarrel.LABEL_FRONT) ? texFront : texSide);
        texturesOut.put("south", state.getValue(BlockBarrel.LABEL_BACK)  ? texFront : texSide);
        texturesOut.put("west",  state.getValue(BlockBarrel.LABEL_RIGHT) ? texFront : texSide);
        texturesOut.put("east",  state.getValue(BlockBarrel.LABEL_LEFT)  ? texFront : texSide);

        return texturesOut.build();
    }

    private static class ModelBarrel implements IModel
    {
        private static final ResourceLocation BARREL_BASE_MODEL    = new ResourceLocation(Reference.MOD_ID, "block/barrel_base");
        private static final ResourceLocation BARREL_OVERLAY_MODEL = new ResourceLocation(Reference.MOD_ID, "block/barrel_overlay");
        private final IModel baseModel;
        private final IModel overlayModel;
        private final ImmutableMap<String, String> textures;

        private ModelBarrel()
        {
            this.baseModel = ModelLoaderRegistry.getMissingModel();
            this.overlayModel = ModelLoaderRegistry.getMissingModel();
            this.textures = ImmutableMap.of();
        }

        private ModelBarrel(IModel baseModel, IModel overlayModel, ImmutableMap<String, String> textures)
        {
            this.baseModel = baseModel;
            this.overlayModel = overlayModel;
            this.textures = textures;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return ImmutableList.of(BARREL_BASE_MODEL, BARREL_OVERLAY_MODEL);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();

            builder.add(new ResourceLocation("enderutilities:blocks/barrel_normal_front"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_normal_front_camo"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_normal_side"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_normal_top"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_creative_front"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_creative_front_camo"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_creative_side"));
            builder.add(new ResourceLocation("enderutilities:blocks/barrel_creative_top"));
            /*
            // FIXME This doesn't work :/ getTextures() does not seem to get
            // called for the model returned from retexture()
            for (String tex : this.textures.values())
            {
                builder.add(new ResourceLocation(tex));
            }
            */

            return builder.build();
        }

        @Override
        public IModel retexture(ImmutableMap<String, String> textures)
        {
            IModel baseModel = ModelLoaderRegistry.getMissingModel();
            IModel overlayModel = ModelLoaderRegistry.getMissingModel();
            
            try
            {
                baseModel = ModelLoaderRegistry.getModel(BARREL_BASE_MODEL);
                baseModel = baseModel.retexture(this.textures);

                overlayModel = ModelLoaderRegistry.getModel(BARREL_OVERLAY_MODEL);
                overlayModel = overlayModel.retexture(this.textures);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load the base model for a Barrel", e);
            }

            return new ModelBarrel(baseModel, overlayModel, textures);
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            return new BakedModelBarrel(this.baseModel, this.overlayModel, state, format, bakedTextureGetter, this.textures);
        }
    }

    public static class ModelLoaderBarrel implements ICustomModelLoader
    {
        private static final ResourceLocation FAKE_LOCATION_NORMAL      = new ResourceLocation(Reference.MOD_ID, "models/block/custom/barrel_normal");
        private static final ResourceLocation FAKE_LOCATION_CREATIVE    = new ResourceLocation(Reference.MOD_ID, "models/block/custom/barrel_creative");
        public static final ResourceLocation LOCATION_NORMAL            = new ResourceLocation(Reference.MOD_ID, "block/custom/barrel_normal");
        public static final ResourceLocation LOCATION_CREATIVE          = new ResourceLocation(Reference.MOD_ID, "block/custom/barrel_creative");

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.equals(FAKE_LOCATION_NORMAL) || modelLocation.equals(FAKE_LOCATION_CREATIVE);
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            return new ModelBarrel();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            QUAD_CACHE_NORMAL.clear();
            QUAD_CACHE_CAMO.clear();
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
