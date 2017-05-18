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
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;

public class ModelCamouflageBlockBaked implements IBakedModel
{
    public static final Map<Long, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE =
            new HashMap<Long, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>>();
    private final ImmutableMap<String, String> textures;
    private final IBlockState defaultState;
    private final IBakedModel bakedBaseModel;
    private final IBakedModel bakedOverlayModel;
    private final TextureAtlasSprite particle;

    public ModelCamouflageBlockBaked(ITextureMapped textureMapping, IModel baseModel, IModel overlayModel, IBlockState defaultState,
            IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.textures = ImmutableMap.copyOf(textureMapping.getTextureMapping());
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

        IBlockState camoState = ((IExtendedBlockState) state).getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCK);
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

    public static class ModelCamouflageBlockBase implements IModel, ITextureMapped
    {
        private final IBlockState defaultState;
        private final ResourceLocation baseModel;
        private final ResourceLocation overlayModel;
        protected final Map<String, String> textures = new HashMap<String, String>();

        public ModelCamouflageBlockBase(IBlockState defaultState, ResourceLocation baseModel, ResourceLocation overlayModel)
        {
            this.defaultState = defaultState;
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
            return Lists.newArrayList(this.baseModel, this.overlayModel);
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
            IModel overlayModel = null;

            try
            {
                baseModel    = ModelLoaderRegistry.getModel(this.baseModel);
                overlayModel = ModelLoaderRegistry.getModel(this.overlayModel);
            }
            catch (Exception e)
            {
                EnderUtilities.logger.warn("Failed to load a model for a camouflage block", e);
            }

            return new ModelCamouflageBlockBaked(this, baseModel, overlayModel, this.defaultState, state, format, bakedTextureGetter);
        }

        @Override
        public ImmutableMap<String, String> getTextureMapping()
        {
            return ImmutableMap.copyOf(this.textures);
        }
    }
}
