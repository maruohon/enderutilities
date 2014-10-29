package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.IModular;

public class SlotUpgradeItem extends Slot
{
	public SlotUpgradeItem(IInventory inventory, int slot, int posX, int posY)
	{
		super(inventory, slot, posX, posY);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if (stack == null)
		{
			return false;
		}

		int maxModules = 0;
		ItemStack toolStack = this.inventory.getStackInSlot(0);
		if (toolStack != null)
		{
			Item item = toolStack.getItem();
			if (item instanceof IModular)
			{
				maxModules = ((IModular)item).getMaxModules(toolStack);
			}
		}

		if (toolStack == null || this.slotNumber > maxModules)
		{
			return false;
		}

		// TODO: Check the max number of particular upgrades
		if (stack.getItem() == EnderUtilitiesItems.enderCapacitor ||
			stack.getItem() == EnderUtilitiesItems.linkCrystal ||
			(stack.getItem() == EnderUtilitiesItems.enderPart && stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)) // Active Ender Core
		{
			return true;
		}

		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}
}
