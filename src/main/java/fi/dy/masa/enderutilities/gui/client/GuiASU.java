package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.inventory.container.ContainerASU;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class GuiASU extends GuiContainerLargeStacks
{
    private final TileEntityASU teasu;
    private final int tier;

    public GuiASU(ContainerASU container, TileEntityASU te)
    {
        super(container, 199, 139, "gui.container.asu");

        this.teasu = te;
        this.tier = te.getStorageTier();
        this.infoArea = new InfoArea(176, 44, 17, 17, "enderutilities.gui.infoarea.asu");
        this.scaledStackSizeTextInventories.add(container.inventory);
        this.scaledStackSizeTextInventories.add(te.getReferenceInventory());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.asu"), 8, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 46, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the slot backgrounds according to how many slots this tier has
        this.drawTexturedModalRect(x + 7, y + 22, 7, 56, this.tier * 18, 18);

        // Draw the reference slot background for an empty slot
        if (this.teasu.getReferenceInventory().getStackInSlot(0) == null)
        {
            this.drawTexturedModalRect(x + 174, y + 22, 199, 0, 18, 28);
        }

        int selectedSlot = ((ContainerASU) this.container).getSelectedSlot();

        if (selectedSlot != -1)
        {
            this.bindTexture(this.guiTextureWidgets);
            Slot slot = this.container.getSlot(selectedSlot);
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 18, 18, 18);
        }
    }
}
