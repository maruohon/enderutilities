package fi.dy.masa.enderutilities.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerEnderUtilitiesInventory
{
	public ContainerEnderFurnace(TileEntityEnderFurnace te, InventoryPlayer inventory)
	{
		super(te, inventory);
	}

	protected void addSlots()
	{
		this.addSlotToContainer(new Slot(this.te, 0, 34, 17));
		this.addSlotToContainer(new Slot(this.te, 1, 34, 53));
		this.addSlotToContainer(new SlotFurnace(this.inventoryPlayer.player, this.te, 2, 88, 35));
	}

	@Override()
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
	}
}
