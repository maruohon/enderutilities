package fi.dy.masa.enderutilities.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

public class BlockPosEU
{
    public final int posX;
    public final int posY;
    public final int posZ;
    public final int dimension;
    public final int face;
    public final EnumFacing side;

    public BlockPosEU(BlockPos pos)
    {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPosEU(int x, int y, int z)
    {
        this(x, y, z, 0, 0);
    }

    public BlockPosEU(int x, int y, int z, int dim)
    {
        this(x, y, z, dim, 0);
    }

    public BlockPosEU(BlockPos pos, int dim, int side)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dim, side);
    }

    public BlockPosEU(BlockPos pos, int dim, EnumFacing side)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dim, side.getIndex());
    }

    public BlockPosEU(int x, int y, int z, int dim, int face)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dimension = dim;
        this.face = face;
        this.side = EnumFacing.getFront(face);
    }

    /**
     * Add the given offsets to the position.
     * Returns a new instance with the changes applied and does not modify the original.
     */
    public BlockPosEU add(int x, int y, int z)
    {
        return new BlockPosEU(this.posX + x, this.posY + y, this.posZ + z, this.dimension, this.face);
    }

    public BlockPosEU subtract(BlockPosEU other)
    {
        if (other.posX == 0 && other.posY == 0 && other.posZ == 0)
        {
            return this;
        }

        return new BlockPosEU(this.posX - other.posX, this.posY - other.posY, this.posZ - other.posZ);
    }

    /**
     * Offset the position by the given amount into the given direction.
     * Returns a new instance with the changes applied and does not modify the original.
     */
    public BlockPosEU offset(EnumFacing facing, int distance)
    {
        return new BlockPosEU(  this.posX + facing.getFrontOffsetX() * distance,
                                this.posY + facing.getFrontOffsetY() * distance,
                                this.posZ + facing.getFrontOffsetZ() * distance,
                                this.dimension, this.face);
    }

    public BlockPosEU clampCoords()
    {
        int x = MathHelper.clamp_int(this.posX, -30000000, 30000000);
        int y = MathHelper.clamp_int(this.posY, 0, 255);
        int z = MathHelper.clamp_int(this.posZ, -30000000, 30000000);

        return new BlockPosEU(x, y, z, this.dimension, this.face);
    }

    public BlockPos toBlockPos()
    {
        return new BlockPos(this.posX, this.posY, this.posZ);
    }

    public NBTTagCompound writeToTag(NBTTagCompound tag)
    {
        tag.setInteger("posX", this.posX);
        tag.setInteger("posY", this.posY);
        tag.setInteger("posZ", this.posZ);
        tag.setInteger("dim", this.dimension);
        tag.setByte("face", (byte)this.face);

        return tag;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setTag("BlockPos", this.writeToTag(new NBTTagCompound()));

        return nbt;
    }

    public static BlockPosEU readFromTag(NBTTagCompound tag)
    {
        if (tag == null ||
            tag.hasKey("posX", Constants.NBT.TAG_INT) == false ||
            tag.hasKey("posY", Constants.NBT.TAG_INT) == false ||
            tag.hasKey("posZ", Constants.NBT.TAG_INT) == false ||
            tag.hasKey("dim", Constants.NBT.TAG_INT) == false ||
            tag.hasKey("face", Constants.NBT.TAG_BYTE) == false)
        {
            return null;
        }

        int x = tag.getInteger("posX");
        int y = tag.getInteger("posY");
        int z = tag.getInteger("posZ");
        int dim = tag.getInteger("dim");
        int face = tag.getByte("face");

        return new BlockPosEU(x, y, z, dim, face);
    }

    public static BlockPosEU readFromNBT(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("BlockPos", Constants.NBT.TAG_COMPOUND) == false)
        {
            return null;
        }

        return readFromTag(nbt.getCompoundTag("BlockPos"));
    }

    public static void removeFromNBT(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return;
        }

        nbt.removeTag("BlockPos");
    }

    /**
     * Helper method to add back a way to do left hand rotations, like ForgeDirection had.
     */
    public static EnumFacing getRotation(EnumFacing facing, EnumFacing axis)
    {
        EnumFacing newFacing = facing.rotateAround(axis.getAxis());

        if (axis.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
        {
            return newFacing;
        }

        // Negative axis direction, if the facing was actually rotated then get the opposite
        return newFacing != facing ? newFacing.getOpposite() : facing;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + dimension;
        result = prime * result + face;
        result = prime * result + posX;
        result = prime * result + posY;
        result = prime * result + posZ;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockPosEU other = (BlockPosEU) obj;
        if (dimension != other.dimension)
            return false;
        if (face != other.face)
            return false;
        if (posX != other.posX)
            return false;
        if (posY != other.posY)
            return false;
        if (posZ != other.posZ)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("BlockPosEU:{x: %d, y: %d, z: %d, dim: %d, face: %d}", this.posX, this.posY, this.posZ, this.dimension, this.face);
        //return "BlockPosEU:{x:" + this.posX + ",y:" + this.posY + ",z:" + this.posZ + "dim:" + this.dimension + ",face:" + this.face + "}";
    }
}
