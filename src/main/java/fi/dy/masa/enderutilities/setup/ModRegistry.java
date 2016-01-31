package fi.dy.masa.enderutilities.setup;

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
