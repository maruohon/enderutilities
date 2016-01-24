package fi.dy.masa.enderutilities.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerEnderUtilities extends Container
{
    public final EntityPlayer player;
    public final InventoryPlayer inventoryPlayer;
    public final IInventory inventory;
    protected SlotRange customInventorySlots;
    protected SlotRange playerArmorSlots;
    protected SlotRange playerMainSlots;
    protected List<SlotRange> mergeSlotRangesExtToPlayer;
    protected List<SlotRange> mergeSlotRangesPlayerToExt;

    public ContainerEnderUtilities(EntityPlayer player, IInventory inventory)
    {
        this.player = player;
        this.inventoryPlayer = player.inventory;
        this.inventory = inventory;
        this.mergeSlotRangesExtToPlayer = new ArrayList<SlotRange>();
        this.mergeSlotRangesPlayerToExt = new ArrayList<SlotRange>();
        this.customInventorySlots = new SlotRange(0, 0); // Init the ranges to an empty range by default
        this.playerArmorSlots = new SlotRange(0, 0);
        this.playerMainSlots = new SlotRange(0, 0);
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
        // This should usually be sufficient, assuming the custom slots are added first
        //this.customInventorySlots = new SlotRange(0, this.inventorySlots.size());

        int playerInvStart = this.inventorySlots.size();

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

        this.playerMainSlots = new SlotRange(playerInvStart, 36);
    }

    public EntityPlayer getPlayer()
    {
        return this.player;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return this.inventory.isUseableByPlayer(player);
    }

    public boolean isSlotInRange(SlotRange range, int slotNum)
    {
        return slotNum >= range.first && slotNum < range.lastExc;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
    {
        this.transferStackFromSlot(player, slotNum);
        return null;
    }

    /**
     * Transfers the stack from the given slot into other parts of the inventory,
     * or other inventories in this Container.
     * The player's inventory and the armor slots have highest "swap priority",
     * after that come player inventory to the "priority slots" that can be added to
     * the list of "priority slot" SlotRanges, and after that come the rest of the "custom inventory".
     * Returns false if no items were moved, true otherwise
     */
    public boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        boolean ret = false;
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;
        if (slot == null || slot.getHasStack() == false)
        {
            return false;
        }

        // From player armor slot to player main inventory
        if (this.isSlotInRange(this.playerArmorSlots, slotNum) == true)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, this.playerMainSlots.first, this.playerMainSlots.lastExc, false);
        }
        // From player main inventory to armor slot or the "external" inventory
        else if (this.isSlotInRange(this.playerMainSlots, slotNum) == true)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, this.playerArmorSlots.first, this.playerArmorSlots.lastExc, false);
            ret |= this.transferStackToPrioritySlots(player, slotNum, false);
            ret |= this.transferStackToSlotRange(player, slotNum, this.customInventorySlots.first, this.customInventorySlots.lastExc, false);
        }
        // From external inventory to player inventory
        else
        {
            ret |= this.transferStackToSlotRange(player, slotNum, this.playerMainSlots.first, this.playerMainSlots.lastExc, false);
        }

        return ret;
    }

    public boolean transferStackToPrioritySlots(EntityPlayer player, int slotNum, boolean reverse)
    {
        boolean ret = false;

        for (SlotRange slotRange : this.mergeSlotRangesPlayerToExt)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, slotRange.first, slotRange.lastExc, reverse);
        }

        return ret;
    }

    public boolean transferStackToSlotRange(EntityPlayer player, int slotNum, int slotStart, int slotEndExclusive, boolean reverse)
    {
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;
        if (slot == null || slot.getHasStack() == false)
        {
            return false;
        }

        ItemStack stack = slot.getStack();
        if (this.mergeItemStack(stack, slotStart, slotEndExclusive, reverse) == false)
        {
            return false;
        }

        if (stack.stackSize <= 0)
        {
            slot.putStack(null);
        }

        slot.onPickupFromSlot(player, stack);

        return true;
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

    public void addMergeSlotRangeExtToPlayer(int start, int numSlots)
    {
        this.mergeSlotRangesExtToPlayer.add(new SlotRange(start, numSlots));
    }

    public void addMergeSlotRangePlayerToExt(int start, int numSlots)
    {
        this.mergeSlotRangesPlayerToExt.add(new SlotRange(start, numSlots));
    }
}
