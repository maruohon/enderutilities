package fi.dy.masa.enderutilities.setup;

import java.util.ArrayList;
import java.util.List;

public class EURegistry
{
    private static List<String> enderBagBlacklist = new ArrayList<String>();
    private static List<String> enderBagWhitelist = new ArrayList<String>();
    private static List<String> teleportBlacklist = new ArrayList<String>();
    //private static List<Class<? extends EntityLivingBase>> teleportBlacklistClasses = new ArrayList<Class<? extends EntityLivingBase>>();

    public static void registerEnderbagLists()
    {
        String[] items = EUConfigs.enderBagBlacklist.getStringList();
        for (String entry : items)
        {
            enderBagBlacklist.add(entry);
        }

        items = EUConfigs.enderBagWhitelist.getStringList();
        for (String entry : items)
        {
            enderBagWhitelist.add(entry);
        }
    }

    public static List<String> getEnderbagBlacklist()
    {
        return enderBagBlacklist;
    }

    public static List<String> getEnderbagWhitelist()
    {
        return enderBagWhitelist;
    }

    public static void registerTeleportBlacklist()
    {
        String[] items = EUConfigs.teleportBlacklist.getStringList();
        for (String entry : items)
        {
            teleportBlacklist.add(entry);
        }
    }

    public static List<String> getTeleportBlacklist()
    {
        return teleportBlacklist;
    }
/*
    public static List<Class<? extends EntityLivingBase>> getTeleportBlacklistClasses()
    {
        return teleportBlacklistClasses;
    }
*/
}
