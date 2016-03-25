package fi.dy.masa.enderutilities.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.util.SlotRange;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ContainerEnderUtilities extends Container
{
    public final EntityPlayer player;
    public final InventoryPlayer inventoryPlayer;
    public final IItemHandler inventory;
    protected MergeSlotRange customInventorySlots;
    protected MergeSlotRange playerArmorSlots;
    protected MergeSlotRange playerMainSlots;
    protected List<MergeSlotRange> mergeSlotRangesExtToPlayer;
    protected List<MergeSlotRange> mergeSlotRangesPlayerToExt;

    public ContainerEnderUtilities(EntityPlayer player, IItemHandler inventory)
    {
        this.player = player;
        this.inventoryPlayer = player.inventory;
        this.inventory = inventory;
        this.mergeSlotRangesExtToPlayer = new ArrayList<MergeSlotRange>();
        this.mergeSlotRangesPlayerToExt = new ArrayList<MergeSlotRange>();
        this.customInventorySlots = new MergeSlotRange(0, 0); // Init the ranges to an empty range by default
        this.playerArmorSlots = new MergeSlotRange(0, 0);
        this.playerMainSlots = new MergeSlotRange(0, 0);
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

        IItemHandlerModifiable inv = new PlayerMainInvWrapper(this.inventoryPlayer);
        // Player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(inv, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        // Player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(inv, i, posX + i * 18, posY + 58));
        }

        this.playerMainSlots = new MergeSlotRange(playerInvStart, 36);
    }

    public EntityPlayer getPlayer()
    {
        return this.player;
    }

    public SlotRange getPlayerMainInventorySlotRange()
    {
        return this.playerMainSlots;
    }

    public SlotRange getCustomInventorySlotRange()
    {
        return this.customInventorySlots;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot)
    {
        return (slot instanceof SlotItemHandler) && (slot instanceof SlotItemHandlerCraftresult) == false && this.inventoryPlayer.getItemStack() != null;
    }

    public boolean isSlotInRange(SlotRange range, int slotNum)
    {
        return slotNum >= range.first && slotNum < range.lastExc;
    }

    @Override
    public Slot getSlot(int slotId)
    {
        return slotId >= 0 && slotId < this.inventorySlots.size() ? super.getSlot(slotId) : null;
    }

    public SlotItemHandlerGeneric getSlotItemHandler(int slotId)
    {
        Slot slot = this.getSlot(slotId);

        return (slot instanceof SlotItemHandlerGeneric) ? (SlotItemHandlerGeneric) slot : null;
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
        Slot slot = this.getSlot(slotNum);
        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        // From player armor slot to player main inventory
        if (this.isSlotInRange(this.playerArmorSlots, slotNum) == true)
        {
            return this.transferStackToSlotRange(player, slotNum, this.playerMainSlots, false);
        }
        // From player main inventory to armor slot or the "external" inventory
        else if (this.isSlotInRange(this.playerMainSlots, slotNum) == true)
        {
            if (this.transferStackToSlotRange(player, slotNum, this.playerArmorSlots, false) == true)
            {
                return true;
            }

            if (this.transferStackToPrioritySlots(player, slotNum, false) == true)
            {
                return true;
            }

            return this.transferStackToSlotRange(player, slotNum, this.customInventorySlots, false);
        }

        // From external inventory to player inventory
        return this.transferStackToSlotRange(player, slotNum, this.playerMainSlots, true);
    }

    public boolean transferStackToPrioritySlots(EntityPlayer player, int slotNum, boolean reverse)
    {
        boolean ret = false;

        for (MergeSlotRange slotRange : this.mergeSlotRangesPlayerToExt)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, slotRange, reverse);
        }

        return ret;
    }

    public boolean transferStackToSlotRange(EntityPlayer player, int slotNum, MergeSlotRange slotRange, boolean reverse)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        ItemStack stack = slot.getStack().copy();
        int amount = Math.min(stack.stackSize, stack.getMaxStackSize());
        stack.stackSize = amount;

        // Simulate the merge
        stack = this.mergeItemStack(stack, slotRange, reverse, true);

        // If the item can't be put back to the slot, then we need to make sure that the whole
        // stack can be merged elsewhere before trying to (partially) merge it. Important for crafting slots!
        /*if (slot.isItemValid(stack) == false && stack != null)
        {
            return false;
        }*/

        // Could not merge anything
        if (stack != null && stack.stackSize == amount)
        {
            return false;
        }

        // Can merge at least some of the items, get the amount that can be merged
        amount = stack != null ? amount - stack.stackSize : amount;

        // Actually get the items for actual merging
        stack = slot.decrStackSize(amount);
        slot.onPickupFromSlot(player, stack);

        // Actually merge the items
        stack = this.mergeItemStack(stack, slotRange, reverse, false);

        // If they couldn't fit after all, then return them. This shouldn't happen, and will cause some issues like gaining XP from nothing.
        if (stack != null)
        {
            slot.insertItem(stack, false);
        }

        return true;
    }

    /**
     * Returns the maximum allowed stack size, based on the given ItemStack and the inventory's max stack size.
     */
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        return stack != null ? Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize()) : slot.getSlotStackLimit();
    }

    /**
     * This should NOT be called from anywhere in this mod, but just in case...
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
    {
        return false;
    }

    /**
     * Merge the given ItemStack to the slot range provided.
     * If simulate is true, then we are checking if the WHOLE stack can be merged.
     * @return If simulate is false, then true is returned if at least some of the items were merged.
     * If simulate is true, then true is returned only if ALL the items were successfully merged.
     */
    protected ItemStack mergeItemStack(ItemStack stack, MergeSlotRange slotRange, boolean reverse, boolean simulate)
    {
        int slotStart = slotRange.first;
        int slotEndExclusive = slotRange.lastExc;
        // FIXME this copy() is needed because the InvWrapper in Forge doesn't make a copy
        // if the whole input stack fits into an empty slot. A PR has been submitted to fix that.
        stack = stack.copy();
        int slotIndex = (reverse == true ? slotEndExclusive - 1 : slotStart);

        // First try to merge the stack into existing stacks in the container
        while (stack != null && slotIndex >= slotStart && slotIndex < slotEndExclusive)
        {
            SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

            if (slot != null && slot.getHasStack() == true && slot.isItemValid(stack) == true)
            {
                stack = slot.insertItem(stack, simulate);
            }

            slotIndex = (reverse == true ? slotIndex - 1 : slotIndex + 1);
        }

        // If there are still items to merge after merging to existing stacks, then try to add it to empty slots
        if (stack != null && slotRange.existingOnly == false)
        {
            slotIndex = (reverse == true ? slotEndExclusive - 1 : slotStart);

            while (stack != null && slotIndex >= slotStart && slotIndex < slotEndExclusive)
            {
                SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

                if (slot != null && slot.getHasStack() == false && slot.isItemValid(stack) == true)
                {
                    stack = slot.insertItem(stack, simulate);
                }

                slotIndex = (reverse == true ? slotIndex - 1 : slotIndex + 1);
            }
        }

        return stack;
    }

    public void addMergeSlotRangeExtToPlayer(int start, int numSlots)
    {
        this.addMergeSlotRangeExtToPlayer(start, numSlots, false);
    }

    public void addMergeSlotRangeExtToPlayer(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesExtToPlayer.add(new MergeSlotRange(start, numSlots, existingOnly));
    }

    public void addMergeSlotRangePlayerToExt(int start, int numSlots)
    {
        this.addMergeSlotRangePlayerToExt(start, numSlots, false);
    }

    public void addMergeSlotRangePlayerToExt(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesPlayerToExt.add(new MergeSlotRange(start, numSlots, existingOnly));
    }
}
