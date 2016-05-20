package fi.dy.masa.enderutilities.util;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.common.FMLCommonHandler;

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

    /**
     * Returns the player's position scaled by the given scale factors, and clamped to within the world border
     * of the destination world, with the given margin to the border
     */
    public static Vec3d getScaledClampedPosition(EntityPlayer player, int destDimension, double scaleX, double scaleY, double scaleZ, int margin)
    {
        // FIXME: for some reason the world border in the Nether always reads as 60M...
        // So we are just getting the border size in the Overworld for now
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
        int worldLimit = 29999984;
        double posX = MathHelper.clamp_double(player.posX * scaleX, -worldLimit, worldLimit);
        double posY = MathHelper.clamp_double(player.posY * scaleY, 0, world != null ? world.getActualHeight() - 1 : 255);
        double posZ = MathHelper.clamp_double(player.posZ * scaleZ, -worldLimit, worldLimit);

        if (world != null)
        {
            WorldBorder border = world.getWorldBorder();
            margin = Math.min(margin, (int)(border.getDiameter() / 2));

            posX = MathHelper.clamp_double(player.posX * scaleX, border.minX() + margin, border.maxX() - margin);
            posZ = MathHelper.clamp_double(player.posZ * scaleZ, border.minZ() + margin, border.maxZ() - margin);
            //System.out.printf("border - size: %.4f posX: %.4f posY: %.4f posZ: %.4f\n", border.getDiameter(), posX, posY, posZ);
        }

        return new Vec3d(posX, posY, posZ);
    }
}
