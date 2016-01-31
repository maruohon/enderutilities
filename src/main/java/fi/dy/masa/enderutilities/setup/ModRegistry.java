package fi.dy.masa.enderutilities.setup;

import net.minecraftforge.fml.common.Loader;

public class ModRegistry
{
    private static boolean modLoadedJEI;

    public static void checkLoadedMods()
    {
        // FIXME is this the correct mod id?
        modLoadedJEI = Loader.isModLoaded("JustEnoughItems");
    }

    public static boolean isModLoadedJEI()
    {
        return modLoadedJEI;
    }
}
