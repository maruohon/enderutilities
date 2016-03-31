package fi.dy.masa.enderutilities.setup;

import net.minecraftforge.fml.common.Loader;

public class ModRegistry
{
    private static boolean modLoadedJEI;

    public static void checkLoadedMods()
    {
        modLoadedJEI = Loader.isModLoaded("JustEnoughItems");
    }

    public static boolean isModLoadedJEI()
    {
        return modLoadedJEI;
    }
}
