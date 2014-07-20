package fi.dy.masa.enderutilities.handler;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.IFuelHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;

public class FuelHandler implements IFuelHandler
{
	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		if (itemStack == null)
		{
			return 0;
		}

		Item item = itemStack.getItem();
		if (item == null)
		{
			return 0;
		}

		if (item == EnderUtilitiesItems.enderBucket)
		{
			FluidStack fluidStack = ((ItemEnderBucket)item).getFluid(itemStack);

			if (fluidStack != null && fluidStack.getFluid() != null && fluidStack.amount > 0)
			{
				if (fluidStack.getFluid().getName().equals("lava"))
				{
					System.out.println("FuelHandler: lava");
					return (20 * fluidStack.amount);
				}
			}
		}

		return 0;
	}

}
