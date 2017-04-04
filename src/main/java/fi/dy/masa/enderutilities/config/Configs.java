package fi.dy.masa.enderutilities.config;

public class Configs
{
    // Client
    public static boolean announceLocationBindingInChat;
    public static boolean handyBagOpenRequiresSneak;
    public static boolean useToolParticles;
    public static boolean useToolSounds;

    // Generic
    public static int barrelCapacityUpgradeStacksPer;
    public static int barrelMaxCapacityUpgrades;
    public static int configFileVersion;
    public static int enderBucketCapacity;
    public static int harvestLevelEnderAlloyAdvanced;
    public static int msuMaxItems;
    public static int portalFrameCheckLimit;
    public static int portalLoopCheckLimit;
    public static int portalAreaCheckLimit;
    public static boolean barrelRenderFullnessBar;
    public static boolean fallingBlockDropsAsItemOnPlacementFail;
    public static boolean handyBagEnableItemUpdate;
    public static boolean registerWoodFencesToOreDict;
    public static boolean useEnderCharge;
    public static boolean replaceEntityItemCollisionBoxHandling;

    // Builder's Wand
    public static boolean buildersWandEnableCopyMode;
    public static boolean buildersWandEnablePasteMode;
    public static boolean buildersWandEnableMoveMode;
    public static boolean buildersWandEnableReplaceMode;
    public static boolean buildersWandEnableReplace3DMode;
    public static boolean buildersWandEnableStackMode;
    public static boolean buildersWandRenderForOtherPlayers;
    public static boolean buildersWandUseTranslucentGhostBlocks;
    public static int buildersWandBlocksPerTick;
    public static int buildersWandReplaceBlocksPerTick;
    public static float buildersWandGhostBlockAlpha;
    public static float buildersWandMaxBlockHardness;

    // Teleport control
    public static boolean enderBowAllowPlayers;
    public static boolean enderBowAllowSelfTP;
    public static boolean enderLassoAllowPlayers;

    // Black lists and white lists
    public static boolean enderBagListTypeIsWhitelist;
    public static String[] enderBagBlacklist;
    public static String[] enderBagWhitelist;
    public static String[] teleportBlacklist;

    // Block disable
    public static boolean disableBlockASU;
    public static boolean disableBlockBarrel;
    public static boolean disableBlockEnderElevator;
    public static boolean disableBlockEnergyBridge;
    public static boolean disableBlockFloor;
    public static boolean disableBlockInserter;
    public static boolean disableBlockMachine_0;
    public static boolean disableBlockMachine_1;
    public static boolean disableBlockMachine_2;
    public static boolean disableBlockMSU;
    public static boolean disableBlockPhasing;
    public static boolean disableBlockPortal;
    public static boolean disableBlockPortalFrame;
    public static boolean disableBlockPortalPanel;
    public static boolean disableBlockStorage_0;

    // Item disable
    public static boolean disableItemCraftingPart;
    public static boolean disableItemEnderCapacitor;
    public static boolean disableItemLinkCrystal;

    public static boolean disableItemBuildersWand;
    public static boolean disableItemChairWand;
    public static boolean disableItemDolly;
    public static boolean disableItemEnderArrow;
    public static boolean disableItemEnderBag;
    public static boolean disableItemEnderBow;
    public static boolean disableItemEnderBucket;
    public static boolean disableItemEnderLasso;
    public static boolean disableItemEnderPearl;
    public static boolean disableItemEnderPorter;
    public static boolean disableItemEnderSword;
    public static boolean disableItemEnderTools;
    public static boolean disableItemHandyBag;
    public static boolean disableItemIceMelter;
    public static boolean disableItemInventorySwapper;
    public static boolean disableItemLivingManipulator;
    public static boolean disableItemMobHarness;
    public static boolean disableItemPickupManager;
    public static boolean disableItemQuickStacker;
    public static boolean disableItemPortalScaler;
    public static boolean disableItemRuler;
    public static boolean disableItemSyringe;
    public static boolean disableItemVoidPickaxe;

