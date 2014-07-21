package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.gui.container.ContainerEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEU;

public class GuiEnderUtilitiesInventory extends GuiContainer
{
	protected TileEntityEU te;
	protected ResourceLocation background;

	public GuiEnderUtilitiesInventory(ContainerEnderUtilitiesInventory container, TileEntityEU te)
	{
		super(container);
		this.te = te;
		this.background = new ResourceLocation(Textures.GUI_SHEET_LOCATION + te.getGuiBackground());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		bindTexture(background);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	protected void bindTexture(ResourceLocation rl)
	{
		this.mc.renderEngine.bindTexture(rl);
	}
}
