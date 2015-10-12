package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.SlotModule;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceReflection;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class GuiToolWorkstation extends GuiEnderUtilitiesInventory
{
    public GuiToolWorkstation(ContainerToolWorkstation container, TileEntityToolWorkstation te)
    {
        super(container, te);
        this.ySize = 176;
    }

    protected int getModuleBackgroundOffset(ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_INVALID) || moduleType.equals(ModuleType.TYPE_ANY))
        {
            return -1;
        }

        return moduleType.getOrdinal() * 18;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.modulestorage", new Object[0]), 8, 56, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int maxModules = 0;

        ItemStack toolStack = this.inventorySlots.getSlot(ContainerToolWorkstation.SLOT_MODULAR_ITEM).getStack();
        if (toolStack != null && toolStack.getItem() instanceof IModular)
        {
            maxModules = ((IModular)toolStack.getItem()).getMaxModules(toolStack);
        }
        // No tool in the tool slot, draw the background
        else
        {
            this.drawTexturedModalRect(x + 7, y + 18, 230, 18, 18, 18);
        }

        // Module slots
        for (int i = 0, dx = 79, dy = 18; i < ContainerToolWorkstation.NUM_MODULE_SLOTS; dx += 18, i++)
        {
            Slot slot = this.inventorySlots.getSlot(i);

            // Draw a darker background over the disabled slots
            if (toolStack == null || i >= maxModules)
            {
                this.drawTexturedModalRect(x + dx, y + dy, 230, 0, 18, 18);
            }
            // Draw the module type background to empty, enabled module slots
            else if (slot instanceof SlotModule && slot.getHasStack() == false)
            {
                int offset = this.getModuleBackgroundOffset(((SlotModule)slot).getModuleType());
                // Only one type of module is allowed in this slot
                if (offset >= 0)
                {
                    this.drawTexturedModalRect(x + dx, y + dy, 176, offset, 18, 18);
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
        Slot slot = null;
        try {
            slot = (Slot)ReferenceReflection.fieldGuiContainerTheSlot.get(this);
        }
        catch (IllegalAccessException e) {
            return;
        }

        // Hovering over the tool slot
        if (slot != null && slot == this.inventorySlots.getSlot(ContainerToolWorkstation.SLOT_MODULAR_ITEM) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.toolworkstation.tool", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }
}
