package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.inventory.container.ContainerDrawbridge;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityDrawbridge;
import fi.dy.masa.enderutilities.util.SlotRange;

public class GuiDrawbridge extends GuiEnderUtilities
{
    private final TileEntityDrawbridge tedb;
    private final boolean advanced;

    public GuiDrawbridge(ContainerDrawbridge container, TileEntityDrawbridge te)
    {
        super(container, 176, 136, "gui.container.drawbridge_normal");

        this.tedb = te;
        this.advanced = te.isAdvanced();

        if (this.advanced)
        {
            this.ySize = 210;
            this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.drawbridge_advanced");
        }

        this.infoArea = new InfoArea(160, 5, 11, 11, "enderutilities.gui.infoarea.drawbridge");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("enderutilities.container.drawbridge" + (this.advanced ? "_advanced" : "")), 8, 5, 0x404040);

        if (this.advanced)
        {
            this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 117, 0x404040);

            String str = I18n.format("enderutilities.gui.label.drawbridge.delay_num", this.tedb.getDelay());
            this.fontRenderer.drawString(str, 54, 19, 0x404040);

            str = I18n.format("enderutilities.gui.label.drawbridge.length_num", this.tedb.getMaxLength());
            this.fontRenderer.drawString(str, 54, 31, 0x404040);
        }
        else
        {
            this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 43, 0x404040);

            String str = I18n.format("enderutilities.gui.label.drawbridge.delay_num", this.tedb.getDelay());
            this.fontRenderer.drawString(str, 114, 19, 0x404040);

            str = I18n.format("enderutilities.gui.label.drawbridge.length_num", this.tedb.getMaxLength());
            this.fontRenderer.drawString(str, 114, 31, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        if (this.advanced)
        {
            // Draw the slot backgrounds for existing/enabled slots
            SlotRange range = this.container.getCustomInventorySlotRange();

            for (int i = range.first; i < range.lastExc; i++)
            {
                Slot slot = this.container.getSlot(i);

                if (slot != null)
                {
                    this.drawTexturedModalRect(x + slot.xPos - 1, y + slot.yPos - 1, 7, 127, 18, 18);
                }
            }
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        if (this.advanced)
        {
            this.buttonList.add(new GuiButtonHoverText(0, x + 18, y + 24, 14, 14, 60, 42,
                    this.guiTextureWidgets, 14, 0, "enderutilities.gui.label.drawbridge.take_blocks"));

            this.buttonList.add(new GuiButtonHoverText(1, x + 39, y + 19, 8, 8, 0, 120,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.drawbridge.delay"));

            this.buttonList.add(new GuiButtonHoverText(2, x + 39, y + 30, 8, 8, 0, 120,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.drawbridge.block_count"));
        }
        else
        {
            this.buttonList.add(new GuiButtonHoverText(0, x + 62, y + 22, 14, 14, 60, 42,
                    this.guiTextureWidgets, 14, 0, "enderutilities.gui.label.drawbridge.take_blocks"));

            this.buttonList.add(new GuiButtonHoverText(1, x + 100, y + 19, 8, 8, 0, 120,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.drawbridge.delay"));

            this.buttonList.add(new GuiButtonHoverText(2, x + 100, y + 31, 8, 8, 0, 120,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.drawbridge.block_count"));
        }
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int dim = this.tedb.getWorld().provider.getDimension();
        int amount = 0;

        if (mouseButton == 0 || mouseButton == 11)
        {
            amount = 1;
        }
        else if (mouseButton == 1 || mouseButton == 9)
        {
            amount = -1;
        }

        if (button.id >= 0 && button.id <= 2)
        {
            if (GuiScreen.isShiftKeyDown()) { amount *= 8; }
            if (GuiScreen.isCtrlKeyDown())  { amount *= 4; }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tedb.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, button.id, amount));
        }
    }
}
