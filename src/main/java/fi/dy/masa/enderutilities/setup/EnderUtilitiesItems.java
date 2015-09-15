package fi.dy.masa.enderutilities.setup;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.item.ItemEnderArrow;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemEnderBow;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemEnderPearlReusable;
import fi.dy.masa.enderutilities.item.ItemEnderPorter;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.ItemPortalScaler;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

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
    public static final ItemEnderUtilities handyBag = new ItemHandyBag();
    public static final ItemEnderUtilities mobHarness = new ItemMobHarness();
    public static final ItemEnderUtilities portalScaler = new ItemPortalScaler();

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
        if (Configs.disableItemHandyBag.getBoolean(false) == false) { GameRegistry.registerItem(handyBag, ReferenceNames.NAME_ITEM_HANDY_BAG); }
        if (Configs.disableItemMobHarness.getBoolean(false) == false) { GameRegistry.registerItem(mobHarness, ReferenceNames.NAME_ITEM_MOB_HARNESS); }
        if (Configs.disableItemPortalScaler.getBoolean(false) == false) { GameRegistry.registerItem(portalScaler, ReferenceNames.NAME_ITEM_PORTAL_SCALER); }

        ItemStack bucket = new ItemStack(Items.bucket);
        ItemStack diamond = new ItemStack(Items.diamond);
        ItemStack emerald = new ItemStack(Items.emerald);
        ItemStack egg = new ItemStack(Items.egg);
        ItemStack eye = new ItemStack(Items.ender_eye);
        ItemStack feather = new ItemStack(Items.feather);
        ItemStack leather = new ItemStack(Items.leather);
        ItemStack obsidian = new ItemStack(Blocks.obsidian);
        ItemStack pearl = new ItemStack(Items.ender_pearl);
        ItemStack string = new ItemStack(Items.string);
        ItemStack wool = new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE);

        ItemStack alloy0 = new ItemStack(enderPart, 1, 0);
        ItemStack alloy1 = new ItemStack(enderPart, 1, 1);
        ItemStack alloy2 = new ItemStack(enderPart, 1, 2);
        ItemStack core0 = new ItemStack(enderPart, 1, 10);
        ItemStack core1 = new ItemStack(enderPart, 1, 11);
        ItemStack core2 = new ItemStack(enderPart, 1, 12);
        //ItemStack active_core0 = new ItemStack(enderPart, 1, 15);
        ItemStack active_core1 = new ItemStack(enderPart, 1, 16);
        ItemStack active_core2 = new ItemStack(enderPart, 1, 17);
        ItemStack ender_stick = new ItemStack(enderPart, 1, 20);
        ItemStack rope = new ItemStack(enderPart, 1, 21);

        // "Usable" items
        if (Configs.disableRecipeEnderArrow.getBoolean(false) == false && Configs.disableItemEnderArrow.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderArrow), " NP", " AN", "A  ", 'N', "nuggetGold", 'P', pearl, 'A', new ItemStack(Items.arrow)));
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
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPearlReusable), "GPG", "PRP", "GPG", 'G', "nuggetGold", 'P', pearl, 'R', "blockRedstone"));
        }
        if (Configs.disableRecipeEnderPearlElite.getBoolean(false) == false && Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable, 1, 1), "FAF", "APA", "FCF", 'F', feather, 'A', alloy1, 'C', core0, 'P', new ItemStack(enderPearlReusable, 1, 0));
        }
        if (Configs.disableRecipeEnderPorterBasic.getBoolean(false) == false && Configs.disableItemEnderPorter.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter), "EAE", "ACA", "EAE", 'E', eye, 'A', alloy2, 'C', active_core1);
        }
        if (Configs.disableRecipeEnderPorterAdvanced.getBoolean(false) == false && Configs.disableItemEnderPorter.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter, 1, 1), "EAE", "ACA", "EAE", 'E', eye, 'A', alloy2, 'C', active_core2);
        }
        if (Configs.disableRecipeMobHarness.getBoolean(false) == false && Configs.disableItemMobHarness.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(mobHarness), "RLR", "ACA", "RLR", 'R', rope, 'L', leather, 'A', alloy0, 'C', core0);
        }
        if (Configs.disableRecipePortalScaler.getBoolean(false) == false && Configs.disableItemPortalScaler.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(portalScaler), "OGO", "OCO", "OAO", 'O', obsidian, 'G', "blockGlassPurple", 'C', active_core1, 'A', alloy2));
        }

        // Tools and weapons
        if (Configs.disableRecipeEnderPickaxe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack pick = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(pick, ItemEnderTool.ToolType.PICKAXE);
            GameRegistry.addRecipe(pick, "AAA", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderAxe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack axe = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(axe, ItemEnderTool.ToolType.AXE);
            GameRegistry.addRecipe(axe, "AA ", "AS ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderShovel.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack shovel = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(shovel, ItemEnderTool.ToolType.SHOVEL);
            GameRegistry.addRecipe(shovel, " A ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderHoe.getBoolean(false) == false && Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ItemStack hoe = new ItemStack(enderTool, 1, 0);
            ((ItemEnderTool)enderTool).setToolType(hoe, ItemEnderTool.ToolType.HOE);
            GameRegistry.addRecipe(hoe, "AA ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderSword.getBoolean(false) == false && Configs.disableItemEnderSword.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderSword, 1, 0), " A ", " A ", " S ", 'A', alloy2, 'S', ender_stick);
        }

        // Parts, modules etc.
        if (Configs.disableRecipePartEnderAlloy0.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 3, 0), "III", "PPP", "III", 'I', "ingotIron", 'P', pearl));
        }
        if (Configs.disableRecipePartEnderAlloy1.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 3, 1), "GGG", "PPP", "III", 'G', "ingotGold", 'P', pearl, 'I', "ingotIron"));
        }
        if (Configs.disableRecipePartEnderAlloy2.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 2), "IEI", "GDG", "OEO", 'I', "ingotIron", 'E', eye, 'G', "ingotGold", 'D', diamond, 'O', obsidian));
        }
        if (Configs.disableRecipePartEnderCore0.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 10), "OAO", "ARA", "OAO", 'O', obsidian, 'A', alloy0, 'R', "blockRedstone"));
        }
        if (Configs.disableRecipePartEnderCore1.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 11), "OAO", "AEA", "OAO", 'O', obsidian, 'A', alloy1, 'E', emerald);
        }
        if (Configs.disableRecipePartEnderCore2.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 12), "OAO", "ADA", "OAO", 'O', obsidian, 'A', alloy2, 'D', diamond);
        }
        if (Configs.disableRecipePartMemoryCardMisc.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 4, 50), "AGA", "ARA", "AEA", 'A', alloy0, 'G', "ingotGold", 'R', new ItemStack(Items.repeater), 'E', "dustRedstone"));
        }
        if (Configs.disableRecipePartMobPersistence.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 4, 45), "ABA", "B B", "ABA", 'A', alloy0, 'B', new ItemStack(Blocks.iron_bars));
        }
        if (Configs.disableRecipePartEnderRelic.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 40), "GEG", "ECE", "GEG", 'G', egg, 'E', emerald, 'C', core2);
        }
        if (Configs.disableRecipePartEnderRope.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 21), "SSS", "LPL", "SSS", 'S', string, 'L', leather, 'P', pearl);
        }
        if (Configs.disableRecipePartEnderStick.getBoolean(false) == false && Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 3, 20), "SSS", "PPP", "SSS", 'S', "stickWood", 'P', pearl));
        }

        if (Configs.disableRecipeModuleEnderCapacitor0.getBoolean(false) == false && Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderCapacitor, 1, 0), " P ", "ARA", "OAO", 'P', pearl, 'A', alloy0, 'R', "dustRedstone", 'O', obsidian));
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
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 0), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassGreen", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalBlock.getBoolean(false) == false && Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 1), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassBlue", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalPortal.getBoolean(false) == false && Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 2), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassPurple", 'A', alloy0));
        }
    }
}
