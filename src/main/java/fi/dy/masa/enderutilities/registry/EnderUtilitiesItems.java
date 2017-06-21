package fi.dy.masa.enderutilities.registry;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
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

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class EnderUtilitiesItems
{
    public static final ItemEnderUtilities ENDER_CAPACITOR      = new ItemEnderCapacitor(ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR);
    public static final ItemEnderUtilities ENDER_PART           = new ItemEnderPart(ReferenceNames.NAME_ITEM_ENDERPART);
    public static final ItemEnderUtilities LINK_CRYSTAL         = new ItemLinkCrystal(ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL);

    public static final ItemEnderUtilities BUILDERS_WAND        = new ItemBuildersWand(ReferenceNames.NAME_ITEM_BUILDERS_WAND);
    public static final ItemEnderUtilities CHAIR_WAND           = new ItemChairWand(ReferenceNames.NAME_ITEM_CHAIR_WAND);
    public static final ItemEnderUtilities DOLLY                = new ItemDolly(ReferenceNames.NAME_ITEM_DOLLY);
    public static final ItemEnderUtilities ENDER_ARROW          = new ItemEnderArrow(ReferenceNames.NAME_ITEM_ENDER_ARROW);
    public static final ItemEnderUtilities ENDER_BAG            = new ItemEnderBag(ReferenceNames.NAME_ITEM_ENDER_BAG);
    public static final ItemEnderUtilities ENDER_BOW            = new ItemEnderBow(ReferenceNames.NAME_ITEM_ENDER_BOW);
    public static final ItemEnderUtilities ENDER_BUCKET         = new ItemEnderBucket(ReferenceNames.NAME_ITEM_ENDER_BUCKET);
    public static final ItemEnderUtilities ENDER_LASSO          = new ItemEnderLasso(ReferenceNames.NAME_ITEM_ENDER_LASSO);
    public static final ItemEnderUtilities ENDER_PEARL_REUSABLE = new ItemEnderPearlReusable(ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE);
    public static final ItemEnderUtilities ENDER_PORTER         = new ItemEnderPorter(ReferenceNames.NAME_ITEM_ENDER_PORTER);
    public static final ItemEnderUtilities ENDER_SWORD          = new ItemEnderSword(ReferenceNames.NAME_ITEM_ENDER_SWORD);
    public static final ItemEnderUtilities ENDER_TOOL           = new ItemEnderTool(ReferenceNames.NAME_ITEM_ENDERTOOL);
    public static final ItemEnderUtilities HANDY_BAG            = new ItemHandyBag(ReferenceNames.NAME_ITEM_HANDY_BAG);
    public static final ItemEnderUtilities ICE_MELTER           = new ItemIceMelter(ReferenceNames.NAME_ITEM_ICE_MELTER);
    public static final ItemEnderUtilities INVENTORY_SWAPPER    = new ItemInventorySwapper(ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER);
    public static final ItemEnderUtilities LIVING_MANIPULATOR   = new ItemLivingManipulator(ReferenceNames.NAME_ITEM_LIVING_MANIPULATOR);
    public static final ItemEnderUtilities MOB_HARNESS          = new ItemMobHarness(ReferenceNames.NAME_ITEM_MOB_HARNESS);
    public static final ItemEnderUtilities NULLIFIER            = new ItemNullifier(ReferenceNames.NAME_ITEM_NULLIFIER);
    public static final ItemEnderUtilities PICKUP_MANAGER       = new ItemPickupManager(ReferenceNames.NAME_ITEM_PICKUP_MANAGER);
    public static final ItemEnderUtilities QUICK_STACKER        = new ItemQuickStacker(ReferenceNames.NAME_ITEM_QUICK_STACKER);
    public static final ItemEnderUtilities PORTAL_SCALER        = new ItemPortalScaler(ReferenceNames.NAME_ITEM_PORTAL_SCALER);
    public static final ItemEnderUtilities RULER                = new ItemRuler(ReferenceNames.NAME_ITEM_RULER);
    public static final ItemEnderUtilities SYRINGE              = new ItemSyringe(ReferenceNames.NAME_ITEM_SYRINGE);
    public static final ItemEnderUtilities VOID_PICKAXE         = new ItemVoidPickaxe(ReferenceNames.NAME_ITEM_VOID_PICKAXE);

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        registerItem(registry, ENDER_CAPACITOR,         Configs.disableItemEnderCapacitor);
        registerItem(registry, ENDER_PART,              Configs.disableItemCraftingPart);
        registerItem(registry, LINK_CRYSTAL,            Configs.disableItemLinkCrystal);

        registerItem(registry, BUILDERS_WAND,           Configs.disableItemBuildersWand);
        registerItem(registry, CHAIR_WAND,              Configs.disableItemChairWand);
        registerItem(registry, DOLLY,                   Configs.disableItemDolly);
        registerItem(registry, ENDER_ARROW,             Configs.disableItemEnderArrow);
        registerItem(registry, ENDER_BAG,               Configs.disableItemEnderBag);
        registerItem(registry, ENDER_BOW,               Configs.disableItemEnderBow);
        registerItem(registry, ENDER_BUCKET,            Configs.disableItemEnderBucket);
        registerItem(registry, ENDER_LASSO,             Configs.disableItemEnderLasso);
        registerItem(registry, ENDER_PEARL_REUSABLE,    Configs.disableItemEnderPearl);
        registerItem(registry, ENDER_PORTER,            Configs.disableItemEnderPorter);
        registerItem(registry, ENDER_SWORD,             Configs.disableItemEnderSword);
        registerItem(registry, ENDER_TOOL,              Configs.disableItemEnderTools);
        registerItem(registry, HANDY_BAG,               Configs.disableItemHandyBag);
        registerItem(registry, ICE_MELTER,              Configs.disableItemIceMelter);
        registerItem(registry, INVENTORY_SWAPPER,       Configs.disableItemInventorySwapper);
        registerItem(registry, LIVING_MANIPULATOR,      Configs.disableItemLivingManipulator);
        registerItem(registry, MOB_HARNESS,             Configs.disableItemMobHarness);
        registerItem(registry, NULLIFIER,               Configs.disableItemNullifier);
        registerItem(registry, PICKUP_MANAGER,          Configs.disableItemPickupManager);
        registerItem(registry, QUICK_STACKER,           Configs.disableItemQuickStacker);
        registerItem(registry, PORTAL_SCALER,           Configs.disableItemPortalScaler);
        registerItem(registry, RULER,                   Configs.disableItemRuler);
        registerItem(registry, SYRINGE,                 Configs.disableItemSyringe);
        registerItem(registry, VOID_PICKAXE,            Configs.disableItemVoidPickaxe);
    }

    private static void registerItem(IForgeRegistry<Item> registry, ItemEnderUtilities item, boolean isDisabled)
    {
        if (isDisabled == false)
        {
            item.setRegistryName(Reference.MOD_ID + ":" + item.getItemNameEnU());
            registry.register(item);
        }
        else
        {
            item.setEnabled(false);
        }
    }
}
