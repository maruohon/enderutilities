package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import fi.dy.masa.enderutilities.inventory.IItemHandlerSelective;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSize;

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
        if (this.getItemHandler() instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize)this.getItemHandler()).getInventoryStackLimit();
        }

        return super.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        //System.out.println("SlotItemHandlerGeneric.getItemStackLimit(stack)");
        if (stack != null && this.getItemHandler() instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize)this.getItemHandler()).getItemStackLimit(stack);
        }

        return this.getSlotStackLimit();
    }

    @Override
    public void putStack(ItemStack stack)
    {
        if (this.getItemHandler() instanceof IItemHandlerModifiable)
        {
            //System.out.println("SlotItemHandlerGeneric#putStack() - setStackInSlot()");
            ((IItemHandlerModifiable)this.getItemHandler()).setStackInSlot(this.getSlotIndex(), stack);
        }
        else
        {
            //System.out.println("SlotItemHandlerGeneric#putStack() - insertItem()");
            this.getItemHandler().insertItem(this.getSlotIndex(), stack, false);
        }

        this.onSlotChanged();
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate)
    {
        return this.getItemHandler().insertItem(this.getSlotIndex(), stack, simulate);
    }

    /**
     * Returns true if the item would be valid for an empty slot.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (this.getItemHandler() instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective)this.getItemHandler()).isItemValidForSlot(this.getSlotIndex(), stack);
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
        if (this.getItemHandler() instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective)this.getItemHandler()).canExtractFromSlot(this.getSlotIndex());
        }

        return true;
    }

    /**
     * Returns true if all the items in this slot can be taken as one stack
     */
    public boolean canTakeAll()
    {
        ItemStack stack = this.getItemHandler().getStackInSlot(this.getSlotIndex());
        if (stack == null)
        {
            return false;
        }

        ItemStack stackEx = this.getItemHandler().extractItem(this.getSlotIndex(), stack.getMaxStackSize(), true);
        return stackEx != null && stack.stackSize == stackEx.stackSize;
    }
}
