package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

public interface IItemHandlerSyncable
{
    public void syncStackInSlot(int slot, ItemStack stack);
}
