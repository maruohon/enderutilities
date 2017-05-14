package fi.dy.masa.enderutilities.registry;

import net.minecraftforge.fml.common.Loader;

public class ModRegistry
{
    public static final String MODID_BAUBLES = "baubles";
    public static final String MODID_JEI     = "justenoughitems";

    private static boolean modLoadedBaubles;
    private static boolean modLoadedJEI;

    public static void checkLoadedMods()
    {
        modLoadedBaubles    = Loader.isModLoaded(MODID_BAUBLES);
        modLoadedJEI        = Loader.isModLoaded(MODID_JEI);
    }

    public static boolean isModLoadedBaubles()
    {
        return modLoadedBaubles;
    }

    public static boolean isModLoadedJEI()
    {
        return modLoadedJEI;
    }
}
