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
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSendString;
import fi.dy.masa.enderutilities.network.message.MessageSendString.Type;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class GuiToolWorkstation extends GuiEnderUtilities
{
    private final TileEntityToolWorkstation te;
    private final ContainerToolWorkstation containerTW;
    protected GuiTextField nameField;
    protected String nameLast = "";

    public GuiToolWorkstation(ContainerToolWorkstation container, TileEntityToolWorkstation te)
    {
        super(container, 176, 217, "gui.container." + te.getTEName());
        this.te = te;
        this.containerTW = container;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.nameField = new GuiTextField(0, this.fontRenderer, 34, 100, 134, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(50);
        this.nameField.setEnabled(true);
        this.nameField.setText(this.te.getItemName());
        this.nameField.setFocused(false);
        this.nameField.setCursorPositionEnd();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, this.guiLeft + 108, this.guiTop + 113, 60, 20, I18n.format("enderutilities.gui.label.setname")));
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
            mouseX >= this.guiLeft + this.nameField.x &&
            mouseX < this.guiLeft + this.nameField.x + this.nameField.width &&
            mouseY >= this.guiTop + this.nameField.y &&
            mouseY < this.guiTop + this.nameField.y + this.nameField.height)
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
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 1)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageSendString(Type.BLOCK, this.nameField.getText()));
            this.nameLast = this.nameField.getText();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomName() ? this.te.getName() : I18n.format(this.te.getName());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.modulestorage"), 8, 56, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.renameitems"), 8, 86, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 125, 0x404040);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        String name = this.te.getItemName();

        if (name.equals(this.nameLast) == false)
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

        ItemStack toolStack = this.containerTW.getContainerItem();

        // No tool in the tool slot, draw the dark background
        if (toolStack.isEmpty() || (toolStack.getItem() instanceof IModular) == false)
        {
            this.drawTexturedModalRect(x + 8, y + 19, 240, 176, 16, 16);
        }

        final int num = ContainerToolWorkstation.NUM_MODULE_SLOTS;
        final int start = ContainerToolWorkstation.CONT_SLOT_MODULES_START;

        // Module slots
        for (int i = 0, slotNum = start, dx = 79, dy = 18; i < num; dx += 18, i++)
        {
            Slot slot = this.containerTW.getSlot(slotNum++);

            // Draw the module type background to empty, enabled module slots
            if (slot instanceof SlotItemHandlerModule && slot.getHasStack() == false)
            {
                if (((SlotItemHandlerModule) slot).getModuleType() == ModuleType.TYPE_INVALID)
                {
                    this.drawTexturedModalRect(x + dx, y + dy, 102, 0, 18, 18);
                }
                else
                {
                    // Draw a darker background for the disabled slots, and the module type background for enabled slots
                    int u = ((SlotItemHandlerModule) slot).getBackgroundIconU();
                    int v = ((SlotItemHandlerModule) slot).getBackgroundIconV();

                    // Only one type of module is allowed in this slot
                    if (u >= 0 && v >= 0)
                    {
                        this.drawTexturedModalRect(x + dx + 1, y + dy + 1, u, v, 16, 16);
                    }
                }
            }

            // First row done
            if (i == 4)
            {
                dy += 18;
                dx -= 5 * 18;
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        Slot slot = this.getSlotUnderMouse();

        // Hovering over the tool slot
        if (slot != null && slot.slotNumber == this.containerTW.getSlotTool() && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.tool_workstation.tool"));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRenderer);
        }
        else if (slot != null && slot.slotNumber == this.containerTW.getSlotRename() && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.tool_workstation.itemtorename"));
            list.add(I18n.format("enderutilities.gui.label.tool_workstation.useemptynametoreset"));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRenderer);
        }
    }
}
