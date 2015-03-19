package fi.dy.masa.enderutilities.client.resources;

import java.util.Map;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

@SideOnly(Side.CLIENT)
public class EnderUtilitiesModelRegistry
{
    public static Map<ResourceLocation, ModelBlock> models = Maps.newHashMap();
    public static IFlexibleBakedModel baseBlockModel;
    public static IFlexibleBakedModel baseItemModel;
    public static ModelBlock modelBlockBaseItems;
    public static ModelBlock modelBlockBaseBlocks;
    public static ItemMeshDefinition baseItemMeshDefinition;
    public static ItemMeshDefinition baseItemBlockMeshDefinition;

    public static void registerBlockModels(ModelManager modelManager, IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        String name = Reference.MOD_ID + ":" + ReferenceNames.NAME_MODEL_BLOCK_BASE;

        // Register the base ISmartBlockModel that the fetches the correct model from the Block/Machine classes
        // via the handleBlockState() method.
        ModelResourceLocation mrl = new ModelResourceLocation(name, "normal");
        baseBlockModel = new EnderUtilitiesSmartBlockModel(itemModelMesher.getModelManager().getModel(mrl));
        modelRegistry.putObject(new ModelResourceLocation(name, "normal"), baseBlockModel);
        modelRegistry.putObject(new ModelResourceLocation(name, "inventory"), baseBlockModel);

        modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + "machine.0", "normal"), baseBlockModel);

        StateMap sm = (new StateMap.Builder()).addPropertiesToIgnore(new IProperty[] {BlockEnderUtilitiesTileEntity.FACING, BlockEnderUtilitiesTileEntity.MACHINE_TYPE, BlockEnderUtilitiesTileEntity.MACHINE_MODE}).build();
        modelManager.getBlockModelShapes().registerBlockWithStateMapper(EnderUtilitiesBlocks.machine_0, sm);

        TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();
        EnderUtilitiesBlocks.machine_0.registerModels(modelRegistry, textures, models);

        // Register the ItemMeshDefinition that will redirect the ItemBlock model retrieval to the same base class that handles the blocks themselves
        itemModelMesher.register(GameRegistry.findItem(Reference.MOD_ID, ReferenceNames.NAME_TILE_MACHINE_0), EnderUtilitiesModelRegistry.baseItemBlockMeshDefinition);
    }

    public static void registerItemModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher)
    {
        String name = Reference.MOD_ID + ":" + ReferenceNames.NAME_MODEL_ITEM_BASE;

        // Register the base ISmartItemModel that the fetches the correct model from the Item classes
        // via the handleItemState() method.
        // For this ISmartItemModel model to be fetched first for each item, the items register the common ItemMeshDefinition
        // 'baseItemMeshDefinition' (see below in setupBaseModels()), which returns the ModelResourceLocation
        // of this ISmartItemModel for all items.
        ModelResourceLocation mrl = new ModelResourceLocation(name, "inventory");
        baseItemModel = new EnderUtilitiesSmartItemModel(itemModelMesher.getModelManager().getModel(mrl));
        modelRegistry.putObject(mrl, baseItemModel);

        TextureMap textures = Minecraft.getMinecraft().getTextureMapBlocks();

        EnderUtilitiesItems.enderArrow.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBag.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBow.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderBucket.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderLasso.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPearlReusable.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPorter.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderCapacitor.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.enderPart.registerModels(modelRegistry, itemModelMesher, textures, models);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerModels(modelRegistry, itemModelMesher, textures, models);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.linkCrystal.registerModels(modelRegistry, itemModelMesher, textures, models);
        EnderUtilitiesItems.mobHarness.registerModels(modelRegistry, itemModelMesher, textures, models);
    }

    public static boolean setupBaseModels()
    {
        // Setup the common ItemMeshDefinition which returns the ModelResourceLocation of the
        // base ISmartItemModel (see above), which then gets the actual model from the item classes.
        baseItemMeshDefinition = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // Base model for the ISmartItemModel
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_MODEL_ITEM_BASE, "inventory");
            }
        };

        baseItemBlockMeshDefinition = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // Base model for the ISmartItemModel, which is used for the ItemBlocks
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_MODEL_BLOCK_BASE, "inventory");
            }
        };

        ModelBlock modelBlock = ModelBlock.deserialize("{\"elements\":[{" + 
                                                            "\"from\": [0, 0, 0], \"to\": [16, 16, 16]," +
                                                            "\"faces\": { \"down\": {\"uv\": [0, 0, 16, 16], \"texture\":\"\"}   }}]}");
        modelBlock.name = "minecraft:builtin/generated";
        models.put(new ResourceLocation(modelBlock.name), modelBlock);

        // enderutilities:item/model_item_base
        String name = Reference.MOD_ID + ":" + "item/" + ReferenceNames.NAME_MODEL_ITEM_BASE;
        modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), models, true);
        if (modelBlock == null)
        {
            EnderUtilities.logger.fatal("Failed to load the base ModelBlock for " + name);
            return false;
        }
        modelBlockBaseItems = modelBlock;

        // enderutilities:block/cube
        name = Reference.MOD_ID + ":" + "block/cube";
        modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), models, true);
        if (modelBlock == null)
        {
            EnderUtilities.logger.fatal("Failed to load the base ModelBlock for " + name);
            return false;
        }

        // enderutilities:block/model_block_base
        name = Reference.MOD_ID + ":" + "block/" + ReferenceNames.NAME_MODEL_BLOCK_BASE;
        modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), models, true);
        if (modelBlock == null)
        {
            EnderUtilities.logger.fatal("Failed to load the base ModelBlock for " + name);
            return false;
        }
        modelBlockBaseBlocks = modelBlock;

        return true;
    }
}
