package fi.dy.masa.enderutilities.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.model.EnderUtilitiesItemSmartModel;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class EnderUtilitiesModelRegistry
{
    public static void registerBlockModels(IRegistry modelRegistry)
    {
        ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        Item item = Item.getItemFromBlock(EnderUtilitiesBlocks.machine_0);
        ModelBakery.addVariantName(item, "enderfurnace.off", "toolworkstation", "enderinfuser");

        registerBlockModel(imm, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE,     0); // Ender Furnace
        registerBlockModel(imm, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION,  1); // Tool Workstation
        registerBlockModel(imm, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER,     2); // Ender Infuser
    }

    public static void registerBlockModel(ItemModelMesher itemModelMesher, String name, int meta)
    {
        /*Item item = Item.getItemFromBlock(Block.getBlockFromName(Reference.MOD_ID + ":" + name));
        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        itemModelMesher.register(item, meta, mrl);*/
        registerModel(itemModelMesher, name, meta, "inventory");
    }

    public static void registerItemMeshDefinitions()
    {
        ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        ItemMeshDefinition imd = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                // We use the Lasso as a base model for the ISmartItemModel, see below
                return new ModelResourceLocation(Reference.MOD_ID + ":" + ReferenceNames.NAME_ITEM_ENDER_LASSO, "inventory");
            }
        };

        itemModelMesher.register(EnderUtilitiesItems.enderArrow,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBag,              imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBow,              imd);
        itemModelMesher.register(EnderUtilitiesItems.enderBucket,           imd);
        itemModelMesher.register(EnderUtilitiesItems.enderLasso,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPearlReusable,    imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPorter,           imd);
        itemModelMesher.register(EnderUtilitiesItems.enderCapacitor,        imd);
        itemModelMesher.register(EnderUtilitiesItems.enderPart,             imd);
        itemModelMesher.register(EnderUtilitiesItems.enderSword,            imd);
        itemModelMesher.register(EnderUtilitiesItems.enderTool,             imd);
        itemModelMesher.register(EnderUtilitiesItems.linkCrystal,           imd);
        itemModelMesher.register(EnderUtilitiesItems.mobHarness,            imd);
    }

    public static void registerItemModels(IRegistry modelRegistry)
    {
        ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        String name = ReferenceNames.NAME_ITEM_ENDER_LASSO;
        // We use the Lasso as a base model for the ISmartItemModel, which will operate as a wrapper for generating the actual models for each item
        registerModel(itemModelMesher, name, 0, "inventory");

        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        modelRegistry.putObject(mrl, new EnderUtilitiesItemSmartModel((IBakedModel)modelRegistry.getObject(mrl)));
    }

    public static void registerModel(ItemModelMesher itemModelMesher, String name, int meta, String type)
    {
        Item item = GameRegistry.findItem(Reference.MOD_ID, name);
        if (item == null)
        {
            EnderUtilities.logger.fatal(String.format("Failed registering model for %s (meta: %d), type: %s", name, meta, type));
            return;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, type);
        itemModelMesher.register(item, meta, mrl);
    }
}
