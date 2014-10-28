package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class GuiEnderUtilitiesInventory extends GuiContainer
{
	protected TileEntityEnderUtilitiesInventory te;
	protected ResourceLocation guiTexture;

	public GuiEnderUtilitiesInventory(ContainerEnderUtilitiesInventory container, TileEntityEnderUtilitiesInventory te)
	{
		super(container);
		this.te = te;
		this.guiTexture = ReferenceTextures.getGuiTexture("gui.container." + te.getTEName());
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
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		bindTexture(this.guiTexture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	protected void drawTooltips(int mouseX, int mouseY)
	{
	}

	protected void bindTexture(ResourceLocation rl)
	{
		this.mc.renderEngine.bindTexture(rl);
	}
}
