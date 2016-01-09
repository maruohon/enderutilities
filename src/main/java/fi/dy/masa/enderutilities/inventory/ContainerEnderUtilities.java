package fi.dy.masa.enderutilities.inventory;

import fi.dy.masa.enderutilities.util.InventoryUtils;
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
     * Adds the "custom inventory" slots to the container (ie. the inventory that this container is for).
     * This must be called before addPlayerInventorySlots() (ie. the order of slots in the container
     * is important for the transferStackInSlot() method)!
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
        ItemStack stackOrig = null;
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;

        // Slot clicked on has items
        if (slot != null && slot.getHasStack() == true && slot.canTakeStack(player) == true)
        {
            stackOrig = slot.getStack();
            ItemStack stackTmp = stackOrig.copy();

            // Clicked on a slot is in the "external" inventory
            if (slotNum < invSize)
            {
                // Try to merge the stack into the player inventory
                if (this.mergeItemStack(stackTmp, invSize, this.getNumMergableSlots(invSize), true) == false)
                {
                    return null;
                }
            }
            // Clicked on slot is in the player inventory, try to merge the stack to the external inventory
            else if (this.mergeItemStack(stackTmp, 0, invSize, false) == false)
            {
                return null;
            }

            // All items moved, empty the slot
            if (stackTmp.stackSize == 0)
            {
                slot.putStack(null);
            }
            // Update the slot
            else
            {
                slot.onSlotChanged();
            }

            // No items were moved FIXME this is redundant, right? In this case we should have already returned above
            if (stackTmp.stackSize == stackOrig.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, stackTmp);
        }

        return stackOrig;
    }

    /**
     * Returns the maximum allowed stack size, based on the given ItemStack and the inventory's max stack size.
     */
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        return stack != null ? Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize()) : slot.getSlotStackLimit();
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
    {
        boolean movedItems = false;
        int slotIndex = (reverse == true ? slotEndExclusive - 1 : slotStart);
        int maxSize = 1;

        Slot slot;
        ItemStack existingStack;

        // First try to merge the stack into existing stacks in the container
        while (stack.stackSize > 0 && slotIndex >= slotStart && slotIndex < slotEndExclusive)
        {
            slot = this.getSlot(slotIndex);
            existingStack = slot.getStack();
            maxSize = this.getMaxStackSizeFromSlotAndStack(slot, stack);

            if (existingStack != null && slot.isItemValid(stack) == true && InventoryUtils.areItemStacksEqual(stack, existingStack) == true)
            {
                if ((existingStack.stackSize + stack.stackSize) <= maxSize)
                {
                    existingStack.stackSize += stack.stackSize;
                    stack.stackSize = 0;
                    slot.putStack(existingStack); // Needed to call setInventorySlotContents() and markDirty()
                    return true;
                }
                else if (existingStack.stackSize < maxSize)
                {
                    stack.stackSize -= maxSize - existingStack.stackSize;
                    existingStack.stackSize = maxSize;
                    slot.putStack(existingStack); // Needed to call setInventorySlotContents() and markDirty()
                    movedItems = true;
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
                maxSize = this.getMaxStackSizeFromSlotAndStack(slot, stack);
                existingStack = slot.getStack();

                if (existingStack == null && slot.isItemValid(stack) == true)
                {
                    if (stack.stackSize <= maxSize)
                    {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                        return true;
                    }
                    else
                    {
                        ItemStack newStack = stack.copy();
                        newStack.stackSize = maxSize;
                        stack.stackSize -= maxSize;
                        slot.putStack(newStack);
                        movedItems = true;
                    }
                }

                slotIndex = (reverse == true ? slotIndex - 1 : slotIndex + 1);
            }
        }

        return movedItems;
    }
}
