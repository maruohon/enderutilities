package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerWrapperSize implements IItemHandler, IItemHandlerSize
{
    protected final IItemHandler baseHandler;

    public ItemHandlerWrapperSize(IItemHandler baseHandler)
    {
        this.baseHandler = baseHandler;
    }

    @Override
    public int getSlots()
    {
        return this.baseHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.baseHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.baseHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return this.baseHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.baseHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.baseHandler).getInventoryStackLimit();
        }

        return 64;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        if (this.baseHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.baseHandler).getItemStackLimit(stack);
        }

        return this.getInventoryStackLimit();
    }
}
