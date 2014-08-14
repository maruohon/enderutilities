package fi.dy.masa.enderutilities.handler;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.IFuelHandler;

public class FuelHandler implements IFuelHandler
{
	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		if (itemStack == null || itemStack.getItem() == null)
		{
			return 0;
		}

		return 0;
	}

}
