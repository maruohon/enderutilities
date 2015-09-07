package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

public class ContainerHandyBag extends Container
{
    public EntityPlayer player;
    public final InventoryItemModular inventory;

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public IInventory craftResult = new InventoryCraftResult();

    public ContainerHandyBag(EntityPlayer player, InventoryItemModular inv)
    {
        this.player = player;
        this.inventory = inv;

        if (inventory.getSizeInventory() > 0)
        {
            this.addSlots();
        }

        this.bindPlayerInventory(player.inventory);
    }

    protected void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        int xOff = 8;
        int yOff = 157;

        // Player main inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(playerInventory, i * 9 + j + 9, xOff + j * 18, yOff + i * 18));
            }
        }

        // Player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, xOff + i * 18, yOff + 58));
        }

        yOff = 8;
        // Player armor slots
        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new Slot(playerInventory, 36 + i, xOff, yOff + i * 18));
        }

        // Player crafting slots
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrix, this.craftResult, 0, 152, 18));

        xOff = 96;
        yOff = 8;
        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, xOff + j * 18, yOff + i * 18));
            }
        }
    }

    protected void addSlots()
    {
        int moduleSlots = this.inventory.getStorageModuleCount();
        int xOff = 8;
        int yOff = 83;

        // The top/middle section of the bag inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new Slot(this.inventory, i * 9 + j, xOff + j * 18, yOff + i * 18));
            }
        }

        // TODO: Add second tier bag extra slots here

        xOff = 98;
        yOff = 55;
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new Slot(this.inventory, i + 27, xOff + i * 18, yOff));
        }
    }

    public int getBagTier()
    {
        return this.inventory.getContainerItemStack().getItemDamage();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return this.inventory.isUseableByPlayer(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        this.inventory.closeInventory();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
    {
        ItemStack stack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotNum);
        int invSize = this.inventory.getSizeInventory();

        // Slot clicked on has items
        if (slot != null && slot.getHasStack() == true)
        {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();

            // Clicked on a slot is in the "external" inventory
            if (slotNum < invSize)
            {
                // Try to merge the stack into the player inventory
                if (this.mergeItemStack(stackInSlot, invSize, this.inventorySlots.size(), true) == false)
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

    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
    {
        boolean successful = false;
        int slotIndex = slotStart;
        int maxStack = Math.min(stack.getMaxStackSize(), this.inventory.getInventoryStackLimit());

        if (reverse)
        {
            slotIndex = slotRange - 1;
        }

        Slot slot;
        ItemStack existingStack;

        if (stack.isStackable() == true)
        {
            while (stack.stackSize > 0 && (reverse == false && slotIndex < slotRange || reverse == true && slotIndex >= slotStart))
            {
                slot = (Slot)this.inventorySlots.get(slotIndex);
                maxStack = Math.min(slot.getSlotStackLimit(), Math.min(stack.getMaxStackSize(), this.inventory.getInventoryStackLimit()));
                existingStack = slot.getStack();

                if (slot.isItemValid(stack) == true && existingStack != null
                    && existingStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, existingStack))
                {
                    int combinedSize = existingStack.stackSize + stack.stackSize;

                    if (combinedSize <= maxStack)
                    {
                        stack.stackSize = 0;
                        existingStack.stackSize = combinedSize;
                        slot.putStack(existingStack); // Needed to call the setInventorySlotContents() method, which does special things on some machines
                        successful = true;
                    }
                    else if (existingStack.stackSize < maxStack)
                    {
                        stack.stackSize -= maxStack - existingStack.stackSize;
                        existingStack.stackSize = maxStack;
                        slot.putStack(existingStack); // Needed to call the setInventorySlotContents() method, which does special things on some machines
                        successful = true;
                    }
                }

                if (reverse == true)
                {
                    --slotIndex;
                }
                else
                {
                    ++slotIndex;
                }
            }
        }

        if (stack.stackSize > 0)
        {
            if (reverse == true)
            {
                slotIndex = slotRange - 1;
            }
            else
            {
                slotIndex = slotStart;
            }

            while (reverse == false && slotIndex < slotRange || reverse == true && slotIndex >= slotStart)
            {
                slot = (Slot)this.inventorySlots.get(slotIndex);
                maxStack = Math.min(slot.getSlotStackLimit(), Math.min(stack.getMaxStackSize(), this.inventory.getInventoryStackLimit()));
                existingStack = slot.getStack();

                if (slot.isItemValid(stack) == true && existingStack == null)
                {
                    if (stack.stackSize > maxStack)
                    {
                        ItemStack newStack = stack.copy();
                        newStack.stackSize = maxStack;
                        stack.stackSize -= maxStack;
                        slot.putStack(newStack);
                    }
                    else
                    {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                    }
                    successful = true;
                    break;
                }

                if (reverse == true)
                {
                    --slotIndex;
                }
                else
                {
                    ++slotIndex;
                }
            }
        }

        return successful;
    }
}
