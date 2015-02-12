package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotOutput extends Slot
{
    public SlotOutput(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }
}
