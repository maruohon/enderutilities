package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityEnderUtilitiesSided extends TileEntityEnderUtilitiesInventory implements ISidedInventory
{
    protected static final int[] SLOTS_EMPTY = new int[0];

    public TileEntityEnderUtilitiesSided(String name, int invSize)
    {
        super(name, invSize);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return SLOTS_EMPTY;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStackIn, EnumFacing direction)
    {
        return this.isItemValidForSlot(slotNum, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int slotNum, ItemStack stack, EnumFacing direction)
    {
        return false;
    }
}
