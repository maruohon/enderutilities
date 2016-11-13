package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiEnderUtilities extends GuiContainer
{
    protected final ContainerEnderUtilities container;
    protected final EntityPlayer player;
    protected final ResourceLocation guiTexture;
    protected final ResourceLocation guiTextureWidgets;
    protected int backgroundU;
    protected int backgroundV;
    protected InfoArea infoArea;

    public GuiEnderUtilities(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container);
        this.container = container;
        this.player = container.player;
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
            GuiButton button = this.buttonList.get(i);

            // Mouse is over the button
            if ((button instanceof GuiButtonHoverText) && button.mousePressed(this.mc, mouseX, mouseY))
            {
                this.drawHoveringText(((GuiButtonHoverText)button).getHoverStrings(), mouseX, mouseY, this.fontRendererObj);
            }
        }

        // Info text has been set, show it if the mouse is over the designated info area
        if (this.infoArea != null)
        {
            int x = (this.width - this.xSize) / 2 + this.infoArea.posX;
            int y = (this.height - this.ySize) / 2 + this.infoArea.posY;

            // Hovering over the info icon
            if (mouseX >= x && mouseX <= x + this.infoArea.width && mouseY >= y && mouseY <= y + this.infoArea.height)
            {
                List<String> list = new ArrayList<String>();
                ItemEnderUtilities.addTooltips(this.infoArea.infoText, list, false);
                this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
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
        this.mc.getTextureManager().bindTexture(rl);
    }

    public static class GuiArea
    {
        public final int posX;
        public final int posY;
        public final int width;
        public final int height;

        public GuiArea(int x, int y, int width, int height)
        {
            this.posX = x;
            this.posY = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class InfoArea extends GuiArea
    {
        public final String infoText;

        public InfoArea(int x, int y, int width, int height, String infoTextKey)
        {
            super(x, y, width, height);

            this.infoText = infoTextKey;
        }
    }
}
