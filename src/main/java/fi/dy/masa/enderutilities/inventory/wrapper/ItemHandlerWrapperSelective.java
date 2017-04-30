package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSelective;

public class ItemHandlerWrapperSelective extends ItemHandlerWrapperSize implements IItemHandlerSelective
{
    public ItemHandlerWrapperSelective(IItemHandler baseHandler)
    {
        super(baseHandler);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (this.isItemValidForSlot(slot, stack))
        {
            return this.baseHandler.insertItem(slot, stack, simulate);
        }

        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.canExtractFromSlot(slot))
        {
            return this.baseHandler.extractItem(slot, amount, simulate);
        }

        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        if (this.baseHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.baseHandler).isItemValidForSlot(slot, stack);
        }

        return true;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        if (this.baseHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.baseHandler).canExtractFromSlot(slot);
        }

        return true;
    }
}
