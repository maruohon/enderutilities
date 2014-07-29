package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.gui.GuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiEnderUtilitiesInventory
{
	private GuiButtonIcon buttonMode;
	private GuiButtonIcon buttonOutput;
	private TileEntityEnderFurnace teef;

	public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
	{
		super(container, te);
		this.teef = te;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404025);
		this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 2, 0x404025);

		if (this.teef.ownerName != null)
		{
			//s = I18n.format("container.owner", new Object[0]);
			//this.fontRendererObj.drawString(s, 80, this.ySize - 96 + 2, 0x404025);
			s = I18n.format(this.teef.ownerName, new Object[0]);
			this.fontRendererObj.drawString(s, this.xSize - this.fontRendererObj.getStringWidth(s) - 6, 20, 0x404025);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;

		// 1: Outputting to Ender Chest, draw the regular arrow instead of the crossed over arrow
		if (this.teef.outputMode == 1)
		{
			this.drawTexturedModalRect(x + 114, y + 34, 176, 82, 24, 17);
		}

		itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
	}

	protected void createButtons()
	{
		int modeOffset = 0;
		int outputOffset = 0;

		if (this.teef.operatingMode == 1)
		{
			modeOffset = 16;
		}
		if (this.teef.outputMode == 1)
		{
			outputOffset = 16;
		}

		this.buttonMode = new GuiButtonIcon(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, this.guiTexture, 200, 14 + modeOffset);
		this.buttonOutput = new GuiButtonIcon(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, this.guiTexture, 200, 46 + outputOffset);
		this.buttonList.clear();
		this.buttonList.add(this.buttonMode);
		this.buttonList.add(this.buttonOutput);
	}

	@Override
	protected void drawTooltips(int mouseX, int mouseY)
	{
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.createButtons();
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
		this.createButtons();
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.te.getWorldObj().provider.dimensionId, this.te.xCoord, this.te.yCoord, this.te.zCoord,
				GuiIds.GUI_ID_ENDER_FURNACE, btn.id, (short)0));
	}
}
