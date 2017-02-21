package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyChest;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;

public class GuiHandyChest extends GuiContainerLargeStacks
{
    public static final int BTN_ID_SORT_CHEST       = 10;
    public static final int BTN_ID_SORT_PLAYER      = 11;

    private static final String[] BUTTON_STRINGS = new String[] {
            "enderutilities.gui.label.moveallitemsexcepthotbar",
            "enderutilities.gui.label.movematchingitemsexcepthotbar",
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
        super(container, 176, 256, "gui.container.handychest." + te.getStorageTier());
        this.tehc = te;
        this.containerHC = container;
        this.chestTier = te.getStorageTier();
        this.invSize = container.inventory.getSlots();
        this.scaledStackSizeTextInventories.add(container.inventory);
    }

    @Override
    public void initGui()
    {
        this.setGuiSize();

        super.initGui();

        this.createButtons();
    }

    protected void setGuiSize()
    {
        switch (this.chestTier)
        {
            case 0: this.ySize = 187; break;
            case 1: this.ySize = 213; break;
            case 2: this.ySize = 249; break;
            case 3:
                this.xSize = 246;
                this.ySize = 256;
                break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int y = this.chestTier == 3 ? 3 : 6;
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handychest"), 8, y, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        Slot slot;
        GuiButton button = this.buttonList.get(this.tehc.getSelectedModule());
        int x = button.xPosition - 1;
        int y = button.yPosition - 1;

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(x, y, 120, 0, 10, 10);

        // Draw the hilight background for the selected module slot
        slot = this.inventorySlots.getSlot(this.invSize + this.tehc.getSelectedModule());
        this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 18, 18, 18);

        int mode = this.tehc.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            button = this.buttonList.get(4 + mode);
            x = button.xPosition - 1;
            y = button.yPosition - 1;

            // Draw the selection marker around the selected action button, ie. the "Quick Action"
            this.drawTexturedModalRect(x, y, 120, 10, 14, 14);
        }

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.tehc.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xPos - 1;
                y = this.guiTop + slot.yPos - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerHC.getSelectedSlot() != -1)
        {
            slot = this.container.getSlot(this.containerHC.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 18, 18, 18);
        }

        int mask = this.tehc.getLockMask();
        for (int i = 0; i < 4; i++)
        {
            // Draw the background icon over empty storage module slots
            if (this.tehc.getModuleInventory().getStackInSlot(i) == null)
            {
                slot = this.inventorySlots.getSlot(this.invSize + i);
                this.drawTexturedModalRect(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, 240, 80, 16, 16);
            }

            // This card has been locked in place
            if ((mask & (1 << i)) != 0)
            {
                button = this.buttonList.get(i);
                this.drawTexturedModalRect(button.xPosition - 2, button.yPosition - 2, 120, 34, 12, 12);
            }
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        if (this.chestTier == 3)
        {
            // Add the Memory Card selection buttons
            for (int i = 0; i < 4; i++)
            {
                this.buttonList.add(new GuiButtonIcon(i, x + 213, y + 178 + i * 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
            }

            int yOff = 158;
            int xOffs[] = new int[] { 9, 27, 45, 117, 135, 153 };

            // Add the Quick Action/move items buttons
            for (int i = 0; i < 6; i++)
            {
                this.buttonList.add(new GuiButtonHoverText(i + 4, x + xOffs[i] + 37, y + yOff, 12, 12, 24, i * 12,
                        this.guiTextureWidgets, 12, 0, BUTTON_STRINGS[i]));
            }

            // Add the sort button for the Handy Chest inventory
            this.buttonList.add(new GuiButtonHoverText(10, x + 228, y + yOff, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));

            // Add the sort button for the player inventory
            this.buttonList.add(new GuiButtonHoverText(11, x + 120, y + yOff + 4, 8, 8, 0, 24,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems.player"));
        }
        else
        {
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
            this.buttonList.add(new GuiButtonHoverText(BTN_ID_SORT_CHEST, x + 9, y + 30, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));

            // Add the sort button for the player inventory
            this.buttonList.add(new GuiButtonHoverText(BTN_ID_SORT_PLAYER, x + 84, y + yOff + 4, 8, 8, 0, 24,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems.player"));
        }
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        if (button.id >= 0 && button.id < 4)
        {
            if (mouseButton == 0)
            {
                PacketHandler.INSTANCE.sendToServer(
                    new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                        ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_SELECT_MODULE, button.id));
            }
            else if (mouseButton == 1)
            {
                PacketHandler.INSTANCE.sendToServer(
                    new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                        ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_LOCK_MODULE, button.id));
            }
        }
        else if (button.id >= 4 && button.id < 10)
        {
            if (isShiftKeyDown())
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
        else if (button.id >= BTN_ID_SORT_CHEST && button.id <= BTN_ID_SORT_PLAYER)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tehc.getWorld().provider.getDimension(), this.tehc.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityHandyChest.GUI_ACTION_SORT_ITEMS, button.id - BTN_ID_SORT_CHEST));
        }
    }
}
