package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.util.EUStringUtils;

public class GuiEnderInfuser extends GuiEnderUtilitiesInventory
{
    private TileEntityEnderInfuser teef;

    public GuiEnderInfuser(ContainerEnderInfuser container, TileEntityEnderInfuser te)
    {
        super(container, te);
        this.teef = te;
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Empty input slot, draw the slot background
        if (this.inventorySlots.getSlot(0).getStack() == null)
        {
            this.drawTexturedModalRect(x + 43, y + 23, 194, 0, 18, 18);
        }

        // Empty chargeable item slot, draw the slot background
        if (this.inventorySlots.getSlot(1).getStack() == null)
        {
            this.drawTexturedModalRect(x + 133, y + 7, 176, 0, 18, 18);
        }

        // Some charge stored, draw the "fluid" into the tank
        if (this.teef.amountStored > 0)
        {
            int h = 48;
            int t = this.teef.amountStored * h / TileEntityEnderInfuser.MAX_AMOUNT;
            this.drawTexturedModalRect(x + 87, y + 23 + h - t, 176, 18 + h - t, 28, t);
        }

        // Currently melting an input item, draw the melting progress bar
        if (this.teef.meltingProgress > 0)
        {
            int t = this.teef.meltingProgress * 15 / 100;
            this.drawTexturedModalRect(x + 66, y + 26, 204, 18, t, 11);
        }

        // Currently charging an item, draw the charging progress bar
        int progress = ((ContainerEnderInfuser)this.inventorySlots).chargeProgress;
        if (progress > 0)
        {
            progress = progress * 15 / 100;
            this.drawTexturedModalRect(x + 116, y + 39, 204, 18, progress, 11);
        }
        //itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Hovering over the "tank" area
        if (mouseX >= x + 87 && mouseX <= x + 114 && mouseY >= y + 23 && mouseY <= y + 68)
        {
            List<String> list = new ArrayList<String>();
            list.add(EUStringUtils.formatNumberWithKSeparators(this.teef.amountStored) + " / " + EUStringUtils.formatNumberWithKSeparators(TileEntityEnderInfuser.MAX_AMOUNT) + " EC");
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
        // Hovering over an empty material slot
        else if (mouseX >= x + 44 && mouseX <= x + 59 && mouseY >= y + 24 && mouseY <= y + 39 && this.inventorySlots.getSlot(0).getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.enderinfuser.input", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
        // Hovering over an empty capacitor input slot
        else if (mouseX >= x + 134 && mouseX <= x + 149 && mouseY >= y + 8 && mouseY <= y + 23 && this.inventorySlots.getSlot(1).getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.enderinfuser.capacitorinput", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }
}
