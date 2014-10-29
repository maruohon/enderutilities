package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
	public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
	{
		super(te, inventory);
	}

	protected void addSlots()
	{
		// Item slot
		this.addSlotToContainer(new SlotItemModular(this.te, 0, 30, 24));

		// Module slots
		for (int i = 0, x = 80, y = 20; i < 15; x += 18)
		{
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 1, x, y));
			++i;
			if (i == 5)
			{
				y += 18;
				x -= 5 * 18;
			}
			else if (i == 10)
			{
				y += 23;
				x -= 5 * 18;
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
	{
		return super.transferStackInSlot(player, slotNum);
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
	{
		return super.mergeItemStack(stack, slotStart, slotRange, reverse);
	}
}
