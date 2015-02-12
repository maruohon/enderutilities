package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IChargeable;

public class SlotChargeable extends Slot
{
    public SlotChargeable(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof IChargeable;
    }
}
