package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiInventorySwapper extends GuiEnderUtilities
{
    public static final int BTN_ID_FIRST_SELECT_MODULE  = 0;
    public static final int BTN_ID_FIRST_SELECT_PRESET  = 4;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 8;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 12;

    public ContainerInventorySwapper container;
    public InventoryItemModular inventory;
    public EntityPlayer player;
    public int invSize;
    public int numModuleSlots;
    public int firstModuleSlotX;
    public int firstModuleSlotY;
    public int firstInvSlotX;
    public int firstInvSlotY;
    public int firstArmorSlotX;
    public int firstArmorSlotY;

    public GuiInventorySwapper(ContainerInventorySwapper container)
    {
        super(container, 199, 249, "gui.container.inventoryswapper");
        this.player = container.player;
        this.container = container;
        this.inventory = container.inventoryItemModular;
        this.invSize = this.inventory.getSlots();
        this.numModuleSlots = this.inventory.getModuleInventory().getSlots();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(this.invSize).xDisplayPosition;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(this.invSize).yDisplayPosition;
        this.firstInvSlotX     = this.guiLeft + this.container.getSlot(this.invSize + 4).xDisplayPosition;
        this.firstInvSlotY     = this.guiTop  + this.container.getSlot(this.invSize + 4).yDisplayPosition;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(this.invSize + 4 + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(this.invSize + 4 + 36).yDisplayPosition;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper", new Object[0]), 6, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards", new Object[0]), 120, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.slotpresets", new Object[0]) + ":", 57, 139, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.inventory.isUseableByPlayer(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.container.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 0, 18, 18);
            }
        }

        // Memory Card slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventory.getModuleInventory().isUseableByPlayer(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, 102, 0, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.inventory.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, 102, 18, 18, 18);
            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.firstModuleSlotY + 18, 120, 0, 10, 10);
        }

        ItemStack stack = this.container.getContainerItem();
        if (stack != null)
        {
            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(this.firstInvSlotX + 93 + sel * 18, this.firstInvSlotY - 29, 120, 0, 10, 10);

            // Draw the colored background for the selected/enabled inventory slots
            final long mask = ItemInventorySwapper.getEnabledSlotsMask(stack);
            long bit = 0x1;
            // Hotbar
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + 58, 102, 18, 18, 18);
                }
                bit <<= 1;
            }

            // Inventory
            for (int r = 0; r < 3; r++)
            {
                for (int c = 0; c < 9; c++)
                {
                    if ((mask & bit) != 0)
                    {
                        this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + r * 18, 102, 18, 18, 18);
                    }
                    bit <<= 1;
                }
            }

            // Armor slots
            for (int r = 0; r < 4; r++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstArmorSlotX - 1, this.firstArmorSlotY - 1 + (3 - r) * 18, 102, 18, 18, 18);
                }
                bit <<= 1;
            }

            // Off Hand slot
            if ((mask & bit) != 0)
            {
                this.drawTexturedModalRect(this.firstArmorSlotX - 1, this.firstArmorSlotY - 1 + 4 * 18, 102, 18, 18, 18);
            }
            bit <<= 1;
        }

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            if (this.inventory.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, 240, 144, 16, 16);
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2 + 7;
        int y = (this.height - this.ySize) / 2 + 36;

        // Hovering over the info icon
        if (mouseX >= x && mouseX <= x + 17 && mouseY >= y && mouseY <= y + 17)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.inventoryswapper.info", list, false);
            //list.add(I18n.format("enderutilities.gui.label.inventoryswapper.info", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 4 + i * 18, this.firstModuleSlotY + 19,
                    8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Add the preset selection buttons
        for (int i = 0; i < ItemInventorySwapper.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_PRESET + i, this.firstInvSlotX + 94 + i * 18, this.firstInvSlotY - 28,
                    8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        // Add the Toggle Row buttons
        // Hotbar is first
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS, this.firstInvSlotX - 17, this.firstInvSlotY + 59,
                14, 14, 60, 28, this.guiTextureWidgets, 14, 0));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i + 1, this.firstInvSlotX - 17, this.firstInvSlotY + 1 + i * 18,
                    14, 14, 60, 28, this.guiTextureWidgets, 14, 0));
        }

        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, this.firstInvSlotX + 1 + i * 18, this.firstInvSlotY - 17,
                    14, 14, 60, 42, this.guiTextureWidgets, 14, 0));
        }

        // Toggle button for armor
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + 9, this.firstArmorSlotX + 1, this.firstArmorSlotY + 91,
                14, 14, 60, 56, this.guiTextureWidgets, 14, 0));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_SELECT_PRESET && button.id < (BTN_ID_FIRST_SELECT_PRESET + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_CHANGE_PRESET, button.id - BTN_ID_FIRST_SELECT_PRESET));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_ROWS && button.id < (BTN_ID_FIRST_TOGGLE_ROWS + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_ROWS, button.id - BTN_ID_FIRST_TOGGLE_ROWS));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_COLUMNS && button.id < (BTN_ID_FIRST_TOGGLE_COLUMNS + 10))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_COLUMNS, button.id - BTN_ID_FIRST_TOGGLE_COLUMNS));
        }
    }
}
