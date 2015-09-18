package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

            // First try to add to existing stacks
            for (int i : slots)
            {
                if (sided.getStackInSlot(i) != null && isItemStackValidForSlot(sided, stackIn, i, side) && tryInsertItemStackToSlot(inv, stackIn, i) == true)
                {
                    return true;
                }
            }

            // Second round, try to add to any slot
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
            // First try to add to existing stacks
            for (int i = 0; i < size; ++i)
            {
                if (inv.getStackInSlot(i) != null && isItemStackValidForSlot(inv, stackIn, i) && tryInsertItemStackToSlot(inv, stackIn, i) == true)
                {
                    return true;
                }
            }

            // Second round, try to add to any slot
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
     * Get the ItemStack that has the given UUID stored in its NBT. If <b>containerTagName</b>
     * is not null, then the UUID is read from a compound tag by that name.
     */
    public static ItemStack getItemStackByUUID(IInventory inv, UUID uuid, String containerTagName)
    {
        int size = inv.getSizeInventory();
        for (int i = 0; i < size; i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && uuid.equals(NBTUtils.getUUIDFromItemStack(stack, containerTagName)))
            {
                return stack;
            }
        }

        return null;
    }
}
