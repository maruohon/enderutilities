package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
	public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
	{
		super(te, inventory);
	}

	@Override
	protected void addSlots()
	{
		// Item slot
		this.addSlotToContainer(new SlotItemModular(this.te, 0, 8, 19));

		// Module slots
		int x = 80, y = 19;
		for (int i = 0; i < 10; x += 18)
		{
			// We initially add all the slots as generic. When the player inserts a tool into the tool slot,
			// we will then re-assign the slot types based on the tool.
			this.addSlotToContainer(new SlotUpgradeModule(this.te, i + 1, x, y, UtilItemModular.ModuleType.TYPE_ANY));
			if (++i == 5)
			{
				y += 18;
				x -= 5 * 18;
			}
		}

		// Module storage inventory slots
		x = 8; y = 66;
		for (int i = 0; i < 9; x += 18, ++i)
		{
			this.addSlotToContainer(new SlotUpgradeModule(this.te, i + 11, x, y, UtilItemModular.ModuleType.TYPE_ANY));
		}
	}

	@Override
	protected int getPlayerInventoryVerticalOffset()
	{
		return 94;
	}
}
