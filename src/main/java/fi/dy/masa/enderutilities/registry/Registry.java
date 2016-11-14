package fi.dy.masa.enderutilities.registry;

import java.util.ArrayList;
import java.util.List;
import fi.dy.masa.enderutilities.config.Configs;

public class Registry
{
    private static List<String> enderBagBlacklist = new ArrayList<String>();
    private static List<String> enderBagWhitelist = new ArrayList<String>();
    private static List<String> teleportBlacklist = new ArrayList<String>();
    //private static List<Class<? extends EntityLivingBase>> teleportBlacklistClasses = new ArrayList<Class<? extends EntityLivingBase>>();

    public static void registerEnderbagLists()
    {
        enderBagBlacklist.clear();
        for (String entry : Configs.enderBagBlacklist)
        {
            enderBagBlacklist.add(entry);
        }

        enderBagWhitelist.clear();
        for (String entry : Configs.enderBagWhitelist)
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
        teleportBlacklist.clear();
        for (String entry : Configs.teleportBlacklist)
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
