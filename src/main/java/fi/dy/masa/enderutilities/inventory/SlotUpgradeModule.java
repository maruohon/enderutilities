package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class SlotUpgradeModule extends Slot
{
	private UtilItemModular.ModuleType moduleType;

	public SlotUpgradeModule(IInventory inventory, int slot, int posX, int posY, UtilItemModular.ModuleType moduleType)
	{
		super(inventory, slot, posX, posY);
		this.moduleType = moduleType;
	}

	public UtilItemModular.ModuleType getModuleType()
	{
		return this.moduleType;
	}

	public void setModuleType(UtilItemModular.ModuleType type)
	{
		this.moduleType = type;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		if (stack == null)
		{
			return false;
		}

		int maxModules = 0;
		// Check if the tool workstation has a modular tool inserted, and then get the max number of modules allowed in the tool
		ItemStack toolStack = this.inventory.getStackInSlot(0);
		if (toolStack != null)
		{
			Item item = toolStack.getItem();
			if (item instanceof IModular)
			{
				maxModules = ((IModular)item).getMaxModules(toolStack);
			}
		}

		// If this slot is part of the tool's module slots range, and the slot number is larger
		// than the max amount of modules allowed.
		if (this.slotNumber >= 1 && this.slotNumber <= 10 && this.slotNumber > maxModules)
		{
			return false;
		}

		UtilItemModular.ModuleType type = UtilItemModular.getModuleType(stack);
		if (type.equals(UtilItemModular.ModuleType.TYPE_INVALID) == false &&
			(this.moduleType.equals(type) == true || this.moduleType.equals(UtilItemModular.ModuleType.TYPE_ANY) == true))
		{
			// Portal Link Crystals are not valid for items/tools
			if (type.equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL) == false || stack.getItemDamage() < 2)
			{
				return true;
			}

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
