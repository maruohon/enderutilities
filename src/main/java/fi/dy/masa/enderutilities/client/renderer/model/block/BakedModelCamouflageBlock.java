package fi.dy.masa.enderutilities.client.renderer.model.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;

public class BakedModelCamouflageBlock implements IBakedModel
{
    public static final Map<BlockRenderLayer, Map<ImmutablePair<IBlockState, IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>>> QUAD_CACHE = new HashMap<>();
    protected final IModel baseModel;
    @Nullable
    protected final IModel overlayModel;
    protected final IBakedModel bakedBaseModel;
    protected final IBakedModel bakedOverlayModel;
    protected final IModelState modelState;
    protected final VertexFormat format;
    protected final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    protected final ImmutableList<BakedQuad> itemQuads;

    static
    {
        for (BlockRenderLayer layer : BlockRenderLayer.values())
        {
            QUAD_CACHE.put(layer, new HashMap<>());
        }
    }

    public BakedModelCamouflageBlock(IModel baseModel, @Nullable IModel overlayModel, ImmutableMap<String, String> textures,
            IModelState modelState, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.baseModel = baseModel;
        this.overlayModel = overlayModel;
        this.modelState = modelState;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.bakedBaseModel = baseModel.bake(modelState, format, bakedTextureGetter);
        this.bakedOverlayModel = overlayModel != null ? overlayModel.bake(modelState, format, bakedTextureGetter) : null;

        this.itemQuads = buildItemModel(this.bakedBaseModel, this.bakedOverlayModel);
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
    synchronized public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        // Item model
        if (state == null || layer == null)
        {
            return this.itemQuads;
        }

        IExtendedBlockState extendedState = (IExtendedBlockState) state;
        IBlockState camoState = extendedState.getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCKSTATE);
        IBlockState camoExtendedState = extendedState.getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCKSTATEEXTENDED);

        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> quads = null;
        Map<ImmutablePair<IBlockState, IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> map = QUAD_CACHE.get(layer);
        ImmutablePair<IBlockState, IBlockState> key;
        boolean validCamo = camoState != null && camoState.getBlock() != Blocks.AIR;

        // Get the base actualState, so that the map keys work properly (reference equality)
        IBlockState stateClean = extendedState.getClean();

        if (map == null)
        {
            map = new HashMap<>();
            QUAD_CACHE.put(layer, map);
        }

        if (validCamo)
        {
            key = ImmutablePair.of(stateClean, camoState);
        }
        else
        {
            key = ImmutablePair.of(stateClean, Blocks.AIR.getDefaultState());
        }

        quads = map.get(key);

        if (quads == null)
        {
            @Nullable
            //IBakedModel bakedOverlayModel = getRotatedBakedModel(this.overlayModel, null, extendedState, this.format, this.bakedTextureGetter);
            IBakedModel bakedOverlayModel = this.overlayModel != null ? this.overlayModel.bake(this.modelState, this.format, this.bakedTextureGetter) : null;

            if (validCamo)
            {
                IBakedModel bakedBaseModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camoState);
                quads = this.getCombinedQuads(layer, bakedBaseModel, camoState, camoExtendedState, bakedOverlayModel, extendedState, true);
            }
            else
            {
                IBakedModel bakedBaseModel = getRotatedBakedModel(this.baseModel, this.bakedBaseModel, state, this.format, this.bakedTextureGetter);
                quads = this.getCombinedQuads(layer, bakedBaseModel, stateClean, extendedState, bakedOverlayModel, extendedState, false);
            }

            map.put(key, quads);
        }

        return quads.get(Optional.ofNullable(side));
    }

    protected ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> getCombinedQuads(
            BlockRenderLayer layer,
            @Nonnull IBakedModel baseModel,
            @Nonnull IBlockState baseActualState,
            @Nonnull IBlockState baseExtendedState,
            @Nullable IBakedModel overlayModel,
            @Nullable IBlockState overlayState,
            boolean isCamoModel)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();

        for (EnumFacing face : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            // This allows the camo model to render on all layers it needs to,
            // but restricts the base model to only render on the one layer it says it should be on.
            // This is because canRenderInLayer() returns true for all layers for camo blocks,
            // so that the camo model can render in all the layers it needs to.
            if ((isCamoModel || baseActualState.getBlock().getRenderLayer() == layer) &&
                baseActualState.getBlock().canRenderInLayer(baseActualState, layer))
            {
                quads.addAll(baseModel.getQuads(baseExtendedState, face, 0));
            }

            // The overlay model is used for cutout type overlays
            if (overlayModel != null && overlayState != null && layer == BlockRenderLayer.CUTOUT)
            {
                quads.addAll(overlayModel.getQuads(overlayState, face, 0));
            }

            builder.put(Optional.of(face), quads.build());
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        if ((isCamoModel || baseActualState.getBlock().getRenderLayer() == layer) &&
            baseActualState.getBlock().canRenderInLayer(baseActualState, layer))
        {
            quads.addAll(baseModel.getQuads(baseExtendedState, null, 0));
        }

        // The overlay model is used for cutout type overlays
        if (overlayModel != null && overlayState != null && layer == BlockRenderLayer.CUTOUT)
        {
            quads.addAll(overlayModel.getQuads(overlayState, null, 0));
        }

        builder.put(Optional.empty(), quads.build());

        return builder.build();
    }

    public static ImmutableList<BakedQuad> buildItemModel(IBakedModel bakedBaseModel, @Nullable IBakedModel bakedOverlayModel)
    {
        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        for (EnumFacing side : EnumFacing.values())
        {
            quads.addAll(bakedBaseModel.getQuads(null, side, 0));

            if (bakedOverlayModel != null)
            {
                quads.addAll(bakedOverlayModel.getQuads(null, side, 0));
            }
        }

        quads.addAll(bakedBaseModel.getQuads(null, null, 0));

        if (bakedOverlayModel != null)
        {
            quads.addAll(bakedOverlayModel.getQuads(null, null, 0));
        }

        return quads.build();
    }

    @Nullable
    public static IBakedModel getRotatedBakedModel(@Nullable IModel model, @Nullable IBakedModel bakedModelDefault, IBlockState state,
            VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        if (model != null)
        {
            if (state.getPropertyKeys().contains(BlockEnderUtilities.FACING))
            {
                return model.bake(TRSRTransformation.from(state.getValue(BlockEnderUtilities.FACING)), format, bakedTextureGetter);
            }
            else if (state.getPropertyKeys().contains(BlockEnderUtilities.FACING_H))
            {
                return model.bake(TRSRTransformation.from(state.getValue(BlockEnderUtilities.FACING_H)), format, bakedTextureGetter);
            }
        }

        return bakedModelDefault;
    }
}
