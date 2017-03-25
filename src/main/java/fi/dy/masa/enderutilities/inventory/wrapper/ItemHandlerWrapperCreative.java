package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ItemHandlerWrapperCreative extends ItemHandlerWrapperSelective
{
    private final TileEntityEnderUtilitiesInventory te;

    public ItemHandlerWrapperCreative(IItemHandler baseHandler, TileEntityEnderUtilitiesInventory te)
    {
        super(baseHandler);

        this.te = te;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (this.te.isCreative())
        {
            return stack;
        }

        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.te.isCreative())
        {
            return super.extractItem(slot, amount, true);
        }

        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return this.te.isCreative() == false;
    }
}
