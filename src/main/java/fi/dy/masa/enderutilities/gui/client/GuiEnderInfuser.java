package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;

import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.util.EUStringUtils;

public class GuiEnderInfuser extends GuiEnderUtilities
{
    public TileEntityEnderInfuser teei;

    public GuiEnderInfuser(ContainerEnderInfuser container, TileEntityEnderInfuser te)
    {
        super(container, 176, 176, "gui.container." + te.getTEName());
        this.teei = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.teei.hasCustomName() ? this.teei.getName() : I18n.format(this.teei.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Some charge stored, draw the "fluid" into the tank
        if (this.teei.amountStored > 0)
        {
            int h = 48;
            int t = this.teei.amountStored * h / TileEntityEnderInfuser.MAX_AMOUNT;
            this.drawTexturedModalRect(x + 87, y + 23 + h - t, 176, 18 + h - t, 28, t);
        }

        // Currently melting an input item, draw the melting progress bar
        if (this.teei.meltingProgress > 0)
        {
            int t = this.teei.meltingProgress * 15 / 100;
            this.drawTexturedModalRect(x + 66, y + 26, 204, 18, t, 11);
        }

        // Currently charging an item, draw the charging progress bar
        int progress = ((ContainerEnderInfuser)this.inventorySlots).chargeProgress;
        if (progress > 0)
        {
            progress = progress * 15 / 100;
            this.drawTexturedModalRect(x + 116, y + 39, 204, 18, progress, 11);
        }

        this.bindTexture(this.guiTextureWidgets);

        // Empty input slot, draw the slot background
        if (this.inventorySlots.getSlot(0).getStack() == null)
        {
            this.drawTexturedModalRect(x + 44, y + 24, 240, 160, 16, 16);
        }

        // Empty chargeable item slot, draw the slot background
        if (this.inventorySlots.getSlot(1).getStack() == null)
        {
            this.drawTexturedModalRect(x + 134, y + 8, 240, 16, 16, 16);
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Hovering over the "tank" area
        if (mouseX >= x + 87 && mouseX <= x + 114 && mouseY >= y + 23 && mouseY <= y + 71)
        {
            List<String> list = new ArrayList<String>();
            int ec = this.teei.amountStored * TileEntityEnderInfuser.ENDER_CHARGE_PER_MILLIBUCKET;
            int ec_capacity = TileEntityEnderInfuser.MAX_AMOUNT * TileEntityEnderInfuser.ENDER_CHARGE_PER_MILLIBUCKET;
            list.add(EUStringUtils.formatNumberWithKSeparators(ec) + " / " + EUStringUtils.formatNumberWithKSeparators(ec_capacity) + " EC");
            list.add("(" + EUStringUtils.formatNumberWithKSeparators(this.teei.amountStored) + " / " + EUStringUtils.formatNumberWithKSeparators(TileEntityEnderInfuser.MAX_AMOUNT) + " mB)");
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
            return;
        }

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
        }
    }
}
