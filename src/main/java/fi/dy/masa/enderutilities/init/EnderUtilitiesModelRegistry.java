package fi.dy.masa.enderutilities.init;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Reference;

@SideOnly(Side.CLIENT)
public class EnderUtilitiesModelRegistry
{
    public static void registerBlockModels(IRegistry modelRegistry)
    {
        ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        Item item = Item.getItemFromBlock(EnderUtilitiesBlocks.machine_0);
        ModelBakery.addVariantName(item, "enderfurnace.off", "toolworkstation", "enderinfuser");

        registerBlockModel(imm, "enderfurnace.off", 0); // Ender Furnace
        registerBlockModel(imm, "toolworkstation",  1); // Tool Workstation
        registerBlockModel(imm, "enderinfuser",     2); // Ender Infuser
    }

    public static void registerBlockModel(ItemModelMesher itemModelMesher, String name, int meta)
    {
        Item item = Item.getItemFromBlock(Block.getBlockFromName(Reference.MOD_ID + ":" + name));
        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        itemModelMesher.register(item, meta, mrl);
    }

    /*public static void registerItemModel(String name, int damage)
    {
        Item item = GameRegistry.findItem(Reference.MOD_ID, name);
        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, damage, mrl);
    }*/

    public static void registerItemMeshDefinitions()
    {
        ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        ItemMeshDefinition imd = new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation(Reference.MOD_ID + ":" + "ismartitemmodel_generic", "inventory");
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
        //ModelResourceLocation mrlInventory = new ModelResourceLocation(Reference.MOD_ID + ":" + "ismartitemmodel_generic", "inventory");
        //modelRegistry.putObject(mrlInventory, null);
    }
}
