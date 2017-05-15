package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.container.base.SlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiInventorySwapper extends GuiEnderUtilities implements IButtonStateCallback
{
    public static final int BTN_ID_FIRST_SELECT_MODULE  = 0;
    public static final int BTN_ID_FIRST_SELECT_PRESET  = 4;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 8;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 20;
    public static final int BTN_ID_LOCKED               = 40;
    public static final int BTN_ID_CYCLE                = 41;

    private final ContainerInventorySwapper containerInvSwapper;
    private final InventoryItemModular inventory;
    private final int invSize;
    private int numModuleSlots;
    private int firstModuleSlotX;
    private int firstModuleSlotY;
    private int firstPlayerInvSlotX;
    private int firstPlayerInvSlotY;
    private int firstArmorSlotX;
    private int firstArmorSlotY;
    private final boolean baublesLoaded;

    public GuiInventorySwapper(ContainerInventorySwapper container)
    {
        super(container, 199, 249, "gui.container.inventoryswapper");

        this.containerInvSwapper = container;
        this.inventory = container.inventoryItemModular;
        this.invSize = this.inventory.getSlots();
        this.numModuleSlots = this.inventory.getModuleInventory().getSlots();
        this.baublesLoaded = ModRegistry.isModLoadedBaubles();

        if (this.baublesLoaded)
        {
            this.xSize = 243;
            this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.inventoryswapper.baubles");
            this.infoArea = new InfoArea(227, 5, 11, 11, "enderutilities.gui.infoarea.inventoryswapper");
        }
        else
        {
            this.infoArea = new InfoArea(7, 43, 11, 11, "enderutilities.gui.infoarea.inventoryswapper");
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        SlotRange armorRange = this.containerInvSwapper.getPlayerArmorSlots();
        SlotRange moduleRange = this.containerInvSwapper.getModuleSlots();
        SlotRange playerRange = this.containerInvSwapper.getPlayerMainInventorySlotRange();
        this.firstModuleSlotX       = this.guiLeft + this.containerInvSwapper.getSlot(moduleRange.first).xPos;
        this.firstModuleSlotY       = this.guiTop  + this.containerInvSwapper.getSlot(moduleRange.first).yPos;
        this.firstPlayerInvSlotX    = this.guiLeft + this.containerInvSwapper.getSlot(playerRange.first).xPos;
        this.firstPlayerInvSlotY    = this.guiTop  + this.containerInvSwapper.getSlot(playerRange.first).yPos;
        this.firstArmorSlotX        = this.guiLeft + this.containerInvSwapper.getSlot(armorRange.first).xPos;
        this.firstArmorSlotY        = this.guiTop  + this.containerInvSwapper.getSlot(armorRange.first).yPos;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int xOff = this.baublesLoaded ? 20 : 0;
        this.fontRenderer.drawString(I18n.format("enderutilities.container.inventoryswapper"), 6 + xOff, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.slotpresets") + ":", 58 + xOff, 139, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.inventory.isAccessibleBy(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.containerInvSwapper.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 0, 18, 18);
            }
        }

        // Memory Card slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventory.getModuleInventory().isAccessibleBy(this.player) == false)
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

        ItemStack stack = this.containerInvSwapper.getContainerItem();

        if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.INVENTORY_SWAPPER)
        {
            ItemInventorySwapper swapper = (ItemInventorySwapper) stack.getItem();
            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(this.firstPlayerInvSlotX + 111 + sel * 12, this.firstPlayerInvSlotY - 29, 120, 0, 10, 10);

            // Draw the colored background for the selected/enabled inventory slots
            final long mask = swapper.getEnabledSlotsMask(stack);
            long bit = 0x1;

            // Hotbar
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstPlayerInvSlotX - 1 + c * 18, this.firstPlayerInvSlotY - 1 + 58, 102, 18, 18, 18);
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
                        this.drawTexturedModalRect(this.firstPlayerInvSlotX - 1 + c * 18, this.firstPlayerInvSlotY - 1 + r * 18, 102, 18, 18, 18);
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

            if (this.baublesLoaded)
            {
                for (int i = 0; i < 7; i++)
                {
                    if ((mask & bit) != 0)
                    {
                        this.drawTexturedModalRect(this.firstArmorSlotX - 21, this.firstArmorSlotY - 1 + i * 18, 102, 18, 18, 18);
                    }
                    bit <<= 1;
                }
            }
        }

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            if (this.inventory.getModuleInventory().getStackInSlot(i).isEmpty())
            {
                this.drawTexturedModalRect(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, 240, 144, 16, 16);
            }
        }

        if (this.baublesLoaded)
        {
            this.bindTexture(this.guiTextureWidgets);

            // Draw the background icon over empty baubles slots
            int swapperBaublesStart = this.containerInvSwapper.getSwapperBaublesSlots().first;
            int playerBaublesStart = this.containerInvSwapper.getPlayerBaublesSlots().first;

            for (int i = 0; i < 7; i++)
            {
                Slot slot = this.containerInvSwapper.getSlot(swapperBaublesStart + i);

                // Swapper's Baubles slots
                if (slot != null && slot.getHasStack() == false)
                {
                    this.drawTexturedModalRect(this.firstModuleSlotX + 77, this.firstArmorSlotY + i * 18, 224, i * 16, 16, 16);
                }

                slot = this.containerInvSwapper.getSlot(playerBaublesStart + i);

                // Player's Baubles slots
                if (slot != null && slot.getHasStack() == false)
                {
                    this.drawTexturedModalRect(this.firstArmorSlotX - 20, this.firstArmorSlotY + i * 18, 224, i * 16, 16, 16);
                }
            }
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
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_PRESET + i, this.firstPlayerInvSlotX + 112 + i * 12, this.firstPlayerInvSlotY - 28,
                    8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        // Add the Toggle Row buttons
        // Hotbar is first
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS, this.firstPlayerInvSlotX - 17, this.firstPlayerInvSlotY + 59,
                14, 14, 60, 28, this.guiTextureWidgets, 14, 0));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i + 1, this.firstPlayerInvSlotX - 17, this.firstPlayerInvSlotY + 1 + i * 18,
                    14, 14, 60, 28, this.guiTextureWidgets, 14, 0));
        }

        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, this.firstPlayerInvSlotX + 1 + i * 18, this.firstPlayerInvSlotY - 17,
                    14, 14, 60, 42, this.guiTextureWidgets, 14, 0));
        }

        // Toggle button for armor
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + 9, this.firstArmorSlotX + 1, this.firstArmorSlotY + 91,
                14, 14, 60, 56, this.guiTextureWidgets, 14, 0));

        // Locked mode toggle
        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_LOCKED, this.firstPlayerInvSlotX + 1, this.firstPlayerInvSlotY - 28, 8, 8, 8, 0,
                this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  0, "enderutilities.gui.label.item.enabled"),
                ButtonState.createTranslate(0, 48, "enderutilities.gui.label.item.disabled")));

        // Cycle mode toggle
        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_CYCLE, this.firstPlayerInvSlotX + 13, this.firstPlayerInvSlotY - 28, 8, 8, 8, 0,
                this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 40, "enderutilities.gui.label.cyclemode.disabled"),
                ButtonState.createTranslate(0, 88, "enderutilities.gui.label.cyclemode.enabled")));

        if (this.baublesLoaded)
        {
            // Toggle button for baubles
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + 10, this.firstArmorSlotX - 19, this.firstArmorSlotY + 127,
                    14, 14, 60, 56, this.guiTextureWidgets, 14, 0));
        }
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
        else if (button.id >= BTN_ID_FIRST_TOGGLE_COLUMNS && button.id <= (BTN_ID_FIRST_TOGGLE_COLUMNS + 10))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_COLUMNS, button.id - BTN_ID_FIRST_TOGGLE_COLUMNS));
        }
        else if (button.id == BTN_ID_LOCKED)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_LOCKED, 0));
        }
        else if (button.id == BTN_ID_CYCLE)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_CYCLE_MODE, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        ItemStack stack = this.containerInvSwapper.getContainerItem();

        if (stack.isEmpty() == false)
        {
            // Locked mode
            if (callbackId == BTN_ID_LOCKED)
            {
                return NBTUtils.getBoolean(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_LOCKED) ? 1 : 0;
            }
            // Cycle mode
            else if (callbackId == BTN_ID_CYCLE)
            {
                return NBTUtils.getBoolean(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_CYCLE_MODE) ? 1 : 0;
            }
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
