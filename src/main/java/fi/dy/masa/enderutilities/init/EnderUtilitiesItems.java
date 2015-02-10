package fi.dy.masa.enderutilities.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.item.ItemEnderArrow;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemEnderBow;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemEnderPearlReusable;
import fi.dy.masa.enderutilities.item.ItemEnderPorter;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class EnderUtilitiesItems
{
    public static final ItemEnderUtilities enderCapacitor = new ItemEnderCapacitor();
    public static final ItemEnderUtilities enderPart = new ItemEnderPart();
    public static final ItemEnderUtilities linkCrystal = new ItemLinkCrystal();

    public static final ItemEnderUtilities enderArrow = new ItemEnderArrow();
    public static final ItemEnderUtilities enderBag = new ItemEnderBag();
    public static final ItemEnderUtilities enderBow = new ItemEnderBow();
    public static final ItemEnderUtilities enderBucket = new ItemEnderBucket();
    public static final ItemEnderUtilities enderLasso = new ItemEnderLasso();
    public static final ItemEnderUtilities enderPearlReusable = new ItemEnderPearlReusable();
    public static final ItemEnderUtilities enderPorter = new ItemEnderPorter();
    public static final Item enderSword = new ItemEnderSword();
    public static final Item enderTool = new ItemEnderTool();
    public static final ItemEnderUtilities mobHarness = new ItemMobHarness();

    public static void init()
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false) { GameRegistry.registerItem(enderPart, ReferenceNames.NAME_ITEM_ENDERPART); }
        if (Configs.disableItemEnderCapacitor.getBoolean(false) == false) { GameRegistry.registerItem(enderCapacitor, ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR); }
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false) { GameRegistry.registerItem(linkCrystal, ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL); }
        if (Configs.disableItemEnderArrow.getBoolean(false) == false) { GameRegistry.registerItem(enderArrow, ReferenceNames.NAME_ITEM_ENDER_ARROW); }
        if (Configs.disableItemEnderBag.getBoolean(false) == false) { GameRegistry.registerItem(enderBag, ReferenceNames.NAME_ITEM_ENDER_BAG); }
        if (Configs.disableItemEnderBow.getBoolean(false) == false) { GameRegistry.registerItem(enderBow, ReferenceNames.NAME_ITEM_ENDER_BOW); }
        if (Configs.disableItemEnderBucket.getBoolean(false) == false) { GameRegistry.registerItem(enderBucket, ReferenceNames.NAME_ITEM_ENDER_BUCKET); }
        if (Configs.disableItemEnderLasso.getBoolean(false) == false) { GameRegistry.registerItem(enderLasso, ReferenceNames.NAME_ITEM_ENDER_LASSO); }
        if (Configs.disableItemEnderPearl.getBoolean(false) == false) { GameRegistry.registerItem(enderPearlReusable, ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE); }
        if (Configs.disableItemEnderPorter.getBoolean(false) == false) { GameRegistry.registerItem(enderPorter, ReferenceNames.NAME_ITEM_ENDER_PORTER); }
        if (Configs.disableItemEnderSword.getBoolean(false) == false) { GameRegistry.registerItem(enderSword, ReferenceNames.NAME_ITEM_ENDER_SWORD); }
        if (Configs.disableItemEnderTools.getBoolean(false) == false) { GameRegistry.registerItem(enderTool, ReferenceNames.NAME_ITEM_ENDERTOOL); }
        if (Configs.disableItemMobHarness.getBoolean(false) == false) { GameRegistry.registerItem(mobHarness, ReferenceNames.NAME_ITEM_MOB_HARNESS); }

        ItemStack bucket = new ItemStack(Items.bucket);
        ItemStack diamond = new ItemStack(Items.diamond);
        ItemStack diamond_block = new ItemStack(Blocks.diamond_block);
        ItemStack emerald = new ItemStack(Items.emerald);
        ItemStack emerald_block = new ItemStack(Blocks.emerald_block);
        ItemStack eye = new ItemStack(Items.ender_eye);
        ItemStack glass = new ItemStack(Blocks.glass);
        ItemStack gold = new ItemStack(Items.gold_ingot);
        ItemStack goldnugget = new ItemStack(Items.gold_nugget);
        ItemStack iron = new ItemStack(Items.iron_ingot);
        ItemStack leather = new ItemStack(Items.leather);
        ItemStack obsidian = new ItemStack(Blocks.obsidian);
        ItemStack pearl = new ItemStack(Items.ender_pearl);
        ItemStack redstone = new ItemStack(Items.redstone);
        ItemStack redstone_block = new ItemStack(Blocks.redstone_block);
        ItemStack stick = new ItemStack(Items.stick);
        ItemStack string = new ItemStack(Items.string);
        ItemStack wool = new ItemStack(Blocks.wool);

        ItemStack alloy0 = new ItemStack(enderPart, 1, 0);
        ItemStack alloy1 = new ItemStack(enderPart, 1, 1);
        ItemStack alloy2 = new ItemStack(enderPart, 1, 2);
        ItemStack core0 = new ItemStack(enderPart, 1, 10);
        ItemStack core1 = new ItemStack(enderPart, 1, 11);
        //ItemStack core2 = new ItemStack(enderPart, 1, 12);
        //ItemStack active_core0 = new ItemStack(enderPart, 1, 15);
        ItemStack active_core1 = new ItemStack(enderPart, 1, 16);
        ItemStack active_core2 = new ItemStack(enderPart, 1, 17);
        ItemStack ender_stick = new ItemStack(enderPart, 1, 20);
        ItemStack rope = new ItemStack(enderPart, 1, 21);

        // "Usable" items
        if (Configs.disableRecipeEnderArrow.getBoolean(false) == false && Configs.disableItemEnderArrow.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderArrow), " NP", " AN", "E  ", 'N', goldnugget, 'P', pearl, 'A', new ItemStack(Items.arrow), 'E', eye);
        }
        if (Configs.disableRecipeEnderBag.getBoolean(false) == false && Configs.disableItemEnderBag.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBag), "RLR", "LCL", "RWR", 'R', rope, 'L', leather, 'C', core1, 'W', wool);
        }
        if (Configs.disableRecipeEnderBow.getBoolean(false) == false && Configs.disableItemEnderBow.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBow), "RA ", "RCA", "RA ", 'R', rope, 'A', alloy1, 'C', core1);
        }
        if (Configs.disableRecipeEnderBucket.getBoolean(false) == false && Configs.disableItemEnderBucket.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBucket), " C ", "ABA", "OAO", 'C', core0, 'A', alloy0, 'B', bucket, 'O', obsidian);
        }
        if (Configs.disableRecipeEnderLasso.getBoolean(false) == false && Configs.disableItemEnderLasso.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderLasso), "RR ", "RC ", "  A", 'R', rope, 'C', core1, 'A', alloy0);
        }
        if (Configs.disableRecipeEnderPearl.getBoolean(false) == false && Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable), "APA", "PRP", "APA", 'A', alloy0, 'P', pearl, 'R', redstone_block); // Regular version
        }
        if (Configs.disableRecipeEnderPearlElite.getBoolean(false) == false && Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable, 1, 1), "RAR", "ACA", "RPR", 'R', rope, 'A', alloy0, 'C', core0, 'P', new ItemStack(enderPearlReusable, 1, 0));
        }
        if (Configs.disableRecipeEnderPorterBasic.getBoolean(false) == false && Configs.disableItemEnderPorter.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter), "EAE", "ACA", "EAE", 'E', eye, 'A', alloy1, 'C', active_core1);
        }
        if (Configs.disableRecipeEnderPorterAdvanced.getBoolean(false) == false && Configs.disableItemEnderPorter.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter, 1, 1), "PAP", "ACA", "ORO", 'P', pearl, 'A', alloy2, 'C', active_core2, 'O', obsidian, 'R', new ItemStack(enderPorter, 1, 0));
        }
        if (Configs.disableRecipeMobHarness.getBoolean(false) == false && Configs.disableItemMobHarness.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(mobHarness), "RLR", "ACA", "RLR", 'R', rope, 'L', leather, 'A', alloy0, 'C', core0);
        }

        // Tools and weapons
        if (Configs.disableRecipeEnderSword.getBoolean(false) == false && Configs.disableItemEnderSword.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderSword), " A ", " A ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderPickaxe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack pick = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(pick, ItemEnderTool.TOOL_TYPE_PICKAXE);
            GameRegistry.addRecipe(pick, "AAA", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderAxe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack axe = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(axe, ItemEnderTool.TOOL_TYPE_AXE);
            GameRegistry.addRecipe(axe, "AA ", "AS ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderShovel.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack shovel = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(shovel, ItemEnderTool.TOOL_TYPE_SHOVEL);
            GameRegistry.addRecipe(shovel, " A ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderHoe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack hoe = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(hoe, ItemEnderTool.TOOL_TYPE_HOE);
            GameRegistry.addRecipe(hoe, "AA ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }

        // Parts, modules etc.
        if (Configs.disableRecipePartEnderAlloy0.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 0), "III", "PPP", "III", 'I', iron, 'P', pearl);
        }
        if (Configs.disableRecipePartEnderAlloy1.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 1), "GGG", "PPP", "III", 'G', gold, 'P', pearl, 'I', iron);
        }
        if (Configs.disableRecipePartEnderAlloy2.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addShapelessRecipe(new ItemStack(enderPart, 1, 2), iron, diamond, eye, obsidian);
        }
        if (Configs.disableRecipePartEnderCore0.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 10), "PAP", "ARA", "OAO", 'P', pearl, 'A', alloy0, 'R', redstone_block, 'O', obsidian);
        }
        if (Configs.disableRecipePartEnderCore1.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 11), "EAE", "AMA", "OAO", 'E', eye, 'A', alloy1, 'M', emerald_block, 'O', obsidian);
        }
        if (Configs.disableRecipePartEnderCore2.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 12), "EAE", "ADA", "OAO", 'E', eye, 'A', alloy2, 'D', diamond_block, 'O', obsidian);
        }
        if (Configs.disableRecipePartEnderRope.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 21), "SSS", "LPL", "SSS", 'S', string, 'L', leather, 'P', pearl);
        }
        if (Configs.disableRecipePartEnderStick.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 20), "SSS", "PPP", "SSS", 'S', stick, 'P', pearl);
        }

        if (Configs.disableRecipeModuleEnderCapacitor0.getBoolean(false) == false && Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderCapacitor, 1, 0), " P ", "ARA", "OAO", 'P', pearl, 'A', alloy0, 'R', redstone, 'O', obsidian);
        }
        if (Configs.disableRecipeModuleEnderCapacitor1.getBoolean(false) == false && Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderCapacitor, 1, 1), " P ", "AEA", "OAO", 'P', pearl, 'A', alloy1, 'E', emerald, 'O', obsidian);
        }
        if (Configs.disableRecipeModuleEnderCapacitor2.getBoolean(false) == false && Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderCapacitor, 1, 2), " P ", "ADA", "OAO", 'P', pearl, 'A', alloy2, 'D', diamond, 'O', obsidian);
        }

        if (Configs.disableRecipeModuleLinkCrystalLocation.getBoolean(false) == false && Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(linkCrystal, 1, 0), "GRG", "RAR", "GRG", 'G', glass, 'R', redstone, 'A', alloy0);
        }
        if (Configs.disableRecipeModuleLinkCrystalBlock.getBoolean(false) == false && Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(linkCrystal, 1, 1), "GRG", "RAR", "GRG", 'G', glass, 'R', redstone, 'A', alloy1);
        }
        if (Configs.disableRecipeModuleLinkCrystalPortal.getBoolean(false) == false && Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            //GameRegistry.addShapelessRecipe(new ItemStack(linkCrystal, 1, 1), "LO", 'L', new ItemStack(linkCrystal, 1, 0), 'O', obsidian);
        }
    }
}
