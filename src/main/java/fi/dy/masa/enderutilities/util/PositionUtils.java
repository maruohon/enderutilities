package fi.dy.masa.enderutilities.util;

import java.util.List;

import net.minecraft.util.math.BlockPos;

public class PositionUtils
{
    public static void getPositionsInBoxSpiralingOutwards(List<BlockPos> positions, int vertR, int horizR, int yLevel, int centerX, int centerZ)
    {
        getPositionsOnPlaneSpiralingOutwards(positions, horizR, yLevel, centerX, centerZ);

        for (int y = 1; y <= vertR; y++)
        {
            getPositionsOnPlaneSpiralingOutwards(positions, horizR, yLevel + y, centerX, centerZ);
            getPositionsOnPlaneSpiralingOutwards(positions, horizR, yLevel - y, centerX, centerZ);
        }
    }

    public static void getPositionsOnPlaneSpiralingOutwards(List<BlockPos> positions, int radius, int yLevel, int centerX, int centerZ)
    {
        positions.add(new BlockPos(centerX, yLevel, centerZ));

        for (int r = 1; r <= radius; r++)
        {
            getPositionsOnRing(positions, r, yLevel, centerX, centerZ);
        }

    }

    public static void getPositionsOnRing(List<BlockPos> positions, int radius, int yLevel, int centerX, int centerZ)
    {
        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;

        for (int x = minX; x <= maxX; x++)
        {
            positions.add(new BlockPos(x, yLevel, minZ));
        }

        for (int z = minZ + 1; z <= maxZ; z++)
        {
            positions.add(new BlockPos(maxX, yLevel, z));
        }

        for (int x = maxX - 1; x >= minX; x--)
        {
            positions.add(new BlockPos(x, yLevel, maxZ));
        }

        for (int z = maxZ - 1; z > minZ; z--)
        {
            positions.add(new BlockPos(minX, yLevel, z));
        }
    }
}
