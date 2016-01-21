package fi.dy.masa.enderutilities.util;

public class BlockPosDistance extends BlockPosEU implements Comparable<BlockPosDistance>
{
    public double distance;

    public BlockPosDistance(BlockPosEU pos)
    {
        this(pos.posX, pos.posY, pos.posZ, pos.dimension, pos.face);
    }

    public BlockPosDistance(int x, int y, int z, int dim, int face)
    {
        super(x, y, z, dim, face);
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
