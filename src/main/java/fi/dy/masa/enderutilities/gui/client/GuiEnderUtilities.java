package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiEnderUtilities extends GuiContainer
{
    protected ContainerEnderUtilities container;
    protected ResourceLocation guiTexture;
    protected ResourceLocation guiTextureWidgets;
    protected int backgroundU;
    protected int backgroundV;

    public GuiEnderUtilities(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container);
        this.container = container;
        this.xSize = xSize;
        this.ySize = ySize;
        this.guiTexture = ReferenceTextures.getGuiTexture(textureName);
        this.guiTextureWidgets = ReferenceTextures.getGuiTexture("gui.widgets");
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
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, this.backgroundU, this.backgroundV, this.xSize, this.ySize);
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
        for (int i = 0; i < this.buttonList.size(); i++)
        {
            GuiButton button = (GuiButton)this.buttonList.get(i);

            // Mouse is over the button
            if ((button instanceof GuiButtonHoverText) && button.mousePressed(this.mc, mouseX, mouseY) == true)
            {
                this.drawHoveringText(((GuiButtonHoverText)button).getHoverStrings(), mouseX, mouseY, this.fontRendererObj);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int l = 0; l < this.buttonList.size(); ++l)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(l);

            if (guibutton.mousePressed(this.mc, mouseX, mouseY) == true)
            {
                // Vanilla GUI only plays the click sound for the left click, we do it for other buttons here
                if (mouseButton != 0)
                {
                    guibutton.playPressSound(this.mc.getSoundHandler());
                }

                this.actionPerformedWithButton(guibutton, mouseButton);
            }
        }
    }

    protected void actionPerformedWithButton(GuiButton guiButton, int mouseButton) throws IOException { }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }
}
