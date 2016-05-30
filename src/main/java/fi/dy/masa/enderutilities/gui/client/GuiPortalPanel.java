package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class GuiPortalPanel extends GuiEnderUtilities
{
    private final TileEntityPortalPanel tepp;

    public GuiPortalPanel(ContainerEnderUtilities container, TileEntityPortalPanel te)
    {
        super(container, 176, 203, "gui.container." + te.getTEName());
        this.tepp = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.tepp.hasCustomName() ? this.tepp.getName() : I18n.format(this.tepp.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 110, 0x404040);
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

        for (int i = 0; i < 16; i++)
        {
            Slot slot = this.container.getSlot(i);
            if (slot.getHasStack() == false)
            {
                this.drawTexturedModalRect(x + slot.xDisplayPosition, y + slot.yDisplayPosition, u, v, 16, 16);
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
        /*int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        Slot slot = this.getSlotUnderMouse();
        // Hovering over an empty material slot
        if (slot != null && slot == this.inventorySlots.getSlot(0) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.enderinfuser.input", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
        // Hovering over an empty capacitor input slot
        else if (slot != null && slot == this.inventorySlots.getSlot(1) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.enderinfuser.chargeableinput", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }*/
    }
}
