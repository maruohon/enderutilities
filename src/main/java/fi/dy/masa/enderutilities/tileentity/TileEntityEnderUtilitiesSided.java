package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class TileEntityEnderUtilitiesSided extends TileEntityEnderUtilitiesInventory implements ISidedInventory
{
    protected static final int[] SLOTS_EMPTY = new int[0];

    public TileEntityEnderUtilitiesSided(String name, int invSize)
    {
        super(name, invSize);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return SLOTS_EMPTY;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
    {
        return this.isItemValidForSlot(slotNum, itemStack);
    }

    @Override
    public boolean canExtractItem(int slotNum, ItemStack itemStack, int side)
    {
        return false;
    }
}
