package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NBTHelper
{
    public static NBTTagCompound writeTagToNBT(NBTTagCompound nbt, String name, NBTTagCompound tag)
    {
        if (name == null)
        {
            return nbt;
        }

        if (nbt == null)
        {
            if (tag == null)
            {
                return nbt;
            }

            nbt = new NBTTagCompound();
        }

        if (tag == null)
        {
            nbt.removeTag(name);
        }
        else
        {
            nbt.setTag(name, tag);
        }

        return nbt;
    }

    public static ItemStack writeNBTToItem(ItemStack stack, NBTTagCompound nbt)
    {
        if (stack != null)
        {
            stack.setTagCompound(nbt);
        }

        return stack;
    }
}
