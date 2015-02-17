package fi.dy.masa.enderutilities.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

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

            for (int i = 0; i < slots.length; ++i)
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
                inv.setInventorySlotContents(slotNum, stackIn);
                return true;
            }
        }
        // The target slot has identical item
        else if (stackIn != null && stackIn.isItemEqual(targetStack) == true && ItemStack.areItemStackTagsEqual(stackIn, targetStack) == true)
        {
            // How much more can the target stack take
            int num = Math.min(inv.getInventoryStackLimit(), targetStack.getMaxStackSize()) - targetStack.stackSize;
            // How much can we add and how much do we have
            num = Math.min(num, stackIn.stackSize);

            if (num > 0)
            {
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
        return (sided.canInsertItem(slotNum, stackIn, side) && sided.isItemValidForSlot(slotNum, stackIn));
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
}
