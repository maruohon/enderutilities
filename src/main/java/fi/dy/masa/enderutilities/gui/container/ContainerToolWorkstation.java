package fi.dy.masa.enderutilities.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
	private TileEntityToolWorkstation tetw;

	public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
	{
		super(te, inventory);
		this.tetw = te;
	}

	protected void addSlots()
	{
		// TODO
		this.addSlotToContainer(new Slot(this.tetw, 0, 34, 17));
	}
}
