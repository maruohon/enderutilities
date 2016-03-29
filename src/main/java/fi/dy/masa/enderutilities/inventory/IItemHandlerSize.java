package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

public interface IItemHandlerSize
{
    public int getInventoryStackLimit();

    public int getItemStackLimit(ItemStack stack);
}
