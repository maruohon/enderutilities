package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.gui.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.reference.ReferenceTileEntity;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesInventory
{

	public TileEntityToolWorkstation()
	{
		super(ReferenceTileEntity.NAME_TILE_TOOL_WORKSTATION);
	}

	/* Returns the name of the inventory */
	@Override
	public String getInventoryName()
	{
		return this.hasCustomInventoryName() ? this.customInventoryName : "container." + ReferenceTileEntity.NAME_TILE_TOOL_WORKSTATION;
	}

	@Override
	public ContainerToolWorkstation getContainer(InventoryPlayer inventory)
	{
		return new ContainerToolWorkstation(this, inventory);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
	}
}
