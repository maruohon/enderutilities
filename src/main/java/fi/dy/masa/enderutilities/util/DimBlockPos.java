package fi.dy.masa.enderutilities.util;

public class DimBlockPos extends BlockPos
{
    public int dimension;

    public DimBlockPos(int dim, int x, int y, int z)
    {
        super(x, y, z);
        this.dimension = dim;
    }

    public DimBlockPos(int dim, BlockPos pos)
    {
        super(pos);
        this.dimension = dim;
    }

    public DimBlockPos(DimBlockPos old)
    {
        super(old.posX, old.posY, old.posZ);
        this.dimension = old.dimension;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + dimension;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        DimBlockPos other = (DimBlockPos) obj;
        if (dimension != other.dimension) return false;
        return true;
    }
}
