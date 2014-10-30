package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperItemModular;

public class SlotUpgradeItem extends Slot
{
	private int itemType;

	public SlotUpgradeItem(IInventory inventory, int slot, int posX, int posY, int itemType)
	{
		super(inventory, slot, posX, posY);
		this.itemType = itemType;
	}

	public int getItemType()
	{
		return this.itemType;
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

		if (this.slotNumber >= 1 && this.slotNumber <= 10 && this.slotNumber > maxModules)
		{
			return false;
		}

		// TODO: Check the max number of particular upgrades
		// If itemType is -1, then any type of module is allowed
		int type = NBTHelperItemModular.getModuleType(stack);
		if (type != -1 && (type == this.itemType || this.itemType == -1))
		{
			return true;
		}

		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		if (this.slotNumber <= 10)
		{
			return 1;
		}
		return 64;
	}
}
