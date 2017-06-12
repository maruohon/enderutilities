package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class PositionUtils
{
    public static final AxisAlignedBB ZERO_BB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    public static final EnumFacing[] ADJACENT_SIDES_ZY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
    public static final EnumFacing[] ADJACENT_SIDES_XY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST };
    public static final EnumFacing[] ADJACENT_SIDES_XZ = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };
    // These are additional offsets from the corresponding sides in ADJACENT_SIDES_XXX
    public static final EnumFacing[] CORNERS_ZY = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.UP };
    public static final EnumFacing[] CORNERS_XY = new EnumFacing[] { EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP };
    public static final EnumFacing[] CORNERS_XZ = new EnumFacing[] { EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH };

    public static final EnumFacing[][] FROM_TO_CW_ROTATION_AXES = new EnumFacing[][] {
        { null, null, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH }, // from down
        { null, null, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH }, // from up
        { EnumFacing.EAST, EnumFacing.WEST, null, null, EnumFacing.DOWN, EnumFacing.UP }, // from north
        { EnumFacing.WEST, EnumFacing.EAST, null, null, EnumFacing.UP, EnumFacing.DOWN }, // from south
        { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN, null, null }, // from west
        { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.UP, null, null } // from east
    };

    public static EnumFacing[] getSidesForAxis(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return ADJACENT_SIDES_ZY;
        }

        return axis == EnumFacing.Axis.Z ? ADJACENT_SIDES_XY : ADJACENT_SIDES_XZ;
    }

    private static EnumFacing[] getCornersForAxis(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return CORNERS_ZY;
        }

        return axis == EnumFacing.Axis.Z ? CORNERS_XY : CORNERS_XZ;
    }

    public static BlockPosEU[] getAdjacentPositions(BlockPosEU center, EnumFacing front, boolean diagonals)
    {
        if (diagonals)
        {
            BlockPosEU[] positions = new BlockPosEU[8];
            EnumFacing[] corners = getCornersForAxis(front.getAxis());
            int i = 0;

            for (EnumFacing side : getSidesForAxis(front.getAxis()))
            {
                positions[i    ] = center.offset(side);
                positions[i + 1] = positions[i].offset(corners[i / 2]);
                i += 2;
            }

            return positions;
        }
        else
        {
            BlockPosEU[] positions = new BlockPosEU[4];
            int i = 0;

            for (EnumFacing side : getSidesForAxis(front.getAxis()))
            {
                positions[i++] = center.offset(side);
            }

            return positions;
        }
    }

    public static BlockPos[] getAdjacentPositions(BlockPos center, EnumFacing front, boolean diagonals)
    {
        if (diagonals)
        {
            BlockPos[] positions = new BlockPos[8];
            EnumFacing[] corners = getCornersForAxis(front.getAxis());
            int i = 0;

            for (EnumFacing side : getSidesForAxis(front.getAxis()))
            {
                positions[i    ] = center.offset(side);
                positions[i + 1] = positions[i].offset(corners[i / 2]);
                i += 2;
            }

            return positions;
        }
        else
        {
            BlockPos[] positions = new BlockPos[4];
            int i = 0;

            for (EnumFacing side : getSidesForAxis(front.getAxis()))
            {
                positions[i++] = center.offset(side);
            }

            return positions;
        }
    }

    public static BlockPos getAreaSizeFromRelativeEndPosition(BlockPos posEndRelative)
    {
        int x = posEndRelative.getX();
        int y = posEndRelative.getY();
        int z = posEndRelative.getZ();

        x = x >= 0 ? x + 1 : x - 1;
        y = y >= 0 ? y + 1 : y - 1;
        z = z >= 0 ? z + 1 : z - 1;

        return new BlockPos(x, y, z);
    }

    public static BlockPos getRelativeEndPositionFromAreaSize(BlockPos size)
    {
        int x = size.getX();
        int y = size.getY();
        int z = size.getZ();

        x = x >= 0 ? x - 1 : x + 1;
        y = y >= 0 ? y - 1 : y + 1;
        z = z >= 0 ? z - 1 : z + 1;

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

    public static boolean isPositionInsideArea(BlockPos pos, BlockPos posMin, BlockPos posMax)
    {
        return pos.getX() >= posMin.getX() && pos.getX() <= posMax.getX() &&
               pos.getY() >= posMin.getY() && pos.getY() <= posMax.getY() &&
               pos.getZ() >= posMin.getZ() && pos.getZ() <= posMax.getZ();
    }

    public static boolean isPositionInsideArea(BlockPosEU pos, BlockPos posMin, BlockPos posMax)
    {
        return pos.getX() >= posMin.getX() && pos.getX() <= posMax.getX() &&
               pos.getY() >= posMin.getY() && pos.getY() <= posMax.getY() &&
               pos.getZ() >= posMin.getZ() && pos.getZ() <= posMax.getZ();
    }

    public static boolean isPositionValid(BlockPosEU pos)
    {
        World world = DimensionManager.getWorld(pos.getDimension());
        return world != null && pos.getY() >= 0 && pos.getY() < world.getHeight() && world.getWorldBorder().contains(pos.toBlockPos());
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
            return new BlockPos(entity.posX, Math.ceil(entity.getEntityBoundingBox().maxY), entity.posZ);
        }

        double y = Math.floor(entity.posY + entity.getEyeHeight());

        switch (entity.getHorizontalFacing())
        {
            case EAST:
                return new BlockPos((int) Math.ceil( entity.posX + entity.width / 2),     (int) y, (int) Math.floor(entity.posZ));
            case WEST:
                return new BlockPos((int) Math.floor(entity.posX - entity.width / 2) - 1, (int) y, (int) Math.floor(entity.posZ));
            case SOUTH:
                return new BlockPos((int) Math.floor(entity.posX), (int) y, (int) Math.ceil( entity.posZ + entity.width / 2)    );
            case NORTH:
                return new BlockPos((int) Math.floor(entity.posX), (int) y, (int) Math.floor(entity.posZ - entity.width / 2) - 1);
            default:
        }

        return pos;
    }

    public static Rotation getReverseRotation(Rotation rotation)
    {
        switch (rotation)
        {
            case CLOCKWISE_90:          return Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90:   return Rotation.CLOCKWISE_90;
            case CLOCKWISE_180:         return Rotation.CLOCKWISE_180;
            default:                    return Rotation.NONE;
        }
    }

    /**
     * Turns a mirror value into a rotation that will result in the same transform
     * in the <b>facingIn</b> facing. If the axis of <b>facingIn</b> is vertical, then NONE is returned.
     */
    /*public static Rotation getRotationFromMirror(EnumFacing facingIn, Mirror mirror, EnumFacing.Axis mirrorAxis)
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
    }*/

    /**
     * Returns the facing that is mirrored by the value <b>mirror</b>
     * in relation to the mirror axis <b>mirrorAxis</b>.
     * So if the original facing is along the mirror axis, then
     * FRONT_BACK mirror will return the opposite facing.
     * If the original facing is NOT on the mirror axis, then LEFT_RIGHT
     * mirror will return the opposite facing.
     */
    /*public static EnumFacing getMirroredFacing(EnumFacing facingIn, Mirror mirror, EnumFacing.Axis mirrorAxis)
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
    }*/

    /**
     * Mirrors and then rotates the given position around the origin
     */
    public static BlockPos getTransformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isMirrored = true;

        switch (mirror)
        {
            // LEFT_RIGHT is essentially NORTH_SOUTH
            case LEFT_RIGHT:
                z = -z;
                break;
            // FRONT_BACK is essentially EAST_WEST
            case FRONT_BACK:
                x = -x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotation)
        {
            case CLOCKWISE_90:
                return new BlockPos(-z, y,  x);
            case COUNTERCLOCKWISE_90:
                return new BlockPos( z, y, -x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
                return isMirrored ? new BlockPos(x, y, z) : pos;
        }
    }

    /**
     * Mirrors and then rotates the given position around the origin
     */
    public static BlockPosEU getTransformedBlockPos(BlockPosEU pos, Mirror mirror, Rotation rotation)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean isMirrored = true;

        switch (mirror)
        {
            // LEFT_RIGHT is essentially NORTH_SOUTH
            case LEFT_RIGHT:
                z = -z;
                break;
            // FRONT_BACK is essentially EAST_WEST
            case FRONT_BACK:
                x = -x;
                break;
            default:
                isMirrored = false;
        }

        switch (rotation)
        {
            case CLOCKWISE_90:
                return new BlockPosEU(-z, y,  x, pos.getDimension(), pos.getFacing());
            case COUNTERCLOCKWISE_90:
                return new BlockPosEU( z, y, -x, pos.getDimension(), pos.getFacing());
            case CLOCKWISE_180:
                return new BlockPosEU(-x, y, -z, pos.getDimension(), pos.getFacing());
            default:
                return isMirrored ? new BlockPosEU(x, y, z, pos.getDimension(), pos.getFacing()) : pos;
        }
    }

    /**
     * Does the opposite transform from getTransformedBlockPos(), to return the original,
     * non-transformed position from the transformed position.
     */
    public static BlockPosEU getOriginalPositionFromTransformed(BlockPosEU pos, Mirror mirror, Rotation rotation)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int tmp;
        boolean noRotation = false;

        switch (rotation)
        {
            case CLOCKWISE_90:
                tmp = x;
                x = -z;
                z = tmp;
            case COUNTERCLOCKWISE_90:
                tmp = x;
                x = z;
                z = -tmp;
            case CLOCKWISE_180:
                x = -x;
                z = -z;
            default:
                noRotation = true;
        }

        switch (mirror)
        {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
            default:
                if (noRotation)
                {
                    return pos;
                }
        }

        return new BlockPosEU(x, y, z, pos.getDimension(), pos.getFacing());
    }

    public static Vec3d transformedVec3d(Vec3d vec, Mirror mirrorIn, Rotation rotationIn)
    {
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
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
     * Gets the "front" facing from the given positions,
     * so that pos1 is in the "front left" corner and pos2 is in the "back right" corner
     * of the area, when looking at the "front" face of the area.
     */
    public static  EnumFacing getFacingFromPositions(BlockPosEU pos1, BlockPosEU pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return null;
        }

        return getFacingFromPositions(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());
    }

    /**
     * Gets the "front" facing from the given positions,
     * so that pos1 is in the "front left" corner and pos2 is in the "back right" corner
     * of the area, when looking at the "front" face of the area.
     */
    public static EnumFacing getFacingFromPositions(BlockPos pos1, BlockPos pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return null;
        }

        return getFacingFromPositions(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());
    }

    private static EnumFacing getFacingFromPositions(int x1,int z1, int x2, int z2)
    {
        if (x2 == x1)
        {
            return z2 > z1 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }

        if (z2 == z1)
        {
            return x2 > x1 ? EnumFacing.EAST : EnumFacing.WEST;
        }

        if (x2 > x1)
        {
            return z2 > z1 ? EnumFacing.EAST : EnumFacing.NORTH;
        }

        return z2 > z1 ? EnumFacing.SOUTH : EnumFacing.WEST;
    }

    public static Vec3d rotatePointAroundAxis(Vec3d point, Vec3d reference, EnumFacing from, EnumFacing to)
    {
        if (from == to)
        {
            return point;
        }

        return rotatePointAroundAxis(point.x, point.y, point.z, reference, from, to);
    }

    public static Vec3d rotatePointAroundAxis(double x, double y, double z, Vec3d reference, EnumFacing from, EnumFacing to)
    {
        if (to == from.getOpposite())
        {
            double rx = reference.x;

            if (from.getAxis().isHorizontal())
            {
                //System.out.printf("rotatePointAroundAxis - opposite, horizontal, from: %s to: %s\n", from, to);
                double rz = reference.z;
                x = rx + (rx - x);
                z = rz + (rz - z);
            }
            // Rotate around the z-axis when the to/from axes are vertical
            else
            {
                //System.out.printf("rotatePointAroundAxis - opposite, vertical, from: %s to: %s\n", from, to);
                double ry = reference.y;
                x = rx + (rx - x);
                y = ry + (ry - y);
            }

            return new Vec3d(x, y, z);
        }

        return rotatePointCWAroundAxis(x, y, z, reference, FROM_TO_CW_ROTATION_AXES[from.getIndex()][to.getIndex()]);
    }

    public static Vec3d rotatePointCWAroundAxis(Vec3d point, Vec3d reference, EnumFacing facing)
    {
        return rotatePointCWAroundAxis(point.x, point.y, point.z, reference, facing);
    }

    public static Vec3d rotatePointCWAroundAxis(double x, double y, double z, Vec3d reference, EnumFacing facing)
    {
        //System.out.printf("rotatePointCWAroundAxis - axis: %s, ref: %s, x: %.4f, y: %.4f, z: %.4f -> ", facing, reference, x, y, z);
        //System.out.printf("rotatePointCWAroundAxis - axis: %s, ref: %s, vec: %s -> ", facing, reference, new Vec3d(x, y, z));
        //System.out.printf("rotatePointCWAroundAxis - axis: %s\n", facing);
        double rx = reference.x;
        double ry = reference.y;
        double rz = reference.z;
        double newX = x;
        double newY = y;
        double newZ = z;

        if (facing.getAxis() == EnumFacing.Axis.Y)
        {
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
            {
                newX = rx - (z - rz);
                newZ = rz + (x - rx);
            }
            else
            {
                newX = rx + (z - rz);
                newZ = rz - (x - rx);
            }
        }
        else if (facing.getAxis() == EnumFacing.Axis.Z)
        {
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
            {
                newX = rx + (y - ry);
                newY = ry - (x - rx);
            }
            else
            {
                newX = rx - (y - ry);
                newY = ry + (x - rx);
            }
        }
        else if (facing.getAxis() == EnumFacing.Axis.X)
        {
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
            {
                newZ = rz - (y - ry);
                newY = ry + (z - rz);
            }
            else
            {
                newZ = rz + (y - ry);
                newY = ry - (z - rz);
            }
        }

        //System.out.printf("x: %.4f, y: %.4f, z: %.4f\n", newX, newY, newZ);
        //System.out.printf("vec: %s\n", new Vec3d(newX, newY, newZ));
        return new Vec3d(newX, newY, newZ);
    }

    public static AxisAlignedBB rotateBoxAroundPoint(AxisAlignedBB bb, Vec3d reference, EnumFacing from, EnumFacing to)
    {
        if (from == to)
        {
            return bb;
        }

        Vec3d min = rotatePointAroundAxis(bb.minX, bb.minY, bb.minZ, reference, from, to);
        Vec3d max = rotatePointAroundAxis(bb.maxX, bb.maxY, bb.maxZ, reference, from, to);

        //System.out.printf("rotateBoxAroundPoint - from: %s to: %s ref: %s bb: %s, min: %s, max: %s\n", from, to, reference, bb, min, max);
        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    /**
     * Returns the MutableBlockPos <b>pos</b> with a position set to <b>posReference</b> offset by <b>amount</b> in the direction <b>side</b>.
     */
    public static MutableBlockPos getOffsetPosition(MutableBlockPos pos, BlockPos posReference, EnumFacing side, int amount)
    {
        switch (side)
        {
            case NORTH:
                pos.setPos(posReference.getX(), posReference.getY(), posReference.getZ() - amount);
            case SOUTH:
                pos.setPos(posReference.getX(), posReference.getY(), posReference.getZ() + amount);
            case EAST:
                pos.setPos(posReference.getX() + amount, posReference.getY(), posReference.getZ());
            case WEST:
                pos.setPos(posReference.getX() - amount, posReference.getY(), posReference.getZ());
            case UP:
                pos.setPos(posReference.getX(), posReference.getY() + amount, posReference.getZ());
            case DOWN:
                pos.setPos(posReference.getX(), posReference.getY() - amount, posReference.getZ());
        }

        return pos;
    }

    /**
     * Returns the rotation that would transform <b>facingOriginal</b> into <b>facingRotated</b>.
     * @param facingOriginal
     * @param facingRotated
     * @return
     */
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
     * The list is sorted by distance to the center position (closest one first).
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

    /**
     * Gets a list of all the TileEntities within the given range of the center position.
     * The list is sorted by distance to the center position (closest one first)
     * @param world
     * @param centerPos
     * @param rangeH
     * @param rangeVertPos the range upwards from the center position
     * @param rangeVertNeg the range downwards from the center position
     * @param filter a predicate to check if the found TileEntity is suitable
     * @return
     */
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

    public static TargetData adjustTargetPosition(TargetData target, Entity entity)
    {
        if (target.blockFace < 0)
        {
            return target;
        }

        float widthAdj = entity.width / 2;
        target.dPosX += target.facing.getFrontOffsetX() * widthAdj;
        target.dPosZ += target.facing.getFrontOffsetZ() * widthAdj;

        // Targeting the bottom face of a block, adjust the position lower
        if (target.facing == EnumFacing.DOWN)
        {
            target.dPosY -= entity.height;
        }

        return target;
    }

    /**
     * Returns the given position scaled by the given scale factors, and clamped to within the world border
     * of the destination world, with the given margin to the border.
     */
    public static Vec3d getScaledClampedPosition(Vec3d pos, @Nullable World world, double scaleX, double scaleY, double scaleZ, int margin)
    {
        // The world border in other dimensions than the Overworld gets always
        // reset to the default 60M blocks when the dimensions loads.
        // The client side WorldBorder is synced on world load, so the
        // border _appears_ to be where the Overworld border is,
        // but on the server side the border is actually at 60M blocks,
        // unless the border has been changed using the commands,
        // because the border change listener will then update the border
        // size from the Overworld border to the other dimensions (see WorldServerMulti),
        // however that change only persists as long as the dimension stays loaded.

        // So we are just getting the border size from the Overworld for now...
        world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);

        int worldLimit = 30000000 - margin;
        // Note: getActualHeight() could be better (at least for the Nether), but it would return 128 for the End too...
        int worldHeight = world != null ? world.getHeight() : 256;
        double posX = MathHelper.clamp(pos.x * scaleX, -worldLimit, worldLimit);
        double posY = MathHelper.clamp(pos.y * scaleY, 0, worldHeight);
        double posZ = MathHelper.clamp(pos.z * scaleZ, -worldLimit, worldLimit);

        if (world != null)
        {
            WorldBorder border = world.getWorldBorder();
            margin = Math.min(margin, (int)(border.getDiameter() / 2));

            posX = MathHelper.clamp(pos.x * scaleX, border.minX() + margin, border.maxX() - margin);
            posZ = MathHelper.clamp(pos.z * scaleZ, border.minZ() + margin, border.maxZ() - margin);
            //System.out.printf("border size: %.2f posX: %.4f posY: %.4f posZ: %.4f\n", border.getDiameter(), posX, posY, posZ);
            //System.out.printf("border: %s (%s)\n", border.getClass().getName(), border);
        }

        return new Vec3d(posX, posY, posZ);
    }

    /**
     * Gets the surrounding collision boxes for an EntityItem, when it is being pushed
     * out of blocks. This is identical to the vanilla method other than the vanilla
     * method not passing the entity to the addCollisionBoxToList() method, which
     * prevents having blocks that don't have any collision boxes for EntityItems,
     * when it comes to the pushing-out-of-blocks code.
     * @param world
     * @param bb
     * @param entity
     * @return
     */
    public static List<AxisAlignedBB> getSurroundingCollisionBoxesForEntityItem(World world, AxisAlignedBB bb, @Nullable Entity entity)
    {
        List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        int xMin = MathHelper.floor(bb.minX) - 1;
        int xMax = MathHelper.ceil( bb.maxX) + 1;
        int yMin = MathHelper.floor(bb.minY) - 1;
        int yMax = MathHelper.ceil( bb.maxY) + 1;
        int zMin = MathHelper.floor(bb.minZ) - 1;
        int zMax = MathHelper.ceil( bb.maxZ) + 1;
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();

        for (int x = xMin; x < xMax; x++)
        {
            for (int z = zMin; z < zMax; z++)
            {
                int edges = (x != xMin && x != xMax - 1 ? 0 : 1) + (z != zMin && z != zMax - 1 ? 0 : 1);

                if (edges != 2 && world.isBlockLoaded(pos.setPos(x, 64, z)))
                {
                    for (int y = yMin; y < yMax; y++)
                    {
                        if (edges <= 0 || y != yMin && y != yMax - 1)
                        {
                            pos.setPos(x, y, z);
                            IBlockState state;

                            if (x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000)
                            {
                                state = world.getBlockState(pos);
                            }
                            else
                            {
                                state = Blocks.BEDROCK.getDefaultState();
                            }

                            state.addCollisionBoxToList(world, pos, bb, list, entity, false);
                        }
                    }
                }
            }
        }

        pos.release();

        return list;
    }
}
