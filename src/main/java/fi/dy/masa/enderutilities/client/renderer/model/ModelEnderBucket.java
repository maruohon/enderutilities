package fi.dy.masa.enderutilities.client.renderer.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.*;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SuppressWarnings("deprecation")
public class ModelEnderBucket implements IModel, IModelCustomData
{
    public static final IModel MODEL = new ModelEnderBucket();
    private final ResourceLocation resourceMain;
    private final ResourceLocation resourceInsideTop;
    private final ResourceLocation resourceInsideBottom;
    private final ResourceLocation resourceModeIcon;
    private final Fluid fluid;
    private final boolean flipGas;
    private final int amount;
    private final int capacity;

    public ModelEnderBucket()
    {
        this(null, null, null, null, null, 0, 0, false);
    }

    public ModelEnderBucket(ResourceLocation main, ResourceLocation insideTop, ResourceLocation insideBottom,
                            ResourceLocation mode, Fluid fluid, int amount, int capacity, boolean flipGas)
    {
        this.resourceMain = main;
        this.resourceInsideTop = insideTop;
        this.resourceInsideBottom = insideBottom;
        this.resourceModeIcon = mode;
        this.fluid = fluid;
        this.flipGas = flipGas;
        this.amount = amount;
        this.capacity = capacity;
    }

