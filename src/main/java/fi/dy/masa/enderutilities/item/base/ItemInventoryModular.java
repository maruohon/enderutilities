package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;

public abstract class ItemInventoryModular extends ItemModular
{
    public int getSizeInventory(ItemStack containerStack)
    {
        return 0;
    }

    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return 64;
    }
}
