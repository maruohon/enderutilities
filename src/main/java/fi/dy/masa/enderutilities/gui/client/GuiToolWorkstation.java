package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.SlotUpgradeModule;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class GuiToolWorkstation extends GuiEnderUtilitiesInventory
{
    public GuiToolWorkstation(ContainerToolWorkstation container, TileEntityToolWorkstation te)
    {
        super(container, te);
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("gui.label.modulestorage", new Object[0]), 8, 56, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int maxModules = 0;
        ItemStack toolStack = this.inventorySlots.getSlot(0).getStack();
        if (toolStack != null)
        {
            if (toolStack.getItem() instanceof IModular)
            {
                maxModules = ((IModular)toolStack.getItem()).getMaxModules(toolStack);
            }
        }
        // No tool in the tool slot, draw the background
        else
        {
            this.drawTexturedModalRect(x + 7, y + 18, 176, 18, 18, 18);
        }

        int moduleType = 0;
        // Module slots
        for (int i = 0, dx = 79, dy = 18; i < 10; dx += 18)
        {
            // Draw a darker background over the disabled slots
            if (this.inventorySlots.getSlot(0).getHasStack() == false || i >= maxModules)
            {
                this.drawTexturedModalRect(x + dx, y + dy, 176, 0, 18, 18);
            }
            // Draw the module type background to empty, enabled module slots
            else if (this.inventorySlots.getSlot(i + 1) instanceof SlotUpgradeModule && ((Slot)this.inventorySlots.getSlot(i + 1)).getHasStack() == false)
            {
                moduleType = ((SlotUpgradeModule)this.inventorySlots.getSlot(i + 1)).getModuleType().getOrdinal();
                // Only one type of module is allowed in this slot
                if (moduleType >= 0 && moduleType <= 3) // 0..3: core, capacitor, link crystal, mob persistance
                {
                    this.drawTexturedModalRect(x + dx, y + dy, 176, 36 + moduleType * 18, 18, 18);
                }
            }

            if (++i == 5)
            {
                dy += 18;
                dx -= 5 * 18;
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
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
