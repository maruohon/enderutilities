package fi.dy.masa.enderutilities.util;

import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import fi.dy.masa.enderutilities.EnderUtilities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class WorldUtils
{
    private static final IntOpenHashSet CUSTOM_END_DIMENSIONS = new IntOpenHashSet();

    public static void setCustomEndDimensions(String[] list)
    {
        CUSTOM_END_DIMENSIONS.clear();

        for (String str : list)
        {
            try
            {
                int dim = Integer.parseInt(str);
                CUSTOM_END_DIMENSIONS.add(dim);
            }
            catch (NumberFormatException e)
            {
                EnderUtilities.logger.warn("Invalid string '{}' for custom End dimension IDs", str, e);
            }
        }
    }

    public static boolean isEndDimension(World world)
    {
        return world.provider instanceof WorldProviderEnd ||
               world.provider.getDimensionType().getId() == 1 ||
               CUSTOM_END_DIMENSIONS.contains(world.provider.getDimension());
               
    }

    public static boolean isNetherDimension(World world)
    {
        return world.provider.isNether() ||
               world.provider instanceof WorldProviderHell ||
               world.provider.getDimensionType().getId() == -1;
    }

    public static boolean isOverworldDimension(World world)
    {
        //return isEndDimension(world) == false && isNetherDimension(world) == false;
        return world.provider instanceof WorldProviderSurface ||
               world.provider.getDimensionType().getId() == 0;
    }
}
