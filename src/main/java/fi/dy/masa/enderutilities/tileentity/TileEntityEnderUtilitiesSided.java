package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityEnderUtilitiesSided extends TileEntityEnderUtilitiesInventory implements ISidedInventory
{
    public TileEntityEnderUtilitiesSided(String name)
    {
        super(name);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing face)
    {
        return null;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, EnumFacing face)
    {
        return this.isItemValidForSlot(slotNum, itemStack);
    }

    @Override
    public boolean canExtractItem(int slotNum, ItemStack itemStack, EnumFacing face)
    {
        return true;
    }
}
