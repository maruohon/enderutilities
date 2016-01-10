package fi.dy.masa.enderutilities.reference;

public class ReferenceNames
{
    public static final String NAME_ENTITY_ENDER_ARROW          = "enderarrow";
    public static final String NAME_ENTITY_ENDER_PEARL_REUSABLE = "enderpearlreusable";

    public static final String NAME_ENTITY_ENDERMAN_FIGHTER     = "endermanfighter";


    public static final String NAME_TILE_MACHINE_0              = "enderfurnace"; // Legacy weight name... :p
    public static final String NAME_TILE_MACHINE_1              = "machine.1";

    public static final String NAME_TILE_ENERGY_BRIDGE_TRANSMITTER   = "energybridge.transmitter";
    public static final String NAME_TILE_ENERGY_BRIDGE_RECEIVER      = "energybridge.receiver";
    public static final String NAME_TILE_ENERGY_BRIDGE_RESONATOR     = "energybridge.resonator";


    public static final String NAME_TILE_ENTITY_ENDER_FURNACE        = "enderfurnace";
    public static final String NAME_TILE_ENTITY_ENDER_INFUSER        = "enderinfuser";
    public static final String NAME_TILE_ENTITY_TOOL_WORKSTATION     = "toolworkstation";

    public static final String NAME_TILE_ENTITY_ENERGY_BRIDGE        = "energybridge";

    public static final String NAME_ITEM_ENDERPART                  = "enderpart";
    public static final String NAME_ITEM_ENDERPART_ENDERALLOY       = "enderalloy";
    public static final String NAME_ITEM_ENDERPART_ENDERCAPACITOR   = "endercapacitor";
    public static final String NAME_ITEM_ENDERPART_ENDERCORE        = "endercore";
    public static final String NAME_ITEM_ENDERPART_ENDERSTICK       = "enderstick";
    public static final String NAME_ITEM_ENDERPART_ENDERROPE        = "enderrope";
    public static final String NAME_ITEM_ENDERPART_LINKCRYSTAL      = "linkcrystal";
    public static final String NAME_ITEM_ENDERPART_MOBPERSISTENCE   = "mobpersistence";
    public static final String NAME_ITEM_ENDERPART_ENDERRELIC       = "enderrelic";
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD      = "memorycard"; // Not actual item, used for all memory cards in places
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD_MISC = "memorycard.misc";
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS  = "memorycard.items";

    public static final String NAME_ITEM_ENDERTOOL              = "endertool";
    public static final String NAME_ITEM_ENDER_PICKAXE          = "enderpickaxe";
    public static final String NAME_ITEM_ENDER_AXE              = "enderaxe";
    public static final String NAME_ITEM_ENDER_SHOVEL           = "endershovel";
    public static final String NAME_ITEM_ENDER_HOE              = "enderhoe";
    public static final String NAME_ITEM_ENDER_SWORD            = "endersword";

    public static final String NAME_ITEM_ENDER_ARROW            = "enderarrow";
    public static final String NAME_ITEM_ENDER_BAG              = "enderbag";
    public static final String NAME_ITEM_ENDER_BOW              = "enderbow";
    public static final String NAME_ITEM_ENDER_BUCKET           = "enderbucket";
    public static final String NAME_ITEM_ENDER_FURNACE          = "enderfurnace";
    public static final String NAME_ITEM_ENDER_LASSO            = "enderlasso";
    public static final String NAME_ITEM_ENDER_PEARL_REUSABLE   = "enderpearlreusable";
    public static final String NAME_ITEM_ENDER_PORTER           = "enderporter";
    public static final String NAME_ITEM_HANDY_BAG              = "handybag";
    public static final String NAME_ITEM_INVENTORY_SWAPPER      = "inventoryswapper";
    public static final String NAME_ITEM_LIVING_MANIPULATOR     = "livingmanipulator";
    public static final String NAME_ITEM_MOB_HARNESS            = "mobharness";
    public static final String NAME_ITEM_PORTAL_SCALER          = "portalscaler";


    public static String getPrefixedName(String name)
    {
        return Reference.MOD_ID + "." + name;
    }
}
