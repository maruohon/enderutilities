package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiEnderUtilitiesInventory
{
	public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
	{
		super(container, te);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404025);
		this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2, 0x404025);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
	}

	@Override
	protected void drawTooltips(int mouseX, int mouseY)
	{
	}
}
