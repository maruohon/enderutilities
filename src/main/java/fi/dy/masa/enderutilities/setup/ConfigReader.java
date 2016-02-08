package fi.dy.masa.enderutilities.setup;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
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
        config = new Configuration(configFile, "0.5.0", true);
        config.load();

        ConfigReader.loadConfigGeneric(config);
        ConfigReader.loadConfigItemControl(config);
        ConfigReader.loadConfigLists(config);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.modID) == true)
        {
            loadConfigGeneric(config);
        }
    }

    public static void loadConfigGeneric(Configuration conf)
    {
        String category;

        category = CATEGORY_GENERIC;
        Configs.buildersWandBlocksPerTick = conf.get(category, "lazyBuildersWandBlocksPerTick", 10).setRequiresMcRestart(false);
        Configs.buildersWandBlocksPerTick.comment = "The number of blocks the Lazy Builder's Wand will place each game tick, default = 10";
        Configs.valueBuildersWandBlocksPerTick = Configs.buildersWandBlocksPerTick.getInt(10);

        Configs.enderBowAllowPlayers = conf.get(category, "enderBowAllowPlayers", true).setRequiresMcRestart(false);
        Configs.enderBowAllowPlayers.comment = "Is the Ender Bow allowed to teleport players (directly or in a 'stack' riding something)";

        Configs.enderBowAllowSelfTP = conf.get(category, "enderBowAllowSelfTP", true).setRequiresMcRestart(false);
        Configs.enderBowAllowSelfTP.comment = "Can the Ender Bow be used in the 'TP Self' mode";

        Configs.enderBucketCapacity = conf.get(category, "enderBucketCapacity", ItemEnderBucket.ENDER_BUCKET_MAX_AMOUNT).setRequiresMcRestart(false);
        Configs.enderBucketCapacity.comment = "Maximum amount the Ender Bucket can hold, in millibuckets. Default: 16000 mB (= 16 buckets).";

        Configs.enderLassoAllowPlayers = conf.get(category, "enderLassoAllowPlayers", true).setRequiresMcRestart(false);
        Configs.enderLassoAllowPlayers.comment = "Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)";

        Configs.harvestLevelEnderAlloyAdvanced = conf.get(category, "harvestLevelEnderAlloyAdvanced", 3).setRequiresMcRestart(true);
        Configs.harvestLevelEnderAlloyAdvanced.comment = "The harvest level of tools made from Advanced Ender Alloy (3 = vanilla diamond tool level).";

        Configs.useEnderCharge = conf.get(category, "useEnderCharge", true).setRequiresMcRestart(false);
        Configs.useEnderCharge.comment = "Do items require Ender Charge to operate? (stored in Ender Capacitors)";
        Configs.valueUseEnderCharge = Configs.useEnderCharge.getBoolean(true);

        category = CATEGORY_CLIENT;
        conf.addCustomCategoryComment(category, "Client side configs");

        Configs.useToolParticles = conf.get(category, "useToolParticles", true).setRequiresMcRestart(false);
        Configs.useToolParticles.comment = "Does the block drops teleporting by Ender tools cause particle effects";

        Configs.useToolSounds = conf.get(category, "useToolSounds", true).setRequiresMcRestart(false);
        Configs.useToolSounds.comment = "Does the block drops teleporting by Ender tools play the sound effect";

        category = "Version";
        Configs.configFileVersion = conf.get(category, "configFileVersion", 5000).setRequiresMcRestart(false);
        Configs.configFileVersion.comment = "Internal config file version tracking. DO NOT CHANGE!!";
        confVersion = Configs.configFileVersion.getInt();

        // Update the version in the config to the current version
        Configs.configFileVersion.setValue(CURRENT_CONFIG_VERSION);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    public static void loadConfigItemControl(Configuration conf)
    {
        String category;

        category = "DisableBlocks";
        conf.addCustomCategoryComment(category, "Completely disable blocks (don't register them to the game.) Note that machines are grouped together and identified by the meta value. You can't disable just a specific meta value.");

        // Block disable
        Configs.disableBlockEnergyBridge          = conf.get(category, "disableBlockEnergyBridge", false).setRequiresMcRestart(true);
        Configs.disableBlockEnergyBridge.comment = "Meta values: 0 = Energy Bridge Resonator; 1 = Energy Bridge Receiver; 2 = Energy Bridge Transmitter";
        Configs.disableBlockMachine_0             = conf.get(category, "disableBlockMachine_0", false).setRequiresMcRestart(true);
        Configs.disableBlockMachine_0.comment = "Info: Machine_0 meta values: 0 = Ender Furnace";
        Configs.disableBlockMachine_1             = conf.get(category, "disableBlockMachine_1", false).setRequiresMcRestart(true);
        Configs.disableBlockMachine_1.comment = "Info: Machine_1 meta values: 0 = Ender Infuser; 1 = Tool Workstation, 2 = Creation Station";
        Configs.disableBlockStorage_0             = conf.get(category, "disableBlockStorage_0", false).setRequiresMcRestart(true);
        Configs.disableBlockStorage_0.comment = "Meta values: 0..2 = Memory Chests, 3..5 = Handy Chests";

        category = "DisableItems";
        conf.addCustomCategoryComment(category, "Completely disable items (don't register them to the game.) Note that some items are grouped together using the damage value (and/or NBT data) to identify them. You can't disable a specific damage value only (so that existing items would vanish).");

        // Item disable
        Configs.disableItemCraftingPart           = conf.get(category, "disableItemCraftingPart", false).setRequiresMcRestart(true);
        Configs.disableItemEnderCapacitor         = conf.get(category, "disableItemEnderCapacitor", false).setRequiresMcRestart(true);
        Configs.disableItemLinkCrystal            = conf.get(category, "disableItemLinkCrystal", false).setRequiresMcRestart(true);

        Configs.disableItemBuildersWand           = conf.get(category, "disableItemBuildersWand", false).setRequiresMcRestart(true);
        Configs.disableItemEnderArrow             = conf.get(category, "disableItemEnderArrow", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBag               = conf.get(category, "disableItemEnderBag", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBow               = conf.get(category, "disableItemEnderBow", false).setRequiresMcRestart(true);
        Configs.disableItemEnderBucket            = conf.get(category, "disableItemEnderBucket", false).setRequiresMcRestart(true);
        Configs.disableItemEnderLasso             = conf.get(category, "disableItemEnderLasso", false).setRequiresMcRestart(true);
        Configs.disableItemEnderPearl             = conf.get(category, "disableItemEnderPearl", false).setRequiresMcRestart(true);
        Configs.disableItemEnderPorter            = conf.get(category, "disableItemEnderPorter", false).setRequiresMcRestart(true);
        Configs.disableItemEnderSword             = conf.get(category, "disableItemEnderSword", false).setRequiresMcRestart(true);
        Configs.disableItemEnderTools             = conf.get(category, "disableItemEnderTools", false).setRequiresMcRestart(true);
        Configs.disableItemHandyBag               = conf.get(category, "disableItemHandyBag", false).setRequiresMcRestart(true);
        Configs.disableItemInventorySwapper       = conf.get(category, "disableItemInventorySwapper", false).setRequiresMcRestart(true);
        Configs.disableItemLivingManipulator      = conf.get(category, "disableItemLivingManipulator", false).setRequiresMcRestart(true);
        Configs.disableItemMobHarness             = conf.get(category, "disableItemMobHarness", false).setRequiresMcRestart(true);
        Configs.disableItemPickupManager          = conf.get(category, "disableItemPickupManager", false).setRequiresMcRestart(true);
        Configs.disableItemPortalScaler           = conf.get(category, "disableItemPortalScaler", false).setRequiresMcRestart(true);
        Configs.disableItemRuler                  = conf.get(category, "disableItemRuler", false).setRequiresMcRestart(true);

        // Recipe disable
        category = "DisableRecipies";
        conf.addCustomCategoryComment(category, "Disable block or item recipies");

        // Blocks
        Configs.disableRecipeEnderFurnace         = conf.get(category, "disableRecipeEnderFurnace", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderInfuser         = conf.get(category, "disableRecipeEnderInfuser", false).setRequiresMcRestart(true);
        Configs.disableRecipeToolWorkstation      = conf.get(category, "disableRecipeToolWorkstation", false).setRequiresMcRestart(true);
        Configs.disableRecipeCreationStation      = conf.get(category, "disableRecipeCreationStation", false).setRequiresMcRestart(true);

        Configs.disableRecipeEnergyBridgeTransmitter    = conf.get(category, "disableRecipeEnergyBridgeTransmitter", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnergyBridgeReceiver       = conf.get(category, "disableRecipeEnergyBridgeReceiver", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnergyBridgeResonator      = conf.get(category, "disableRecipeEnergyBridgeResonator", false).setRequiresMcRestart(true);

        Configs.disableRecipeHandyChest_0      = conf.get(category, "disableRecipeHandyChest0", false).setRequiresMcRestart(true);
        Configs.disableRecipeHandyChest_1      = conf.get(category, "disableRecipeHandyChest1", false).setRequiresMcRestart(true);
        Configs.disableRecipeHandyChest_2      = conf.get(category, "disableRecipeHandyChest2", false).setRequiresMcRestart(true);

        Configs.disableRecipeMemoryChest_0      = conf.get(category, "disableRecipeMemoryChest0", false).setRequiresMcRestart(true);
        Configs.disableRecipeMemoryChest_1      = conf.get(category, "disableRecipeMemoryChest1", false).setRequiresMcRestart(true);
        Configs.disableRecipeMemoryChest_2      = conf.get(category, "disableRecipeMemoryChest2", false).setRequiresMcRestart(true);

        // Items
        Configs.disableRecipeBuildersWand         = conf.get(category, "disableRecipeBuildersWand", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderArrow           = conf.get(category, "disableRecipeEnderArrow", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBag             = conf.get(category, "disableRecipeEnderBag", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBow             = conf.get(category, "disableRecipeEnderBow", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderBucket          = conf.get(category, "disableRecipeEnderBucket", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderLasso           = conf.get(category, "disableRecipeEnderLasso", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPearl           = conf.get(category, "disableRecipeEnderPearl", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPearlElite      = conf.get(category, "disableRecipeEnderPearlElite", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPorterBasic     = conf.get(category, "disableRecipeEnderPorterBasic", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPorterAdvanced  = conf.get(category, "disableRecipeEnderPorterAdvanced", false).setRequiresMcRestart(true);
        Configs.disableRecipeHandyBag             = conf.get(category, "disableRecipeHandyBag", false).setRequiresMcRestart(true);
        Configs.disableRecipeInventorySwapper     = conf.get(category, "disableRecipeInventorySwapper", false).setRequiresMcRestart(true);
        Configs.disableRecipeLivingManipulator    = conf.get(category, "disableRecipeLivingManipulator", false).setRequiresMcRestart(true);
        Configs.disableRecipeMobHarness           = conf.get(category, "disableRecipeMobHarness", false).setRequiresMcRestart(true);
        Configs.disableRecipePickupManager        = conf.get(category, "disableRecipePickupManager", false).setRequiresMcRestart(true);
        Configs.disableRecipePortalScaler         = conf.get(category, "disableRecipePortalScaler", false).setRequiresMcRestart(true);
        Configs.disableRecipeRuler                = conf.get(category, "disableRecipeRuler", false).setRequiresMcRestart(true);

        // Tools and weapons
        Configs.disableRecipeEnderSword           = conf.get(category, "disableRecipeEnderSword", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderPickaxe         = conf.get(category, "disableRecipeEnderPickaxe", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderAxe             = conf.get(category, "disableRecipeEnderAxe", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderShovel          = conf.get(category, "disableRecipeEnderShovel", false).setRequiresMcRestart(true);
        Configs.disableRecipeEnderHoe             = conf.get(category, "disableRecipeEnderHoe", false).setRequiresMcRestart(true);

        // Items - crafting parts, modules, etc.
        Configs.disableRecipeModuleEnderCapacitor0      = conf.get(category, "disableRecipeModuleEnderCapacitor0", false).setRequiresMcRestart(true);
        Configs.disableRecipeModuleEnderCapacitor1      = conf.get(category, "disableRecipeModuleEnderCapacitor1", false).setRequiresMcRestart(true);
        Configs.disableRecipeModuleEnderCapacitor2      = conf.get(category, "disableRecipeModuleEnderCapacitor2", false).setRequiresMcRestart(true);
        Configs.disableRecipeModuleLinkCrystalLocation  = conf.get(category, "disableRecipeModuleLinkCrystalLocation", false).setRequiresMcRestart(true);
        Configs.disableRecipeModuleLinkCrystalBlock     = conf.get(category, "disableRecipeModuleLinkCrystalBlock", false).setRequiresMcRestart(true);
        Configs.disableRecipeModuleLinkCrystalPortal    = conf.get(category, "disableRecipeModuleLinkCrystalPortal", false).setRequiresMcRestart(true);

        Configs.disableRecipePartEnderAlloy0      = conf.get(category, "disableRecipePartEnderAlloy0", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderAlloy1      = conf.get(category, "disableRecipePartEnderAlloy1", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderAlloy2      = conf.get(category, "disableRecipePartEnderAlloy2", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderCore0       = conf.get(category, "disableRecipePartEnderCore0", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderCore1       = conf.get(category, "disableRecipePartEnderCore1", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderCore2       = conf.get(category, "disableRecipePartEnderCore2", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMemoryCardMisc   = conf.get(category, "disableRecipePartMemoryCardMisc", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMemoryCardItems6b  = conf.get(category, "disableRecipePartMemoryCardItems6b", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMemoryCardItems8b  = conf.get(category, "disableRecipePartMemoryCardItems8b", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMemoryCardItems10b = conf.get(category, "disableRecipePartMemoryCardItems10b", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMemoryCardItems12b = conf.get(category, "disableRecipePartMemoryCardItems12b", false).setRequiresMcRestart(true);
        Configs.disableRecipePartMobPersistence   = conf.get(category, "disableRecipePartMobPersistence", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderRelic       = conf.get(category, "disableRecipePartEnderRelic", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderRope        = conf.get(category, "disableRecipePartEnderRope", false).setRequiresMcRestart(true);
        Configs.disableRecipePartEnderStick       = conf.get(category, "disableRecipePartEnderStick", false).setRequiresMcRestart(true);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    public static void loadConfigLists(Configuration conf)
    {
        String category;

        category = "EnderBag";
        Configs.enderBagListType = conf.get(category, "listType", "whitelist").setRequiresMcRestart(false);
        Configs.enderBagListType.comment = "Target control list type used for Ender Bag. Allowed values: blacklist, whitelist.";

        Configs.enderBagBlacklist = conf.get(category, "blackList", new String[] {}).setRequiresMcRestart(false);
        Configs.enderBagBlacklist.comment = "Block types the Ender Bag is NOT allowed to (= doesn't properly) work with.";

        Configs.enderBagWhitelist = conf.get(category, "whiteList", new String[] {"minecraft:chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:ender_chest", "minecraft:furnace", "minecraft:hopper", "minecraft:trapped_chest"}).setRequiresMcRestart(false);
        Configs.enderBagWhitelist.comment = "Block types the Ender Bag is allowed to (= should properly) work with. **NOTE** Only some vanilla blocks work properly atm!!";

        category = "Teleporting";
        Configs.teleportBlacklist = conf.get(category, "entityBlackList", new String[] {"EntityDragon", "EntityDragonPart", "EntityEnderCrystal", "EntityWither"}).setRequiresMcRestart(false);
        Configs.teleportBlacklist.comment = "Entities that are not allowed to be teleported using any methods";

        //updateConfigs(conf);

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    public static void updateConfigs(Configuration conf)
    {
    }
}
