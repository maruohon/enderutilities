package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiEnderUtilities extends GuiContainer
{
    public ResourceLocation guiTexture;
    public int backgroundU;
    public int backgroundV;

    public GuiEnderUtilities(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container);
        this.xSize = xSize;
        this.ySize = ySize;
        this.guiTexture = ReferenceTextures.getGuiTexture(textureName);
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
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, this.backgroundU, this.backgroundV, this.xSize, this.ySize);
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }
}
