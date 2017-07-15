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
    public static final Map<Long, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>>> QUAD_CACHE = new HashMap<>();
    protected final ImmutableMap<String, String> textures;
    protected final IBlockState defaultState;
    protected final IBakedModel bakedBaseModel;
    @Nullable
    protected final IBakedModel bakedOverlayModel;
    protected final IModel baseModel;
    protected final VertexFormat format;
    protected final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    protected final TextureAtlasSprite particle;

    public ModelCamouflageBlockBaked(ITextureMapped textureMapping, IModel baseModel, @Nullable IModel overlayModel, IBlockState defaultState,
            IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        this.baseModel = baseModel;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.textures = ImmutableMap.copyOf(textureMapping.getTextureMapping());
        this.defaultState = defaultState;
        this.particle = bakedTextureGetter.apply(new ResourceLocation(this.textures.get("particle")));
        this.bakedBaseModel = ((IRetexturableModel) baseModel).retexture(this.textures).bake(state, format, bakedTextureGetter);

        if (overlayModel != null)
        {
            this.bakedOverlayModel = ((IRetexturableModel) overlayModel).retexture(this.textures).bake(state, format, bakedTextureGetter);
        }
        else
        {
            this.bakedOverlayModel = null;
        }
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
            return this.bakedBaseModel.getQuads(state, side, rand);
        }

        IBlockState camoState = ((IExtendedBlockState) state).getValue(BlockEnderUtilitiesTileEntity.CAMOBLOCK);
        ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> quads;
        long id = 0;
        boolean validCamo = camoState != null && camoState.getBlock() != Blocks.AIR;

        if (validCamo)
        {
            id = ((long) Block.getStateId(state)) << 32 | Block.getStateId(camoState);
        }
        else
        {
            id = ((long) Block.getStateId(state)) << 32;
        }

        quads = QUAD_CACHE.get(id);

        if (quads == null)
        {
            if (validCamo)
            {
                IBakedModel camoModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(camoState);
                quads = this.getCombinedQuads(camoModel, camoState, this.bakedOverlayModel, state);
            }
            else
            {
                quads = this.getCombinedQuads(this.getBakedBaseModel(state), state, null, null);
            }

            QUAD_CACHE.put(id, quads);
        }

        return quads.get(Optional.fromNullable(side));
    }

    protected ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> getCombinedQuads(
            @Nonnull IBakedModel baseModel, @Nonnull IBlockState stateBase,
            @Nullable IBakedModel optionalModel, @Nullable IBlockState stateOptional)
    {
        ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();

        for (EnumFacing face : EnumFacing.values())
        {
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            if (optionalModel != null)
            {
                quads.addAll(optionalModel.getQuads(stateOptional, face, 0));
            }

            quads.addAll(baseModel.getQuads(stateBase, face, 0));
            builder.put(Optional.of(face), quads.build());
        }

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

        if (optionalModel != null)
        {
            quads.addAll(optionalModel.getQuads(stateOptional, null, 0));
        }

        quads.addAll(baseModel.getQuads(stateBase, null, 0));
        builder.put(Optional.<EnumFacing>absent(), quads.build());

        return builder.build();
    }

    protected IBakedModel getBakedBaseModel(IBlockState state)
    {
        return this.bakedBaseModel;
    }

    public static class ModelCamouflageBlockBase implements IModel, ITextureMapped
    {
        protected final IBlockState defaultState;
        protected final ResourceLocation baseModelLocation;
        @Nullable
        protected final ResourceLocation overlayModelLocation;
        protected final Map<String, String> textures = new HashMap<String, String>();

        public ModelCamouflageBlockBase(IBlockState defaultState, ResourceLocation baseModel, @Nullable ResourceLocation overlayModel)
        {
            this.defaultState = defaultState;
            this.baseModelLocation = baseModel;
            this.overlayModelLocation = overlayModel;
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
                return Lists.newArrayList(this.baseModelLocation, this.overlayModelLocation);
            }
            else
            {
                return Lists.newArrayList(this.baseModelLocation);
            }
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

            return new ModelCamouflageBlockBaked(this, baseModel, overlayModel, this.defaultState, state, format, bakedTextureGetter);
        }

        @Override
        public ImmutableMap<String, String> getTextureMapping()
        {
            return ImmutableMap.copyOf(this.textures);
        }
    }
}
