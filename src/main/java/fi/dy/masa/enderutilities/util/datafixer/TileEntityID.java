package fi.dy.masa.enderutilities.util.datafixer;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class TileEntityID implements IFixableData
{
    private static final Map<String, String> OLD_TO_NEW_ID_MAP = new HashMap<String, String>();

    public int getFixVersion()
    {
        return 704;
    }

    public NBTTagCompound fixTagCompound(NBTTagCompound tag)
    {
        String newId = OLD_TO_NEW_ID_MAP.get(tag.getString("id"));

        if (newId != null)
        {
            tag.setString("id", newId);
        }

        return tag;
    }

    public static Map<String, String> getMap()
    {
        return OLD_TO_NEW_ID_MAP;
    }

    static
    {
        OLD_TO_NEW_ID_MAP.put("asu",                    "enderutilities:asu");
        OLD_TO_NEW_ID_MAP.put("barrel",                 "enderutilities:barrel");
        OLD_TO_NEW_ID_MAP.put("creationstation",        "enderutilities:creation_station");
        OLD_TO_NEW_ID_MAP.put("drawbridge",             "enderutilities:draw_bridge");
        OLD_TO_NEW_ID_MAP.put("enderelevator",          "enderutilities:ender_elevator");
        OLD_TO_NEW_ID_MAP.put("enderfurnace",           "enderutilities:ender_furnace");
        OLD_TO_NEW_ID_MAP.put("enderinfuser",           "enderutilities:ender_infuser");
        OLD_TO_NEW_ID_MAP.put("energybridge",           "enderutilities:energy_bridge");
        OLD_TO_NEW_ID_MAP.put("handychest",             "enderutilities:handy_chest");
        OLD_TO_NEW_ID_MAP.put("inserter",               "enderutilities:inserter");
        OLD_TO_NEW_ID_MAP.put("jsu",                    "enderutilities:jsu");
        OLD_TO_NEW_ID_MAP.put("memorychest",            "enderutilities:memory_chest");
        OLD_TO_NEW_ID_MAP.put("msu",                    "enderutilities:msu");
        OLD_TO_NEW_ID_MAP.put("portal",                 "enderutilities:portal");
        OLD_TO_NEW_ID_MAP.put("portal_panel",           "enderutilities:portal_panel");
        OLD_TO_NEW_ID_MAP.put("quickstackeradvanced",   "enderutilities:quick_stacker_advanced");
        OLD_TO_NEW_ID_MAP.put("sound_block",            "enderutilities:sound_block");
        OLD_TO_NEW_ID_MAP.put("toolworkstation",        "enderutilities:tool_workstation");

        OLD_TO_NEW_ID_MAP.put("enderutilities:creationstation",         "enderutilities:creation_station");
        OLD_TO_NEW_ID_MAP.put("enderutilities:drawbridge",              "enderutilities:draw_bridge");
        OLD_TO_NEW_ID_MAP.put("enderutilities:enderelevator",           "enderutilities:ender_elevator");
        OLD_TO_NEW_ID_MAP.put("enderutilities:enderelevator_layer",     "enderutilities:ender_elevator_layer");
        OLD_TO_NEW_ID_MAP.put("enderutilities:enderelevator_slab",      "enderutilities:ender_elevator_slab");
        OLD_TO_NEW_ID_MAP.put("enderutilities:enderfurnace",            "enderutilities:ender_furnace");
        OLD_TO_NEW_ID_MAP.put("enderutilities:enderinfuser",            "enderutilities:ender_infuser");
        OLD_TO_NEW_ID_MAP.put("enderutilities:energybridge",            "enderutilities:energy_bridge");
        OLD_TO_NEW_ID_MAP.put("enderutilities:handychest",              "enderutilities:handy_chest");
        OLD_TO_NEW_ID_MAP.put("enderutilities:memorychest",             "enderutilities:memory_chest");
        OLD_TO_NEW_ID_MAP.put("enderutilities:quickstackeradvanced",    "enderutilities:quick_stacker_advanced");
        OLD_TO_NEW_ID_MAP.put("enderutilities:toolworkstation",         "enderutilities:tool_workstation");
    }
}
