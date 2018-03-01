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
    protected final IBakedModel bakedBaseModel;
    @Nullable
    protected final IBakedModel bakedOverlayModel;
    protected final IModel baseModel;
    protected final VertexFormat format;
    protected final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    protected final TextureAtlasSprite particle;
    protected final ImmutableList<BakedQuad> itemQuads;

    static
    {
        for (BlockRenderLayer layer : BlockRenderLayer.values())
        {
            QUAD_CACHE.put(layer, new HashMap<>());
        }
    }

    public BakedModelCamouflageBlock(ImmutableMap<String, String> textures, IModel baseModel, @Nullable IModel overlayModel,
            IModelState modelState, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.baseModel = baseModel;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.particle = bakedTextureGetter.apply(new ResourceLocation(textures.get("particle")));
        this.bakedBaseModel = baseModel.bake(modelState, format, bakedTextureGetter);

        if (overlayModel != null)
        {
            this.bakedOverlayModel = overlayModel.bake(modelState, format, bakedTextureGetter);
        }
        else
        {
            this.bakedOverlayModel = null;
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        for (EnumFacing side : EnumFacing.values())
        {
            quads.addAll(this.bakedBaseModel.getQuads(null, side, 0));

            if (this.bakedOverlayModel != null)
            {
                quads.addAll(this.bakedOverlayModel.getQuads(null, side, 0));
            }
        }

        quads.addAll(this.bakedBaseModel.getQuads(null, null, 0));

        if (this.bakedOverlayModel != null)
        {
            quads.addAll(this.bakedOverlayModel.getQuads(null, null, 0));
        }

        this.itemQuads = quads.build();
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
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        // Item model
        if (state == null || layer == null)
        {
            return this.itemQuads;
        }

        IBlockState camoState = ((IExtendedBlockState) state).getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCKSTATE);
        IBlockState camoExtendedState = ((IExtendedBlockState) state).getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCKSTATEEXTENDED);

        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> quads = null;
        boolean validCamo = camoState != null && camoState.getBlock() != Blocks.AIR;
        Map<ImmutablePair<IBlockState, IBlockState>, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> map = QUAD_CACHE.get(layer);
        ImmutablePair<IBlockState, IBlockState> key;

        // Get the base actualState, so that the map keys work properly (reference equality)
        state = ((IExtendedBlockState) state).getClean();

        if (map == null)
        {
            map = new HashMap<>();
            QUAD_CACHE.put(layer, map);
        }

        if (validCamo)
        {
            key = ImmutablePair.of(state, camoState);
        }
        else
        {
            key = ImmutablePair.of(state, Blocks.AIR.getDefaultState());
        }

        quads = map.get(key);

        if (quads == null)
        {
            if (validCamo)
            {
                IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camoState);
                quads = this.getCombinedQuads(layer, model, camoState, camoExtendedState, this.bakedOverlayModel, state, true);
            }
            else
            {
                IBakedModel model = this.getBakedBaseModel(state);
                quads = this.getCombinedQuads(layer, model, state, state, this.bakedOverlayModel, state, false);
            }

            map.put(key, quads);
        }

        return quads.get(Optional.ofNullable(side));
    }

    protected ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> getCombinedQuads(BlockRenderLayer layer,
            @Nonnull IBakedModel baseModel, @Nonnull IBlockState baseActualState, @Nonnull IBlockState baseExtendedState,
            @Nullable IBakedModel optionalModel, @Nullable IBlockState optionalState, boolean isCamoModel)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();

        for (EnumFacing face : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            // This allows the camo models to render on all layers they need to,
            // but restricts the base model to only render on the one layer they say they should be in.
            // This is because the canRenderInLayer() returns true for all layers for camo blocks,
            // so that the camo model can render in all the layers it needs to.
            if ((isCamoModel || baseActualState.getBlock().getBlockLayer() == layer) &&
                baseActualState.getBlock().canRenderInLayer(baseActualState, layer))
            {
                quads.addAll(baseModel.getQuads(baseExtendedState, face, 0));
            }

            // The optional model is only used for the Elevator color overlay
            if (optionalModel != null && optionalState != null && layer == BlockRenderLayer.CUTOUT)
            {
                quads.addAll(optionalModel.getQuads(optionalState, face, 0));
            }

            builder.put(Optional.of(face), quads.build());
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        if ((isCamoModel || baseActualState.getBlock().getBlockLayer() == layer) &&
            baseActualState.getBlock().canRenderInLayer(baseActualState, layer))
        {
            quads.addAll(baseModel.getQuads(baseExtendedState, null, 0));
        }

        // The optional model is only used for the Elevator color overlay
        if (optionalModel != null && optionalState != null && layer == BlockRenderLayer.CUTOUT)
        {
            quads.addAll(optionalModel.getQuads(optionalState, null, 0));
        }

        builder.put(Optional.empty(), quads.build());

        return builder.build();
    }

    protected IBakedModel getBakedBaseModel(IBlockState state)
    {
        if (state.getPropertyKeys().contains(BlockEnderUtilities.FACING))
        {
            return this.baseModel.bake(new TRSRTransformation(state.getValue(BlockEnderUtilities.FACING)), this.format, this.bakedTextureGetter);
        }
        else if (state.getPropertyKeys().contains(BlockEnderUtilities.FACING_H))
        {
            return this.baseModel.bake(new TRSRTransformation(state.getValue(BlockEnderUtilities.FACING_H)), this.format, this.bakedTextureGetter);
        }
        else
        {
            return this.bakedBaseModel;
        }
    }
}
