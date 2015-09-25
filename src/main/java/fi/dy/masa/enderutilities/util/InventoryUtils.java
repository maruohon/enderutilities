package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class InventoryUtils
{
    /**
     * Tries to move all items from the inventory invSrc into invDst within the provided slot range.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveAllItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst)
    {
        return tryMoveAllItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, slotMinSrc, slotMaxSrc, slotMinDst, slotMaxDst, false);
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst within the provided slot range.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveAllItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst, boolean ignoreStackLimit)
    {
        boolean movedAll = true;

        if (invSrc instanceof ISidedInventory)
        {
            ISidedInventory sidedSrc = (ISidedInventory) invSrc;
            int[] slotsSrc = sidedSrc.getAccessibleSlotsFromSide(sideSrc);

            for (int i : slotsSrc)
            {
                if (i >= slotMinSrc && i <= slotMaxSrc)
                {
                    ItemStack stack = invSrc.getStackInSlot(i);
                    if (stack != null)
                    {
                        if (sidedSrc.canExtractItem(i, stack, sideSrc) == true)
                        {
                            if (tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit) == true)
                            {
                                invSrc.setInventorySlotContents(i, null);
                            }
                            else
                            {
                                movedAll = false;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            int max = Math.min(slotMaxSrc, invSrc.getSizeInventory() - 1);
            for (int i = slotMinSrc; i <= max; i++)
            {
                ItemStack stack = invSrc.getStackInSlot(i);
                if (stack != null)
                {
                    if (tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit) == true)
                    {
                        invSrc.setInventorySlotContents(i, null);
                    }
                    else
                    {
                        movedAll = false;
                    }
                }
            }
        }

        return movedAll;
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveAllItems(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst)
    {
        return tryMoveAllItems(invSrc, invDst, sideSrc, sideDst, false);
    }

    /**
     * Tries to move all items from the inventory invSrc into invDst.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveAllItems(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst, boolean ignoreStackLimit)
    {
        return tryMoveAllItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, 0, invSrc.getSizeInventory() - 1, 0, invDst.getSizeInventory() - 1, ignoreStackLimit);
    }

    /**
     * Tries to move matching/existing items from the inventory invSrc into invDst.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveMatchingItems(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst, boolean ignoreStackLimit)
    {
        return tryMoveMatchingItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, 0, invSrc.getSizeInventory() - 1, 0, invDst.getSizeInventory() - 1, false);
    }

    /**
     * Tries to move matching/existing items from the inventory invSrc into invDst within the provided slot range.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveMatchingItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst)
    {
        return tryMoveMatchingItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, slotMinSrc, slotMaxSrc, slotMinDst, slotMaxDst, false);
    }

    /**
     * Tries to move matching/existing items from the inventory invSrc into invDst within the provided slot range.
     * @return true if all items were successfully moved, false if none or just some were moved
     */
    public static boolean tryMoveMatchingItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst, boolean ignoreStackLimit)
    {
        boolean movedAll = true;

        if (invSrc instanceof ISidedInventory)
        {
            ISidedInventory sidedSrc = (ISidedInventory) invSrc;
            int[] slotsSrc = sidedSrc.getAccessibleSlotsFromSide(sideSrc);

            for (int i : slotsSrc)
            {
                if (i >= slotMinSrc && i <= slotMaxSrc)
                {
                    ItemStack stack = invSrc.getStackInSlot(i);
                    if (stack != null && getSlotOfFirstMatchingItemStackWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst) != -1
                        && sidedSrc.canExtractItem(i, stack, sideSrc) == true)
                    {
                        if (tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit) == true)
                        {
                            invSrc.setInventorySlotContents(i, null);
                        }
                        else
                        {
                            movedAll = false;
                        }
                    }
                }
            }
        }
        else
        {
            int max = Math.min(slotMaxSrc, invSrc.getSizeInventory() - 1);
            for (int i = slotMinSrc; i <= max; i++)
            {
                ItemStack stack = invSrc.getStackInSlot(i);
                if (stack != null)
                {
                    if (getSlotOfFirstMatchingItemStackWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst) != -1
                        && tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit) == true)
                    {
                        invSrc.setInventorySlotContents(i, null);
                    }
                    else
                    {
                        movedAll = false;
                    }
                }
            }
        }

        return movedAll;
    }

    /**
     * Tries to fill all the existing stacks in invDst from invSrc.
     */
    public static void fillStacksOfMatchingItems(IInventory invSrc, IInventory invDst)
    {
        fillStacksOfMatchingItemsWithinSlotRange(invSrc, invDst, 0, 0, 0, invSrc.getSizeInventory() - 1, 0, invDst.getSizeInventory() - 1, false);
    }

    /**
     * Tries to fill all the existing stacks in invDst from invSrc within the provided slot ranges.
     */
    public static void fillStacksOfMatchingItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst)
    {
        fillStacksOfMatchingItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, slotMinSrc, slotMaxSrc, slotMinDst, slotMaxDst, false);
    }

    /**
     * Tries to fill all the existing stacks in invDst from invSrc within the provided slot ranges.
     * Set ignoreStackLimit to true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit().
     */
    public static void fillStacksOfMatchingItemsWithinSlotRange(IInventory invSrc, IInventory invDst, int sideSrc, int sideDst,
            int slotMinSrc, int slotMaxSrc, int slotMinDst, int slotMaxDst, boolean ignoreStackLimit)
    {
        if (invSrc instanceof ISidedInventory)
        {
            ISidedInventory sidedSrc = (ISidedInventory) invSrc;
            int[] slotsSrc = sidedSrc.getAccessibleSlotsFromSide(sideSrc);

            for (int i : slotsSrc)
            {
                if (i >= slotMinSrc && i <= slotMaxSrc)
                {
                    ItemStack stack = invSrc.getStackInSlot(i);
                    if (stack != null && sidedSrc.canExtractItem(i, stack, sideSrc) == true)
                    {
                        List<Integer> matchingSlots = getSlotNumbersOfMatchingItemStacksWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst);
                        for (int dstSlot : matchingSlots)
                        {
                            if (dstSlot >= slotMinDst && dstSlot <= slotMaxDst && tryInsertItemStackToSlot(invDst, stack, dstSlot, sideDst, ignoreStackLimit) == 0)
                            {
                                invSrc.setInventorySlotContents(i, null);
                            }
                        }
                    }
                }
            }
        }
        else
        {
            int max = Math.min(slotMaxSrc, invSrc.getSizeInventory() - 1);
            for (int i = slotMinSrc; i <= max; i++)
            {
                ItemStack stack = invSrc.getStackInSlot(i);
                if (stack != null)
                {
                    List<Integer> matchingSlots = getSlotNumbersOfMatchingItemStacksWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst);
                    for (int dstSlot : matchingSlots)
                    {
                        if (dstSlot >= slotMinDst && dstSlot <= slotMaxDst && tryInsertItemStackToSlot(invDst, stack, dstSlot, ignoreStackLimit) == 0)
                        {
                            invSrc.setInventorySlotContents(i, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * If the whole stack was successfully inserted into the inventory, true
     * is returned. If only some or if none of the items were inserted, false is returned.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     */
    public static boolean tryInsertItemStackToInventoryWithinSlotRange(IInventory inv, ItemStack stackIn, int side, int slotMin, int slotMax)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, slotMin, slotMax, false);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * Set ignoreStackLimit to true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit().
     * The method first checks for ISidedInventory and falls back to IInventory.
     * If the whole stack was successfully inserted into the inventory, true
     * is returned. If only some or if none of the items were inserted, false is returned.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     */
    public static boolean tryInsertItemStackToInventoryWithinSlotRange(IInventory inv, ItemStack stackIn, int side, int slotMin, int slotMax, boolean ignoreStackLimit)
    {
        if (inv instanceof ISidedInventory)
        {
            ISidedInventory sided = (ISidedInventory) inv;
            int[] slots = sided.getAccessibleSlotsFromSide(side);

            // First try to add to existing stacks
            for (int i : slots)
            {
                if (i >= slotMin && i <= slotMax && sided.getStackInSlot(i) != null && tryInsertItemStackToSlot(inv, stackIn, i, side, ignoreStackLimit) == 0)
                {
                    return true;
                }
            }

            // Second round, try to add to any slot
            for (int i : slots)
            {
                if (i >= slotMin && i <= slotMax && tryInsertItemStackToSlot(inv, stackIn, i, side, ignoreStackLimit) == 0)
                {
                    return true;
                }
            }
        }
        // IInventory
        else
        {
            int max = Math.min(slotMax, inv.getSizeInventory() - 1);
            // First try to add to existing stacks
            for (int i = slotMin; i <= max; ++i)
            {
                if (inv.getStackInSlot(i) != null && tryInsertItemStackToSlot(inv, stackIn, i, ignoreStackLimit) == 0)
                {
                    return true;
                }
            }

            // Second round, try to add to any slot
            for (int i = slotMin; i <= max; ++i)
            {
                if (tryInsertItemStackToSlot(inv, stackIn, i, ignoreStackLimit) == 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

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
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, 0, inv.getSizeInventory() - 1);
    }

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
     * @param ignoreStackLimit Ignore the stack limit of the item, only use the inventory stack limit
     * @return true if all items were successfully inserted, false if none or only some were
     */
    public static boolean tryInsertItemStackToInventory(IInventory inv, ItemStack stackIn, int side, boolean ignoreStackLimit)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, 0, inv.getSizeInventory() - 1, ignoreStackLimit);
    }

    /**
     * Try insert the items in <b>stackIn</b> into existing stacks with identical items in the inventory <b>inv</b>.
     * @param inv
     * @param stackIn
     * @param side
     * @param ignoreStackLimit
     * @return 0 if all items were inserted, 1 if some items were inserted, -1 if no items were inserted
     */
    public static int tryInsertItemStackToExistingStacksInInventory(IInventory inv, ItemStack stackIn, int side, boolean ignoreStackLimit)
    {
        int origStackSize = stackIn.stackSize;

        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItemStacks(inv, stackIn);
        for (int slot : slots)
        {
            // If the entire (remaining) stack was inserted to the current slot, then we are done
            if (tryInsertItemStackToSlot(inv, stackIn, slot, side, ignoreStackLimit) == 0)
            {
                return 0;
            }
        }

        return stackIn.stackSize != origStackSize ? 1 : -1;
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     * NOTE: DO NOT call this method with a null stackIn!
     * @param inv
     * @param stackIn
     * @param slotNum
     * @return -1 if nothing was moved, 0 if all items were moved, 1 if only some items were moved
     */
    public static int tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum)
    {
        return tryInsertItemStackToSlot(inv, stackIn, slotNum, false);
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     * Set ignoreStackLimit to true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * NOTE: DO NOT call this method with a null stackIn!
     * @param inv
     * @param stackIn
     * @param slotNum
     * @param ignoreStackLimit true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * @return -1 if nothing was moved, 0 if all items were moved, 1 if only some items were moved
     */
    public static int tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum, boolean ignoreStackLimit)
    {
        return tryInsertItemStackToSlot(inv, stackIn, slotNum, 0, ignoreStackLimit);
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then the stackSize of stackIn will be subtracted from
     * accordingly (ie. the stack contains the remaining items after the method returns).
     * Set ignoreStackLimit to true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * NOTE: DO NOT call this method with a null stackIn!
     * @param inv
     * @param stackIn
     * @param slotNum
     * @param side
     * @param ignoreStackLimit true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * @return -1 if nothing was moved, 0 if all items were moved, 1 if only some items were moved
     */
    public static int tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum, int side, boolean ignoreStackLimit)
    {
        if (inv instanceof ISidedInventory)
        {
            if (isItemStackValidForSlot((ISidedInventory)inv, stackIn, slotNum, side) == false)
            {
                return -1;
            }
        }
        else if (isItemStackValidForSlot(inv, stackIn, slotNum) == false)
        {
            return -1;
        }

        ItemStack targetStack = inv.getStackInSlot(slotNum);
        int max = inv.getInventoryStackLimit();

        if (ignoreStackLimit == false)
        {
            max = Math.min(max, stackIn.getMaxStackSize());
        }

        // Empty target slot
        if (targetStack == null)
        {
            // The target slot can't take the whole stack
            if (max > 0 && max < stackIn.stackSize)
            {
                ItemStack tmp = stackIn.copy();
                tmp.stackSize = max;
                inv.setInventorySlotContents(slotNum, tmp);
                stackIn.stackSize -= max;
                return 1;
            }
            // The target slot can take the whole stack
            else if (max >= stackIn.stackSize)
            {
                inv.setInventorySlotContents(slotNum, stackIn);
                return 0;
            }
        }
        // The target slot has identical item
        else if (stackIn.isItemEqual(targetStack) == true && ItemStack.areItemStackTagsEqual(stackIn, targetStack) == true)
        {
            // How much more can the target stack take, and how much does stackIn have
            int num = Math.min(max - targetStack.stackSize, stackIn.stackSize);

            if (num > 0)
            {
                targetStack.stackSize += num;
                // Call the method just in case something uses it to do special things
                inv.setInventorySlotContents(slotNum, targetStack);
                stackIn.stackSize -= num;

                // Return 0 if everything was successfully inserted
                return (stackIn.stackSize <= 0 ? 0 : 1);
            }
        }

        return -1;
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
        return getSlotOfFirstMatchingItem(inv, item, OreDictionary.WILDCARD_VALUE);
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
            if (stack != null && stack.getItem() == item && (stack.getItemDamage() == damage || damage == OreDictionary.WILDCARD_VALUE))
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
        return getSlotOfLastMatchingItem(inv, item, OreDictionary.WILDCARD_VALUE);
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
            if (stack != null && stack.getItem() == item && (stack.getItemDamage() == damage || damage == OreDictionary.WILDCARD_VALUE))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there are no matching ItemStacks in the inventory.
     */
    public static int getSlotOfFirstMatchingItemStack(IInventory inv, ItemStack stackIn)
    {
        return getSlotOfFirstMatchingItemStackWithinSlotRange(inv, stackIn, 0, inv.getSizeInventory() - 1);
    }

    /**
     * Get the slot number of the first slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfFirstMatchingItemStackWithinSlotRange(IInventory inv, ItemStack stackIn, int slotMin, int slotMax)
    {
        int max = Math.min(inv.getSizeInventory() - 1, slotMax);
        for (int i = slotMin; i <= max; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if ((stack != null && stackIn != null && stack.isItemEqual(stackIn) == true && ItemStack.areItemStackTagsEqual(stack, stackIn) == true)
                || (stack == null && stackIn == null))
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
        return getSlotOfLastMatchingItemStackWithinSlotRange(inv, stackIn, 0, inv.getSizeInventory() - 1);
    }

    /**
     * Get the slot number of the last slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfLastMatchingItemStackWithinSlotRange(IInventory inv, ItemStack stackIn, int slotMin, int slotMax)
    {
        int max = Math.min(inv.getSizeInventory() - 1, slotMax);
        for (int i = max; i >= slotMin; --i)
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
        return getSlotNumbersOfMatchingItems(inv, item, OreDictionary.WILDCARD_VALUE);
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
            if (stack != null && stack.getItem() == item && (stack.getItemDamage() == damage || damage == OreDictionary.WILDCARD_VALUE))
            {
                slots.add(Integer.valueOf(i));
            }
        }

        return slots;
    }

    /**
     * Get all the slot numbers that have matching ItemStacks (including NBT, ignoring stackSize).
     * Note: stackIn can be null.
     * @return an ArrayList containing the slot numbers of the slots with matching ItemStacks
     */
    public static List<Integer> getSlotNumbersOfMatchingItemStacks(IInventory inv, ItemStack stackIn)
    {
        return getSlotNumbersOfMatchingItemStacksWithinSlotRange(inv, stackIn, 0, inv.getSizeInventory() - 1);
    }

    /**
     * Get all the slot numbers that have matching ItemStacks (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return an ArrayList containing the slot numbers of the slots with matching ItemStacks
     */
    public static List<Integer> getSlotNumbersOfMatchingItemStacksWithinSlotRange(IInventory inv, ItemStack stackIn, int slotMin, int slotMax)
    {
        List<Integer> slots = new ArrayList<Integer>();
        int max = Math.min(inv.getSizeInventory() - 1, slotMax);

        for (int i = slotMin; i <= max; ++i)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if ((stack != null && stackIn != null && stack.isItemEqual(stackIn) == true && ItemStack.areItemStackTagsEqual(stack, stackIn) == true)
                || (stack == null && stackIn == null))
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

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. The items are collected starting from the end of the given inventory.
     * If no matching items are found, a null ItemStack is returned.
     */
    public static ItemStack collectItemsFromInventory(IInventory inv, ItemStack stackTemplate, int maxAmount)
    {
        ItemStack stack = stackTemplate.copy();
        stack.stackSize = 0;
        for (int i = inv.getSizeInventory() - 1; i >= 0 && stack.stackSize < maxAmount; i--)
        {
            ItemStack stackTmp = inv.getStackInSlot(i);
            if (areItemStacksEqual(stackTmp, stackTemplate) == true)
            {
                int num = Math.min(maxAmount - stack.stackSize, stackTmp.stackSize);
                stack.stackSize += num;
                stackTmp.stackSize -= num;
                inv.setInventorySlotContents(i, stackTmp.stackSize > 0 ? stackTmp : null);
            }
        }

        return stack.stackSize > 0 ? stack : null;
    }

    /**
     * Collects one stack of items that are identical to stackTemplate, and fills that stack as full as possible
     * first from invTarget and if it's still not full, then also from invStorage.
     * @param invTarget
     * @param invStorage
     * @param stackTemplate
     * @param ignoreStackLimitOnTarget
     * @param ignoreStackLimitOnStorage
     * @return
     */
    public static ItemStack collectOneStackAndMoveOthers(IInventory invTarget, IInventory invStorage, ItemStack stackTemplate,
        boolean ignoreStackLimitOnTarget, boolean ignoreStackLimitOnStorage)
    {
        int maxStackSize = invTarget.getInventoryStackLimit();

        if (ignoreStackLimitOnTarget == false)
        {
            Math.min(maxStackSize, stackTemplate.getMaxStackSize());
        }

        // Get our initial collected stack from the target inventory
        ItemStack stack = collectItemsFromInventory(invTarget, stackTemplate, maxStackSize);

        // Move all the remaining identical items to the storage inventory
        List<Integer> slots = getSlotNumbersOfMatchingItemStacks(invTarget, stack);
        for (int slot : slots)
        {
            ItemStack stackTmp = invTarget.getStackInSlot(slot);
            if (tryInsertItemStackToInventory(invStorage, stackTmp, 0, ignoreStackLimitOnStorage) == true)
            {
                invTarget.setInventorySlotContents(slot, null);
            }
            else
            {
                invTarget.setInventorySlotContents(slot, stackTmp);
            }
        }

        // If the initial collected stack wasn't full, try to fill it from the storage inventory
        if (stack != null && stack.stackSize < maxStackSize)
        {
            ItemStack stackTmp = collectItemsFromInventory(invStorage, stack, maxStackSize - stack.stackSize);
            if (stackTmp != null)
            {
                stack.stackSize += stackTmp.stackSize;
            }
        }

        return stack;
    }

    /**
     * Loops through the invTarget inventory and leaves one stack of every item type found
     * and moves the rest to invStorage. The stacks are also first collected from invTarget
     * and filled as full as possible and if it's still not full, then more items are moved from invStorage.
     * @param invTarget the target inventory that will be cleaned up and where the filled stacks are left in
     * @param invStorage the "external" inventory where the excess items are moved to
     * @param reverse set to true to start the looping from the end of invTarget and thus leave the last stack of each item
     * @param ignoreStackLimitOnTarget set to true to ignore the ItemStack's stack limit and only use the inventory stack limit
     * @param ignoreStackLimitOnSTorage set to true to ignore the ItemStack's stack limit and only use the inventory stack limit
     */
    public static void leaveOneFullStackOfEveryItem(IInventory invTarget, IInventory invStorage, boolean reverse,
        boolean ignoreStackLimitOnTarget, boolean ignoreStackLimitOnStorage)
    {
        if (reverse == false)
        {
            for (int i = 0; i < invTarget.getSizeInventory(); i++)
            {
                ItemStack stack = invTarget.getStackInSlot(i);
                if (stack != null)
                {
                    stack = collectOneStackAndMoveOthers(invTarget, invStorage, stack, ignoreStackLimitOnTarget, ignoreStackLimitOnStorage);
                    invTarget.setInventorySlotContents(i, stack);
                }
            }
        }
        else
        {
            for (int i = invTarget.getSizeInventory() - 1; i >= 0; i--)
            {
                ItemStack stack = invTarget.getStackInSlot(i);
                if (stack != null)
                {
                    stack = collectOneStackAndMoveOthers(invTarget, invStorage, stack, ignoreStackLimitOnTarget, ignoreStackLimitOnStorage);
                    invTarget.setInventorySlotContents(i, stack);
                }
            }
        }
    }
}
