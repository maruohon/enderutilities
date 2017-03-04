package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class NBTUtils
{
    @Nullable
    public static NBTTagCompound writeTagToNBT(@Nullable NBTTagCompound nbt, @Nonnull String name, @Nullable NBTBase tag)
    {
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
    @Nonnull
    public static ItemStack setRootCompoundTag(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        if (nbt != null && nbt.hasNoTags())
        {
            nbt = null;
        }

        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Get the root compound tag from the ItemStack.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static NBTTagCompound getRootCompoundTag(@Nonnull ItemStack stack, boolean create)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (create == false)
        {
            return nbt;
        }

        // create = true
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        return nbt;
    }

    /**
     * Get a compound tag by the given name <b>tagName</b> from the other compound tag <b>nbt</b>.
     * If one doesn't exist, then it will be created and added if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static NBTTagCompound getCompoundTag(@Nullable NBTTagCompound nbt, @Nonnull String tagName, boolean create)
    {
        if (nbt == null)
        {
            return null;
        }

        if (create == false)
        {
            return nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) ? nbt.getCompoundTag(tagName) : null;
        }

        // create = true

        if (nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == false)
        {
            nbt.setTag(tagName, new NBTTagCompound());
        }

        return nbt.getCompoundTag(tagName);
    }

    /**
     * Returns a compound tag by the given name <b>tagName</b>. If <b>tagName</b> is null,
     * then the root compound tag is returned instead. If <b>create</b> is <b>false</b>
     * and the tag doesn't exist, null is returned and the tag is not created.
     * If <b>create</b> is <b>true</b>, then the tag(s) are created and added if necessary.
     */
    @Nullable
    public static NBTTagCompound getCompoundTag(@Nonnull ItemStack stack, @Nullable String tagName, boolean create)
    {
        NBTTagCompound nbt = getRootCompoundTag(stack, create);

        if (tagName != null)
        {
            nbt = getCompoundTag(nbt, tagName, create);
        }

        return nbt;
    }

    /**
     * Get a nested compound tag by the name <b>tagName</b> from inside
     * another compound tag <b>containerTagName</b>.
     * If some of the tags don't exist, then they will be created and added
     * if <b>create</b> is true, otherwise null is returned.
     */
    @Nullable
    public static NBTTagCompound getCompoundTag(@Nonnull ItemStack stack, @Nullable String containerTagName,
            @Nonnull String tagName, boolean create)
    {
        NBTTagCompound nbt = getRootCompoundTag(stack, create);

        if (containerTagName != null)
        {
            nbt = getCompoundTag(nbt, containerTagName, create);
        }

        return getCompoundTag(nbt, tagName, create);
    }

    /**
     * Remove a compound tag by the name <b>tagName</b>. If <b>containerTagName</b> is not null, then
     * the tag is removed from inside a tag by that name.
     */
    public static void removeCompoundTag(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);

        if (nbt != null && nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND))
        {
            nbt.removeTag(tagName);

            if (nbt.hasNoTags())
            {
                if (containerTagName != null)
                {
                    stack.getTagCompound().removeTag(containerTagName);
                }
                else
                {
                    stack.setTagCompound(null);
                }
            }
        }
    }

    /**
     * Returns a copy of the compound tag <b>tag</b>, but excludes a top-level member called <b>exclude</b>.
     * If the resulting tag has no other keys, then null is returned instead of an empty compound.
     * @param tag
     * @param exclude
     * @return
     */
    @Nullable
    public static NBTTagCompound copyCompoundExcludingTag(@Nonnull NBTTagCompound tag, String exclude)
    {
        NBTTagCompound newTag = new NBTTagCompound();

        for (String key : tag.getKeySet())
        {
            if (key.equals(exclude) == false)
            {
                newTag.setTag(key, tag.getTag(key).copy());
            }
        }

        return newTag.hasNoTags() ? null : newTag;
    }

    @Nonnull
    public static String getOrCreateString(@Nonnull ItemStack stack, @Nullable String containerTagName,
            @Nonnull String tagName, @Nonnull String value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);

        if (nbt.hasKey(tagName, Constants.NBT.TAG_STRING) == false)
        {
            nbt.setString(tagName, value);
            return value;
        }

        return nbt.getString(tagName);
    }

    /**
     * Gets the stored UUID from the given ItemStack. If <b>containerTagName</b> is not null,
     * then the UUID is read from a compound tag by that name.
     * If <b>create</b> is true and a UUID isn't found, a new random UUID will be created and added.
     * If <b>create</b> is false and a UUID isn't found, then null is returned.
     */
    @Nullable
    public static UUID getUUIDFromItemStack(@Nonnull ItemStack stack, @Nullable String containerTagName, boolean create)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, create);

        UUID uuid = getUUIDFromNBT(nbt);

        if (uuid == null && create)
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
    @Nullable
    public static UUID getUUIDFromNBT(@Nullable NBTTagCompound nbt)
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
    public static void setUUID(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull UUID uuid)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);

        nbt.setLong("UUIDM", uuid.getMostSignificantBits());
        nbt.setLong("UUIDL", uuid.getLeastSignificantBits());
    }

    /**
     * Return the boolean value from a tag <b>tagName</b>, or false if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static boolean getBoolean(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getBoolean(tagName) : false;
    }

    public static void setBoolean(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, boolean value)
    {
        getCompoundTag(stack, containerTagName, true).setBoolean(tagName, value);
    }

    public static void toggleBoolean(@Nonnull NBTTagCompound nbt, @Nonnull String tagName)
    {
        nbt.setBoolean(tagName, ! nbt.getBoolean(tagName));
    }

    /**
     * Toggle a boolean value in the given ItemStack's NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void toggleBoolean(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        toggleBoolean(nbt, tagName);
    }

    /**
     * Return the byte value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static byte getByte(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getByte(tagName) : 0;
    }

    /**
     * Set a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setByte(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, byte value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(@Nonnull NBTTagCompound nbt, @Nonnull String tagName, int minValue, int maxValue)
    {
        cycleByteValue(nbt, tagName, minValue, maxValue, false);
    }

    /**
     * Cycle a byte value in the given NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(@Nonnull NBTTagCompound nbt, @Nonnull String tagName, int minValue, int maxValue, boolean reverse)
    {
        byte value = nbt.getByte(tagName);

        if (reverse)
        {
            if (--value < minValue)
            {
                value = (byte)maxValue;
            }
        }
        else
        {
            if (++value > maxValue)
            {
                value = (byte)minValue;
            }
        }

        nbt.setByte(tagName, value);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     * The low end of the range is 0.
     */
    public static void cycleByteValue(@Nonnull ItemStack stack, @Nullable String containerTagName,
            @Nonnull String tagName, int maxValue)
    {
        cycleByteValue(stack, containerTagName, tagName, maxValue, false);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     * The low end of the range is 0.
     */
    public static void cycleByteValue(@Nonnull ItemStack stack, @Nullable String containerTagName,
            @Nonnull String tagName, int maxValue, boolean reverse)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        cycleByteValue(nbt, tagName, 0, maxValue, reverse);
    }

    /**
     * Cycle a byte value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(@Nonnull ItemStack stack, @Nullable String containerTagName,
            @Nonnull String tagName, int minValue, int maxValue, boolean reverse)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        cycleByteValue(nbt, tagName, minValue, maxValue, reverse);
    }

    /**
     * Return the short value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static short getShort(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getShort(tagName) : 0;
    }

    /**
     * Set an integer value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setShort(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, short value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setShort(tagName, value);
    }

    /**
     * Return the integer value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static int getInteger(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getInteger(tagName) : 0;
    }

    /**
     * Set an integer value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setInteger(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, int value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setInteger(tagName, value);
    }

    /**
     * Return the long value from a tag <b>tagName</b>, or 0 if it doesn't exist.
     * If <b>containerTagName</b> is not null, then the value is retrieved from inside a compound tag by that name.
     */
    public static long getLong(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, false);
        return nbt != null ? nbt.getLong(tagName) : 0;
    }

    /**
     * Set a long value in the given ItemStack's NBT in a tag <b>tagName</b>. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void setLong(@Nonnull ItemStack stack, @Nullable String containerTagName, @Nonnull String tagName, long value)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName, true);
        nbt.setLong(tagName, value);
    }

    @Nonnull
    public static NBTTagList writeInts(int... values)
    {
        NBTTagList tagList = new NBTTagList();

        for (int i : values)
        {
            tagList.appendTag(new NBTTagInt(i));
        }

        return tagList;
    }

    @Nonnull
    public static NBTTagList writeDoubles(double... values)
    {
        NBTTagList tagList = new NBTTagList();

        for (double d : values)
        {
            tagList.appendTag(new NBTTagDouble(d));
        }

        return tagList;
    }

    /**
     * Returns the number of stored ItemStacks in the <b>containerStack</b>.
     * If containerStack is missing the NBT data completely, then -1 is returned.
     * @param containerStack
     * @return the number of tags in the NBTTagList, or -1 of the TagList doesn't exist
     */
    /*public static int getNumberOfStoredItemStacks(ItemStack containerStack)
    {
        NBTTagList list = getStoredItemsList(containerStack, false);
        return list != null ? list.tagCount() : -1;
    }*/

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
    @Nullable
    public static NBTTagList getTagList(@Nonnull ItemStack containerStack, @Nullable String containerTagName,
            @Nonnull String tagName, int tagType, boolean create)
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
    public static void setTagList(@Nonnull ItemStack containerStack, @Nullable String containerTagName,
            @Nonnull String tagName, @Nonnull NBTTagList tagList)
    {
        NBTTagCompound nbt = getCompoundTag(containerStack, containerTagName, true);
        nbt.setTag(tagName, tagList);
    }

    /**
     * Inserts a new tag into the given NBTTagList at position <b>index</b>.
     * To do this the list will be re-created and the new list is returned.
     */
    @Nonnull
    public static NBTTagList insertToTagList(@Nonnull NBTTagList tagList, @Nonnull NBTBase tag, int index)
    {
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
    @Nullable
    public static NBTTagList getStoredItemsList(@Nonnull ItemStack containerStack, boolean create)
    {
        return getTagList(containerStack, null, "Items", Constants.NBT.TAG_COMPOUND, create);
    }

    /**
     * Sets the NBTTagList storing the items in the containerStack. If <b>tagList</b> is null, then the existing
     * list (if any) is removed.
     * @param containerStack
     * @param tagList
     */
    /*public static void setStoredItemsList(ItemStack containerStack, NBTTagList tagList)
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
    }*/

    /**
     * Reads the stored items from the provided NBTTagCompound, from a NBTTagList by the name <b>tagName</b>
     * and writes them to the provided array of ItemStacks <b>items</b>.
     * @param tag
     * @param items
     * @param tagName
     */
    public static void readStoredItemsFromTag(@Nonnull NBTTagCompound nbt, @Nonnull ItemStack[] items, @Nonnull String tagName)
    {
        if (nbt.hasKey(tagName, Constants.NBT.TAG_LIST) == false)
        {
            return;
        }

        NBTTagList nbtTagList = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
        int num = nbtTagList.tagCount();

        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            byte slotNum = tag.getByte("Slot");

            if (slotNum >= 0 && slotNum < items.length)
            {
                items[slotNum] = ItemStack.loadItemStackFromNBT(tag);

                if (items[slotNum] != null && tag.hasKey("ActualCount", Constants.NBT.TAG_INT))
                {
                    items[slotNum].stackSize = tag.getInteger("ActualCount");
                }
            }
            /*else
            {
                EnderUtilities.logger.warn("Failed to read items from NBT, invalid slot: " + slotNum + " (max: " + (items.length - 1) + ")");
            }*/
        }
    }

    /**
     * Writes the ItemStacks in <b>items</b> to a new NBTTagList and returns that list.
     * @param items
     */
    @Nonnull
    public static NBTTagList createTagListForItems(@Nonnull ItemStack[] items)
    {
        int invSlots = items.length;
        NBTTagList nbtTagList = new NBTTagList();

        // Write all the ItemStacks into a TAG_List
        for (int slotNum = 0; slotNum < invSlots && slotNum <= 127; slotNum++)
        {
            if (items[slotNum] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                items[slotNum].writeToNBT(tag);
                tag.setInteger("ActualCount", items[slotNum].stackSize);
                tag.setByte("Slot", (byte)slotNum);
                nbtTagList.appendTag(tag);
            }
        }

        return nbtTagList;
    }

    /**
     * Writes the ItemStacks in <b>items</b> to the NBTTagCompound <b>nbt</b>
     * in a NBTTagList by the name <b>tagName</b>.
     * @param nbt
     * @param items
     * @param tagName the NBTTagList tag name where the items will be written to
     * @param keepExtraSlots set to true to append existing items in slots that are outside of the currently written slot range
     */
    @Nonnull
    public static NBTTagCompound writeItemsToTag(@Nonnull NBTTagCompound nbt, @Nonnull ItemStack[] items, @Nonnull String tagName, boolean keepExtraSlots)
    {
        int invSlots = items.length;
        NBTTagList nbtTagList = createTagListForItems(items);

        if (keepExtraSlots == true && nbt.hasKey(tagName, Constants.NBT.TAG_LIST) == true)
        {
            // Read the old items and append any existing items that are outside the current written slot range
            NBTTagList nbtTagListExisting = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < nbtTagListExisting.tagCount(); i++)
            {
                NBTTagCompound tag = nbtTagListExisting.getCompoundTagAt(i);
                byte slotNum = tag.getByte("Slot");
                if (slotNum >= invSlots && slotNum <= 127)
                {
                    nbtTagList.appendTag(tag);
                }
            }
        }

        // Write the items to the compound tag
        if (nbtTagList.tagCount() > 0)
        {
            nbt.setTag(tagName, nbtTagList);
        }
        else
        {
            nbt.removeTag(tagName);
        }

        return nbt;
    }

    /**
     * Writes the ItemStacks in <b>items</b> to the container ItemStack <b>containerStack</b>
     * in a NBTTagList by the name <b>tagName</b>.
     * @param containerStack
     * @param items
     * @param tagName the NBTTagList tag name where the items will be written to
     * @param keepExtraSlots set to true to append existing items in slots that are outside of the currently written slot range
     */
    public static void writeItemsToContainerItem(@Nonnull ItemStack containerStack, @Nonnull ItemStack[] items,
            @Nonnull String tagName, boolean keepExtraSlots)
    {
        // Write the items to the "container" ItemStack's NBT
        NBTTagCompound nbt = getRootCompoundTag(containerStack, true);
        writeItemsToTag(nbt, items, tagName, keepExtraSlots);

        // This checks for hasNoTags and then removes the tag if it's empty
        setRootCompoundTag(containerStack, nbt);
    }

    public static void setPositionInTileEntityNBT(@Nonnull NBTTagCompound tag, @Nonnull BlockPos pos)
    {
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
    }
}
