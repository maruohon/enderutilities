package fi.dy.masa.enderutilities.setup;

import cpw.mods.fml.common.Loader;

public class ModRegistry
{
    private static boolean modLoadedNEI;

    public static void checkLoadedMods()
    {
        modLoadedNEI = Loader.isModLoaded("NotEnoughItems");
    }

    public static boolean isModLoadedNEI()
    {
        return modLoadedNEI;
    }
}
