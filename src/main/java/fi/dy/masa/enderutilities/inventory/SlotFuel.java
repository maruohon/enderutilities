package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class SlotFuel extends SlotGeneric
{
    public SlotFuel(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || (TileEntityEnderFurnace.isItemFuel(stack) == true && super.isItemValid(stack));
    }
}
