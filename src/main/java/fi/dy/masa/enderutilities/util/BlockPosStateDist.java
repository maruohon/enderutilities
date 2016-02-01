package fi.dy.masa.enderutilities.util;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockPosStateDist extends BlockPosEU implements Comparable<BlockPosStateDist>
{
    public double distance;
    public BlockInfo blockInfo;

    public BlockPosStateDist(BlockPosEU pos, BlockInfo blockInfo)
    {
        this(pos.posX, pos.posY, pos.posZ, pos.dimension, pos.face, blockInfo);
    }

    public BlockPosStateDist(BlockPos pos, int dim, EnumFacing side, BlockInfo blockInfo)
    {
        super(pos, dim, side);
        this.blockInfo = blockInfo;
    }

    public BlockPosStateDist(int x, int y, int z, int dim, int face, BlockInfo blockInfo)
    {
        super(x, y, z, dim, face);
        this.blockInfo = blockInfo;
    }

    public void setSquaredDistance(double dist)
    {
        this.distance = dist;
    }

    public double getSquaredDistanceFrom(double x, double y, double z)
    {
        return (x - this.posX) * (x - this.posX) + (y - this.posY) * (y - this.posY) + (z - this.posZ) * (z - this.posZ);
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
}
