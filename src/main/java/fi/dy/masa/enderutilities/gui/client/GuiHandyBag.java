package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiHandyBag extends GuiContainer
{
    protected ResourceLocation guiTexture;

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container);
        this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.handybag." + container.getBagTier());
        this.xSize = 176;
        this.ySize = 238;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        int startX = 40;
        int startY = 17;
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, startX, startY, this.xSize, this.ySize);
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }
}
