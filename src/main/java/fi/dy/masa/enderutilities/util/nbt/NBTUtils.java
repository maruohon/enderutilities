package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;
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

    /**
     * Sets the root compound tag in the given ItemStack. An empty compound will be stripped completely.
     */
    public static ItemStack setRootCompoundTag(ItemStack stack, NBTTagCompound nbt)
    {
        if (nbt.hasNoTags() == true)
        {
            nbt = null;
        }

        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Returns a compound tag by the given name <b>tagName</b>. If <b>tagName</b> is null,
     * then the root compound tag is returned instead. If <b>create</b> is <b>false</b>
     * and the tag doesn't exist, null is returned and the tag is not created.
     * If <b>create</b> is <b>true</b>, then the tag(s) are created and added if necessary.
     */
    public static NBTTagCompound getCompoundTag(ItemStack stack, String tagName, boolean create)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (create == false)
        {
            if (nbt == null)
            {
                return null;
            }

            if (tagName != null)
            {
                return nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == true ? nbt.getCompoundTag(tagName) : null;
            }

            return nbt;
        }

        // create = true

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (tagName == null)
        {
            return nbt;
        }

        if (nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == false)
        {
            nbt.setTag(tagName, new NBTTagCompound());
        }

        return nbt.getCompoundTag(tagName);
    }

    /**
     * Gets the stored UUID from the given ItemStack. If <b>containerTagName</b> is not null,
     * then the UUID is read from a compound tag by that name.
     * If <b>create</b> is true and a UUID isn't found, a new random UUID will be created and added.
     * If <b>create</b> is false and a UUID isn't found, then null is returned.
     */
    public static UUID getUUIDFromItemStack(ItemStack stack, String containerTagName, boolean create)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, create);

        UUID uuid = getUUIDFromNBT(nbt);
        if (uuid == null && create == true)
        {
            uuid = UUID.randomUUID();
            nbt.setLong("UUIDM", uuid.getMostSignificantBits());
            nbt.setLong("UUIDL", uuid.getLeastSignificantBits());
        }

        return uuid;
    }

    /**
     * Gets the stored UUID from the given compound tag. If one isn't found, null is returned.
     */
    public static UUID getUUIDFromNBT(NBTTagCompound nbt)
    {
        if (nbt != null && nbt.hasKey("UUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("UUIDL", Constants.NBT.TAG_LONG))
        {
            return new UUID(nbt.getLong("UUIDM"), nbt.getLong("UUIDL"));
        }

        return null;
    }

    /**
     * Stores the given UUID to the given ItemStack. If <b>containerTagName</b> is not null,
     * then the UUID is stored inside a compound tag by that name. Otherwise it is stored
     * directly inside the root compound tag.
     */
    public static void setUUID(ItemStack stack, UUID uuid, String containerTagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);

        nbt.setLong("UUIDM", uuid.getMostSignificantBits());
        nbt.setLong("UUIDL", uuid.getLeastSignificantBits());
    }

    /**
     * Return the boolean value from a tag <b>tagName</b>, or false if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static boolean getBoolean(ItemStack stack, String containerTagName, String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getBoolean(tagName) : false;
    }

    public static void setBoolean(ItemStack stack, String containerTagName, String tagName, boolean value)
    {
        getCompoundTag(stack, containerTagName, true).setBoolean(tagName, value);
    }

    public static void toggleBoolean(NBTTagCompound nbt, String tagName)
    {
        nbt.setBoolean(tagName, ! nbt.getBoolean(tagName));
    }

    /**
     * Toggle a boolean value in the given ItemStack's NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void toggleBoolean(ItemStack stack, String containerTagName, String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        toggleBoolean(nbt, tagName);
    }

    /**
     * Return the byte value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static byte getByte(ItemStack stack, String containerTagName, String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getByte(tagName) : 0;
    }

    /**
     * Set a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setByte(ItemStack stack, String containerTagName, String tagName, byte value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(NBTTagCompound nbt, String tagName, int maxValue)
    {
        byte value = nbt.getByte(tagName);
        nbt.setByte(tagName, ++value > maxValue ? 0 : value);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(ItemStack stack, String containerTagName, String tagName, int maxValue)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        cycleByteValue(nbt, tagName, maxValue);
    }

    /**
     * Return the integer value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static long getInteger(ItemStack stack, String containerTagName, String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getInteger(tagName) : 0;
    }

    /**
     * Set an integer value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setInteger(ItemStack stack, String containerTagName, String tagName, int value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setInteger(tagName, value);
    }

    /**
     * Return the long value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static long getLong(ItemStack stack, String containerTagName, String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getLong(tagName) : 0;
    }

    /**
     * Set a long value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setLong(ItemStack stack, String containerTagName, String tagName, long value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setLong(tagName, value);
    }

    /**
     * Returns the number of stored ItemStacks in the <b>containerStack</b>.
     * If containerStack is missing the NBT data completely, then -1 is returned.
     * @param containerStack
     * @return the number of tags in the NBTTagList, or -1 of the TagList doesn't exist
     */
    public static int getNumberOfStoredItemStacks(ItemStack containerStack)
    {
        NBTTagList list = getStoredItemsList(containerStack, false);
        return list != null ? list.tagCount() : -1;
    }

    /**
     * Returns a TagList for the key <b<tagName</b> and creates and adds it if one isn't found.
     * If <b>containerTagName</b> is not null, then it is retrieved from inside a compound tag by that name.
     * @param containerStack
     * @param containerTagName the compound tag name holding the TagList, or null if it's directly inside the root compound
     * @param tagName the name/key of the TagList
     * @param tagType the type of tags the list is holding
     * @param create true = the tag(s) will be created if they are not found, false = no tags will be created
     * @return the requested TagList (will be created and added if necessary if <b>create</b> is true) or null (if <b>create</b> is false)
     */
    public static NBTTagList getTagList(ItemStack containerStack, String containerTagName, String tagName, int tagType, boolean create)
    {
        NBTTagCompound nbt = getCompoundTag(containerStack, containerTagName, create);
        if (create == true && nbt.hasKey(tagName, Constants.NBT.TAG_LIST) == false)
        {
            nbt.setTag(tagName, new NBTTagList());
        }

        return nbt != null ? nbt.getTagList(tagName, tagType) : null;
    }

    /**
     * Writes the given <b>tagList</b> into the ItemStack containerStack.
     * The compound tags are created if necessary.
     */
    public static void setTagList(ItemStack containerStack, String containerTagName, String tagName, NBTTagList tagList)
    {
        NBTTagCompound nbt = getCompoundTag(containerStack, containerTagName, true);
        nbt.setTag(tagName, tagList);
    }

    /**
     * Inserts a new tag into the given NBTTagList at position <b>index</b>.
     * To do this the list will be re-created and the new list is returned.
     */
    public static NBTTagList insertToTagList(NBTTagList tagList, NBTBase tag, int index)
    {
        if (tagList == null || tag == null)
        {
            return tagList;
        }

        int count = tagList.tagCount();
        if (index >= count)
        {
            index = count > 0 ? count - 1 : 0;
        }

        NBTTagList newList = new NBTTagList();
        for (int i = 0; i < index; i++)
        {
            newList.appendTag(tagList.removeTag(0));
        }

        newList.appendTag(tag);

        count = tagList.tagCount();
        for (int i = 0; i < count; i++)
        {
            newList.appendTag(tagList.removeTag(0));
        }

        return newList;
    }

    /**
     * Returns the NBTTagList containing all the stored ItemStacks in the containerStack.
     * If the TagList doesn't exist and <b>create</b> is true, then the tag will be created and added.
     * @param containerStack
     * @return the NBTTagList holding the stored items, or null if it doesn't exist and <b>create</b> is false
     */
    public static NBTTagList getStoredItemsList(ItemStack containerStack, boolean create)
    {
        return getTagList(containerStack, null, "Items", Constants.NBT.TAG_COMPOUND, create);
    }

    /**
     * Sets the NBTTagList storing the items in the containerStack. If <b>tagList</b> is null, then the existing
     * list (if any) is removed.
     * @param containerStack
     * @param tagList
     */
    public static void setStoredItemsList(ItemStack containerStack, NBTTagList tagList)
    {
        if (tagList == null)
        {
            NBTTagCompound nbt = getCompoundTag(containerStack, null, false);
            if (nbt != null)
            {
                nbt.removeTag("Items");
                setRootCompoundTag(containerStack, nbt);
            }

            return;
        }

        NBTTagCompound nbt = getCompoundTag(containerStack, null, true);
        nbt.setTag("Items", tagList);
    }
}
