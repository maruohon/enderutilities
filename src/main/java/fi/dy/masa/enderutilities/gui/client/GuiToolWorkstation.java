package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.inventory.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class GuiToolWorkstation extends GuiEnderUtilities
{
    private final TileEntityToolWorkstation te;

    public GuiToolWorkstation(ContainerToolWorkstation container, TileEntityToolWorkstation te)
    {
        super(container, 176, 176, "gui.container." + te.getTEName());
        this.te = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomName() ? this.te.getName() : I18n.format(this.te.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.modulestorage", new Object[0]), 8, 56, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        ItemStack toolStack = this.inventorySlots.getSlot(ContainerToolWorkstation.SLOT_MODULAR_ITEM).getStack();
        // No tool in the tool slot, draw the dark background
        if (toolStack == null || (toolStack.getItem() instanceof IModular) == false)
        {
            this.drawTexturedModalRect(x + 8, y + 19, 240, 176, 16, 16);
        }

        // Module slots
        for (int i = 0, slotNum = ContainerToolWorkstation.SLOT_MODULAR_ITEM + 1, dx = 79, dy = 18; i < ContainerToolWorkstation.NUM_MODULE_SLOTS; dx += 18, i++)
        {
            Slot slot = this.inventorySlots.getSlot(slotNum++);

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
        if (slot != null && slot == this.inventorySlots.getSlot(ContainerToolWorkstation.SLOT_MODULAR_ITEM) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.toolworkstation.tool", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }
}
