package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class TileEntityEnderUtilitiesSided extends TileEntityEnderUtilitiesInventory implements ISidedInventory
{
	public TileEntityEnderUtilitiesSided(String name)
	{
		super(name);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return null;
	}

	@Override
	public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
	{
		return true;
	}

	@Override
	public boolean canExtractItem(int slotNum, ItemStack itemStack, int side)
	{
		return true;
	}
}
