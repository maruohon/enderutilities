package fi.dy.masa.enderutilities.registry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.*;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.item.tool.ItemVoidPickaxe;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.recipes.ShapedUpgradeOreRecipe;

public class EnderUtilitiesItems
{
    public static final ItemEnderUtilities ENDER_CAPACITOR = new ItemEnderCapacitor();
    public static final ItemEnderUtilities ENDER_PART = new ItemEnderPart();
    public static final ItemEnderUtilities LINK_CRYSTAL = new ItemLinkCrystal();

    public static final ItemEnderUtilities BUILDERS_WAND = new ItemBuildersWand();
    public static final ItemEnderUtilities CHAIR_WAND = new ItemChairWand();
    public static final ItemEnderUtilities DOLLY = new ItemDolly();
    public static final ItemEnderUtilities ENDER_ARROW = new ItemEnderArrow();
    public static final ItemEnderUtilities ENDER_BAG = new ItemEnderBag();
    public static final ItemEnderUtilities ENDER_BOW = new ItemEnderBow();
    public static final ItemEnderUtilities ENDER_BUCKET = new ItemEnderBucket();
    public static final ItemEnderUtilities ENDER_LASSO = new ItemEnderLasso();
    public static final ItemEnderUtilities ENDER_PEARL_REUSABLE = new ItemEnderPearlReusable();
    public static final ItemEnderUtilities ENDER_PORTER = new ItemEnderPorter();
    public static final ItemEnderUtilities ENDER_SWORD = new ItemEnderSword();
    public static final ItemEnderUtilities ENDER_TOOL = new ItemEnderTool();
    public static final ItemEnderUtilities HANDY_BAG = new ItemHandyBag();
    public static final ItemEnderUtilities ICE_MELTER = new ItemIceMelter();
    public static final ItemEnderUtilities INVENTORY_SWAPPER = new ItemInventorySwapper();
    public static final ItemEnderUtilities LIVING_MANIPULATOR = new ItemLivingManipulator();
    public static final ItemEnderUtilities MOB_HARNESS = new ItemMobHarness();
    public static final ItemEnderUtilities NULLIFIER = new ItemNullifier();
    public static final ItemEnderUtilities PICKUP_MANAGER = new ItemPickupManager();
    public static final ItemEnderUtilities QUICK_STACKER = new ItemQuickStacker();
    public static final ItemEnderUtilities PORTAL_SCALER = new ItemPortalScaler();
    public static final ItemEnderUtilities RULER = new ItemRuler();
    public static final ItemEnderUtilities SYRINGE = new ItemSyringe();
    public static final ItemEnderUtilities VOID_PICKAXE = new ItemVoidPickaxe();

