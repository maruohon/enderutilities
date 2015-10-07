package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEnderUtilities extends Container
{
    protected InventoryPlayer inventoryPlayer;
    protected IInventory inventory;

    public ContainerEnderUtilities(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        this.inventoryPlayer = inventoryPlayer;
        this.inventory = inventory;
    }

    /**
     * Adds the "custom" inventory slots to the container (ie. the inventory that this container is for).
     */
    protected void addCustomInventorySlots()
    {
    }

    /**
     * Adds the player inventory slots to the container.
     * posX and posY are the positions of the top-left-most slot of the player inventory.
     */
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        // Player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(this.inventoryPlayer, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        // Player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new Slot(this.inventoryPlayer, i, posX + i * 18, posY + 58));
        }
    }

    /**
     * Returns the number of inventory slots that are used when merging stacks when shift-clicking
     */
    protected int getNumMergableSlots(int invSize)
    {
        // Our inventory plus the player's inventory
        return invSize + 36;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return this.inventory.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
    {
        return this.transferStackInSlot(player, slotNum, this.inventory.getSizeInventory());
    }

    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum, int invSize)
    {
        ItemStack stack = null;
        Slot slot = this.getSlot(slotNum);

        // Slot clicked on has items
        if (slot != null && slot.getHasStack() == true)
        {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();

            // Clicked on a slot is in the "external" inventory
            if (slotNum < invSize)
            {
                // Try to merge the stack into the player inventory
                if (this.mergeItemStack(stackInSlot, invSize, this.getNumMergableSlots(invSize), true) == false)
                {
                    return null;
                }
            }
            // Clicked on slot is in the player inventory, try to merge the stack to the external inventory
            else if (this.mergeItemStack(stackInSlot, 0, invSize, false) == false)
            {
                return null;
            }

            // All items moved, empty the slot
            if (stackInSlot.stackSize == 0)
            {
                slot.putStack(null);
            }
            // Update the slot
            else
            {
                slot.onSlotChanged();
            }

            // No items were moved
            if (stackInSlot.stackSize == stack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }

    /**
     * Returns the maximum allowed stack size, based on the given ItemStack and the inventory's max stack size.
     */
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        return Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
    {
        boolean successful = false;
        int slotIndex = (reverse == true ? slotEndExclusive - 1 : slotStart);
        int maxSizeStackInv = 1;
        int maxSizeTmp = maxSizeStackInv;

        Slot slot;
        ItemStack existingStack;

        // First try to merge the stack into existing stacks in the container
        while (stack.stackSize > 0 && slotIndex >= slotStart && slotIndex < slotEndExclusive)
        {
            slot = this.getSlot(slotIndex);
            maxSizeStackInv = this.getMaxStackSizeFromSlotAndStack(slot, stack);
            maxSizeTmp = Math.min(slot.getSlotStackLimit(), maxSizeStackInv);
            existingStack = slot.getStack();

            if (slot.isItemValid(stack) == true && existingStack != null
                && existingStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, existingStack))
            {
                int combinedSize = existingStack.stackSize + stack.stackSize;

                if (combinedSize <= maxSizeTmp)
                {
                    stack.stackSize = 0;
                    existingStack.stackSize = combinedSize;
                    slot.putStack(existingStack); // Needed to call the setInventorySlotContents() method, which does special things on some machines
                    return true;
                }
                else if (existingStack.stackSize < maxSizeTmp)
                {
                    stack.stackSize -= maxSizeTmp - existingStack.stackSize;
                    existingStack.stackSize = maxSizeTmp;
                    slot.putStack(existingStack); // Needed to call the setInventorySlotContents() method, which does special things on some machines
                    successful = true;
                }
            }

            slotIndex = (reverse == true ? slotIndex - 1 : slotIndex + 1);
        }

        // If there are still items to merge after merging to existing stacks, then try to add it to empty slots
        if (stack.stackSize > 0)
        {
            slotIndex = (reverse == true ? slotEndExclusive - 1 : slotStart);

            while (slotIndex >= slotStart && slotIndex < slotEndExclusive)
            {
                slot = this.getSlot(slotIndex);
                maxSizeStackInv = this.getMaxStackSizeFromSlotAndStack(slot, stack);
                maxSizeTmp = Math.min(slot.getSlotStackLimit(), maxSizeStackInv);
                existingStack = slot.getStack();

                if (existingStack == null && slot.isItemValid(stack) == true)
                {
                    if (stack.stackSize <= maxSizeTmp)
                    {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        ItemStack newStack = stack.copy();
                        newStack.stackSize = maxSizeTmp;
                        stack.stackSize -= maxSizeTmp;
                        slot.putStack(newStack);
                        successful = true;
                    }
                }

                slotIndex = (reverse == true ? slotIndex - 1 : slotIndex + 1);
            }
        }

        return successful;
    }
}
