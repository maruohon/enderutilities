package fi.dy.masa.enderutilities.inventory;

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
        if (this.itemHandler instanceof ItemStackHandlerBasic)
        {
            return ((ItemStackHandlerBasic)this.itemHandler).getInventoryStackLimit();
        }

        return super.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        if (this.itemHandler instanceof ItemStackHandlerBasic)
        {
            return ((ItemStackHandlerBasic)this.itemHandler).getInventoryStackLimit(stack);
        }

        return super.getItemStackLimit(stack);
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
}
