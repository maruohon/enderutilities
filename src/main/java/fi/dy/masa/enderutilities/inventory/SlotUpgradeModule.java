package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class SlotUpgradeModule extends Slot
{
	protected UtilItemModular.ModuleType moduleType;

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
		ItemStack toolStack = this.inventory.getStackInSlot(0);
		if (stack == null || toolStack == null || (toolStack.getItem() instanceof IModular) == false)
		{
			return false;
		}

		IModular imodular = (IModular)toolStack.getItem();
		// If the slot number is larger than the max amount of modules allowed for the tool
		if (this.slotNumber > imodular.getMaxModules(toolStack))
		{
			return false;
		}

		UtilItemModular.ModuleType type = UtilItemModular.getModuleType(stack);
		if (type.equals(UtilItemModular.ModuleType.TYPE_INVALID) == false &&
			(this.moduleType.equals(type) == true || this.moduleType.equals(UtilItemModular.ModuleType.TYPE_ANY) == true))
		{
			// FIXME This doesn't actually check how many modules of this subtype there are already installed...
			if (imodular.getMaxModules(toolStack, stack) > 0)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}
}
