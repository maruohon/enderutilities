package fi.dy.masa.enderutilities.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BlockPosStateDist extends BlockPosEU implements Comparable<BlockPosStateDist>
{
    public double distance;
    public BlockInfo blockInfo;

    public BlockPosStateDist(BlockPosEU pos, BlockInfo blockInfo)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), pos.getDimension(), pos.getFacing(), blockInfo);
    }

    public BlockPosStateDist(BlockPos pos, int dim, EnumFacing facing, BlockInfo blockInfo)
    {
        super(pos, dim, facing);
        this.blockInfo = blockInfo;
    }

    public BlockPosStateDist(int x, int y, int z, int dim, EnumFacing facing, BlockInfo blockInfo)
    {
        super(x, y, z, dim, facing);
        this.blockInfo = blockInfo;
    }

    public void setSquaredDistance(double dist)
    {
        this.distance = dist;
    }

    public double getSquaredDistanceFrom(double x, double y, double z)
    {
        return (x - this.getX()) * (x - this.getX()) + (y - this.getY()) * (y - this.getY()) + (z - this.getZ()) * (z - this.getZ());
    }

    @Override
    public int compareTo(BlockPosStateDist other)
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

    @Override
    public String toString()
    {
        String pos = super.toString();
        String block = this.blockInfo.toString();
        return pos + " & " + block;
    }
}
