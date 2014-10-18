package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class GuiToolWorkstation extends GuiEnderUtilitiesInventory
{
	private TileEntityToolWorkstation tetw;

	public GuiToolWorkstation(ContainerToolWorkstation container, TileEntityToolWorkstation te)
	{
		super(container, te);
		this.tetw = te;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
		this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 4, 0x404025);

		if (this.tetw.getOwnerName() != null)
		{
			//s = I18n.format("container.owner", new Object[0]);
			//this.fontRendererObj.drawString(s, 80, this.ySize - 96 + 2, 0x404025);
			s = I18n.format(this.tetw.getOwnerName(), new Object[0]);
			this.fontRendererObj.drawString(s, this.xSize - this.fontRendererObj.getStringWidth(s) - 6, 20, 0x404025);
		}
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

	@Override
	public void initGui()
	{
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks)
	{
		super.drawScreen(mouseX, mouseY, gameTicks);

		this.drawTooltips(mouseX, mouseY);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}
}
