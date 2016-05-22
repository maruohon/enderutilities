package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class BlockPosDistance implements Comparable<BlockPosDistance>
{
    public final BlockPos pos;
    public final double distance;

    public BlockPosDistance(BlockPos pos, Entity entity)
    {
        this(pos, entity.posX, entity.posY, entity.posZ);
    }

    public BlockPosDistance(BlockPos pos, double x, double y, double z)
    {
        this.pos = pos;
        this.distance = this.getSquaredCenterDistanceFrom(x, y, z);
    }

    public double getSquaredCenterDistanceFrom(double x, double y, double z)
    {
        return this.getSquaredDistanceFrom(x - 0.5, y - 0.5, z - 0.5);
    }

    public double getSquaredDistanceFrom(double x, double y, double z)
    {
        return (x - this.pos.getX()) * (x - this.pos.getX()) +
               (y - this.pos.getY()) * (y - this.pos.getY()) +
               (z - this.pos.getZ()) * (z - this.pos.getZ());
    }

    @Override
    public int compareTo(BlockPosDistance other)
    {
        if (other == null)
        {
            throw new NullPointerException();
        }

        if (this.distance != other.distance)
        {
            return (this.distance - other.distance) > 0 ? 1 : -1;
        }

        return 0;
    }
}
