package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotItemInput extends Slot
{
    public SlotItemInput(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    /**
     * Checks if the given ItemStack is valid for this slot.
     * Does the check by calling isItemValidForSlot() on the IInventory.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return this.inventory.isItemValidForSlot(this.slotNumber, stack);
    }
}
