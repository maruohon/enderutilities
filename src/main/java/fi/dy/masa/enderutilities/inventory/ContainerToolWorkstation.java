package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

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
			// TODO add item types for slots (last parameter)
			// We should check how many cores are allowed, add that number of those slots, same for capacitors and link crystals etc.
			// Then add the rest of the slots as generic.
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 1, x, y, -1));
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
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 11, x, y, -1));
		}
	}

	@Override
	protected int getPlayerInventoryVerticalOffset()
	{
		return 94;
	}
}
