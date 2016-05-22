package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyChest;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;

public class GuiHandyChest extends GuiContainerLargeStacks
{
    private static final String[] BUTTON_STRINGS = new String[] {
            "enderutilities.gui.label.moveallitemsexcepthotbar",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.leaveonefilledstack",
            "enderutilities.gui.label.fillstacks",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.moveallitems",
            "enderutilities.gui.label.sortitems"
    };

    private final TileEntityHandyChest tehc;
    private final ContainerHandyChest containerHC;
    private final int chestTier;
    private final int invSize;

    public GuiHandyChest(ContainerHandyChest container, TileEntityHandyChest te)
    {
        super(container, 176, 249, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0));
        this.tehc = te;
        this.containerHC = container;
        this.chestTier = te.getStorageTier();
        this.invSize = container.inventory.getSlots();
        this.scaledStackSizeTextInventories.add(container.inventory);
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
            case 0: this.ySize = 187; break;
            case 1: this.ySize = 213; break;
            case 2: this.ySize = 249; break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handychest", new Object[0]), 8, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(this.guiLeft + 101 + this.tehc.getSelectedModule() * 18, this.guiTop + 26, 120, 0, 10, 10);

        // Draw the hilight background for the selected module slot
        this.drawTexturedModalRect(this.guiLeft + 97 + this.tehc.getSelectedModule() * 18, this.guiTop + 7, 102, 18, 18, 18);

        int y = 77 + this.chestTier * 36;
        int x = 9;
        int mode = this.tehc.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            x = new int[] { 9, 27, 45, 117, 135, 153 }[mode];
        }

        // Draw the selection marker around the selected action button, ie. the "Quick Action"
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 120, 10, 14, 14);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.tehc.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerHC.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.containerHC.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 18, 18, 18);
        }

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < 4; i++)
        {
            if (this.tehc.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(this.guiLeft + 98 + i * 18, this.guiTop + 8, 240, 80, 16, 16);
            }
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Add the Memory Card selection buttons
        for (int i = 0; i < 4; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + 102 + i * 18, y + 27, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        int yOff = 78 + this.chestTier * 36;
        int xOffs[] = new int[] { 9, 27, 45, 117, 135, 153 };

        // Add the Quick Action/move items buttons
        for (int i = 0; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(i + 4, x + xOffs[i] + 1, y + yOff, 12, 12, 24, i * 12,
                    this.guiTextureWidgets, 12, 0, BUTTON_STRINGS[i]));
        }

        // Add the sort button for the Handy Chest inventory
        this.buttonList.add(new GuiButtonHoverText(10, x + 9, y + 30, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));

        // Add the sort button for the player inventory
        this.buttonList.add(new GuiButtonHoverText(11, x + 84, y + yOff + 4, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id < 4)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                    ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_SELECT_MODULE, button.id));
        }
        else if (button.id >= 4 && button.id < 10)
        {
            if (isShiftKeyDown() == true)
            {
                PacketHandler.INSTANCE.sendToServer(
                        new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                            ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_SET_QUICK_ACTION, button.id - 4));
            }
            else
            {
                PacketHandler.INSTANCE.sendToServer(
                    new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                        ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_MOVE_ITEMS, button.id - 4));
            }
        }
        else if (button.id >= 10 && button.id <= 11)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_SORT_ITEMS, button.id - 10));
        }
    }
}
