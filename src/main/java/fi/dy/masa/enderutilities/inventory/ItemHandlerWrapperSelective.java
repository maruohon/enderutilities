package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public class ItemHandlerWrapperSelective implements IItemHandler
{
    protected final IItemHandler baseHandler;

    public ItemHandlerWrapperSelective(IItemHandler baseHandler)
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
        if (this.isItemValidForSlot(slot, stack) == true)
        {
            return this.baseHandler.insertItem(slot, stack, simulate);
        }

        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.canExtractFromSlot(slot) == true)
        {
            return this.baseHandler.extractItem(slot, amount, simulate);
        }

        return null;
    }

    protected boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    protected boolean canExtractFromSlot(int slot)
    {
        return true;
    }
}
