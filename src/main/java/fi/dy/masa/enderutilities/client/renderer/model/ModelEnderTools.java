package fi.dy.masa.enderutilities.client.renderer.model;

import java.util.Collection;
import java.util.Map;

import javax.vecmath.Matrix4f;

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
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelPart;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SuppressWarnings("deprecation")
public class ModelEnderTools extends ModelModularItem
{
    /*private static final float NORTH_Z_BASE = 7.496f / 16f;
    private static final float SOUTH_Z_BASE = 8.504f / 16f;
    private static final float NORTH_Z_MODULE = 7.2f / 16f;
    private static final float SOUTH_Z_MODULE = 8.8f / 16f;*/

    private final ResourceLocation resourceRod;
    private final ResourceLocation resourceHead;
    private final ResourceLocation resourceCore;
    private final ResourceLocation resourceCapacitor;
    private final ResourceLocation resourceLinkCrystal;

    public ModelEnderTools()
    {
        this(null, false, false, 0, 0, 0, 0);
    }

    public ModelEnderTools(ItemEnderTool.ToolType tool, boolean powered, boolean broken, int mode, int core, int capacitor, int linkCrystal)
    {
        String toolClass = tool != null ? tool.getToolClass() : "none";
        String strHead = toolClass + ".head." + (broken ? "broken." : "") + (powered ? "glow." : "normal.") + mode;
        this.resourceRod = ReferenceTextures.getItemTexture("endertool." + toolClass + ".rod");
        this.resourceHead = ReferenceTextures.getItemTexture("endertool." + strHead);
        this.resourceCore = core >= 1 && core <= 3 ? ReferenceTextures.getItemTexture("endertool.module.core." + core) : null;
        this.resourceCapacitor = capacitor >= 1 && capacitor <= 3 ? ReferenceTextures.getItemTexture("endertool.module.capacitor." + capacitor) : null;
        this.resourceLinkCrystal = linkCrystal >= 1 && linkCrystal <= 3 ? ReferenceTextures.getItemTexture("endertool.module.linkcrystal." + linkCrystal) : null;
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

        builder.add(ReferenceTextures.getItemTexture("endertool.axe.rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.rod"));

        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.glow.3"));

        // Broken versions
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.axe.head.broken.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.hoe.head.broken.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.pickaxe.head.broken.glow.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.normal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.normal.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.normal.3"));

        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.glow.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.glow.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.shovel.head.broken.glow.3"));

        // Modules
        builder.add(ReferenceTextures.getItemTexture("endertool.module.core.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.core.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.core.3"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.capacitor.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.capacitor.2"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.capacitor.3"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.linkcrystal.1"));
        builder.add(ReferenceTextures.getItemTexture("endertool.module.linkcrystal.2"));

        return builder.build();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        ItemEnderTool.ToolType tool = ItemEnderTool.ToolType.fromToolClass(customData.get("toolClass"));
        boolean broken = (customData.get("broken") != null ? customData.get("broken").equals("true") : false);
        boolean powered = (customData.get("powered") != null ? customData.get("powered").equals("true") : false);
        int mode = 1;
        int core = 0;
        int capacitor = 0;
        int linkCrystal = 0;

        try
        {
            mode = Integer.parseInt(customData.get("mode")) + 1;
            core = Integer.parseInt(customData.get("core")) + 1;
            capacitor = Integer.parseInt(customData.get("capacitor")) + 1;
            linkCrystal = Integer.parseInt(customData.get("lc")) + 1;
        }
        catch (NumberFormatException e)
        {
            EnderUtilities.logger.warn("ModelEnderTools: Failed to parse tool/module types");
        }

        return new ModelEnderTools(tool, powered, broken, mode, core, capacitor, linkCrystal);
    }

    @Override
    public IFlexibleBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);
        TRSRTransformation transform = state.apply(Optional.<IModelPart>absent()).or(TRSRTransformation.identity());
        TextureAtlasSprite rodSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        rodSprite = bakedTextureGetter.apply(this.resourceRod);

        if (this.resourceRod != null)
        {
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceRod))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceHead != null)
        {
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceHead))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceCore != null)
        {
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceCore))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceCapacitor != null)
        {
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceCapacitor))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        if (this.resourceLinkCrystal != null)
        {
            IFlexibleBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceLinkCrystal))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getGeneralQuads());
        }

        return new BakedEnderTool(this, builder.build(), rodSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IFlexibleBakedModel>newHashMap());
    }

    protected static class BakedEnderTool extends ItemLayerModel.BakedModel implements ISmartItemModel, IPerspectiveAwareModel
    {
        private final ModelEnderTools parent;
        private final Map<String, IFlexibleBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;

        public BakedEnderTool(ModelEnderTools parent, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
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
            //System.out.println("handleItemState()");
            ItemEnderTool item = (ItemEnderTool)stack.getItem();
            String toolClass = ItemEnderTool.ToolType.fromStack(stack).getToolClass();
            String broken = String.valueOf(item.isToolBroken(stack));
            String powered = String.valueOf(ItemEnderTool.PowerStatus.fromStack(stack) == ItemEnderTool.PowerStatus.POWERED);
            String mode = String.valueOf(ItemEnderTool.DropsMode.fromStack(stack).ordinal());
            String core = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE));
            String cap = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCAPACITOR));
            String lc = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL));
            String key = toolClass + broken + powered + mode + core + cap + lc;

            if (this.cache.containsKey(key) == false)
            {
                ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
                map.put("toolClass", toolClass);
                map.put("broken", broken);
                map.put("powered", powered);
                map.put("mode", mode);
                map.put("core", core);
                map.put("capacitor", cap);
                map.put("lc", lc);
                IModel model = parent.process(map.build());

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
}
