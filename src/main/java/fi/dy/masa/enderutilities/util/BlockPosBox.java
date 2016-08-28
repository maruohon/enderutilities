package fi.dy.masa.enderutilities.util;

import net.minecraft.util.math.BlockPos;

public class BlockPosBox
{
    public final BlockPos posMin;
    public final BlockPos posMax;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final int sizeLayer;
    public final int count;

    public BlockPosBox(BlockPosEU pos1, BlockPosEU pos2)
    {
        this(pos1.toBlockPos(), pos2.toBlockPos());
    }

    public BlockPosBox(BlockPos pos1, BlockPos pos2)
    {
        this.posMin = PositionUtils.getMinCorner(pos1, pos2);
        this.posMax = PositionUtils.getMaxCorner(pos1, pos2);
        this.sizeX = this.posMax.getX() - this.posMin.getX() + 1;
        this.sizeY = this.posMax.getY() - this.posMin.getY() + 1;
        this.sizeZ = this.posMax.getZ() - this.posMin.getZ() + 1;
        this.sizeLayer = this.sizeX * this.sizeZ;
        this.count = this.sizeX * this.sizeY * this.sizeZ;
    }

    public boolean containsPosition(BlockPos pos)
    {
        return pos.getX() >= this.posMin.getX() && pos.getX() <= this.posMax.getX() &&
                pos.getY() >= this.posMin.getY() && pos.getY() <= this.posMax.getY() &&
                pos.getZ() >= this.posMin.getZ() && pos.getZ() <= this.posMax.getZ();
    }

    public BlockPos getPosAtIndex(int index)
    {
        if (index < 0 || index >= this.count)
        {
            return null;
        }

        return this.posMin.add(index % this.sizeX, index / this.sizeLayer, (index / this.sizeX) % this.sizeZ);
    }

    @Override
    public String toString()
    {
        return "BlockPosBox{min=" + this.posMin + ",max=" + this.posMax +
                ",sizeX=" + this.sizeX + ",sizeY=" + this.sizeY + ",sizeZ=" + this.sizeZ + ",count=" + this.count + "}";
    }
}
