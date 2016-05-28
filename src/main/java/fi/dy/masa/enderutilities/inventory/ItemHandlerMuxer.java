package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerMuxer implements IItemHandler
{
    protected final IItemHandlerProvider provider;

    public ItemHandlerMuxer(IItemHandlerProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public int getSlots()
    {
        return this.provider.getInventory().getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.provider.getInventory().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.provider.getInventory().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return this.provider.getInventory().extractItem(slot, amount, simulate);
    }
}