    @Override
    public IModelState getDefaultState()
    {
        return TRSRTransformation.identity();
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.normal.main"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.normal.insidetop"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.normal.insidebottom"));

        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.linked.main"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.linked.insidetop"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.linked.insidebottom"));

        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.mode.drain"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.mode.fill"));
        builder.add(ReferenceTextures.getItemTexture("enderbucket.32.mode.bind"));

        return builder.build();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        String fluidName = customData.get("fluid");
        Fluid fluid = fluidName != null ? FluidRegistry.getFluid(fluidName) : null;
        boolean flip = this.flipGas;
        String tmp = customData.get("linked");
        boolean isLinked = (tmp != null && tmp.equals("true"));
        int amount = 0;
        int capacity = 1000;
        String mode = null;

        if (customData.containsKey("mode") == true)
        {
            tmp = customData.get("mode");
            if (tmp != null && (tmp.equals("drain") || tmp.equals("fill") || tmp.equals("bind")))
            {
                mode = tmp;
            }
        }

        if (customData.containsKey("amount") == true)
        {
            try
            {
                amount = Integer.valueOf(customData.get("amount"));
            }
            catch (NumberFormatException e) {}
        }

        if (customData.containsKey("capacity") == true)
        {
            try
            {
                capacity = Integer.valueOf(customData.get("capacity"));
            }
            catch (NumberFormatException e) {}
        }

        String rlBase = Reference.MOD_ID + ":items/enderbucket.32.";
        ResourceLocation main = new ResourceLocation(rlBase + (isLinked ? "linked." : "normal.") + "main");
        ResourceLocation inTop = new ResourceLocation(rlBase + (isLinked ? "linked." : "normal.") + "insidetop");
        ResourceLocation inBot = new ResourceLocation(rlBase + (isLinked ? "linked." : "normal.") + "insidebottom");
        ResourceLocation rlMode = mode != null ? new ResourceLocation(rlBase + "mode." + mode) : null;

        return new ModelEnderBucket(main, inTop, inBot, rlMode, fluid, amount, capacity, flip);
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);
        TRSRTransformation transform = state.apply(Optional.<IModelPart>absent()).or(TRSRTransformation.identity());
        TextureAtlasSprite mainSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        if (this.resourceMain != null)
        {
            mainSprite = bakedTextureGetter.apply(this.resourceMain);
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceMain))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceInsideTop != null)
        {
            // Inset the inside part a little, the fluid model will be on top of it
            IModelState stateTmp = this.getTransformedModelState(state, 0f, 0.95f);
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceInsideTop))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceInsideBottom != null)
        {
            // Inset the inside part a little, the fluid model will be on top of it
            IModelState stateTmp = this.getTransformedModelState(state, 0f, 0.95f);
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceInsideBottom))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceModeIcon != null)
        {
            // Offset the mode icons a bit, so that they stick out slightly
            IModelState stateTmp = this.getTransformedModelState(state, 0.0125f, 1f);
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceModeIcon))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.fluid != null)
        {
            TextureAtlasSprite fluidTex = bakedTextureGetter.apply(fluid.getStill());
            float capacity = this.capacity > 0 ? this.capacity : 1000;
            float height = (float)this.amount / capacity;
            // top x: 4 .. 12 ; y: 3 .. 7
            // bottom: x: 6.5 .. 10 ; y: 9 .. 13
            float yt = 7 - height * 4;
            float yb = 13 - height * 4;
            // Top part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform,   4f, yt, 12f,  7f, 0.469f, fluidTex, EnumFacing.NORTH, 0xffffffff));
            // Bottom part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform, 6.5f, yb, 10f, 13f, 0.469f, fluidTex, EnumFacing.NORTH, 0xffffffff));

            // Top part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform,   4f, yt, 12f,  7f, 0.531f, fluidTex, EnumFacing.SOUTH, 0xffffffff));
            // Bottom part fluid
            builder.add(ItemTextureQuadConverter.genQuad(format, transform, 6.5f, yb, 10f, 13f, 0.531f, fluidTex, EnumFacing.SOUTH, 0xffffffff));
        }

        return new BakedEnderBucket(this, builder.build(), mainSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IFlexibleBakedModel>newHashMap());
    }

    private IModelState getTransformedModelState(IModelState state, float offZ, float scaleZ)
    {
        TRSRTransformation tr = new TRSRTransformation(new Vector3f(0f, 0f, offZ), null, new Vector3f(1f, 1f, scaleZ), null);
        return new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(tr));
    }

    protected static class BakedEnderBucket extends ItemLayerModel.BakedModel implements ISmartItemModel, IPerspectiveAwareModel
    {
        private final ModelEnderBucket parent;
        private final Map<String, IFlexibleBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;

        public BakedEnderBucket(ModelEnderBucket parent, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
                ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, Map<String, IFlexibleBakedModel> cache)
        {
            super(quads, particle, format);
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
        }

        @Override
        public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, this.transforms, cameraTransformType);
        }

        @Override
        public IBakedModel handleItemState(ItemStack stack)
        {
            if ((stack.getItem() instanceof ItemEnderBucket) == false)
            {
                return this;
            }

            ItemEnderBucket item = (ItemEnderBucket)stack.getItem();
            String linked = item.getBucketLinkMode(stack) == ItemEnderBucket.LINK_MODE_ENABLED ? "true" : "false";
            int capacity = item.getCapacityCached(stack, null);
            int modeInt = item.getBucketMode(stack);
            String mode = "none";
            int amount = 0;

            FluidStack fluidStack = item.getFluidCached(stack);
            Fluid fluid = null;

            if (fluidStack != null)
            {
                amount = fluidStack.amount;
                fluid = fluidStack.getFluid();
            }

            if (modeInt == ItemEnderBucket.OPERATION_MODE_DRAIN_BUCKET) mode = "drain";
            else if (modeInt == ItemEnderBucket.OPERATION_MODE_FILL_BUCKET) mode = "fill";
            else if (modeInt == ItemEnderBucket.OPERATION_MODE_BINDING) mode = "bind";

            String key = linked + "_" + mode + "_" + fluid + "_" + amount + "_" + capacity;

            if (this.cache.containsKey(key) == false)
            {
                ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
                if (fluid != null)
                {
                    map.put("fluid", fluid.getName());
                }
                map.put("linked", linked);
                map.put("mode", mode);
                map.put("amount", String.valueOf(amount));
                map.put("capacity", String.valueOf(capacity));

                IModel model = this.parent.process(map.build());

                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
                {
                    public TextureAtlasSprite apply(ResourceLocation location)
                    {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };

                IFlexibleBakedModel bakedModel = model.bake(new SimpleModelState(this.transforms), this.getFormat(), textureGetter);
                this.cache.put(key, bakedModel);

                return bakedModel;
            }

            return this.cache.get(key);
        }
    }

    public enum LoaderEnderBucket implements ICustomModelLoader
    {
        instance;

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && modelLocation.getResourcePath().contains("generated_model_enderbucket");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws IOException
        {
            return MODEL;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager)
        {
            // no need to clear cache since we create a new model instance
        }
    }
}
