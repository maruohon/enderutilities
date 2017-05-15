package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

public interface IItemHandlerSyncable
{
    /**
     * Used to sync an ItemStack into a slot, even if the stack normally
     * wouldn't be allowed in that slot.
     * @param slot
     * @param stack
     */
    public void syncStackInSlot(int slot, ItemStack stack);
}
