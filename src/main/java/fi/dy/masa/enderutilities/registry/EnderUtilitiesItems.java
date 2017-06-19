package fi.dy.masa.enderutilities.registry;

import net.minecraftforge.fml.common.registry.GameRegistry;
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
