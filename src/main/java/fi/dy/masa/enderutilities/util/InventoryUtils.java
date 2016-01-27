package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

            for (int slotNum : slotsSrc)
            {
                if (slotNum >= slotMinSrc && slotNum <= slotMaxSrc)
                {
                    ItemStack stack = invSrc.getStackInSlot(slotNum);

                    if (stack != null && sidedSrc.canExtractItem(slotNum, stack, sideSrc) == true)
                    {
                        stack = tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit);
                        invSrc.setInventorySlotContents(slotNum, stack);

                        if (stack != null)
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

            for (int slotNum = slotMinSrc; slotNum <= max; slotNum++)
            {
                ItemStack stack = invSrc.getStackInSlot(slotNum);

                if (stack != null)
                {
                    stack = tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit);
                    invSrc.setInventorySlotContents(slotNum, stack);

                    if (stack != null)
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
        return tryMoveMatchingItemsWithinSlotRange(invSrc, invDst, sideSrc, sideDst, 0, invSrc.getSizeInventory() - 1, 0, invDst.getSizeInventory() - 1, ignoreStackLimit);
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

            for (int slotNum : slotsSrc)
            {
                if (slotNum >= slotMinSrc && slotNum <= slotMaxSrc)
                {
                    ItemStack stack = invSrc.getStackInSlot(slotNum);

                    if (stack != null && sidedSrc.canExtractItem(slotNum, stack, sideSrc) == true)
                    {
                        if (getSlotOfFirstMatchingItemStackWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst) != -1)
                        {
                            stack = tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit);
                            invSrc.setInventorySlotContents(slotNum, stack);
                        }

                        if (stack != null)
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

            for (int slotNum = slotMinSrc; slotNum <= max; slotNum++)
            {
                ItemStack stack = invSrc.getStackInSlot(slotNum);

                if (stack != null)
                {
                    if (getSlotOfFirstMatchingItemStackWithinSlotRange(invDst, stack, slotMinDst, slotMaxDst) != -1)
                    {
                        stack = tryInsertItemStackToInventoryWithinSlotRange(invDst, stack, sideDst, slotMinDst, slotMaxDst, ignoreStackLimit);
                        invSrc.setInventorySlotContents(slotNum, stack);
                    }

                    if (stack != null)
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
                            if (dstSlot >= slotMinDst && dstSlot <= slotMaxDst)
                            {
                                stack = tryInsertItemStackToSlot(invDst, stack, dstSlot, sideDst, ignoreStackLimit);
                                invSrc.setInventorySlotContents(i, stack);
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
                        if (dstSlot >= slotMinDst && dstSlot <= slotMaxDst)
                        {
                            stack = tryInsertItemStackToSlot(invDst, stack, dstSlot, ignoreStackLimit);
                            invSrc.setInventorySlotContents(i, stack);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * The return value is the stack of remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryWithinSlotRange(IInventory inv, ItemStack stackIn, int side, int slotMin, int slotMax)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, slotMin, slotMax, false);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory, inside the given slot range.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * The return value is the stack of remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     */
    public static ItemStack tryInsertItemStackToInventoryWithinSlotRange(IInventory inv, ItemStack stackIn, int side,
            int slotMin, int slotMax, boolean ignoreStackLimit)
    {
        // FIXME should we make a copy of the stack here?

        if (inv instanceof ISidedInventory)
        {
            ISidedInventory sided = (ISidedInventory) inv;
            int[] slots = sided.getAccessibleSlotsFromSide(side);

            // First try to add to existing stacks
            for (int i : slots)
            {
                if (i >= slotMin && i <= slotMax && sided.getStackInSlot(i) != null)
                {
                    stackIn = tryInsertItemStackToSlot(inv, stackIn, i, side, ignoreStackLimit);

                    if (stackIn == null)
                    {
                        return null;
                    }
                }
            }

            // Second round, try to add to any slot
            for (int i : slots)
            {
                if (i >= slotMin && i <= slotMax)
                {
                    stackIn = tryInsertItemStackToSlot(inv, stackIn, i, side, ignoreStackLimit);

                    if (stackIn == null)
                    {
                        return null;
                    }
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
                if (inv.getStackInSlot(i) != null)
                {
                    stackIn = tryInsertItemStackToSlot(inv, stackIn, i, ignoreStackLimit);

                    if (stackIn == null)
                    {
                        return null;
                    }
                }
            }

            // Second round, try to add to any slot
            for (int i = slotMin; i <= max; ++i)
            {
                stackIn = tryInsertItemStackToSlot(inv, stackIn, i, ignoreStackLimit);

                if (stackIn == null)
                {
                    return null;
                }
            }
        }

        return stackIn;
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * The return value is the stack of remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     *
     * @param inv The instance of IInventory or ISidedInventory
     * @param stackIn The ItemStack to try and insert into the inventory
     * @param side The side of the block we try to insert from, in case of ISidedInventory
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToInventory(IInventory inv, ItemStack stackIn, int side)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, 0, inv.getSizeInventory() - 1);
    }

    /**
     * Tries to insert the given ItemStack stack to the target inventory.
     * The method first checks for ISidedInventory and falls back to IInventory.
     * The return value is the stack of remaining items that couldn't be inserted.
     * If all items were successfully inserted, then null is returned.
     *
     * @param inv The instance of IInventory or ISidedInventory
     * @param stackIn The ItemStack to try and insert into the inventory
     * @param side The side of the block we try to insert from, in case of ISidedInventory
     * @param ignoreStackLimit Ignore the stack limit of the item, only use the inventory stack limit
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToInventory(IInventory inv, ItemStack stackIn, int side, boolean ignoreStackLimit)
    {
        return tryInsertItemStackToInventoryWithinSlotRange(inv, stackIn, side, 0, inv.getSizeInventory() - 1, ignoreStackLimit);
    }

    /**
     * Try insert the items in <b>stackIn</b> into existing stacks with identical items in the inventory <b>inv</b>.
     * @param inv
     * @param stackIn
     * @param side
     * @param ignoreStackLimit
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToExistingStacksInInventory(IInventory inv, ItemStack stackIn, int side, boolean ignoreStackLimit)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItemStacks(inv, stackIn);

        for (int slot : slots)
        {
            stackIn = tryInsertItemStackToSlot(inv, stackIn, slot, side, ignoreStackLimit);

            // If the entire (remaining) stack was inserted to the current slot, then we are done
            if (stackIn == null)
            {
                return null;
            }
        }

        return stackIn;
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then a stack with the remaining items is returned.
     * If all items were successfully inserted, then null is returned.
     * @param inv
     * @param stackIn
     * @param slotNum
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum)
    {
        return tryInsertItemStackToSlot(inv, stackIn, slotNum, false);
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then a stack with the remaining items is returned.
     * If all items were successfully inserted, then null is returned.
     * Set ignoreStackLimit to true to ignore the ItemStack#getMaxStackSize() and only check the IInventory#getInventoryStackLimit()
     * @param inv
     * @param stackIn
     * @param slotNum
     * @param ignoreStackLimit true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum, boolean ignoreStackLimit)
    {
        return tryInsertItemStackToSlot(inv, stackIn, slotNum, 0, ignoreStackLimit);
    }

    /**
     * Tries to insert the given ItemStack stackIn to the target inventory, to the specified slot slotNum.
     * If only some of the items were inserted, then a stack with the remaining items is returned.
     * If all items were successfully inserted, then null is returned.
     * Set ignoreStackLimit to true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * @param inv
     * @param stackIn
     * @param slotNum
     * @param side
     * @param ignoreStackLimit true to ignore the ItemStack.getMaxStackSize() and only check the IInventory.getInventoryStackLimit()
     * @return null if all items were successfully inserted, otherwise the stack containing the remaining items
     */
    public static ItemStack tryInsertItemStackToSlot(IInventory inv, ItemStack stackIn, int slotNum, int side, boolean ignoreStackLimit)
    {
        if (inv instanceof ISidedInventory)
        {
            if (isItemStackValidForSlot((ISidedInventory)inv, stackIn, slotNum, side) == false)
            {
                return stackIn;
            }
        }
        else if (isItemStackValidForSlot(inv, stackIn, slotNum) == false)
        {
            return stackIn;
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
                ItemStack stackTmp = stackIn.copy();
                stackTmp.stackSize = max;
                stackIn.stackSize -= max;
                inv.setInventorySlotContents(slotNum, stackTmp);

                return stackIn;
            }
            // The target slot can take the whole stack
            else if (max >= stackIn.stackSize)
            {
                inv.setInventorySlotContents(slotNum, stackIn.copy());

                return null;
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
                stackIn.stackSize -= num;
                inv.setInventorySlotContents(slotNum, targetStack);

                return stackIn.stackSize > 0 ? stackIn : null;
            }
        }

        return stackIn;
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
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
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
     * Returns the first ItemStack from the inventory that has the given Item in it, or null.
     */
    public static ItemStack getFirstMatchingItem(IInventory inv, Item item)
    {
        int slot = getSlotOfFirstMatchingItem(inv, item);
        return slot != -1 ? inv.getStackInSlot(slot) : null;
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
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
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
     * @return The slot number of the first slot with a matching ItemStack, or -1 if there were no matches.
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

            if (areItemStacksEqual(stack, stackIn) == true)
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
     * @return The slot number of the last slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfLastMatchingItemStack(IInventory inv, ItemStack stackIn)
    {
        return getSlotOfLastMatchingItemStackWithinSlotRange(inv, stackIn, 0, inv.getSizeInventory() - 1);
    }

    /**
     * Get the slot number of the last slot containing a matching ItemStack (including NBT, ignoring stackSize) within the given slot range.
     * Note: stackIn can be null.
     * @return The slot number of the last slot with a matching ItemStack, or -1 if there were no matches.
     */
    public static int getSlotOfLastMatchingItemStackWithinSlotRange(IInventory inv, ItemStack stackIn, int slotMin, int slotMax)
    {
        int max = Math.min(inv.getSizeInventory() - 1, slotMax);

        for (int i = max; i >= slotMin; --i)
        {
            ItemStack stack = inv.getStackInSlot(i);

            if (areItemStacksEqual(stack, stackIn) == true)
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
     * If <b>damage</b> is OreDictionary.WILDCARD_VALUE, then the item damage is ignored.
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

            if (areItemStacksEqual(stack, stackIn) == true)
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

            if (stack != null && uuid.equals(NBTUtils.getUUIDFromItemStack(stack, containerTagName, false)) == true)
            {
                return stack;
            }
        }

        return null;
    }

    /**
     * Collects items from the inventory that are identical to stackTemplate and makes a new ItemStack
     * out of them, up to stackSize = maxAmount. If <b>reverse</b> is true, then the items are collected
     * starting from the end of the given inventory.
     * If no matching items are found, null is returned.
     */
    public static ItemStack collectItemsFromInventory(IInventory inv, ItemStack stackTemplate, int maxAmount, boolean reverse)
    {
        ItemStack stack = stackTemplate.copy();
        stack.stackSize = 0;

        int inc = (reverse == true ? -1 : 1);
        int start = (reverse == true ? (inv.getSizeInventory() - 1) : 0);

        for (int i = start; i >= 0 && i < inv.getSizeInventory() && stack.stackSize < maxAmount; i += inc)
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
            maxStackSize = Math.min(maxStackSize, stackTemplate.getMaxStackSize());
        }

        // Get our initial collected stack from the target inventory
        ItemStack stack = collectItemsFromInventory(invTarget, stackTemplate, maxStackSize, true);

        // Move all the remaining identical items to the storage inventory
        List<Integer> slots = getSlotNumbersOfMatchingItemStacks(invTarget, stack);

        for (int slot : slots)
        {
            ItemStack stackTmp = invTarget.getStackInSlot(slot);
            stackTmp = tryInsertItemStackToInventory(invStorage, stackTmp, 0, ignoreStackLimitOnStorage);
            invTarget.setInventorySlotContents(slot, stackTmp);
        }

        // If the initial collected stack wasn't full, try to fill it from the storage inventory
        if (stack != null && stack.stackSize < maxStackSize)
        {
            ItemStack stackTmp = collectItemsFromInventory(invStorage, stack, maxStackSize - stack.stackSize, true);

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
     * @param ignoreStackLimitOnStorage set to true to ignore the ItemStack's stack limit and only use the inventory stack limit
     */
    public static void leaveOneFullStackOfEveryItem(IInventory invTarget, IInventory invStorage, boolean reverse,
        boolean ignoreStackLimitOnTarget, boolean ignoreStackLimitOnStorage)
    {
        int inc = (reverse == true ? -1 : 1);
        int start = (reverse == true ? (invTarget.getSizeInventory() - 1) : 0);

        for (int i = start; i >= 0 && i < invTarget.getSizeInventory(); i += inc)
        {
            ItemStack stack = invTarget.getStackInSlot(i);

            if (stack != null)
            {
                stack = collectOneStackAndMoveOthers(invTarget, invStorage, stack, ignoreStackLimitOnTarget, ignoreStackLimitOnStorage);
                invTarget.setInventorySlotContents(i, stack);
            }
        }
    }

    /**
     * Checks if there is a matching ItemStack in the inventory inside the given slot range.
     */
    public static boolean matchingStackFoundInSlotRange(IInventory inv, SlotRange slotRange, ItemStack stackTemplate, boolean ignoreMeta, boolean ignoreNbt)
    {
        if (stackTemplate == null)
        {
            return false;
        }

        Item item = stackTemplate.getItem();
        int meta = stackTemplate.getItemDamage();
        int end = Math.min(slotRange.lastExc, inv.getSizeInventory());

        for (int i = slotRange.first; i < end; i++)
        {
            ItemStack stackTmp = inv.getStackInSlot(i);
            if (stackTmp == null || stackTmp.getItem() != item)
            {
                continue;
            }

            if (ignoreMeta == false && (meta != OreDictionary.WILDCARD_VALUE && stackTmp.getItemDamage() != meta))
            {
                continue;
            }

            if (ignoreNbt == false && ItemStack.areItemStackTagsEqual(stackTemplate, stackTmp) == false)
            {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * @param inv
     * @return true if all the slots in the inventory are empty, ie. null
     */
    public static boolean isInventoryEmpty(IInventory inv)
    {
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            if (inv.getStackInSlot(i) != null)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the minimum stack size from the inventory <b>inv</b> from
     * stacks that are not empty, or -1 if all stacks are empty.
     * @param inv
     * @return minimum stack size from the inventory, or -1 if all stacks are empty
     */
    public static int getMinNonEmptyStackSize(IInventory inv)
    {
        int minSize = -1;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && (minSize < 0 || stack.stackSize < minSize))
            {
                minSize = stack.stackSize;
            }
        }

        return minSize;
    }

    /**
     * Checks if the given inventory <b>inv</b> has at least <b>amount</b> number of items
     * matching the item in <b>stackTemplate</b>.
     */
    public static boolean checkInventoryHasItems(IInventory inv, ItemStack stackTemplate, int amount)
    {
        int found = 0;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stackTmp = inv.getStackInSlot(i);

            if (stackTmp != null && areItemStacksEqual(stackTmp, stackTemplate) == true)
            {
                found += stackTmp.stackSize;
            }

            if (found >= amount)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the inventory <b>invStorage</b> has all the items from the other inventory <b>invTemplate</b>
     * in at least the amountPerStack quantity per each stack from the template inventory.
     */
    public static boolean checkInventoryHasAllItems(IInventory invStorage, IInventory invTemplate, int amountPerStack)
    {
        Map<ItemType, Integer> quantities = new HashMap<ItemType, Integer>();

        // First get the sum of all the items required based on the template inventory
        for (int i = 0; i < invTemplate.getSizeInventory(); i++)
        {
            ItemStack stackTmp = invTemplate.getStackInSlot(i);

            if (stackTmp != null)
            {
                ItemType item = new ItemType(stackTmp);
                Integer amount = quantities.get(item);
                amount = (amount != null) ? amount + amountPerStack : amountPerStack;
                quantities.put(item, Integer.valueOf(amount));
            }
        }

        // Then check if the storage inventory has the required amount of each of those items
        Set<ItemType> items = quantities.keySet();
        for (ItemType item : items)
        {
            Integer amount = quantities.get(item);
            if (amount != null)
            {
                if (checkInventoryHasItems(invStorage, item.getStack(), amount) == false)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Creates a copy of the whole inventory and returns it in a new ItemStack array.
     * @param inv
     * @return an array of ItemStacks containing a copy of the entire inventory
     */
    public static ItemStack[] createInventorySnapshot(IInventory inv)
    {
        ItemStack[] items = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < items.length; i++)
        {
            items[i] = ItemStack.copyItemStack(inv.getStackInSlot(i));
        }

        return items;
    }

    /**
     * Adds amountPerStack items to all the stacks in invTarget based on the template inventory contents array <b>template</b>.
     * If the existing stack doesn't match the template, then nothing will be added to that stack.
     * If the existing stack is null, then it will be set to a new stack based on the template.
     * All the items are taken from the inventory <b>invStorage</b>.
     * If emptySlotsOnly is true, then only slots that are empty in the target inventory will be re-stocked.
     * @param invTarget
     * @param invStorage
     * @param template
     * @param amountPerStack
     * @param emptySlotsOnly
     * @return true if ALL the items from the template inventory contents and in the quantity amountPerStack were successfully added
     */
    public static boolean restockInventoryBasedOnTemplate(IInventory invTarget, IInventory invStorage, ItemStack[] template,
            int amountPerStack, boolean emptySlotsOnly)
    {
        int i = 0;
        int amount = 0;
        boolean allSuccess = true;

        for (i = 0; i < template.length && i < invTarget.getSizeInventory(); i++)
        {
            if (template[i] == null)
            {
                continue;
            }

            ItemStack stackExisting = invTarget.getStackInSlot(i);

            if (emptySlotsOnly == true && stackExisting != null)
            {
                continue;
            }

            if (stackExisting != null && areItemStacksEqual(stackExisting, template[i]) == false)
            {
                allSuccess = false;
                continue;
            }

            amount = Math.min(invTarget.getInventoryStackLimit(), template[i].getMaxStackSize());

            if (stackExisting != null)
            {
                amount = Math.max(amount - stackExisting.stackSize, 0);
            }

            amount = Math.min(amount, amountPerStack);

            if (amount <= 0)
            {
                allSuccess = false;
                continue;
            }

            ItemStack stackNew = collectItemsFromInventory(invStorage, template[i], amount, false);

            if (stackNew == null)
            {
                allSuccess = false;
                continue;
            }

            if (stackNew.stackSize < amount)
            {
                allSuccess = false;
            }

            if (stackExisting != null)
            {
                stackNew.stackSize += stackExisting.stackSize;
            }

            invTarget.setInventorySlotContents(i, stackNew);
        }

        return allSuccess;
    }
}
