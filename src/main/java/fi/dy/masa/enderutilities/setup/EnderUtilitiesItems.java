package fi.dy.masa.enderutilities.setup;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import fi.dy.masa.enderutilities.item.*;
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

    public static final ItemEnderUtilities buildersWand = new ItemBuildersWand();
    public static final ItemEnderUtilities enderArrow = new ItemEnderArrow();
    public static final ItemEnderUtilities enderBag = new ItemEnderBag();
    public static final ItemEnderUtilities enderBow = new ItemEnderBow();
    public static final ItemEnderUtilities enderBucket = new ItemEnderBucket();
    public static final ItemEnderUtilities enderLasso = new ItemEnderLasso();
    public static final ItemEnderUtilities enderPearlReusable = new ItemEnderPearlReusable();
    public static final ItemEnderUtilities enderPorter = new ItemEnderPorter();
    public static final ItemEnderUtilities enderSword = new ItemEnderSword();
    public static final ItemEnderUtilities enderTool = new ItemEnderTool();
    public static final ItemEnderUtilities handyBag = new ItemHandyBag();
    public static final ItemEnderUtilities inventorySwapper = new ItemInventorySwapper();
    public static final ItemEnderUtilities livingManipulator = new ItemLivingManipulator();
    public static final ItemEnderUtilities mobHarness = new ItemMobHarness();
    public static final ItemEnderUtilities pickupManager = new ItemPickupManager();
    public static final ItemEnderUtilities quickStacker = new ItemQuickStacker();
    public static final ItemEnderUtilities portalScaler = new ItemPortalScaler();
    public static final ItemEnderUtilities ruler = new ItemRuler();

    public static void init()
    {
        registerItem(enderPart,             ReferenceNames.NAME_ITEM_ENDERPART,                 Configs.disableItemCraftingPart);
        registerItem(enderCapacitor,        ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR,  Configs.disableItemEnderCapacitor);
        registerItem(linkCrystal,           ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL,     Configs.disableItemLinkCrystal);
        registerItem(buildersWand,          ReferenceNames.NAME_ITEM_BUILDERS_WAND,             Configs.disableItemBuildersWand);
        registerItem(enderArrow,            ReferenceNames.NAME_ITEM_ENDER_ARROW,               Configs.disableItemEnderArrow);
        registerItem(enderBag,              ReferenceNames.NAME_ITEM_ENDER_BAG,                 Configs.disableItemEnderBag);
        registerItem(enderBow,              ReferenceNames.NAME_ITEM_ENDER_BOW,                 Configs.disableItemEnderBow);
        registerItem(enderBucket,           ReferenceNames.NAME_ITEM_ENDER_BUCKET,              Configs.disableItemEnderBucket);
        registerItem(enderLasso,            ReferenceNames.NAME_ITEM_ENDER_LASSO,               Configs.disableItemEnderLasso);
        registerItem(enderPearlReusable,    ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE,      Configs.disableItemEnderPearl);
        registerItem(enderPorter,           ReferenceNames.NAME_ITEM_ENDER_PORTER,              Configs.disableItemEnderPorter);
        registerItem(enderSword,            ReferenceNames.NAME_ITEM_ENDER_SWORD,               Configs.disableItemEnderSword);
        registerItem(enderTool,             ReferenceNames.NAME_ITEM_ENDERTOOL,                 Configs.disableItemEnderTools);
        registerItem(handyBag,              ReferenceNames.NAME_ITEM_HANDY_BAG,                 Configs.disableItemHandyBag);
        registerItem(inventorySwapper,      ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER,         Configs.disableItemInventorySwapper);
        registerItem(livingManipulator,     ReferenceNames.NAME_ITEM_LIVING_MANIPULATOR,        Configs.disableItemLivingManipulator);
        registerItem(mobHarness,            ReferenceNames.NAME_ITEM_MOB_HARNESS,               Configs.disableItemMobHarness);
        registerItem(pickupManager,         ReferenceNames.NAME_ITEM_PICKUP_MANAGER,            Configs.disableItemPickupManager);
        registerItem(quickStacker,          ReferenceNames.NAME_ITEM_QUICK_STACKER,             Configs.disableItemQuickStacker);
        registerItem(portalScaler,          ReferenceNames.NAME_ITEM_PORTAL_SCALER,             Configs.disableItemPortalScaler);
        registerItem(ruler,                 ReferenceNames.NAME_ITEM_RULER,                     Configs.disableItemRuler);

        ItemStack bucket = new ItemStack(Items.BUCKET);
        ItemStack diamond = new ItemStack(Items.DIAMOND);
        ItemStack emerald = new ItemStack(Items.EMERALD);
        ItemStack enderChest = new ItemStack(Blocks.ENDER_CHEST);
        ItemStack egg = new ItemStack(Items.EGG);
        ItemStack eye = new ItemStack(Items.ENDER_EYE);
        ItemStack feather = new ItemStack(Items.FEATHER);
        ItemStack hopper = new ItemStack(Blocks.HOPPER);
        ItemStack leather = new ItemStack(Items.LEATHER);
        ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        ItemStack piston = new ItemStack(Blocks.PISTON);
        ItemStack redstone = new ItemStack(Items.REDSTONE);
        ItemStack repeater = new ItemStack(Items.REPEATER);
        ItemStack string = new ItemStack(Items.STRING);
        ItemStack wool = new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE);

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
        if (Configs.disableRecipeBuildersWand == false && Configs.disableItemBuildersWand == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(buildersWand), "C  ", " A ", "  A", 'C', active_core2, 'A', alloy2));
        }
        if (Configs.disableRecipeEnderArrow == false && Configs.disableItemEnderArrow == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderArrow), " NP", " AN", "A  ", 'N', "nuggetGold", 'P', pearl, 'A', new ItemStack(Items.ARROW)));
        }
        if (Configs.disableRecipeEnderBag == false && Configs.disableItemEnderBag == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBag), "RLR", "LCL", "RWR", 'R', rope, 'L', leather, 'C', core1, 'W', wool);
        }
        if (Configs.disableRecipeEnderBow == false && Configs.disableItemEnderBow == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBow), "RA ", "RCA", "RA ", 'R', rope, 'A', alloy1, 'C', core1);
        }
        if (Configs.disableRecipeEnderBucket == false && Configs.disableItemEnderBucket == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderBucket), " C ", "ABA", "OAO", 'C', core0, 'A', alloy0, 'B', bucket, 'O', obsidian);
        }
        if (Configs.disableRecipeEnderLasso == false && Configs.disableItemEnderLasso == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderLasso), "RR ", "RC ", "  A", 'R', rope, 'C', core1, 'A', alloy0);
        }
        if (Configs.disableRecipeEnderPearl == false && Configs.disableItemEnderPearl == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPearlReusable), "GPG", "PRP", "GPG", 'G', "nuggetGold", 'P', pearl, 'R', "blockRedstone"));
        }
        if (Configs.disableRecipeEnderPearlElite == false && Configs.disableItemEnderPearl == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPearlReusable, 1, 1), "FAF", "APA", "FCF", 'F', feather, 'A', alloy1, 'C', core0, 'P', new ItemStack(enderPearlReusable, 1, 0));
        }
        if (Configs.disableRecipeEnderPorterBasic == false && Configs.disableItemEnderPorter == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter), "EAE", "ACA", "EAE", 'E', eye, 'A', alloy2, 'C', active_core1);
        }
        if (Configs.disableRecipeEnderPorterAdvanced == false && Configs.disableItemEnderPorter == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPorter, 1, 1), "EAE", "ACA", "EAE", 'E', eye, 'A', alloy2, 'C', active_core2);
        }
        if (Configs.disableRecipeHandyBag == false && Configs.disableItemHandyBag == false)
        {
            GameRegistry.addRecipe(new ItemStack(handyBag, 1, 0), "RAR", "ACA", "LAL", 'R', rope, 'A', alloy1, 'C', enderChest, 'L', leather);
            GameRegistry.addRecipe(new ItemStack(handyBag, 1, 1), "RAR", "ABA", "LAL", 'R', rope, 'A', alloy2, 'B', new ItemStack(handyBag, 1, 0), 'L', leather);
        }
        if (Configs.disableRecipeInventorySwapper == false && Configs.disableItemInventorySwapper == false)
        {
            GameRegistry.addRecipe(new ItemStack(inventorySwapper), "RAR", "ACA", "PAP", 'R', rope, 'A', alloy1, 'C', enderChest, 'P', piston);
        }
        if (Configs.disableRecipeLivingManipulator == false && Configs.disableItemLivingManipulator == false)
        {
            GameRegistry.addRecipe(new ItemStack(livingManipulator), "AAA", "BCA", "  A", 'A', alloy1, 'B', alloy2, 'C', core1);
        }
        if (Configs.disableRecipeMobHarness == false && Configs.disableItemMobHarness == false)
        {
            GameRegistry.addRecipe(new ItemStack(mobHarness), "LRL", "LCL", "LRL", 'R', rope, 'L', leather, 'C', core0);
        }
        if (Configs.disableRecipePickupManager == false && Configs.disableItemPickupManager == false)
        {
            GameRegistry.addRecipe(new ItemStack(pickupManager), "AHA", "ACA", "ARA", 'A', alloy1, 'H', hopper, 'C', enderChest, 'R', repeater);
        }
        if (Configs.disableRecipePortalScaler == false && Configs.disableItemPortalScaler == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(portalScaler), "OGO", "OCO", "OAO", 'O', obsidian, 'G', "blockGlassPurple", 'C', active_core2, 'A', alloy2));
        }
        if (Configs.disableRecipeQuickStacker == false && Configs.disableItemQuickStacker == false)
        {
            GameRegistry.addRecipe(new ItemStack(quickStacker), "APA", "PRP", "APA", 'A', alloy1, 'P', piston, 'R', redstone);
        }
        if (Configs.disableRecipeRuler == false && Configs.disableItemRuler == false)
        {
            GameRegistry.addRecipe(new ItemStack(ruler), "A  ", "AS ", "AAA", 'A', alloy0, 'S', ender_stick);
        }

        // Tools and weapons
        if (Configs.disableRecipeEnderPickaxe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderTool, 1, 0), "AAA", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderAxe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderTool, 1, 1), "AA ", "AS ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderShovel == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderTool, 1, 2), " A ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderHoe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderTool, 1, 3), "AA ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderSword == false && Configs.disableItemEnderSword == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderSword, 1, 0), " A ", " A ", " S ", 'A', alloy2, 'S', ender_stick);
        }

        // Parts, modules etc.
        if (Configs.disableRecipePartEnderAlloy0 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(enderPart, 2, 0), "ingotIron", "ingotIron", pearl));
        }
        if (Configs.disableRecipePartEnderAlloy1 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(enderPart, 1, 1), "ingotGold", alloy0));
        }
        if (Configs.disableRecipePartEnderAlloy2 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 2), "IEI", "GDG", "OEO", 'I', "ingotIron", 'E', eye, 'G', "ingotGold", 'D', diamond, 'O', obsidian));
        }
        if (Configs.disableRecipePartEnderCore0 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 10), "OAO", "ARA", "OAO", 'O', obsidian, 'A', alloy0, 'R', "blockRedstone"));
        }
        if (Configs.disableRecipePartEnderCore1 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 11), "OAO", "AEA", "OAO", 'O', obsidian, 'A', alloy1, 'E', emerald);
        }
        if (Configs.disableRecipePartEnderCore2 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 12), "OAO", "ADA", "OAO", 'O', obsidian, 'A', alloy2, 'D', diamond);
        }
        if (Configs.disableRecipePartMemoryCardMisc == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 4, 50), "AGA", "ARA", "AEA", 'A', alloy0, 'G', "ingotGold", 'R', repeater, 'E', "dustRedstone"));
        }
        if (Configs.disableRecipePartMemoryCardItems6b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 2, 51), "AGA", "ACA", "ARA", 'A', alloy1, 'G', "ingotGold", 'C', new ItemStack(Blocks.CHEST), 'R', repeater));
        }
        if (Configs.disableRecipePartMemoryCardItems8b == false
            && Configs.disableRecipePartMemoryCardItems6b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 52), "MM", "MM", "SS", 'M', new ItemStack(enderPart, 1, 51), 'S', "slimeball"));
            GameRegistry.addShapelessRecipe(new ItemStack(enderPart, 4, 51), new ItemStack(enderPart, 1, 52));
        }
        if (Configs.disableRecipePartMemoryCardItems10b == false
            && Configs.disableRecipePartMemoryCardItems8b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 53), "MM", "MM", "SS", 'M', new ItemStack(enderPart, 1, 52), 'S', "slimeball"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 2, 53), "AGA", "ACA", "ARA", 'A', alloy2, 'G', "ingotGold", 'C', enderChest, 'R', repeater));
            //GameRegistry.addShapelessRecipe(new ItemStack(enderPart, 4, 52), new ItemStack(enderPart, 1, 53));
        }
        if (Configs.disableRecipePartMemoryCardItems12b == false
            && Configs.disableRecipePartMemoryCardItems10b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 54), "MM", "MM", "SS", 'M', new ItemStack(enderPart, 1, 53), 'S', "slimeball"));
            //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderPart, 1, 54), "AGA", "ACA", "ARA", 'A', alloy2, 'G', "ingotGold", 'C', enderChest, 'R', repeater));
            //GameRegistry.addShapelessRecipe(new ItemStack(enderPart, 4, 53), new ItemStack(enderPart, 1, 54));
        }
        if (Configs.disableRecipePartMobPersistence == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 4, 45), "ABA", "B B", "ABA", 'A', alloy0, 'B', new ItemStack(Blocks.IRON_BARS));
        }
        if (Configs.disableRecipePartEnderRelic == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 1, 40), "GEG", "ECE", "GEG", 'G', egg, 'E', emerald, 'C', core2);
        }
        if (Configs.disableRecipePartEnderRope == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderPart, 3, 21), "SSS", "LPL", "SSS", 'S', string, 'L', leather, 'P', pearl);
        }
        if (Configs.disableRecipePartEnderStick == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(enderPart, 2, 20), "stickWood", "stickWood", pearl));
        }

        if (Configs.disableRecipeModuleEnderCapacitor0 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(enderCapacitor, 1, 0), " P ", "ARA", "OAO", 'P', pearl, 'A', alloy0, 'R', "dustRedstone", 'O', obsidian));
        }
        if (Configs.disableRecipeModuleEnderCapacitor1 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderCapacitor, 1, 1), " P ", "AEA", "OAO", 'P', pearl, 'A', alloy1, 'E', emerald, 'O', obsidian);
        }
        if (Configs.disableRecipeModuleEnderCapacitor2 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ItemStack(enderCapacitor, 1, 2), " P ", "ADA", "OAO", 'P', pearl, 'A', alloy2, 'D', diamond, 'O', obsidian);
        }

        if (Configs.disableRecipeModuleLinkCrystalLocation == false && Configs.disableItemLinkCrystal == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 0), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassGreen", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalBlock == false && Configs.disableItemLinkCrystal == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 1), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassBlue", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalPortal == false && Configs.disableItemLinkCrystal == false)
        {
            //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 2), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassPurple", 'A', alloy0));
        }
    }

    private static void registerItem(Item item, String registryName, boolean isDisabled)
    {
        if (isDisabled == false)
        {
            item.setRegistryName(registryName);
            GameRegistry.register(item);
        }
    }
}
