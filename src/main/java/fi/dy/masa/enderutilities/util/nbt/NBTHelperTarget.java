package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class NBTHelperTarget
{
    public int posX;
    public int posY;
    public int posZ;
    public double dPosX;
    public double dPosY;
    public double dPosZ;
    public int dimension;
    /* Face of the target block */
    public int blockFace;

    public NBTHelperTarget()
    {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.dPosX = 0.0d;
        this.dPosY = 0.0d;
        this.dPosZ = 0.0d;
        this.dimension = 0;
        this.blockFace = -1;
    }

    public static boolean hasTargetTag(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("Target", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Target");
        if (tag != null &&
            tag.hasKey("posX", Constants.NBT.TAG_ANY_NUMERIC) == true &&
            tag.hasKey("posY", Constants.NBT.TAG_ANY_NUMERIC) == true &&
            tag.hasKey("posZ", Constants.NBT.TAG_ANY_NUMERIC) == true &&
            tag.hasKey("Dim", Constants.NBT.TAG_INT) == true)
        {
            return true;
        }

        return false;
    }

    public NBTTagCompound readTargetTagFromNBT(NBTTagCompound nbt)
    {
        if (hasTargetTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Target");
        this.posX = tag.getInteger("posX");
        this.posY = tag.getInteger("posY");
        this.posZ = tag.getInteger("posZ");
        this.dimension = tag.getInteger("Dim");
        this.blockFace = tag.getInteger("BlockFace");

        this.dPosX = tag.hasKey("dPosX", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosX") : this.posX + 0.5d;
        this.dPosY = tag.hasKey("dPosY", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosY") : this.posY;
        this.dPosZ = tag.hasKey("dPosZ", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosZ") : this.posZ + 0.5d;

        return tag;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, int pX, int pY, int pZ, int dim, int face, double hitX, double hitY, double hitZ, boolean doHitOffset)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        double x = pX;
        double y = pY;
        double z = pZ;

        if (doHitOffset == true)
        {
            x += hitX;
            y += hitY;
            z += hitZ;
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("posX", (int)x);
        tag.setInteger("posY", (int)y);
        tag.setInteger("posZ", (int)z);
        tag.setInteger("Dim", dim);
        tag.setInteger("BlockFace", face);
        tag.setDouble("dPosX", x);
        tag.setDouble("dPosY", y);
        tag.setDouble("dPosZ", z);

        nbt.setTag("Target", tag);

        return nbt;
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
        tag.setInteger("Dim", this.dimension);
        tag.setInteger("BlockFace", this.blockFace);
        tag.setDouble("dPosX", this.dPosX);
        tag.setDouble("dPosY", this.dPosY);
        tag.setDouble("dPosZ", this.dPosZ);

        nbt.setTag("Target", tag);

        return nbt;
    }

    public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
    {
        return NBTHelper.writeTagToNBT(nbt, "Target", null);
    }
}
