package fi.dy.masa.enderutilities.config;

import java.io.File;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;

public class ConfigReader
{
    public static final int CURRENT_CONFIG_VERSION = 5000;
    public static final String CATEGORY_CLIENT = "Client";
    public static final String CATEGORY_GENERIC = "Generic";
    public static int confVersion = 0;
    public static File configurationFile;
    public static Configuration config;

    public static void loadConfigsAll(File configFile)
    {
        EnderUtilities.logger.info("Loading configuration...");

        configurationFile = configFile;
        config = new Configuration(configFile, null, true);
        config.load();

        ConfigReader.loadConfigGeneric(config);
        ConfigReader.loadConfigItemControl(config);
        ConfigReader.loadConfigLists(config);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.getModID()))
        {
            loadConfigGeneric(config);
        }
    }

    public static void loadConfigGeneric(Configuration conf)
    {
        Property prop;
        String category = CATEGORY_GENERIC;

        prop = conf.get(category, "enderBowAllowPlayers", true).setRequiresMcRestart(false);
        prop.setComment("Is the Ender Bow allowed to teleport players (directly or in a 'stack' riding something)");
        Configs.enderBowAllowPlayers = prop.getBoolean();

        prop = conf.get(category, "enderBowAllowSelfTP", true).setRequiresMcRestart(false);
        prop.setComment("Can the Ender Bow be used in the 'TP Self' mode");
        Configs.enderBowAllowSelfTP = prop.getBoolean();

        prop = conf.get(category, "enderBucketCapacity", 16000).setRequiresMcRestart(false);
        prop.setComment("Maximum amount the Ender Bucket can hold, in millibuckets. Default: 16000 mB (= 16 buckets).");
        Configs.enderBucketCapacity = prop.getInt();

        prop = conf.get(category, "enderLassoAllowPlayers", true).setRequiresMcRestart(false);
        prop.setComment("Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)");
        Configs.enderLassoAllowPlayers = prop.getBoolean();

        prop = conf.get(category, "harvestLevelEnderAlloyAdvanced", 3).setRequiresMcRestart(true);
        prop.setComment("The harvest level of tools made from Advanced Ender Alloy (3 = vanilla diamond tool level).");
        Configs.harvestLevelEnderAlloyAdvanced = prop.getInt();

        prop = conf.get(category, "lazyBuildersWandBlocksPerTick", 10).setRequiresMcRestart(false);
        prop.setComment("The number of blocks the Lazy Builder's Wand will place each game tick in the \"build modes\", default = 10");
        Configs.buildersWandBlocksPerTick = prop.getInt();

        prop = conf.get(category, "lazyBuildersWandEnableCopyMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the Copy mode functionality in survival mode");
        Configs.buildersWandEnableCopyMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandEnableMoveMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the Move mode functionality in survival mode");
        Configs.buildersWandEnableMoveMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandEnablePasteMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the Paste mode functionality in survival mode");
        Configs.buildersWandEnablePasteMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandEnableReplaceMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the Replace mode functionality in survival mode");
        Configs.buildersWandEnableReplaceMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandEnableReplace3DMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the Replace 3D mode functionality in survival mode");
        Configs.buildersWandEnableReplace3DMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandEnableStackMode", true).setRequiresMcRestart(false);
        prop.setComment("Enables the \"Stack Area\" mode functionality in survival mode");
        Configs.buildersWandEnableStackMode = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandGhostBlockAlpha", 0.7d).setRequiresMcRestart(false);
        prop.setComment("The alpha value to use for the translucent ghost block rendering mode");
        Configs.buildersWandGhostBlockAlpha = (float) MathHelper.clamp(prop.getDouble(), 0, 1);

        prop = conf.get(category, "lazyBuildersWandMaxBlockHardness", 10d).setRequiresMcRestart(false);
        prop.setComment("The maximum block hardness of the blocks the wand can break/move in survival mode");
        Configs.buildersWandMaxBlockHardness = (float) prop.getDouble();

        prop = conf.get(category, "lazyBuildersWandAndRulerRenderForOtherPlayers", true).setRequiresMcRestart(false);
        prop.setComment("Render the Ruler and Builder's Wand areas/selections also for the items held by other players");
        Configs.buildersWandRenderForOtherPlayers = prop.getBoolean();

        prop = conf.get(category, "lazyBuildersWandReplaceBlocksPerTick", 1).setRequiresMcRestart(false);
        prop.setComment("The number of blocks to replace per game tick in the Replace mode, default = 1 (= 20 blocks per second)");
        Configs.buildersWandReplaceBlocksPerTick = prop.getInt();

        prop = conf.get(category, "lazyBuildersWandUseTranslucentGhostBlocks", true).setRequiresMcRestart(false);
        prop.setComment("Use translucent ghost block rendering");
        Configs.buildersWandUseTranslucentGhostBlocks = prop.getBoolean();

        prop = conf.get(category, "useEnderCharge", true).setRequiresMcRestart(false);
        prop.setComment("Do items require Ender Charge to operate? (stored in Ender Capacitors)");
        Configs.useEnderCharge = prop.getBoolean(true);

        prop = conf.get(category, "portalFrameCheckLimit", 2000).setRequiresMcRestart(false);
        prop.setComment("How many Portal Frame blocks to check at most");
        Configs.portalFrameCheckLimit = prop.getInt();

        prop = conf.get(category, "portalLoopCheckLimit", 1000).setRequiresMcRestart(false);
        prop.setComment("How many blocks to check at most when checking portal enclosing loops");
        Configs.portalLoopCheckLimit = prop.getInt();

        prop = conf.get(category, "portalAreaCheckLimit", 8000).setRequiresMcRestart(false);
        prop.setComment("How many blocks to check at most when checking that one portal area is valid");
        Configs.portalAreaCheckLimit = prop.getInt();

        category = CATEGORY_CLIENT;
        conf.addCustomCategoryComment(category, "Client side configs");

        prop = conf.get(category, "announceLocationBindingInChat", false).setRequiresMcRestart(false);
        prop.setComment("Prints a chat message when items are bound to a new location");
        Configs.announceLocationBindingInChat = prop.getBoolean();

        prop = conf.get(category, "handyBagOpenRequiresSneak", false).setRequiresMcRestart(false);
        prop.setComment("Reverse the sneak behaviour on opening the Handy Bag instead of the regular inventory");
        Configs.handyBagOpenRequiresSneak = prop.getBoolean(false);

        prop = conf.get(category, "useToolParticles", true).setRequiresMcRestart(false);
        prop.setComment("Does the block drops teleporting by Ender tools cause particle effects");
        Configs.useToolParticles = prop.getBoolean();

        prop = conf.get(category, "useToolSounds", true).setRequiresMcRestart(false);
        prop.setComment("Does the block drops teleporting by Ender tools play the sound effect");
        Configs.useToolSounds = prop.getBoolean();

        category = "Version";
        prop = conf.get(category, "configFileVersion", 6500).setRequiresMcRestart(false);
        prop.setComment("Internal config file version tracking. DO NOT CHANGE!!");
        confVersion = prop.getInt();

        // Update the version in the config to the current version
        prop.setValue(CURRENT_CONFIG_VERSION);

        if (conf.hasChanged())
        {
            conf.save();
        }
    }

    public static void loadConfigItemControl(Configuration conf)
    {
        Property prop;
        String category = "DisableBlocks";
        conf.addCustomCategoryComment(category, "Completely disable blocks (don't register them to the game.) Note that machines are grouped together and identified by the meta value. You can't disable just a specific meta value.");

        // Block disable
        Configs.disableBlockEnderElevator = conf.get(category, "disableBlockEnderElevator", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableBlockPortal        = conf.get(category, "disableBlockPortal", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableBlockPortalFrame   = conf.get(category, "disableBlockPortalFrame", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableBlockPortalPanel   = conf.get(category, "disableBlockPortalPanel", false).setRequiresMcRestart(true).getBoolean();

        prop = conf.get(category, "disableBlockEnergyBridge", false).setRequiresMcRestart(true);
        prop.setComment("Meta values: 0 = Energy Bridge Resonator; 1 = Energy Bridge Receiver; 2 = Energy Bridge Transmitter");
        Configs.disableBlockEnergyBridge = prop.getBoolean();

        prop = conf.get(category, "disableBlockMachine_0", false).setRequiresMcRestart(true);
        prop.setComment("Info: Machine_0 meta values: 0 = Ender Furnace");
        Configs.disableBlockMachine_0 = prop.getBoolean();

        prop = conf.get(category, "disableBlockMachine_1", false).setRequiresMcRestart(true);
        prop.setComment("Info: Machine_1 meta values: 0 = Ender Infuser; 1 = Tool Workstation, 2 = Creation Station");
        Configs.disableBlockMachine_1 = prop.getBoolean();

        prop = conf.get(category, "disableBlockStorage_0", false).setRequiresMcRestart(true);
        prop.setComment("Meta values: 0..2 = Memory Chests, 3..5 = Handy Chests");
        Configs.disableBlockStorage_0 = prop.getBoolean();

        category = "DisableItems";
        conf.addCustomCategoryComment(category, "Completely disable items (don't register them to the game.) Note that some items are grouped together using the damage value (and/or NBT data) to identify them. You can't disable a specific damage value only (so that existing items would vanish).");

        // Item disable
        Configs.disableItemCraftingPart           = conf.get(category, "disableItemCraftingPart", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderCapacitor         = conf.get(category, "disableItemEnderCapacitor", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemLinkCrystal            = conf.get(category, "disableItemLinkCrystal", false).setRequiresMcRestart(true).getBoolean();

        Configs.disableItemBuildersWand           = conf.get(category, "disableItemBuildersWand", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderArrow             = conf.get(category, "disableItemEnderArrow", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderBag               = conf.get(category, "disableItemEnderBag", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderBow               = conf.get(category, "disableItemEnderBow", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderBucket            = conf.get(category, "disableItemEnderBucket", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderLasso             = conf.get(category, "disableItemEnderLasso", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderPearl             = conf.get(category, "disableItemEnderPearl", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderPorter            = conf.get(category, "disableItemEnderPorter", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderSword             = conf.get(category, "disableItemEnderSword", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemEnderTools             = conf.get(category, "disableItemEnderTools", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemHandyBag               = conf.get(category, "disableItemHandyBag", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemIceMelter              = conf.get(category, "disableItemIceMelter", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemInventorySwapper       = conf.get(category, "disableItemInventorySwapper", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemLivingManipulator      = conf.get(category, "disableItemLivingManipulator", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemMobHarness             = conf.get(category, "disableItemMobHarness", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemPickupManager          = conf.get(category, "disableItemPickupManager", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemQuickStacker           = conf.get(category, "disableItemQuickStacker", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemPortalScaler           = conf.get(category, "disableItemPortalScaler", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemRuler                  = conf.get(category, "disableItemRuler", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableItemSyringe                = conf.get(category, "disableItemSyringe", false).setRequiresMcRestart(true).getBoolean();

        // Recipe disable
        category = "DisableRecipies";
        conf.addCustomCategoryComment(category, "Disable block or item recipies");

        // Blocks
        Configs.disableRecipeAdvancedQuickStacker = conf.get(category, "disableRecipeAdvancedQuickStacker", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeCreationStation      = conf.get(category, "disableRecipeCreationStation", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderElevator        = conf.get(category, "disableRecipeEnderElevator", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderFurnace         = conf.get(category, "disableRecipeEnderFurnace", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderInfuser         = conf.get(category, "disableRecipeEnderInfuser", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePortalFrame                = conf.get(category, "disableRecipeFrame", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePortalPanel          = conf.get(category, "disableRecipePortalPanel", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeToolWorkstation      = conf.get(category, "disableRecipeToolWorkstation", false).setRequiresMcRestart(true).getBoolean();

        Configs.disableRecipeEnergyBridgeTransmitter = conf.get(category, "disableRecipeEnergyBridgeTransmitter", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnergyBridgeReceiver    = conf.get(category, "disableRecipeEnergyBridgeReceiver", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnergyBridgeResonator   = conf.get(category, "disableRecipeEnergyBridgeResonator", false).setRequiresMcRestart(true).getBoolean();

        Configs.disableRecipeHandyChest_0         = conf.get(category, "disableRecipeHandyChest0", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeHandyChest_1         = conf.get(category, "disableRecipeHandyChest1", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeHandyChest_2         = conf.get(category, "disableRecipeHandyChest2", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeHandyChest_3         = conf.get(category, "disableRecipeHandyChest3", false).setRequiresMcRestart(true).getBoolean();

        Configs.disableRecipeMemoryChest_0        = conf.get(category, "disableRecipeMemoryChest0", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeMemoryChest_1        = conf.get(category, "disableRecipeMemoryChest1", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeMemoryChest_2        = conf.get(category, "disableRecipeMemoryChest2", false).setRequiresMcRestart(true).getBoolean();

        // Items
        Configs.disableRecipeBuildersWand         = conf.get(category, "disableRecipeBuildersWand", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderArrow           = conf.get(category, "disableRecipeEnderArrow", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderBag             = conf.get(category, "disableRecipeEnderBag", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderBow             = conf.get(category, "disableRecipeEnderBow", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderBucket          = conf.get(category, "disableRecipeEnderBucket", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderLasso           = conf.get(category, "disableRecipeEnderLasso", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderPearl           = conf.get(category, "disableRecipeEnderPearl", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderPearlElite      = conf.get(category, "disableRecipeEnderPearlElite", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderPorterBasic     = conf.get(category, "disableRecipeEnderPorterBasic", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderPorterAdvanced  = conf.get(category, "disableRecipeEnderPorterAdvanced", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeHandyBag             = conf.get(category, "disableRecipeHandyBag", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeIceMelter            = conf.get(category, "disableRecipeIceMelter", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeIceMelterSuper       = conf.get(category, "disableRecipeIceMelterSuper", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeInventorySwapper     = conf.get(category, "disableRecipeInventorySwapper", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeLivingManipulator    = conf.get(category, "disableRecipeLivingManipulator", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeMobHarness           = conf.get(category, "disableRecipeMobHarness", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePickupManager        = conf.get(category, "disableRecipePickupManager", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeQuickStacker         = conf.get(category, "disableRecipeQuickStacker", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePortalScaler         = conf.get(category, "disableRecipePortalScaler", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeRuler                = conf.get(category, "disableRecipeRuler", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeSyringe              = conf.get(category, "disableRecipeSyringe", false).setRequiresMcRestart(true).getBoolean();

        // Tools and weapons
        Configs.disableRecipeEnderSword           = conf.get(category, "disableRecipeEnderSword", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderPickaxe         = conf.get(category, "disableRecipeEnderPickaxe", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderAxe             = conf.get(category, "disableRecipeEnderAxe", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderShovel          = conf.get(category, "disableRecipeEnderShovel", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeEnderHoe             = conf.get(category, "disableRecipeEnderHoe", false).setRequiresMcRestart(true).getBoolean();

        // Items - crafting parts, modules, etc.
        Configs.disableRecipeModuleEnderCapacitor0      = conf.get(category, "disableRecipeModuleEnderCapacitor0", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeModuleEnderCapacitor1      = conf.get(category, "disableRecipeModuleEnderCapacitor1", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeModuleEnderCapacitor2      = conf.get(category, "disableRecipeModuleEnderCapacitor2", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeModuleLinkCrystalLocation  = conf.get(category, "disableRecipeModuleLinkCrystalLocation", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeModuleLinkCrystalBlock     = conf.get(category, "disableRecipeModuleLinkCrystalBlock", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipeModuleLinkCrystalPortal    = conf.get(category, "disableRecipeModuleLinkCrystalPortal", false).setRequiresMcRestart(true).getBoolean();

        Configs.disableRecipePartEnderAlloy0      = conf.get(category, "disableRecipePartEnderAlloy0", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderAlloy1      = conf.get(category, "disableRecipePartEnderAlloy1", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderAlloy2      = conf.get(category, "disableRecipePartEnderAlloy2", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderCore0       = conf.get(category, "disableRecipePartEnderCore0", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderCore1       = conf.get(category, "disableRecipePartEnderCore1", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderCore2       = conf.get(category, "disableRecipePartEnderCore2", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMemoryCardMisc   = conf.get(category, "disableRecipePartMemoryCardMisc", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMemoryCardItems6b  = conf.get(category, "disableRecipePartMemoryCardItems6b", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMemoryCardItems8b  = conf.get(category, "disableRecipePartMemoryCardItems8b", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMemoryCardItems10b = conf.get(category, "disableRecipePartMemoryCardItems10b", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMemoryCardItems12b = conf.get(category, "disableRecipePartMemoryCardItems12b", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartMobPersistence   = conf.get(category, "disableRecipePartMobPersistence", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderRelic       = conf.get(category, "disableRecipePartEnderRelic", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderRope        = conf.get(category, "disableRecipePartEnderRope", false).setRequiresMcRestart(true).getBoolean();
        Configs.disableRecipePartEnderStick       = conf.get(category, "disableRecipePartEnderStick", false).setRequiresMcRestart(true).getBoolean();

        if (conf.hasChanged())
        {
            conf.save();
        }
    }

    public static void loadConfigLists(Configuration conf)
    {
        Property prop;
        String category;

        category = "EnderBag";

        prop = conf.get(category, "listType", "whitelist").setRequiresMcRestart(false);
        prop.setComment("Target control list type used for Ender Bag. Allowed values: blacklist, whitelist.");
        Configs.enderBagListType = prop.getString();

        prop = conf.get(category, "blackList", new String[] {}).setRequiresMcRestart(false);
        prop.setComment("Block types the Ender Bag is NOT allowed to (= doesn't properly) work with.");
        Configs.enderBagBlacklist = prop.getStringList();

        prop = conf.get(category, "whiteList", new String[] {"minecraft:chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:ender_chest", "minecraft:furnace", "minecraft:hopper", "minecraft:trapped_chest"}).setRequiresMcRestart(false);
        prop.setComment("Block types the Ender Bag is allowed to (= should properly) work with. **NOTE** Only some vanilla blocks work properly atm!!");
        Configs.enderBagWhitelist = prop.getStringList();

        category = "Teleporting";
        prop = conf.get(category, "entityBlackList", new String[] {"EntityDragon", "EntityDragonPart", "EntityEnderCrystal", "EntityWither"}).setRequiresMcRestart(false);
        prop.setComment("Entities that are not allowed to be teleported using any methods");
        Configs.teleportBlacklist = prop.getStringList();

        //updateConfigs(conf);

        if (conf.hasChanged())
        {
            conf.save();
        }
    }

    public static void updateConfigs(Configuration conf)
    {
    }
}