    public static void registerItems()
    {
        registerItem(ENDER_PART,            ReferenceNames.NAME_ITEM_ENDERPART,                 Configs.disableItemCraftingPart);
        registerItem(ENDER_CAPACITOR,       ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR,  Configs.disableItemEnderCapacitor);
        registerItem(LINK_CRYSTAL,          ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL,     Configs.disableItemLinkCrystal);
        registerItem(BUILDERS_WAND,         ReferenceNames.NAME_ITEM_BUILDERS_WAND,             Configs.disableItemBuildersWand);
        registerItem(CHAIR_WAND,            ReferenceNames.NAME_ITEM_CHAIR_WAND,                Configs.disableItemChairWand);
        registerItem(DOLLY,                 ReferenceNames.NAME_ITEM_DOLLY,                     Configs.disableItemDolly);
        registerItem(ENDER_ARROW,           ReferenceNames.NAME_ITEM_ENDER_ARROW,               Configs.disableItemEnderArrow);
        registerItem(ENDER_BAG,             ReferenceNames.NAME_ITEM_ENDER_BAG,                 Configs.disableItemEnderBag);
        registerItem(ENDER_BOW,             ReferenceNames.NAME_ITEM_ENDER_BOW,                 Configs.disableItemEnderBow);
        registerItem(ENDER_BUCKET,          ReferenceNames.NAME_ITEM_ENDER_BUCKET,              Configs.disableItemEnderBucket);
        registerItem(ENDER_LASSO,           ReferenceNames.NAME_ITEM_ENDER_LASSO,               Configs.disableItemEnderLasso);
        registerItem(ENDER_PEARL_REUSABLE,  ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE,      Configs.disableItemEnderPearl);
        registerItem(ENDER_PORTER,          ReferenceNames.NAME_ITEM_ENDER_PORTER,              Configs.disableItemEnderPorter);
        registerItem(ENDER_SWORD,           ReferenceNames.NAME_ITEM_ENDER_SWORD,               Configs.disableItemEnderSword);
        registerItem(ENDER_TOOL,            ReferenceNames.NAME_ITEM_ENDERTOOL,                 Configs.disableItemEnderTools);
        registerItem(HANDY_BAG,             ReferenceNames.NAME_ITEM_HANDY_BAG,                 Configs.disableItemHandyBag);
        registerItem(ICE_MELTER,            ReferenceNames.NAME_ITEM_ICE_MELTER,                Configs.disableItemIceMelter);
        registerItem(INVENTORY_SWAPPER,     ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER,         Configs.disableItemInventorySwapper);
        registerItem(LIVING_MANIPULATOR,    ReferenceNames.NAME_ITEM_LIVING_MANIPULATOR,        Configs.disableItemLivingManipulator);
        registerItem(MOB_HARNESS,           ReferenceNames.NAME_ITEM_MOB_HARNESS,               Configs.disableItemMobHarness);
        registerItem(NULLIFIER,             ReferenceNames.NAME_ITEM_NULLIFIER,                 Configs.disableItemNullifier);
        registerItem(PICKUP_MANAGER,        ReferenceNames.NAME_ITEM_PICKUP_MANAGER,            Configs.disableItemPickupManager);
        registerItem(QUICK_STACKER,         ReferenceNames.NAME_ITEM_QUICK_STACKER,             Configs.disableItemQuickStacker);
        registerItem(PORTAL_SCALER,         ReferenceNames.NAME_ITEM_PORTAL_SCALER,             Configs.disableItemPortalScaler);
        registerItem(RULER,                 ReferenceNames.NAME_ITEM_RULER,                     Configs.disableItemRuler);
        registerItem(SYRINGE,               ReferenceNames.NAME_ITEM_SYRINGE,                   Configs.disableItemSyringe);
        registerItem(VOID_PICKAXE,          ReferenceNames.NAME_ITEM_VOID_PICKAXE,              Configs.disableItemVoidPickaxe);
    }

    public static void registerRecipes()
    {
        if (Configs.registerWoodFencesToOreDict)
        {
            OreDictionary.registerOre("fenceWood", Blocks.ACACIA_FENCE);
            OreDictionary.registerOre("fenceWood", Blocks.BIRCH_FENCE);
            OreDictionary.registerOre("fenceWood", Blocks.DARK_OAK_FENCE);
            OreDictionary.registerOre("fenceWood", Blocks.JUNGLE_FENCE);
            OreDictionary.registerOre("fenceWood", Blocks.OAK_FENCE);
            OreDictionary.registerOre("fenceWood", Blocks.SPRUCE_FENCE);
        }

        ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
        ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
        ItemStack wool = new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE);

        ItemStack alloy0 = new ItemStack(ENDER_PART, 1, 0);
        ItemStack alloy1 = new ItemStack(ENDER_PART, 1, 1);
        ItemStack alloy2 = new ItemStack(ENDER_PART, 1, 2);
        ItemStack core0 = new ItemStack(ENDER_PART, 1, 10);
        ItemStack core1 = new ItemStack(ENDER_PART, 1, 11);
        ItemStack active_core1 = new ItemStack(ENDER_PART, 1, 16);
        ItemStack active_core2 = new ItemStack(ENDER_PART, 1, 17);
        ItemStack ender_stick = new ItemStack(ENDER_PART, 1, 20);
        ItemStack rope = new ItemStack(ENDER_PART, 1, 21);
        //Object fenceWood = OreDictionary.doesOreNameExist("fenceWood") ? "fenceWood" : Blocks.OAK_FENCE;

