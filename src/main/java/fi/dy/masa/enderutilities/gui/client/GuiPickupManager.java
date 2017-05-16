package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModules;
import fi.dy.masa.enderutilities.item.ItemPickupManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class GuiPickupManager extends GuiContainerLargeStacks implements IButtonStateCallback
{
    public static final int NUM_LINK_CRYSTAL_SLOTS = 3;

    private final ContainerPickupManager containerPickupManager;
    private final InventoryItem inventoryItemTransmit;
    private final InventoryItemModules inventoryItemModules;
    private final InventoryItem inventoryItemFilters;
    private final int firstLinkCrystalSlot;

    public GuiPickupManager(ContainerPickupManager container)
    {
        super(container, 176, 256, "gui.container.pickupmanager");

        this.infoArea = new InfoArea(153, 87, 18, 18, "enderutilities.gui.infoarea.pickupmanager");
        this.containerPickupManager = container;
        this.inventoryItemTransmit = container.inventoryItemTransmit;
        this.inventoryItemModules = container.inventoryItemModules;
        this.inventoryItemFilters = container.inventoryItemFilters;
        this.firstLinkCrystalSlot = UtilItemModular.getFirstIndexOfModuleType(
                this.inventoryItemModules.getContainerItemStack(), ModuleType.TYPE_LINKCRYSTAL);
        this.scaledStackSizeTextInventories.add(this.inventoryItemTransmit);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("enderutilities.container.pickupmanager"), 8, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.transportfilters"), 8, 19, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.inventoryfilters"), 81, 112, 0x404040);
        this.fontRenderer.drawString(I18n.format("enderutilities.gui.label.filterpresets") + ":", 8, 163, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Transmit slot is not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slot
        if (this.inventoryItemTransmit.isAccessibleBy(this.player) == false)
        {
            this.drawTexturedModalRect(x + 88, y + 28, 102, 0, 18, 18);
        }

        // Memory Card slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventoryItemModules.isAccessibleBy(this.player) == false)
        {
            for (int i = 0; i < NUM_LINK_CRYSTAL_SLOTS; i++)
            {
                this.drawTexturedModalRect(x + 116 - 1 + i * 18, y + 29 - 1, 102, 0, 18, 18);
            }
        }

        // Filter slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventoryItemFilters.isAccessibleBy(this.player) == false)
        {
            // Transport filters
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    this.drawTexturedModalRect(x + 8 - 1 + j * 18, y + 47 - 1 + i * 18, 102, 0, 18, 18);
                }
            }

            // Input filters
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 9; j++)
                {
                    this.drawTexturedModalRect(x + 8 - 1 + j * 18, y + 123 - 1 + i * 18, 102, 0, 18, 18);
                }
            }
        }

        ItemStack containerStack = this.containerPickupManager.getContainerItem();

        if (containerStack.isEmpty() == false)
        {
            // Draw the colored background for the selected module slot
            int index = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_LINKCRYSTAL);
            this.drawTexturedModalRect(x + 116 - 1 + index * 18, y + 29 - 1, 102, 18, 18, 18);

            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(x + 119 + index * 18, y + 17, 120, 0, 10, 10);

            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(containerStack, ItemPickupManager.TAG_NAME_CONTAINER, ItemPickupManager.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(x + 101 + sel * 18, y + 162, 120, 0, 10, 10);
        }

        // Draw the background icon over empty storage module slots
        for (int slot = this.firstLinkCrystalSlot, i = 0; i < NUM_LINK_CRYSTAL_SLOTS; slot++, i++)
        {
            if (this.inventoryItemModules.getStackInSlot(slot).isEmpty())
            {
                this.drawTexturedModalRect(x + 116 + i * 18, y + 29, 240, 32, 16, 16);
            }
        }
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        Slot slot = this.getSlotUnderMouse();

        // Hovering over the transport slot
        if (slot != null && slot == this.inventorySlots.getSlot(0) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.transportitemsslot"));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRenderer);
        }
        else
        {
            super.drawTooltips(mouseX, mouseY);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Add the Link Crystal selection buttons
        for (int i = 0; i < NUM_LINK_CRYSTAL_SLOTS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + 120 + i * 18, y + 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Add the preset selection buttons
        for (int i = 0; i < ItemPickupManager.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i + 3, x + 102 + i * 18, y + 163, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        // Add the transport filter settings buttons

        // Match or ignore this group of filters
        this.buttonList.add(new GuiButtonStateCallback( 7, x +  9, y + 29, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 98, "enderutilities.gui.label.filtergroup.disabled"),
                ButtonState.createTranslate(60, 28, "enderutilities.gui.label.filtergroup.enabled")));

        // Blacklist or Whitelist
        this.buttonList.add(new GuiButtonStateCallback( 8, x + 27, y + 29, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 70, "enderutilities.gui.label.blacklist"),
                ButtonState.createTranslate(60, 84, "enderutilities.gui.label.whitelist")));

        // Match or ignore damage/metadata
        this.buttonList.add(new GuiButtonStateCallback( 9, x + 45, y + 29, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 112, "enderutilities.gui.label.meta.match"),
                ButtonState.createTranslate(60, 126, "enderutilities.gui.label.meta.ignore")));

        // Match or ignore NBT
        this.buttonList.add(new GuiButtonStateCallback(10, x + 63, y + 29, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 154, "enderutilities.gui.label.nbt.ignore"),
                ButtonState.createTranslate(60, 140, "enderutilities.gui.label.nbt.match")));

        // Add the inventory filter settings buttons

        // Match or ignore this group of filters
        this.buttonList.add(new GuiButtonStateCallback(11, x +  9, y + 105, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 98, "enderutilities.gui.label.filtergroup.disabled"),
                ButtonState.createTranslate(60, 28, "enderutilities.gui.label.filtergroup.enabled")));

        // Blacklist or Whitelist
        this.buttonList.add(new GuiButtonStateCallback(12, x + 27, y + 105, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 70, "enderutilities.gui.label.blacklist"),
                ButtonState.createTranslate(60, 84, "enderutilities.gui.label.whitelist")));

        // Match or ignore damage/metadata
        this.buttonList.add(new GuiButtonStateCallback(13, x + 45, y + 105, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 112, "enderutilities.gui.label.meta.match"),
                ButtonState.createTranslate(60, 126, "enderutilities.gui.label.meta.ignore")));

        // Match or ignore NBT
        this.buttonList.add(new GuiButtonStateCallback(14, x + 63, y + 105, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 154, "enderutilities.gui.label.nbt.ignore"),
                ButtonState.createTranslate(60, 140, "enderutilities.gui.label.nbt.match")));
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        ItemStack stack = this.containerPickupManager.getContainerItem();

        if (stack.isEmpty() == false)
        {
            switch (callbackId)
            {
                case  7: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_TXFILTER_ENABLED) == 0 ? 0 : 1;
                case  8: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_TXFILTER_MODE) == 0 ? 0 : 1;
                case  9: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_TXFILTER_META) == 0 ? 0 : 1;
                case 10: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_TXFILTER_NBT) == 0 ? 0 : 1;

                case 11: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_INVFILTER_ENABLED) == 0 ? 0 : 1;
                case 12: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_INVFILTER_MODE) == 0 ? 0 : 1;
                case 13: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_INVFILTER_META) == 0 ? 0 : 1;
                case 14: return ItemPickupManager.getSettingValue(stack, ItemPickupManager.TAG_NAME_INVFILTER_NBT) == 0 ? 0 : 1;
            }
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id <= 2)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_SELECT_MODULE, button.id));
        }
        else if (button.id >= 3 && button.id <= 6)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_CHANGE_PRESET, button.id - 3));
        }
        else if (button.id >= 7 && button.id <= 10)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS, button.id - 7));
        }
        else if (button.id >= 11 && button.id <= 14)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_TOGGLE_INVENTORY_SETTINGS, button.id - 11));
        }
    }
}
