package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

public class BlockPosEU
{
    private final int posX;
    private final int posY;
    private final int posZ;
    private final int dimension;
    private final EnumFacing facing;

    public BlockPosEU(BlockPos pos)
    {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPosEU(int x, int y, int z)
    {
        this(x, y, z, 0, EnumFacing.DOWN);
    }

    public BlockPosEU(int x, int y, int z, int dim)
    {
        this(x, y, z, dim, EnumFacing.DOWN);
    }

    public BlockPosEU(BlockPos pos, int dim, int facing)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dim, EnumFacing.byIndex(facing));
    }

    public BlockPosEU(BlockPos pos, int dim, EnumFacing facing)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dim, facing);
    }

    public BlockPosEU(int x, int y, int z, int dim, EnumFacing facing)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dimension = dim;
        this.facing = facing != null ? facing : EnumFacing.DOWN;
    }

    public int getX()
    {
        return this.posX;
    }

    public int getY()
    {
        return this.posY;
    }

    public int getZ()
    {
        return this.posZ;
    }

    public int getDimension()
    {
        return this.dimension;
    }

    public EnumFacing getFacing()
    {
        return this.facing;
    }

    public BlockPosEU add(BlockPosEU pos)
    {
        return this.add(pos.posX, pos.posY, pos.posZ);
    }

    public BlockPosEU subtract(BlockPosEU other)
    {
        return this.add(-other.posX, -other.posY, -other.posZ);
    }

    /**
     * Add the given offsets to the position. Keeps the same dimension and facing.
     */
    public BlockPosEU add(int x, int y, int z)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return this;
        }

        return new BlockPosEU(this.posX + x, this.posY + y, this.posZ + z, this.dimension, this.facing);
    }

    public BlockPosEU offset(EnumFacing facing)
    {
        return this.offset(facing, 1);
    }

    /**
     * Offset the position by the given amount into the given direction.
     * Returns a new instance with the changes applied and does not modify the original.
     */
    public BlockPosEU offset(EnumFacing facing, int distance)
    {
        return new BlockPosEU(  this.posX + facing.getXOffset() * distance,
                                this.posY + facing.getYOffset() * distance,
                                this.posZ + facing.getZOffset() * distance,
                                this.dimension, this.facing);
    }

    public boolean isWithinDistance(Entity entity, double maxDist)
    {
        return this.isWithinDistance(entity.getPositionVector(), entity.getEntityWorld().provider.getDimension(), maxDist);
    }

    public boolean isWithinDistance(Vec3d pos, int dimension, double maxDist)
    {
        double dx = pos.x - this.posX;
        double dy = pos.y - this.posY;
        double dz = pos.z - this.posZ;

        return this.dimension == dimension && (dx * dx + dy * dy + dz * dz) <= (maxDist * maxDist);
    }

    public BlockPosEU clampCoordsToWorldBounds()
    {
        int x = MathHelper.clamp(this.posX, -30000000, 30000000);
        int y = MathHelper.clamp(this.posY, 0, 255);
        int z = MathHelper.clamp(this.posZ, -30000000, 30000000);

        return new BlockPosEU(x, y, z, this.dimension, this.facing);
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
        tag.setByte("face", (byte) this.facing.getIndex());

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

        return new BlockPosEU(x, y, z, dim, EnumFacing.byIndex(face));
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
        result = prime * result + facing.getIndex();
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
        if (obj == null || (obj instanceof BlockPosEU) == false)
            return false;

        BlockPosEU other = (BlockPosEU) obj;
        if (dimension != other.dimension)
            return false;
        if (facing != other.facing)
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
        return String.format("BlockPosEU:{x: %d, y: %d, z: %d, dim: %d, face: %d}", this.posX, this.posY, this.posZ, this.dimension, this.facing);
        //return "BlockPosEU:{x:" + this.posX + ",y:" + this.posY + ",z:" + this.posZ + "dim:" + this.dimension + ",face:" + this.face + "}";
    }
}
