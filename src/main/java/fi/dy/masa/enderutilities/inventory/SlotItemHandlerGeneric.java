package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public class SlotItemHandlerGeneric extends SlotItemHandler
{
    public SlotItemHandlerGeneric(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit()
    {
        //System.out.println("SlotItemHandlerGeneric.getSlotStackLimit()");
        if (this.itemHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize)this.itemHandler).getInventoryStackLimit();
        }

        return super.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        //System.out.println("SlotItemHandlerGeneric.getItemStackLimit(stack)");
        if (stack != null && this.itemHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize)this.itemHandler).getItemStackLimit(stack);
        }

        return this.getSlotStackLimit();
    }

    @Override
    public void putStack(ItemStack stack)
    {
        if (this.itemHandler instanceof IItemHandlerModifiable)
        {
            //System.out.println("SlotItemHandlerGeneric#putStack() - setStackInSlot()");
            ((IItemHandlerModifiable)this.itemHandler).setStackInSlot(this.getSlotIndex(), stack);
        }
        else
        {
            //System.out.println("SlotItemHandlerGeneric#putStack() - insertItem()");
            this.itemHandler.insertItem(this.getSlotIndex(), stack, false);
        }

        this.onSlotChanged();
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate)
    {
        return this.itemHandler.insertItem(this.getSlotIndex(), stack, simulate);
    }

    /**
     * Returns true if the item would be valid for an empty slot.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (this.itemHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective)this.itemHandler).isItemValidForSlot(this.getSlotIndex(), stack);
        }

        return true; // super.isItemValid(stack);
    }

    /**
     * Returns true if at least some of the items can be put to this slot right now.
     */
    /*public boolean canPutItems(ItemStack stack)
    {
        return super.isItemValid(stack);
    }*/

    @Override
    public boolean canTakeStack(EntityPlayer player)
    {
        if (this.itemHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective)this.itemHandler).canExtractFromSlot(this.getSlotIndex());
        }

        return true;
    }

    /**
     * Returns true if all the items in this slot can be taken as one stack
     */
    public boolean canTakeAll()
    {
        ItemStack stack = this.itemHandler.getStackInSlot(this.getSlotIndex());
        if (stack == null)
        {
            return false;
        }

        ItemStack stackEx = this.itemHandler.extractItem(this.getSlotIndex(), stack.getMaxStackSize(), true);
        return stackEx != null && stack.stackSize == stackEx.stackSize;
    }
}
