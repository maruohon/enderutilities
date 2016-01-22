package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.inventory.ContainerTemplatedChest;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityTemplatedChest;

public class GuiTemplatedChest extends GuiTileEntityInventory
{
    protected TileEntityTemplatedChest tetc;
    protected int chestTier;

    public GuiTemplatedChest(ContainerTemplatedChest container, TileEntityTemplatedChest te)
    {
        super(container, 176, 207, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0), te);
        this.tetc = te;
        this.chestTier = te.getStorageTier();
    }

    @Override
    public void initGui()
    {
        this.setGuiYSize();

        super.initGui();

        this.createButtons();
    }

    protected void setGuiYSize()
    {
        switch(this.chestTier)
        {
            case 0: this.ySize = 140; break;
            case 1: this.ySize = 176; break;
            case 2: this.ySize = 207; break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int y = this.chestTier == 0 ? 47 : 83;

        if (this.chestTier == 2)
        {
            this.fontRendererObj.drawString(I18n.format("enderutilities.container.templatedchest", new Object[0]), 8, 45, 0x404040);
            this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 115, 0x404025);
            this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards", new Object[0]), 98, 14, 0x404040);
        }
        else
        {
            this.fontRendererObj.drawString(I18n.format("enderutilities.container.templatedchest", new Object[0]), 8, 6, 0x404040);
            this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, y, 0x404025);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTexture);

        if (this.chestTier == 2)
        {
            int selected = this.tetc.getSelectedModule();
            this.drawTexturedModalRect(this.guiLeft + 101 + selected * 18, this.guiTop + 44, 218, 10, 10, 10);
        }

        int invSize = this.tetc.getSizeInventory();

        // Draw the colored background icon for locked/"templated" slots
        int mask = this.tetc.getTemplateMask();
        for (int i = 0, bit = 0x1; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                int x = this.guiLeft + slot.xDisplayPosition;
                int y = this.guiTop + slot.yDisplayPosition;

                this.drawTexturedModalRect(x - 1, y - 1, 176, 0, 18, 18);
            }
        }

        // Draw a faint version of the template item for empty locked slots
        for (int i = 0, bit = 0x1; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                int x = this.guiLeft + slot.xDisplayPosition;
                int y = this.guiTop + slot.yDisplayPosition;

                ItemStack stack = this.tetc.getStackInSlot(i);
                if (stack == null)
                {
                    stack = this.tetc.getTemplateStack(i);
                    if (stack != null)
                    {
                        itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
                    }
                }
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        switch(this.chestTier)
        {
            case 0: x += 152; y += 6; break;
            case 1: x += 152; y += 6; break;
            case 2: x +=   6; y += 6; break;
            default:
        }

        // Hovering over the info icon
        if (mouseX >= x && mouseX <= x + 17 && mouseY >= y && mouseY <= y + 17)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.templatedchest.info", list, false);
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        if (this.chestTier == 2)
        {
            this.buttonList.clear();

            int x = (this.width - this.xSize) / 2;
            int y = (this.height - this.ySize) / 2;

            // Add the Memory Card selection buttons
            for (int i = 0; i < 4; i++)
            {
                this.buttonList.add(new GuiButtonIcon(i, x + 102 + i * 18, y + 45, 8, 8, 194, 0, this.guiTexture, 8, 0));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
    }
}
