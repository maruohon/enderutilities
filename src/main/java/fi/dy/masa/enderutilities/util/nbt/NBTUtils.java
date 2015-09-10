package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class NBTUtils
{
    public static NBTTagCompound writeTagToNBT(NBTTagCompound nbt, String name, NBTBase tag)
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

    public static ItemStack writeNBTToItemStack(ItemStack stack, NBTTagCompound nbt)
    {
        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Returns a compound tag by the name <b>tagName</b> from the given ItemStack's root compound tag.
     * If such tag doesn't exist, it will be created and added.
     * If <b>tagName</b> is null, then the root tag will be returned and created if necessary.
     */
    public static NBTTagCompound getOrCreateCompoundTag(ItemStack stack, String tagName)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (tagName != null)
        {
            if (nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == false)
            {
                NBTTagCompound tag = new NBTTagCompound();
                nbt.setTag(tagName, tag);
            }

            nbt = nbt.getCompoundTag(tagName);
        }

        return nbt;
    }

    public static void toggleBoolean(NBTTagCompound nbt, String tagName)
    {
        nbt.setBoolean(tagName, ! nbt.getBoolean(tagName));
    }

    public static void cycleByteValue(NBTTagCompound nbt, String tagName, int maxValue)
    {
        byte mode = nbt.getByte(tagName);
        nbt.setByte(tagName, ++mode > maxValue ? 0 : mode);
    }

    /**
     * Returns the number of stored ItemStacks in the <b>containerStack</b>.
     * If containerStack is missing the NBT data completely, then -1 is returned.
     * @param containerStack
     * @return the number of tags in the NBTTagList, or -1 of the TagList doesn't exist
     */
    public static int getNumberOfStoredItemStacks(ItemStack containerStack)
    {
        NBTTagList list = getStoredItemsList(containerStack);
        return list != null ? list.tagCount() : -1;
    }

    /**
     * Returns the NBTTagList containing all the stored ItemStacks in the containerStack, or null in case it fails.
     * @param containerStack
     * @return the NBTTagList holding the stored items
     */
    public static NBTTagList getStoredItemsList(ItemStack containerStack)
    {
        if (containerStack.getTagCompound() == null || containerStack.getTagCompound().hasKey("Items", Constants.NBT.TAG_LIST) == false)
        {
            return null;
        }

        return containerStack.getTagCompound().getTagList("Items", Constants.NBT.TAG_COMPOUND);
    }
}
