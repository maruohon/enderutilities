package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiEnderUtilitiesInventory
{
	private GuiButtonIcon buttonMode;
	private GuiButtonIcon buttonOutput;

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
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
	}

	@Override
	protected void drawTooltips(int mouseX, int mouseY)
	{
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonMode = new GuiButtonIcon(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, this.guiTexture, 200, 14);
		this.buttonOutput = new GuiButtonIcon(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, this.guiTexture, 200, 46);
		this.buttonList.add(this.buttonMode);
		this.buttonList.add(this.buttonOutput);
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

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
	}
}
