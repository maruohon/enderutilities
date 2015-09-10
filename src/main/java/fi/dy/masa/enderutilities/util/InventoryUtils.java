package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class InventoryUtils
{
    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * If the whole stack was successfully inserted into the inventory, true
     * is returned. If only some or if none of the items were inserted, false is returned.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     * @param inv The instance of IInventory or ISidedInventory
     * @param stackIn The ItemStack to try and insert into the inventory
     * @param side The side of the block we try to insert from, in case of ISidedInventory
     * @return true if all items were successfully inserted, false if none or only some were
     */
    public static boolean tryInsertItemStackToInventory(IInventory inv, ItemStack stackIn, int side)
    {
        if (inv instanceof ISidedInventory)
        {
            ISidedInventory sided = (ISidedInventory) inv;
            int[] slots = sided.getAccessibleSlotsFromSide(side);

            for (int i : slots)
            {
                if (isItemStackValidForSlot(sided, stackIn, i, side) && tryInsertItemStackToSlot(inv, stackIn, i) == true)
                {
                    return true;
                }
            }
        }
        // IInventory
        else
        {
            int size = inv.getSizeInventory();
            for (int i = 0; i < size; ++i)
            {
                if (isItemStackValidForSlot(inv, stackIn, i) && tryInsertItemStackToSlot(inv, stackIn, i) == true)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If the whole stack was successfully inserted into the slot, true is returned.
     * If none or only some of the items were inserted, false is returned.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     * NOTE: Do NOT call this method with a null stackIn!
     * @param inv
     * @param stackIn
     * @param slotNum
     * @return
     */
    public static boolean tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum)
    {
        ItemStack targetStack = inv.getStackInSlot(slotNum);

        // Empty slot
        if (targetStack == null)
        {
            int num = Math.min(inv.getInventoryStackLimit(), stackIn.getMaxStackSize());

            // The target slot can't take the whole stack
            if (num > 0 && num < stackIn.stackSize)
            {
                ItemStack tmp = stackIn.copy();
                tmp.stackSize = num;
                inv.setInventorySlotContents(slotNum, tmp);
                stackIn.stackSize -= num;
                return false;
            }
            // The target slot can take the whole stack
            else if (num >= stackIn.stackSize)
            {
                inv.setInventorySlotContents(slotNum, stackIn.copy());
                return true;
            }
        }
        // The target slot has identical item
        else if (stackIn.isItemEqual(targetStack) == true && ItemStack.areItemStackTagsEqual(stackIn, targetStack) == true)
        {
            // How much more can the target stack take
            int num = Math.min(inv.getInventoryStackLimit(), targetStack.getMaxStackSize()) - targetStack.stackSize;
            // How much can we add and how much do we have
            num = Math.min(num, stackIn.stackSize);

            if (num > 0)
            {
                targetStack = targetStack.copy();
                targetStack.stackSize += num;
                // Call the method just in case something uses it to do other stuff
                inv.setInventorySlotContents(slotNum, targetStack);
                stackIn.stackSize -= num;

                if (stackIn.stackSize <= 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the ItemStack in stackIn is valid for slot slotNum in the ISidedInventory sided, when inserted from side 'side'.
     * @param sided
     * @param stackIn
     * @param slotNum
     * @param side
     * @return true if stackIn is valid for slotNum in sided from side
     */
    public static boolean isItemStackValidForSlot(ISidedInventory sided, ItemStack stackIn, int slotNum, int side)
    {
        return (sided.isItemValidForSlot(slotNum, stackIn) && sided.canInsertItem(slotNum, stackIn, side));
    }

    /**
     * Check if the ItemStack in stackIn is valid for slot slotNum in the IInventory inv.
     * @param inv
     * @param stackIn
     * @param slotNum
     * @return true if stackIn is valid for slot slotNum
     */
    public static boolean isItemStackValidForSlot(IInventory inv, ItemStack stackIn, int slotNum)
    {
        //return inv.isItemValidForSlot(slotNum, stackIn) && ((inv instanceof ISidedInventory) == false || ((ISidedInventory)inv).canInsertItem(slotNum, stackIn, side));
        return inv.isItemValidForSlot(slotNum, stackIn);
    }

    /**
     * Checks if the given ItemStacks have the same item, damage and NBT. Ignores stack sizes.
     * Can be given null ItemStacks as input.
     * @param stack1
     * @param stack2
     * @return Returns true if the ItemStacks have the same item, damage and NBT tags.
     */
    public static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2)
    {
        if (stack1 == null || stack2 == null)
        {
            return stack1 == stack2;
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /**
     * Returns the slot number of the first empty slot in the given inventory.
     * @param inv
     * @return The slot number of the first empty slot, or -1 if there are no empty slots.
     */
    public static int getFirstEmptySlot(IInventory inv)
    {
        return getSlotOfFirstMatchingItemStack(inv, null);
    }

    /**
     * Returns the slot number of the last empty slot in the given inventory.
     * @param inv
     * @return The slot number of the last empty slot, or -1 if there are no empty slots.
     */
    public static int getLastEmptySlot(IInventory inv)
    {
        return getSlotOfLastMatchingItemStack(inv, null);
    }

    /**
     * Get the slot number of the first slot containing a matching item.
     * @param inv
     * @param item
     * @return The slot number of the first slot with a matching item, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfFirstMatchingItem(IInventory inv, Item item)
    {
        int size = inv.getSizeInventory();
        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the last slot containing a matching item.
     * @param inv
     * @param item
     * @return The slot number of the last slot with a matching item, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfLastMatchingItem(IInventory inv, Item item)
    {
        int size = inv.getSizeInventory();
        for (int i = size - 1; i >= 0; --i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the first slot containing a matching item and damage value.
     * @param inv
     * @param item
     * @return The slot number of the first slot with a matching item and damage value, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfFirstMatchingItem(IInventory inv, Item item, int damage)
    {
        int size = inv.getSizeInventory();
        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item && stack.getItemDamage() == damage)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the last slot containing a matching item and damage value.
     * @param inv
     * @param item
     * @return The slot number of the last slot with a matching item and damage value, or -1 if there are no such items in the inventory.
     */
    public static int getSlotOfLastMatchingItem(IInventory inv, Item item, int damage)
    {
        int size = inv.getSizeInventory();
        for (int i = size - 1; i >= 0; --i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item && stack.getItemDamage() == damage)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @param inv
     * @param item
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there are no matching ItemStacks in the inventory.
     */
    public static int getSlotOfFirstMatchingItemStack(IInventory inv, ItemStack stackIn)
    {
        int size = inv.getSizeInventory();
        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if ((stack == null && stackIn == null)
                || (stack != null && stackIn != null && stack.isItemEqual(stackIn) == true && ItemStack.areItemStackTagsEqual(stack, stackIn) == true))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the last slot containing a matching ItemStack (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @param inv
     * @param item
     * @return The slot number of the last slot with a matching ItemStack, or -1 if there are no matching ItemStacks in the inventory.
     */
    public static int getSlotOfLastMatchingItemStack(IInventory inv, ItemStack stackIn)
    {
        int size = inv.getSizeInventory();
        for (int i = size - 1; i >= 0; --i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if ((stack == null && stackIn == null)
                || (stack != null && stackIn != null && stack.isItemEqual(stackIn) == true && ItemStack.areItemStackTagsEqual(stack, stackIn) == true))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get all the slot numbers that have matching items in the given inventory.
     * @param inv
     * @param item
     * @return an ArrayList containing the slot numbers of the slots with matching items
     */
    public static List<Integer> getSlotNumbersOfMatchingItems(IInventory inv, Item item)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item)
            {
                slots.add(Integer.valueOf(i));
            }
        }

        return slots;
    }

    /**
     * Get all the slot numbers that have matching items in the given inventory.
     * @param inv
     * @param item
     * @return an ArrayList containing the slot numbers of the slots with matching items
     */
    public static List<Integer> getSlotNumbersOfMatchingItems(IInventory inv, Item item, int damage)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.getItem() == item && stack.getItemDamage() == damage)
            {
                slots.add(Integer.valueOf(i));
            }
        }

        return slots;
    }

    /**
     * Get all the slot numbers that have matching ItemStacks (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @param inv
     * @param item
     * @return an ArrayList containing the slot numbers of the slots with matching ItemStacks
     */
    public static List<Integer> getSlotNumbersOfMatchingItemStacks(IInventory inv, ItemStack stackIn)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if ((stack == null && stackIn == null)
                || (stack != null && stackIn != null && stack.isItemEqual(stackIn) == true && ItemStack.areItemStackTagsEqual(stack, stackIn) == true))
            {
                slots.add(Integer.valueOf(i));
            }
        }

        return slots;
    }

    /**
     * Returns the total number of stored items in the containerStack.
     * @param containerStack
     * @return
     */
    public static int getTotalNumberOfStoredItems(ItemStack containerStack)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack);
        if (nbtTagList == null)
        {
            return 0;
        }

        int count = 0;
        int num = nbtTagList.tagCount();
        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);

            if (tag.hasKey("CountReal", Constants.NBT.TAG_INT))
            {
                count += tag.getInteger("CountReal");
            }
            else
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                if (stack != null)
                {
                    count += stack.stackSize;
                }
            }
        }

        return count;
    }

    /**
     * Reads the display names of all the stored items in <b>containerStack</b>.
     */
    public static void readItemNamesFromContainerItem(ItemStack containerStack, List<String> listNames)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack);
        if (nbtTagList == null)
        {
            return;
        }

        int num = nbtTagList.tagCount();
        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

            if (stack != null)
            {
                listNames.add(stack.getDisplayName());
            }
        }
    }

    /**
     * Adds a formatted list of ItemStack sizes and the display names of the ItemStacks
     * to the <b>listLines</b> list. Returns the total number of items stored.
     * @param containerStack
     * @param listLines
     * @return total number of items stored
     */
    @SideOnly(Side.CLIENT)
    public static int getFormattedItemListFromContainerItem(ItemStack containerStack, List<String> listLines)
    {
        int itemCount = 0;
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack);

        if (nbtTagList != null && nbtTagList.tagCount() > 0)
        {
            int num = nbtTagList.tagCount();
            for (int i = 0; i < num; ++i)
            {
                NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
                if (tag != null)
                {
                    ItemStack tmpStack = ItemStack.loadItemStackFromNBT(tag);

                    if (tmpStack != null)
                    {
                        if (tag.hasKey("CountReal", Constants.NBT.TAG_INT) == true)
                        {
                            itemCount += tag.getInteger("CountReal");
                        }
                        else
                        {
                            itemCount += tmpStack.stackSize;
                        }

                        String preWhite = EnumChatFormatting.WHITE.toString();
                        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
                        listLines.add(String.format("  %s%4d%s %s", preWhite, tmpStack.stackSize, rst, tmpStack.getDisplayName()));
                    }
                }
            }
        }

        return itemCount;
    }

    /**
     * Reads the stored ItemStacks from the container ItemStack <b>containerStack</b> and stores
     * them in the array <b>items</b>. <b>Note:</b> The <b>items</b> array must have been allocated before calling this method!
     * @param containerStack
     * @param items
     */
    public static void readItemsFromContainerItem(ItemStack containerStack, ItemStack[] items)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack);
        if (nbtTagList == null)
        {
            return;
        }

        int num = nbtTagList.tagCount();
        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            byte slotNum = tag.getByte("Slot");

            if (slotNum >= 0 && slotNum < items.length)
            {
                items[slotNum] = ItemStack.loadItemStackFromNBT(tag);

                if (tag.hasKey("CountReal", Constants.NBT.TAG_INT))
                {
                    items[slotNum].stackSize = tag.getInteger("CountReal");
                }
            }
        }
    }

    /**
     * Writes the ItemStacks in <b>items</b> to the container ItemStack <b>containerStack</b>.
     * The items will be written inside a TAG_Compound named "Items".
     * @param containerStack
     * @param items
     */
    public static void writeItemsToContainerItem(ItemStack containerStack, ItemStack[] items)
    {
        NBTTagList nbtTagList = new NBTTagList();
        int invSlots = items.length;
        // Write all the ItemStacks into a TAG_List
        for (int slotNum = 0; slotNum < invSlots; ++slotNum)
        {
            if (items[slotNum] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)slotNum);
                tag.setInteger("CountReal", items[slotNum].stackSize);
                items[slotNum].writeToNBT(tag);
                nbtTagList.appendTag(tag);
            }
        }

        // Write the module list to the tool
        NBTTagCompound nbt = containerStack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        if (nbtTagList.tagCount() > 0)
        {
            nbt.setTag("Items", nbtTagList);
        }
        else
        {
            nbt.removeTag("Items");
        }

        // Strip empty compound tags
        if (nbt.hasNoTags() == true)
        {
            nbt = null;
        }

        containerStack.setTagCompound(nbt);
    }
}