        // "Usable" items
        if (Configs.disableRecipeBuildersWand == false && Configs.disableItemBuildersWand == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BUILDERS_WAND), "C  ", " A ", "  A", 'C', active_core2, 'A', alloy2));
        }
        if (Configs.disableRecipeChairWand == false && Configs.disableItemChairWand == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(CHAIR_WAND), "S  ", " I ", "  I", 'S', "stairWood", 'I', alloy0));
        }
        if (Configs.disableRecipeDolly == false && Configs.disableItemDolly == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(DOLLY), "  A", "  A", "AAS", 'A', alloy1, 'S', Blocks.SLIME_BLOCK));
        }
        if (Configs.disableRecipeEnderArrow == false && Configs.disableItemEnderArrow == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_ARROW), " NP", " AN", "A  ", 'N', "nuggetGold", 'P', pearl, 'A', new ItemStack(Items.ARROW)));
        }
        if (Configs.disableRecipeEnderBag == false && Configs.disableItemEnderBag == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_BAG), "RLR", "LCL", "RWR", 'R', rope, 'L', Items.LEATHER, 'C', core1, 'W', wool);
        }
        if (Configs.disableRecipeEnderBow == false && Configs.disableItemEnderBow == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_BOW), "RA ", "RCA", "RA ", 'R', rope, 'A', alloy1, 'C', core1);
        }
        if (Configs.disableRecipeEnderBucket == false && Configs.disableItemEnderBucket == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_BUCKET), " C ", "ABA", " A ", 'C', core0, 'A', alloy0, 'B', Items.BUCKET);
        }
        if (Configs.disableRecipeEnderLasso == false && Configs.disableItemEnderLasso == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_LASSO), "RR ", "RC ", "  A", 'R', rope, 'C', core1, 'A', alloy0);
        }
        if (Configs.disableRecipeEnderPearl == false && Configs.disableItemEnderPearl == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PEARL_REUSABLE), "GPG", "PRP", "GPG", 'G', "nuggetGold", 'P', pearl, 'R', Items.REDSTONE));
        }
        if (Configs.disableRecipeEnderPearlElite == false && Configs.disableItemEnderPearl == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PEARL_REUSABLE, 1, 1), "FAF", "APA", "FCF", 'F', Items.FEATHER, 'A', alloy1, 'C', core0, 'P', new ItemStack(ENDER_PEARL_REUSABLE, 1, 0));
        }
        if (Configs.disableRecipeEnderPorterBasic == false && Configs.disableItemEnderPorter == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PORTER), "EAE", "ACA", "EAE", 'E', Items.ENDER_EYE, 'A', alloy2, 'C', active_core1);
        }
        if (Configs.disableRecipeEnderPorterAdvanced == false && Configs.disableItemEnderPorter == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PORTER, 1, 1), "EAE", "ACA", "EAE", 'E', Items.ENDER_EYE, 'A', alloy2, 'C', active_core2);
        }
        if (Configs.disableRecipeHandyBag == false && Configs.disableItemHandyBag == false)
        {
            GameRegistry.addRecipe(new ItemStack(HANDY_BAG, 1, 0), "RAR", "ACA", "LAL", 'R', rope, 'A', alloy1, 'C', Blocks.ENDER_CHEST, 'L', Items.LEATHER);

            RecipeSorter.register(Reference.MOD_ID + ":shapedupgradeore", ShapedUpgradeOreRecipe.class, RecipeSorter.Category.SHAPED, "");
            GameRegistry.addRecipe(new ShapedUpgradeOreRecipe(new ItemStack(HANDY_BAG, 1, 1), HANDY_BAG, 0, "AAA", "CBC", "AAA", 'A', alloy2, 'B', new ItemStack(HANDY_BAG, 1, 0), 'C', Blocks.CHEST));
        }
        if (Configs.disableRecipeInventorySwapper == false && Configs.disableItemInventorySwapper == false)
        {
            GameRegistry.addRecipe(new ItemStack(INVENTORY_SWAPPER), "RAR", "ACA", "SAS", 'R', rope, 'A', alloy0, 'C', Blocks.CHEST, 'S', Blocks.STICKY_PISTON);
        }
        if (Configs.disableRecipeIceMelter == false && Configs.disableItemIceMelter == false)
        {
            GameRegistry.addRecipe(new ItemStack(ICE_MELTER), "   ", " C ", "R  ", 'R', Items.BLAZE_ROD, 'C', active_core2);
        }
        if (Configs.disableRecipeIceMelterSuper == false && Configs.disableItemIceMelter == false)
        {
            GameRegistry.addRecipe(new ItemStack(ICE_MELTER, 1, 1), "  N", " C ", "R  ", 'R', Items.BLAZE_ROD, 'C', active_core2, 'N', Items.NETHER_STAR);
        }
        if (Configs.disableRecipeLivingManipulator == false && Configs.disableItemLivingManipulator == false)
        {
            GameRegistry.addRecipe(new ItemStack(LIVING_MANIPULATOR), "AAA", "BCA", "  A", 'A', alloy1, 'B', alloy2, 'C', core1);
        }
        if (Configs.disableRecipeMobHarness == false && Configs.disableItemMobHarness == false)
        {
            GameRegistry.addRecipe(new ItemStack(MOB_HARNESS), "LRL", "LCL", "LRL", 'R', rope, 'L', Items.LEATHER, 'C', core0);
        }
        if (Configs.disableRecipeNullifier == false && Configs.disableItemNullifier == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(NULLIFIER), "CLC", "A A", "CLC", 'C', "chestWood", 'A', alloy0, 'L', Items.LAVA_BUCKET));
        }
        if (Configs.disableRecipePickupManager == false && Configs.disableItemPickupManager == false)
        {
            GameRegistry.addRecipe(new ItemStack(PICKUP_MANAGER), "AHA", "ACA", "ARA", 'A', alloy1, 'H', Blocks.HOPPER, 'C', Blocks.ENDER_CHEST, 'R', Items.REPEATER);
        }
        if (Configs.disableRecipePortalScaler == false && Configs.disableItemPortalScaler == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(PORTAL_SCALER), "OGO", "OCO", "OAO", 'O', obsidian, 'G', "blockGlassPurple", 'C', active_core2, 'A', alloy2));
        }
        if (Configs.disableRecipeQuickStacker == false && Configs.disableItemQuickStacker == false)
        {
            GameRegistry.addRecipe(new ItemStack(QUICK_STACKER), "RPR", "ACA", "RPR", 'A', alloy0, 'P', Blocks.PISTON, 'C', Items.COMPARATOR, 'R', Items.REDSTONE);
        }
        if (Configs.disableRecipeRuler == false && Configs.disableItemRuler == false)
        {
            GameRegistry.addRecipe(new ItemStack(RULER), "A  ", "AS ", "AAA", 'A', alloy0, 'S', ender_stick);
        }
        if (Configs.disableRecipeSyringeEmpty == false && Configs.disableItemSyringe == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SYRINGE, 1, 0), "A  ", " G ", "  S", 'A', alloy1, 'G', "blockGlass", 'S', ender_stick));
        }
        if (Configs.disableRecipeSyringeParalyzer == false && Configs.disableItemSyringe == false)
        {
            GameRegistry.addShapelessRecipe(new ItemStack(SYRINGE, 1, 1), SYRINGE, Items.FERMENTED_SPIDER_EYE, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM);
        }
        if (Configs.disableRecipeSyringeStimulant == false && Configs.disableItemSyringe == false)
        {
            GameRegistry.addShapelessRecipe(new ItemStack(SYRINGE, 1, 2), SYRINGE, Items.SUGAR, Items.SPECKLED_MELON, Items.CARROT);
        }
        if (Configs.disableRecipeSyringePassifier == false && Configs.disableItemSyringe == false)
        {
            GameRegistry.addShapelessRecipe(new ItemStack(SYRINGE, 1, 3), SYRINGE, Items.SUGAR, Items.CARROT, Items.CAKE, Items.COOKIE, Items.COOKIE);
        }

        // Tools and weapons
        if (Configs.disableRecipeEnderPickaxe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_TOOL, 1, 0), "AAA", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderAxe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_TOOL, 1, 1), "AA ", "AS ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderShovel == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_TOOL, 1, 2), " A ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderHoe == false && Configs.disableItemEnderTools == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_TOOL, 1, 3), "AA ", " S ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeEnderSword == false && Configs.disableItemEnderSword == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_SWORD, 1, 0), " A ", " A ", " S ", 'A', alloy2, 'S', ender_stick);
        }
        if (Configs.disableRecipeVoidPickaxe == false && Configs.disableItemVoidPickaxe == false)
        {
            GameRegistry.addRecipe(new ItemStack(VOID_PICKAXE, 1, 0), "AAA", "LSL", "FSF", 'A', alloy2, 'S', ender_stick, 'L', Items.LAVA_BUCKET, 'F', Items.FIRE_CHARGE);
        }

        // Parts, modules etc.
        if (Configs.disableRecipePartEnderAlloy0 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ENDER_PART, 4, 0), "ingotIron", "ingotIron", "ingotIron", "ingotIron", pearl));
        }
        if (Configs.disableRecipePartEnderAlloy1 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ENDER_PART, 4, 1), "ingotGold", "ingotGold", "ingotGold", "ingotGold", pearl));
        }
        if (Configs.disableRecipePartEnderAlloy2 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 2, 2), "IEI", "GDG", "OEO", 'I', "ingotIron", 'E', Items.ENDER_EYE, 'G', "ingotGold", 'D', Items.DIAMOND, 'O', obsidian));
        }
        if (Configs.disableRecipePartEnderCore0 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 10), "OAO", "ARA", "OAO", 'O', obsidian, 'A', alloy0, 'R', "blockRedstone"));
        }
        if (Configs.disableRecipePartEnderCore1 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 1, 11), "OAO", "AEA", "OAO", 'O', obsidian, 'A', alloy1, 'E', Items.EMERALD);
        }
        if (Configs.disableRecipePartEnderCore2 == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 1, 12), "OAO", "ADA", "OAO", 'O', obsidian, 'A', alloy2, 'D', Items.DIAMOND);
        }
        if (Configs.disableRecipePartCreativeBreaking == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 1, 30), "ANA", " S ", " S ", 'A', alloy2, 'N', Items.NETHER_STAR, 'S', ender_stick);
        }
        if (Configs.disableRecipePartMemoryCardMisc == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 4, 50), " G ", "ARA", "AEA", 'A', alloy0, 'G', "ingotGold", 'R', Items.REPEATER, 'E', "dustRedstone"));
        }
        if (Configs.disableRecipePartMemoryCardItems6b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 2, 51), " G ", "ACA", "ACA", 'A', alloy0, 'G', "ingotGold", 'C', Blocks.CHEST));
        }
        if (Configs.disableRecipePartMemoryCardItems8b == false
            && Configs.disableRecipePartMemoryCardItems6b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 52), "MM", "MM", "SS", 'M', new ItemStack(ENDER_PART, 1, 51), 'S', "slimeball"));
            GameRegistry.addShapelessRecipe(new ItemStack(ENDER_PART, 4, 51), new ItemStack(ENDER_PART, 1, 52));
        }
        if (Configs.disableRecipePartMemoryCardItems10b == false
            && Configs.disableRecipePartMemoryCardItems8b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 53), "MM", "MM", "SS", 'M', new ItemStack(ENDER_PART, 1, 52), 'S', "slimeball"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 2, 53), " G ", "ACA", "ACA", 'A', alloy2, 'G', "ingotGold", 'C', Blocks.CHEST));
            GameRegistry.addShapelessRecipe(new ItemStack(ENDER_PART, 4, 52), new ItemStack(ENDER_PART, 1, 53));
        }
        if (Configs.disableRecipePartMemoryCardItems12b == false
            && Configs.disableRecipePartMemoryCardItems10b == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 54), "MM", "MM", "SS", 'M', new ItemStack(ENDER_PART, 1, 53), 'S', "slimeball"));
            GameRegistry.addShapelessRecipe(new ItemStack(ENDER_PART, 4, 53), new ItemStack(ENDER_PART, 1, 54));
        }
        if (Configs.disableRecipePartMobPersistence == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 4, 45), "ABA", "B B", "ABA", 'A', alloy0, 'B', new ItemStack(Blocks.IRON_BARS));
        }
        if (Configs.disableRecipePartEnderRelic == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 40), "GEG", "EDE", "GEG", 'G', "blockGlass", 'E', Items.EGG, 'D', Items.DRAGON_BREATH));
        }
        if (Configs.disableRecipePartEnderRope == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 4, 21), "SSS", "LPL", "SSS", 'S', Items.STRING, 'L', Items.LEATHER, 'P', pearl);
        }
        if (Configs.disableRecipePartEnderStick == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ENDER_PART, 2, 20), "stickWood", "stickWood", "stickWood", "stickWood", pearl));
        }
        if (Configs.disableRecipePartStorageKey == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 80), "AS ", "AS ", " S ", 'A', alloy0, 'S', "stickWood"));
        }
        if (Configs.disableRecipePartBarrelLabel == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 1, 70), "SSS", "SIS", "SSS", 'S', Items.STICK, 'I', alloy0);
        }
        if (Configs.disableRecipePartBarrelStructure == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_PART, 1, 71), "OIO", "ICI", "OIO", 'O', obsidian, 'I', Blocks.IRON_BARS, 'C', active_core1);
        }
        if (Configs.disableRecipePartBarrelCapacity == false && Configs.disableItemCraftingPart == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_PART, 1, 72), "ACA", "ABA", "ACA", 'A', alloy1, 'C', "chestWood", 'B', EnderUtilitiesBlocks.BARREL));
        }

        if (Configs.disableRecipeModuleEnderCapacitor0 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ENDER_CAPACITOR, 1, 0), " P ", "ARA", "OAO", 'P', pearl, 'A', alloy0, 'R', "dustRedstone", 'O', obsidian));
        }
        if (Configs.disableRecipeModuleEnderCapacitor1 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_CAPACITOR, 1, 1), " P ", "AEA", "OAO", 'P', pearl, 'A', alloy1, 'E', Items.EMERALD, 'O', obsidian);
        }
        if (Configs.disableRecipeModuleEnderCapacitor2 == false && Configs.disableItemEnderCapacitor == false)
        {
            GameRegistry.addRecipe(new ItemStack(ENDER_CAPACITOR, 1, 2), " P ", "ADA", "OAO", 'P', pearl, 'A', alloy2, 'D', Items.DIAMOND, 'O', obsidian);
        }

        if (Configs.disableRecipeModuleLinkCrystalLocation == false && Configs.disableItemLinkCrystal == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(LINK_CRYSTAL, 1, 0), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassGreen", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalBlock == false && Configs.disableItemLinkCrystal == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(LINK_CRYSTAL, 1, 1), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassBlue", 'A', alloy0));
        }
        if (Configs.disableRecipeModuleLinkCrystalPortal == false && Configs.disableItemLinkCrystal == false)
        {
            //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(linkCrystal, 1, 2), "RGR", "GAG", "RGR", 'R', "dustRedstone", 'G', "blockGlassPurple", 'A', alloy0));
        }
    }

    private static void registerItem(ItemEnderUtilities item, String registryName, boolean isDisabled)
    {
        if (isDisabled == false)
        {
            item.setRegistryName(Reference.MOD_ID + ":" + registryName);
            GameRegistry.register(item);
        }
        else
        {
            item.setEnabled(false);
        }
    }
}
