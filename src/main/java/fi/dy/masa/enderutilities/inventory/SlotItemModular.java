package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;

public class SlotItemModular extends Slot
{
    public SlotItemModular(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof IModular;
    }
}
