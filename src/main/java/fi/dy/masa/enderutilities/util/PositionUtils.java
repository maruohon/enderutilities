package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PositionUtils
{
    public static final EnumFacing[] ADJACENT_SIDES_ZY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
    public static final EnumFacing[] ADJACENT_SIDES_XY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST };
    public static final EnumFacing[] ADJACENT_SIDES_XZ = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };

    public static EnumFacing[] getSidesForAxis(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return ADJACENT_SIDES_ZY;
        }

        return axis == EnumFacing.Axis.Z ? ADJACENT_SIDES_XY : ADJACENT_SIDES_XZ;
    }

    public static BlockPos getAreaSizeFromRelativeEndPosition(BlockPos posEnd)
    {
        int x = posEnd.getX();
        int y = posEnd.getY();
        int z = posEnd.getZ();

        x = x >= 0 ? x + 1 : x - 1;
        y = y >= 0 ? y + 1 : y - 1;
        z = z >= 0 ? z + 1 : z - 1;

        return new BlockPos(x, y, z);
    }

    public static BlockPos getMinCorner(BlockPos pos1, BlockPos pos2)
    {
        return new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getMaxCorner(BlockPos pos1, BlockPos pos2)
    {
        return new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    public static BlockPos getPositionInfrontOfEntity(Entity entity)
    {
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);

        if (entity.rotationPitch >= 80)
        {
            return pos.down();
        }
        else if (entity.rotationPitch <= -80)
        {
            return pos.up((int)Math.ceil(entity.getEntityBoundingBox().maxY - entity.posY) + 1);
        }

        EnumFacing facing = entity.getHorizontalFacing();

        return pos.up().offset(facing, 2);
    }

    public static Rotation getRotationFromMirror(EnumFacing facingIn, Mirror mirror, EnumFacing.Axis mirrorAxis)
    {
        EnumFacing.Axis facingAxis = facingIn.getAxis();

        if (facingAxis.isHorizontal() == false)
        {
            return Rotation.NONE;
        }

        if (facingAxis == mirrorAxis)
        {
            return mirror == Mirror.FRONT_BACK ? Rotation.CLOCKWISE_180 : Rotation.NONE;
        }

        return mirror == Mirror.LEFT_RIGHT ? Rotation.CLOCKWISE_180 : Rotation.NONE;
    }

    public static EnumFacing getMirroredFacing(EnumFacing facingIn, Mirror mirror, EnumFacing.Axis mirrorAxis)
    {
        EnumFacing.Axis facingAxis = facingIn.getAxis();

        if (facingAxis.isHorizontal() == false)
        {
            return facingIn;
        }

        if (facingAxis == mirrorAxis)
        {
            return mirror == Mirror.FRONT_BACK ? facingIn.getOpposite() : facingIn;
        }

        return mirror == Mirror.LEFT_RIGHT ? facingIn.getOpposite() : facingIn;
    }

    /**
     * Rotates the given position around the origin
     */
    public static BlockPos getTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isMirrored = true;

        switch (mirror)
        {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotation)
        {
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
                return isMirrored ? new BlockPos(x, y, z) : pos;
        }
    }

    public static BlockPosEU getTransformedBlockPos(BlockPosEU pos, Mirror mirror, Rotation rotation)
    {
        int x = pos.posX;
        int y = pos.posY;
        int z = pos.posZ;
        boolean isMirrored = true;

        switch (mirror)
        {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotation)
        {
            case CLOCKWISE_90:
                return new BlockPosEU(-z, y, x, pos.dimension, pos.face);
            case COUNTERCLOCKWISE_90:
                return new BlockPosEU(z, y, -x, pos.dimension, pos.face);
            case CLOCKWISE_180:
                return new BlockPosEU(-x, y, -z, pos.dimension, pos.face);
            default:
                return isMirrored ? new BlockPosEU(x, y, z, pos.dimension, pos.face) : pos;
        }
    }

    public static Vec3d transformedVec3d(Vec3d vec, Mirror mirrorIn, Rotation rotationIn)
    {
        double x = vec.xCoord;
        double y = vec.yCoord;
        double z = vec.zCoord;
        boolean isMirrored = true;

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                z = 1.0D - z;
                break;
            case FRONT_BACK:
                x = 1.0D - x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotationIn)
        {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(z, y, 1.0D - x);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - z, y, x);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - x, y, 1.0D - z);
            default:
                return isMirrored ? new Vec3d(x, y, z) : vec;
        }
    }

    /**
     * Returns the MutableBlockPos <b>pos</b> with a position set to <b>posReference</b> offset by <b>amount</b> in the direction <b>side</b>.
     */
    public static MutableBlockPos getOffsetPosition(MutableBlockPos pos, BlockPos posReference, EnumFacing side, int amount)
    {
        switch (side)
        {
            case NORTH:
                pos.set(posReference.getX(), posReference.getY(), posReference.getZ() - amount);
            case SOUTH:
                pos.set(posReference.getX(), posReference.getY(), posReference.getZ() + amount);
            case EAST:
                pos.set(posReference.getX() + amount, posReference.getY(), posReference.getZ());
            case WEST:
                pos.set(posReference.getX() - amount, posReference.getY(), posReference.getZ());
            case UP:
                pos.set(posReference.getX(), posReference.getY() + amount, posReference.getZ());
            case DOWN:
                pos.set(posReference.getX(), posReference.getY() - amount, posReference.getZ());
        }

        return pos;
    }

    public static Rotation getRotation(EnumFacing facingOriginal, EnumFacing facingRotated)
    {
        if (facingOriginal.getAxis() == EnumFacing.Axis.Y ||
            facingRotated.getAxis() == EnumFacing.Axis.Y || facingOriginal == facingRotated)
        {
            return Rotation.NONE;
        }

        if (facingRotated == facingOriginal.getOpposite())
        {
            return Rotation.CLOCKWISE_180;
        }

        return facingRotated == facingOriginal.rotateY() ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
    }

    public static boolean isWithinRange(BlockPos pos, Entity entity, int rangeH, int rangeV)
    {
        return isWithinRange(pos, entity.posX, entity.posY, entity.posZ, rangeH, rangeV);
    }

    public static boolean isWithinRange(BlockPos pos, double x, double y, double z, int rangeH, int rangeV)
    {
        return Math.abs(pos.getX() - x + 0.5) <= rangeH &&
               Math.abs(pos.getZ() - z + 0.5) <= rangeH &&
               Math.abs(pos.getY() - y + 0.5) <= rangeV;
    }

    public static boolean isWithinRange(BlockPos pos1, BlockPos pos2, int rangeH, int rangeVertPos, int rangeVertNeg)
    {
        return Math.abs(pos2.getX() - pos1.getX()) <= rangeH &&
               Math.abs(pos2.getZ() - pos1.getZ()) <= rangeH &&
               (pos2.getY() - pos1.getY()) <= rangeVertPos && (pos1.getY() - pos2.getY()) <= rangeVertNeg;
    }

    /**
     * Gets a list of all the TileEntities within the given range of the center position.
     * The list is sorted by distance to the center position.
     * @param world
     * @param centerPos
     * @param rangeH
     * @param rangeVertPos the range upwards from the center position
     * @param rangeVertNeg the range downwards from the center position
     * @return
     */
    public static List<BlockPosDistance> getTileEntityPositions(World world, BlockPos centerPos, int rangeH, int rangeVertPos, int rangeVertNeg)
    {
        return getTileEntityPositions(world, centerPos, rangeH, rangeVertPos, rangeVertNeg, null);
    }

    public static List<BlockPosDistance> getTileEntityPositions(World world, BlockPos centerPos, int rangeH,
            int rangeVertPos, int rangeVertNeg, Predicate <? super TileEntity> filter)
    {
        List<BlockPosDistance> posDist = new ArrayList<BlockPosDistance>();

        for (int cx = (centerPos.getX() - rangeH) >> 4; cx <= ((centerPos.getX() + rangeH) >> 4); cx++)
        {
            for (int cz = (centerPos.getZ() - rangeH) >> 4; cz <= ((centerPos.getZ() + rangeH) >> 4); cz++)
            {
                if (world.isBlockLoaded(new BlockPos(cx << 4, centerPos.getY(), cz << 4), world.isRemote) == false)
                {
                    continue;
                }

                Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
                if (chunk != null)
                {
                    Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();

                    for (BlockPos pos : map.keySet())
                    {
                        if (PositionUtils.isWithinRange(centerPos, pos, rangeH, rangeVertPos, rangeVertNeg) &&
                            (filter == null || filter.apply(map.get(pos))))
                        {
                            posDist.add(new BlockPosDistance(pos, centerPos));
                        }
                    }
                }
            }
        }

        Collections.sort(posDist);

        return posDist;
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
