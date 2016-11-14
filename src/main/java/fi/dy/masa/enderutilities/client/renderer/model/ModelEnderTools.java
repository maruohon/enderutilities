package fi.dy.masa.enderutilities.client.renderer.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelCustomData;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public class ModelEnderTools implements IModel, IModelCustomData
{
    public static final IModel MODEL = new ModelEnderTools();
    private final ResourceLocation resourceRod;
    private final ResourceLocation resourceHead;
    private final ResourceLocation resourceCore;
    private final ResourceLocation resourceCapacitor;
    private final ResourceLocation resourceLinkCrystal;
    private final String tool;
    protected static final Map<String, String> moduleTransforms = Maps.newHashMap();

    public ModelEnderTools()
    {
        this(null, false, false, 0, 0, 0, 0);
    }

    public ModelEnderTools(String toolClass, boolean powered, boolean broken, int mode, int core, int capacitor, int linkCrystal)
    {
        this.tool = toolClass;
        String strHead = toolClass + "_head_" + (broken ? "broken_" : "") + (powered ? "glow_" : "normal_") + mode;
        this.resourceRod = ReferenceTextures.getItemTexture("endertool_" + toolClass + "_rod");
        this.resourceHead = ReferenceTextures.getItemTexture("endertool_" + strHead);
        this.resourceCore = core >= 1 && core <= 3 ? ReferenceTextures.getItemTexture("endertool_module_core_" + core) : null;
        this.resourceCapacitor = capacitor >= 1 && capacitor <= 4 ? ReferenceTextures.getItemTexture("endertool_module_capacitor_" + capacitor) : null;
        this.resourceLinkCrystal = linkCrystal >= 1 && linkCrystal <= 3 ? ReferenceTextures.getItemTexture("endertool_module_linkcrystal_" + linkCrystal) : null;
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

        builder.add(ReferenceTextures.getItemTexture("endertool_axe_rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_rod"));

        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_glow_3"));

        // Broken versions
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_axe_head_broken_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_hoe_head_broken_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_pickaxe_head_broken_glow_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_normal_3"));

        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_glow_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_glow_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_shovel_head_broken_glow_3"));

        // Sword
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_rod"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_normal_3"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_normal_4"));

        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_broken_normal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_broken_normal_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_broken_normal_3"));
        builder.add(ReferenceTextures.getItemTexture("endertool_sword_head_broken_normal_4"));

        // Modules
        builder.add(ReferenceTextures.getItemTexture("endertool_module_core_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_core_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_core_3"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_capacitor_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_capacitor_2"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_capacitor_3"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_capacitor_4"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_linkcrystal_1"));
        builder.add(ReferenceTextures.getItemTexture("endertool_module_linkcrystal_2"));

        return builder.build();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData)
    {
        for (Map.Entry<String, String> entry : customData.entrySet())
        {
            String key = entry.getKey();
            //System.out.printf("customData: %s => %s\n", key, entry.getValue());
            if (key != null && key.startsWith("tr_") == true)
            {
                moduleTransforms.put(key, entry.getValue());
            }
        }

        String toolClass = customData.get("toolClass");
        boolean broken = (customData.get("broken") != null ? customData.get("broken").equals("true") : false);
        boolean powered = (customData.get("powered") != null ? customData.get("powered").equals("true") : false);
        int mode = 1;
        int core = 0;
        int capacitor = 0;
        int linkCrystal = 0;

        if (customData.containsKey("mode") == true)
        {
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
        }

        return new ModelEnderTools(toolClass, powered, broken, mode, core, capacitor, linkCrystal);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
                                    Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = IPerspectiveAwareModel.MapWrapper.getTransforms(state);
        //TRSRTransformation transform = state.apply(Optional.<IModelPart>absent()).or(TRSRTransformation.identity());
        TextureAtlasSprite rodSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        rodSprite = bakedTextureGetter.apply(this.resourceRod);

        if (this.resourceRod != null)
        {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceRod))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceHead != null)
        {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceHead))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceCore != null)
        {
            IModelState stateTmp = this.getTransformedModelState(state, "co");
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceCore))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceCapacitor != null)
        {
            IModelState stateTmp = this.getTransformedModelState(state, "ca");
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceCapacitor))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (this.resourceLinkCrystal != null)
        {
            IModelState stateTmp = this.getTransformedModelState(state, "lc");
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(this.resourceLinkCrystal))).bake(stateTmp, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        return new BakedEnderTool(this, builder.build(), rodSprite, format, Maps.immutableEnumMap(transformMap), Maps.<String, IBakedModel>newHashMap());
    }

    private IModelState getTransformedModelState(IModelState state, String module)
    {
        ModuleTransforms mt = new ModuleTransforms(this, this.tool, module);
        TRSRTransformation tr = new TRSRTransformation(new Vector3f(mt.tx, mt.ty, mt.tz), null, new Vector3f(mt.sx, mt.sy, mt.sz), null);
        return new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(tr));
    }

    protected static class ModuleTransforms
    {
        public final float tx;
        public final float ty;
        public final float tz;
        public final float sx;
        public final float sy;
        public final float sz;

        private ModuleTransforms(ModelEnderTools parent, String tool, String module)
        {
            float tx = 0f, ty = 0f, tz = 0f, sx = 1.02f, sy = 1.02f, sz = 1.6f;
            String id = tool.equals("sword") == true ? "w" : tool.substring(0, 1);

            try
            {
                String str = moduleTransforms.get("tr_tx_" + id + "_" + module);
                if (str != null) tx = Float.valueOf(str);

                str = moduleTransforms.get("tr_ty_" + id + "_" + module);
                if (str != null) ty = Float.valueOf(str);

                str = moduleTransforms.get("tr_tz_" + id + "_" + module);
                if (str != null) tz = Float.valueOf(str);

                str = moduleTransforms.get("tr_sx_" + id + "_" + module);
                if (str != null) sx = Float.valueOf(str);

                str = moduleTransforms.get("tr_sy_" + id + "_" + module);
                if (str != null) sy = Float.valueOf(str);

                str = moduleTransforms.get("tr_sz_" + id + "_" + module);
                if (str != null) sz = Float.valueOf(str);
            }
            catch (NumberFormatException e)
            {
                EnderUtilities.logger.warn("Exception while parsing Ender Tool module transformations");
            }

            //System.out.printf("tx: %.2f, ty: %.2f, tz: %.2f, sx: %.2f, sy: %.2f, sz: %.2f\n", tx, ty, tz, sx, sy, sz);
            this.tx = tx;
            this.ty = ty;
            this.tz = tz;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }
    }

    private static final class BakedEnderToolOverrideHandler extends ItemOverrideList
    {
        public static final BakedEnderToolOverrideHandler INSTANCE = new BakedEnderToolOverrideHandler();

        private BakedEnderToolOverrideHandler()
        {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModelIn, ItemStack stack, World world, EntityLivingBase entity)
        {
            boolean isTool = stack.getItem() == EnderUtilitiesItems.enderTool;
            ItemLocationBoundModular item = (ItemLocationBoundModular)stack.getItem();
            String core = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE));
            String cap = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCAPACITOR));
            String lc = String.valueOf(item.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL));
            String toolClass;
            String broken;
            String powered;
            String mode;
            String key;

            if (isTool == true)
            {
                ItemEnderTool itemTool = (ItemEnderTool)stack.getItem();
                toolClass = ItemEnderTool.ToolType.fromStack(stack).getToolClass();
                broken = String.valueOf(itemTool.isToolBroken(stack));
                powered = String.valueOf(ItemEnderTool.PowerStatus.fromStack(stack) == ItemEnderTool.PowerStatus.POWERED);
                mode = String.valueOf(ItemEnderTool.DropsMode.fromStack(stack).ordinal());
                key = toolClass + broken + powered + mode + core + cap + lc;
            }
            else
            {
                ItemEnderSword itemSword = (ItemEnderSword)stack.getItem();
                toolClass = "sword";
                broken = String.valueOf(itemSword.isToolBroken(stack));
                powered = "false";
                mode = String.valueOf(ItemEnderSword.SwordMode.fromStack(stack).ordinal());
                key = broken + powered + mode + core + cap + lc;
            }

            BakedEnderTool originalModel = (BakedEnderTool) originalModelIn;

            if (originalModel.cache.containsKey(key) == false)
            {
                ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
                map.put("toolClass", toolClass);
                map.put("broken", broken);
                map.put("powered", powered);
                map.put("mode", mode);
                map.put("core", core);
                map.put("capacitor", cap);
                map.put("lc", lc);

                IModel model = originalModel.parent.process(map.build());

                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
                {
                    public TextureAtlasSprite apply(ResourceLocation location)
                    {
                        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
                    }
                };

                IBakedModel bakedModel = model.bake(new SimpleModelState(originalModel.transforms), originalModel.format, textureGetter);
                originalModel.cache.put(key, bakedModel);

                return bakedModel;
            }

            return originalModel.cache.get(key);
        }
    }

    protected static class BakedEnderTool implements IPerspectiveAwareModel
    {
        private final ModelEnderTools parent;
        private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;
        private final ImmutableList<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final VertexFormat format;

        public BakedEnderTool(ModelEnderTools parent, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format,
                ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, Map<String, IBakedModel> cache)
        {
            this.quads = quads;
            this.particle = particle;
            this.format = format;
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
        }

        @Override
        public ItemOverrideList getOverrides()
        {
            return BakedEnderToolOverrideHandler.INSTANCE;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, this.transforms, cameraTransformType);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
        {
            if(side == null) return quads;
            return ImmutableList.of();
        }

        public boolean isAmbientOcclusion() { return true;  }
        public boolean isGui3d() { return false; }
        public boolean isBuiltInRenderer() { return false; }
        public TextureAtlasSprite getParticleTexture() { return particle; }
        public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    }

    public enum LoaderEnderTools implements ICustomModelLoader
    {
        instance;

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && modelLocation.getResourcePath().contains("generated_model_endertool");
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