    // Recipe disable
    // Blocks
    public static boolean disableRecipeAdjustableStorageUnit;
    public static boolean disableRecipeAdvancedQuickStacker;
    public static boolean disableRecipeBarrel;
    public static boolean disableRecipeCreationStation;
    public static boolean disableRecipeEnderElevator;
    public static boolean disableRecipeEnderInfuser;
    public static boolean disableRecipeFloor;
    public static boolean disableRecipeInserter;
    public static boolean disableRecipeJunkStorageUnit;
    public static boolean disableRecipeMassiveStorageBundle;
    public static boolean disableRecipeMassiveStorageUnit;
    public static boolean disableRecipeMolecularExciter;
    public static boolean disableRecipePhasingBlock;
    public static boolean disableRecipePortalFrame;
    public static boolean disableRecipePortalPanel;
    public static boolean disableRecipeToolWorkstation;

    public static boolean disableRecipeEnderFurnace;

    public static boolean disableRecipeEnergyBridgeTransmitter;
    public static boolean disableRecipeEnergyBridgeReceiver;
    public static boolean disableRecipeEnergyBridgeResonator;

    public static boolean disableRecipeHandyChest_0;
    public static boolean disableRecipeHandyChest_1;
    public static boolean disableRecipeHandyChest_2;
    public static boolean disableRecipeHandyChest_3;

    public static boolean disableRecipeMemoryChest_0;
    public static boolean disableRecipeMemoryChest_1;
    public static boolean disableRecipeMemoryChest_2;

    // Items
    public static boolean disableRecipeBuildersWand;
    public static boolean disableRecipeChairWand;
    public static boolean disableRecipeDolly;
    public static boolean disableRecipeEnderArrow;
    public static boolean disableRecipeEnderBag;
    public static boolean disableRecipeEnderBow;
    public static boolean disableRecipeEnderBucket;
    public static boolean disableRecipeEnderLasso;
    public static boolean disableRecipeEnderPearl;
    public static boolean disableRecipeEnderPearlElite;
    public static boolean disableRecipeEnderPorterBasic;
    public static boolean disableRecipeEnderPorterAdvanced;
    public static boolean disableRecipeHandyBag;
    public static boolean disableRecipeIceMelter;
    public static boolean disableRecipeIceMelterSuper;
    public static boolean disableRecipeInventorySwapper;
    public static boolean disableRecipeLivingManipulator;
    public static boolean disableRecipeMobHarness;
    public static boolean disableRecipePickupManager;
    public static boolean disableRecipeQuickStacker;
    public static boolean disableRecipePortalScaler;
    public static boolean disableRecipeRuler;
    public static boolean disableRecipeSyringe;
    public static boolean disableRecipeVoidPickaxe;

    public static boolean disableRecipeEnderSword;
    public static boolean disableRecipeEnderPickaxe;
    public static boolean disableRecipeEnderAxe;
    public static boolean disableRecipeEnderShovel;
    public static boolean disableRecipeEnderHoe;

    // Parts
    public static boolean disableRecipePartBarrelCapacity;
    public static boolean disableRecipePartBarrelLabel;
    public static boolean disableRecipePartBarrelStructure;
    public static boolean disableRecipePartCreativeBreaking;
    public static boolean disableRecipePartEnderAlloy0;
    public static boolean disableRecipePartEnderAlloy1;
    public static boolean disableRecipePartEnderAlloy2;
    public static boolean disableRecipePartEnderCore0;
    public static boolean disableRecipePartEnderCore1;
    public static boolean disableRecipePartEnderCore2;
    public static boolean disableRecipePartMemoryCardMisc;
    public static boolean disableRecipePartMemoryCardItems6b;
    public static boolean disableRecipePartMemoryCardItems8b;
    public static boolean disableRecipePartMemoryCardItems10b;
    public static boolean disableRecipePartMemoryCardItems12b;
    public static boolean disableRecipePartMobPersistence;
    public static boolean disableRecipePartEnderRelic;
    public static boolean disableRecipePartEnderRope;
    public static boolean disableRecipePartEnderStick;

    // Modules
    public static boolean disableRecipeModuleEnderCapacitor0;
    public static boolean disableRecipeModuleEnderCapacitor1;
    public static boolean disableRecipeModuleEnderCapacitor2;
    public static boolean disableRecipeModuleLinkCrystalLocation;
    public static boolean disableRecipeModuleLinkCrystalBlock;
    public static boolean disableRecipeModuleLinkCrystalPortal;
}

