package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.network.message.MessageSendString;
import fi.dy.masa.enderutilities.network.message.MessageSendString.Type;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class GuiPortalPanel extends GuiEnderUtilities
{
    private final TileEntityPortalPanel tepp;
    protected GuiTextField nameField;
    protected String nameLast = "";

    public GuiPortalPanel(ContainerEnderUtilities container, TileEntityPortalPanel te)
    {
        super(container, 176, 251, "gui.container." + te.getTEName());
        this.tepp = te;
        this.infoArea = new InfoArea(151, 5, 17, 17, "enderutilities.gui.infoarea.portalpanel");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.nameField = new GuiTextField(0, this.fontRendererObj, 11, 131, 154, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(50);
        this.nameField.setEnabled(true);
        this.nameField.setText(this.tepp.getPanelDisplayName());
        this.nameField.setFocused(false);
        this.nameField.setCursorPositionEnd();

        this.createButtons();

    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode) == false)
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        // Clear the field on right click
        if (mouseButton == 1 &&
            mouseX >= this.guiLeft + this.nameField.xPosition &&
            mouseX < this.guiLeft + this.nameField.xPosition + this.nameField.width &&
            mouseY >= this.guiTop + this.nameField.yPosition &&
            mouseY < this.guiTop + this.nameField.yPosition + this.nameField.height)
        {

            this.nameField.setText("");
            this.nameField.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
        }
        else
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            this.nameField.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.tepp.hasCustomName() ? this.tepp.getName() : I18n.format(this.tepp.getName());
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.settargetname"), 8, 118, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 156, 0x404040);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        String name = this.tepp.getPanelDisplayName();
        if (this.nameLast.equals(name) == false)
        {
            this.nameField.setText(name);
            this.nameLast = name;
        }

        this.nameField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int u = 240;
        int v = 32;
        int active = this.tepp.getActiveTargetId();

        for (int i = 0; i < 16; i++)
        {
            Slot slot = this.container.getSlot(i);

            if (i < 8 && active == i)
            {
                //System.out.println("active: " + i);
                this.drawTexturedModalRect(x + slot.xPos - 1, y + slot.yPos - 1,      102, 54, 18, 18);
                this.drawTexturedModalRect(x + slot.xPos - 1, y + slot.yPos - 1 + 18, 102, 54, 18, 18);
            }

            if (slot.getHasStack() == false)
            {
                this.drawTexturedModalRect(x + slot.xPos, y + slot.yPos, u, v, 16, 16);
            }

            if (i == 7)
            {
                u = 0;
                v = 240;
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        super.drawTooltips(mouseX, mouseY);

        Slot slot = this.getSlotUnderMouse();

        // Hovering over an empty dye slot
        if (slot != null && slot.getHasStack() == false && slot.slotNumber >= 8 && slot.slotNumber <= 15)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.portalpanel.dyeslot", list, false);
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int xOff = 57;
        int yOff = 56;

        for (int i = 0; i < 8; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + xOff, y + yOff, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
            xOff += 18;

            if (i == 3)
            {
                xOff = 57;
                yOff = 106;
            }
        }

        this.buttonList.add(new GuiButton(8, this.guiLeft + 108, this.guiTop + 144, 60, 20, I18n.format("enderutilities.gui.label.setname")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id < 8)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tepp.getWorld().provider.getDimension(), this.tepp.getPos(),
                    ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, 0, button.id));
        }
        else if (button.id == 8)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageSendString(Type.BLOCK, this.nameField.getText()));
            this.nameLast = this.nameField.getText();
        }
    }
}
