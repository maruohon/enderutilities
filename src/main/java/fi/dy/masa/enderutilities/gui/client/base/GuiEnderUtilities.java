package fi.dy.masa.enderutilities.gui.client.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar.ScrollbarAction;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiEnderUtilities extends GuiContainer
{
    protected final ContainerEnderUtilities container;
    protected final EntityPlayer player;
    protected final ResourceLocation guiTextureWidgets;
    protected ResourceLocation guiTexture;
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

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Info text has been set, show it if the mouse is over the designated info area
        if (this.infoArea != null && this.infoArea.isMouseOver(mouseX, mouseY, x, y))
        {
            this.drawHoveringText(this.infoArea.getInfoLines(), mouseX, mouseY, this.fontRendererObj);
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

    @Override
    public void handleMouseInput() throws IOException
    {
        int dWheel = Mouse.getEventDWheel();

        if (dWheel != 0)
        {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            for (int i = 0; i < this.buttonList.size(); i++)
            {
                GuiButton button = this.buttonList.get(i);

                if (button.mousePressed(this.mc, mouseX, mouseY))
                {
                    this.actionPerformedWithButton(button, 10 + dWheel / 120);
                    break;
                }
            }
        }
        else
        {
            super.handleMouseInput();
        }
    }

    protected void actionPerformedWithButton(GuiButton guiButton, int mouseButton) throws IOException { }

    public void scrollbarAction(int scrollbarId, ScrollbarAction action, int position)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    public static class InfoArea
    {
        private final int posX;
        private final int posY;
        private final int width;
        private final int height;
        private final String infoText;
        private final Object[] args;

        public InfoArea(int x, int y, int width, int height, String infoTextKey, Object... args)
        {
            this.posX = x;
            this.posY = y;
            this.width = width;
            this.height = height;
            this.infoText = infoTextKey;
            this.args = args;
        }

        public List<String> getInfoLines()
        {
            List<String> lines = new ArrayList<String>();
            ItemEnderUtilities.addTooltips(this.infoText, lines, false, this.args);
            return lines;
        }

        public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop)
        {
            return mouseX >= guiLeft + this.posX && mouseX <= guiLeft + this.posX + this.width &&
                   mouseY >= guiTop + this.posY && mouseY <= guiTop + this.posY + this.height;
        }
    }
}
