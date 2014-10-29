package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
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
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		int maxModules = 0;
		ItemStack toolStack = this.inventorySlots.getSlot(0).getStack();
		if (toolStack != null)
		{
			Item item = toolStack.getItem();
			if (item instanceof IModular)
			{
				maxModules = ((IModular)item).getMaxModules(toolStack);
			}
		}

		// Module slots
		for (int i = 0, dx = 79, dy = 19; i < 15; dx += 18)
		{
			if (this.inventorySlots.getSlot(0).getHasStack() == false || i >= maxModules)
			{
				this.drawTexturedModalRect(x + dx, y + dy, 176, 32, 18, 18);
			}

			++i;
			if (i == 5)
			{
				dy += 18;
				dx -= 5 * 18;
			}
			else if (i == 10)
			{
				dy += 23;
				dx -= 5 * 18;
			}
		}
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
