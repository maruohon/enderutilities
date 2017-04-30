package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public interface IItemHandlerSize extends IItemHandler
{
    public int getInventoryStackLimit();

    public int getItemStackLimit(ItemStack stack);
}
