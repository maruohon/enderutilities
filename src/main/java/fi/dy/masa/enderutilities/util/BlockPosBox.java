package fi.dy.masa.enderutilities.util;

import net.minecraft.util.math.BlockPos;

public class BlockPosBox
{
    public final BlockPos posMin;
    public final BlockPos posMax;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final int count;

    public BlockPosBox(BlockPos pos1, BlockPos pos2)
    {
        this.posMin = PositionUtils.getMinCorner(pos1, pos2);
        this.posMax = PositionUtils.getMaxCorner(pos1, pos2);
        this.sizeX = this.posMax.getX() - this.posMin.getX() + 1;
        this.sizeY = this.posMax.getY() - this.posMin.getY() + 1;
        this.sizeZ = this.posMax.getZ() - this.posMin.getZ() + 1;
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

        int sizeLayer = this.sizeX * this.sizeZ;

        return new BlockPos(index % this.sizeX, index / sizeLayer, (index / this.sizeX) % this.sizeZ);
    }
}
