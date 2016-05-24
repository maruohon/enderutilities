package fi.dy.masa.enderutilities.util;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PositionUtils
{
    public static final int[] CORNER_ROTATIONS = new int[] { };

    public static BlockPos getPosOffset(BlockPosEU pos1, BlockPosEU posFrom)
    {
        return new BlockPos(posFrom.posX - pos1.posX, posFrom.posY - pos1.posY, posFrom.posZ - pos1.posZ);
    }

    public static BlockPos getPosOffset(BlockPos posWhat, BlockPos posFrom)
    {
        return new BlockPos(posFrom.getX() - posWhat.getX(), posFrom.getY() - posWhat.getY(), posFrom.getZ() - posWhat.getZ());
    }

    public static BlockPos getAreaSize(BlockPosEU pos1, BlockPosEU pos2)
    {
        return new BlockPos(pos2.posX - pos1.posX + 1, pos2.posY - pos1.posY + 1, pos2.posZ - pos1.posZ + 1);
    }

    public static BlockPos getAreaSize(BlockPos pos1, BlockPos pos2)
    {
        return new BlockPos(pos2.getX() - pos1.getX() + 1, pos2.getY() - pos1.getY() + 1, pos2.getZ() - pos1.getZ() + 1);
    }

    public static BlockPos getMinCorner(BlockPos pos1, BlockPos pos2)
    {
        return new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getMaxCorner(BlockPos pos1, BlockPos pos2)
    {
        return new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    /**
     * Rotates the given position around the origin
     */
    public static BlockPos getTransformedPosition(BlockPos pos, Rotation rotation)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        switch (rotation)
        {
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
        }

        return pos;
    }

    public static Rotation getRotation(EnumFacing facingOriginal, EnumFacing facingRotated)
    {
        if (facingOriginal.getAxis() == EnumFacing.Axis.Y || facingOriginal == facingRotated)
        {
            return Rotation.NONE;
        }

        if (facingRotated == facingOriginal.getOpposite())
        {
            return Rotation.CLOCKWISE_180;
        }

        return facingRotated == facingOriginal.rotateY() ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
    }

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
