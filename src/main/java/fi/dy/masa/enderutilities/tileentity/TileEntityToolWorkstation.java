package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesInventory
{

	public TileEntityToolWorkstation()
	{
		super(ReferenceBlocksItems.NAME_TILEENTITY_TOOL_WORKSTATION);
		this.itemStacks = new ItemStack[16];
	}

	@Override
	public boolean canUpdate()
	{
		return false;
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
