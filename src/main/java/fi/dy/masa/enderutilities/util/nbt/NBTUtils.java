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

    public static ItemStack writeNBTToItemStack(ItemStack stack, NBTTagCompound nbt)
    {
        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Returns the root compound tag of the given ItemStack.
     * If the tag is null, then a new tag will be created and written to the ItemStack.
     */
    public static NBTTagCompound getOrCreateRootCompoundTag(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        return nbt;
    }

    /**
     * Returns a compound tag by the name <b>tagName</b> from the given ItemStack's root compound tag.
     * If such tag doesn't exist, it will be created and added.
     * If <b>tagName</b> is null, then the root tag will be returned and created if necessary.
     */
    public static NBTTagCompound getOrCreateCompoundTag(ItemStack stack, String tagName)
    {
        NBTTagCompound nbt = getOrCreateRootCompoundTag(stack);

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

    /**
     * Get a compound tag by the given name. If the tag doesn't exist, null is returned
     * and the tag is NOT created. If <b>tagName</b> is null, then the root tag is returned.
     */
    public static NBTTagCompound getCompoundTag(ItemStack stack, String tagName)
    {
        if (tagName == null)
        {
            return stack.getTagCompound();
        }

        NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null && nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) ? nbt.getCompoundTag(tagName) : null;
    }

    /**
     * Gets the stored UUID from the given ItemStack. If <b>containerTagName</b> is not null,
     * then the UUID is read from a compound tag by that name.
     * If the ItemStack doesn't yet have an UUID, then a new random UUID will be created and stored.
     */
    public static UUID getOrCreateUUIDFromItemStack(ItemStack stack, String containerTagName)
    {
        NBTTagCompound nbt;
        if (containerTagName != null)
        {
            nbt = getOrCreateCompoundTag(stack, containerTagName);
        }
        else
        {
            nbt = getOrCreateRootCompoundTag(stack);
        }

        if (nbt.hasKey("UUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("UUIDL", Constants.NBT.TAG_LONG))
        {
            return new UUID(nbt.getLong("UUIDM"), nbt.getLong("UUIDL"));
        }

        UUID uuid = UUID.randomUUID();
        nbt.setLong("UUIDM", uuid.getMostSignificantBits());
        nbt.setLong("UUIDL", uuid.getLeastSignificantBits());
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
     * Gets the stored UUID from the given ItemStack. If containerTagName is not null, then
     * the UUID is read from a compound tag by that name. If there is no stored UUID, null is returned.
     */
    public static UUID getUUIDFromItemStack(ItemStack stack, String containerTagName)
    {
        NBTTagCompound nbt = getCompoundTag(stack, containerTagName);
        return getUUIDFromNBT(nbt);
    }

    /**
     * Stores the given UUID to the given ItemStack. If <b>containerTagName</b> is not null,
     * then the UUID is stored inside a compound tag by that name. Otherwise it is stored
     * directly inside the root compound tag.
     */
    public static void setUUID(ItemStack stack, UUID uuid, String containerTagName)
    {
        NBTTagCompound nbt;
        if (containerTagName != null)
        {
            nbt = getOrCreateCompoundTag(stack, containerTagName);
        }
        else
        {
            nbt = getOrCreateRootCompoundTag(stack);
        }

        nbt.setLong("UUIDM", uuid.getMostSignificantBits());
        nbt.setLong("UUIDL", uuid.getLeastSignificantBits());
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
        NBTTagCompound nbt = NBTUtils.getOrCreateCompoundTag(stack, containerTagName);
        toggleBoolean(nbt, tagName);
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
     * Cycle a byte value in the given ItemStack's NBT. If <b>containerTagName</b>
     * is not null, then the value is stored inside a compound tag by that name.
     */
    public static void cycleByteValue(ItemStack stack, String containerTagName, String tagName, int maxValue)
    {
        NBTTagCompound nbt = NBTUtils.getOrCreateCompoundTag(stack, containerTagName);
        cycleByteValue(nbt, tagName, maxValue);
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
