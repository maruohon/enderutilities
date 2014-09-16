package fi.dy.masa.enderutilities.item;

import net.minecraft.item.ItemStack;

public interface IChargeable
{
	public int getCapacity(ItemStack stack);

	public int getCharge(ItemStack stack);

	public int addCharge(ItemStack stack, int amount, boolean simulate);

	public int useCharge(ItemStack stack, int amount, boolean simulate);
}
