package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

public interface IModularInventoryCallback
{
    public ItemStack getContainerStack();

    public void inventoryChanged(int invId);
}
