/* Parts of the code taken from MineFactoryReloaded, credits powercrystals, skyboy and others */
package fi.dy.masa.enderutilities.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.tileentity.TileEntityEU;

public class ContainerEnderUtilitiesInventory extends Container
{
	protected TileEntityEU te;
	protected InventoryPlayer inventoryPlayer;

	public ContainerEnderUtilitiesInventory(TileEntityEU te, InventoryPlayer inventory)
	{
		this.te = te;
		this.inventoryPlayer = inventory;

		if (te.getSizeInventory() > 0)
		{
			this.addSlots();
		}

		this.bindPlayerInventory(inventory);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return te.isInvalid() == false && te.isUseableByPlayer(player);
	}

	protected void addSlots()
	{
	}

	protected int getPlayerInventoryVerticalOffset()
	{
		return 84;
	}

	protected int getPlayerInventoryHorizontalOffset()
	{
		return 8;
	}

	protected void bindPlayerInventory(InventoryPlayer inventory)
	{
		int yOff = getPlayerInventoryVerticalOffset();
		int xOff = getPlayerInventoryHorizontalOffset();

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventory, j + i * 9 + 9, xOff + j * 18, yOff + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventory, i, xOff + i * 18, yOff + 58));
		}
	}
}
