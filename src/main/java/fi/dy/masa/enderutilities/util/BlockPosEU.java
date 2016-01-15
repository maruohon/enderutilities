package fi.dy.masa.enderutilities.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import net.minecraftforge.common.util.ForgeDirection;

public class BlockPosEU
{
    public int posX;
    public int posY;
    public int posZ;
    public int face;

    public BlockPosEU(int x, int y, int z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        //this.clampCoords();
    }

    public BlockPosEU(BlockPosEU old)
    {
        this.posX = old.posX;
        this.posY = old.posY;
        this.posZ = old.posZ;
    }

    public BlockPosEU(BlockPosEU old, ForgeDirection dir, int distance)
    {
        this(old);
        this.offset(dir, distance);
    }

    public void add(int x, int y, int z)
    {
        this.posX += x;
        this.posY += y;
        this.posZ += z;

        //this.clampCoords();
    }

    public void offset(ForgeDirection dir, int distance)
    {
        this.posX += dir.offsetX * distance;
        this.posY += dir.offsetY * distance;
        this.posZ += dir.offsetZ * distance;
    }

    public void clampCoords()
    {
        this.posX = MathHelper.clamp_int(this.posX, -30000000, 30000000);
        this.posY = MathHelper.clamp_int(this.posY, 0, 255);
        this.posZ = MathHelper.clamp_int(this.posZ, -30000000, 30000000);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("posX", this.posX);
        tag.setInteger("posY", this.posY);
        tag.setInteger("posZ", this.posZ);
        tag.setInteger("face", this.face);

        nbt.setTag("BlockPos", tag);

        return nbt;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + posX;
        result = prime * result + posY;
        result = prime * result + posZ;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BlockPosEU other = (BlockPosEU) obj;
        if (posX != other.posX) return false;
        if (posY != other.posY) return false;
        if (posZ != other.posZ) return false;
        return true;
    }
}
