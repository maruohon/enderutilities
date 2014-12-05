package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class NBTHelperEnderCharge
{
    public int enderChargeCapacity;
    public int enderChargeAmount;

    public NBTHelperEnderCharge()
    {
        this.enderChargeCapacity = 0;
        this.enderChargeAmount = 0;
    }

    public static boolean hasChargeTag(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("EnderCharge", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.getCompoundTag("EnderCharge");
        if (tag != null &&
            tag.hasKey("Capacity", Constants.NBT.TAG_ANY_NUMERIC) == true &&
            tag.hasKey("Amount", Constants.NBT.TAG_ANY_NUMERIC) == true)
        {
            return true;
        }

        return false;
    }

    public NBTTagCompound readChargeTagFromNBT(NBTTagCompound nbt)
    {
        if (hasChargeTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("EnderCharge");
        this.enderChargeCapacity = tag.getInteger("Capacity");
        this.enderChargeAmount = tag.getInteger("Amount");

        return tag;
    }

    public static NBTTagCompound writeChargeTagToNBT(NBTTagCompound nbt, int capacity, int amount)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Capacity", capacity);
        tag.setInteger("Amount", amount);

        nbt.setTag("EnderCharge", tag);

        return nbt;
    }

    public static NBTTagCompound removeChargeTagFromNBT(NBTTagCompound nbt)
    {
        return NBTHelper.writeTagToNBT(nbt, "EnderCharge", null);
    }
}
