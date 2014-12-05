package fi.dy.masa.enderutilities.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class TooltipHelper
{
    public static String getLocalizedDimensionName(int dim)
    {
        WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dim);
        if (worldServer != null && worldServer.provider != null)
        {
            return worldServer.provider.getDimensionName();
        }

        return "";
    }
}
