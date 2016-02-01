package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.InventoryItem;
import fi.dy.masa.enderutilities.inventory.InventoryItemModules;
import fi.dy.masa.enderutilities.item.ItemPickupManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceReflection;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class GuiPickupManager extends GuiEnderUtilities
{
    public ContainerPickupManager container;
    public InventoryItem inventoryItemTransmit;
    public InventoryItemModules inventoryItemModules;
    public InventoryItem inventoryItemFilters;
    public EntityPlayer player;
    public int numModuleSlots;

    public GuiPickupManager(ContainerPickupManager container)
    {
        super(container, 176, 256, "gui.container.pickupmanager");
        this.player = container.player;
        this.container = container;
        this.inventoryItemTransmit = container.inventoryItemTransmit;
        this.inventoryItemModules = container.inventoryItemModules;
        this.inventoryItemFilters = container.inventoryItemFilters;
        // FIXME the -1 is after adding the capacitor module...
        this.numModuleSlots = this.inventoryItemModules.getSizeInventory() - 1;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.createButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        this.createButtons(); // Re-create the buttons to reflect the current state
        super.drawScreen(mouseX, mouseY, gameTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.pickupmanager", new Object[0]), 8, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.transportfilters", new Object[0]), 8, 19, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.inventoryfilters", new Object[0]), 81, 112, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.filterpresets", new Object[0]) + ":", 8, 163, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        ItemStack containerStack = this.container.getModularItem();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Transmit slot is not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slot
        if (this.inventoryItemTransmit.isUseableByPlayer(this.player) == false)
        {
            this.drawTexturedModalRect(x + 88, y + 28, 102, 0, 18, 18);
        }

        // Memory Card slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventoryItemModules.isUseableByPlayer(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(x + 116 - 1 + i * 18, y + 29 - 1, 102, 0, 18, 18);
            }
        }

        // Filter slots are not accessible, because the item isn't currently accessible
        // Draw the dark background icon over the disabled slots
        if (this.inventoryItemFilters.isUseableByPlayer(this.player) == false)
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

        // Draw the colored background for the selected module slot
        if (containerStack != null)
        {
            int index = UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_LINKCRYSTAL);
            this.drawTexturedModalRect(x + 116 - 1 + index * 18, y + 29 - 1, 102, 18, 18, 18);
            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(x + 119 + index * 18, y + 17, 120, 0, 10, 10);
        }

        if (containerStack != null)
        {
            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(containerStack, ItemPickupManager.TAG_NAME_CONTAINER, ItemPickupManager.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(x + 101 + sel * 18, y + 162, 120, 0, 10, 10);
        }

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        //this.bindTexture(TextureMap.locationBlocksTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        /*IIcon icon = EnderUtilitiesItems.linkCrystal.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_LINKCRYSTAL);
        for (int i = 0; icon != null && i < this.numModuleSlots; i++)
        {
            // FIXME the +1 is after adding the capacitor module...
            if (this.inventoryItemModules.getStackInSlot(i + 1) == null)
            {
                this.drawTexturedModelRectFromIcon(x + 116 + i * 18, y + 29, icon, 16, 16);
            }
        }*/

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8*/
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        super.drawTooltips(mouseX, mouseY);

        Slot slot = null;
        try
        {
            slot = (Slot)ReferenceReflection.fieldGuiContainerTheSlot.get(this);
        }
        catch (IllegalAccessException e)
        {
            return;
        }

        // Hovering over the tool slot
        if (slot != null && slot == this.inventorySlots.getSlot(0) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.transportitemsslot", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void addConditionalButton(int id, int x, int y, int w, int h, ItemStack stack, String tag, int u1, int v1, int u2, int v2, String s1, String s2)
    {
        if (ItemPickupManager.getSettingValue(stack, tag) == 0)
        {
            this.buttonList.add(new GuiButtonHoverText(id, x, y, w, h, u1, v1, this.guiTextureWidgets, w, 0, "enderutilities.gui.label." + s1));
        }
        else
        {
            this.buttonList.add(new GuiButtonHoverText(id, x, y, w, h, u2, v2, this.guiTextureWidgets, w, 0, "enderutilities.gui.label." + s2));
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        int id = 0;
        // Add the Link Crystal selection buttons
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 120 + i * 18, y + 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Add the preset selection buttons
        for (int i = 0; i < ItemPickupManager.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 102 + i * 18, y + 163, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        ItemStack containerStack = this.container.getModularItem();

        // Add the transport filter settings buttons

        // Match or ignore this group of filters
        this.addConditionalButton(id++, x +  9, y + 29, 14, 14, containerStack, ItemPickupManager.TAG_NAME_TXFILTER_ENABLED, 60, 98, 60, 28, "filtergroup.disabled", "filtergroup.enabled");

        // Blacklist or Whitelist
        this.addConditionalButton(id++, x + 27, y + 29, 14, 14, containerStack, ItemPickupManager.TAG_NAME_TXFILTER_MODE, 60, 70, 60, 84, "blacklist", "whitelist");

        // Match or ignore damage/metadata
        this.addConditionalButton(id++, x + 45, y + 29, 14, 14, containerStack, ItemPickupManager.TAG_NAME_TXFILTER_META, 60, 112, 60, 126, "meta.match", "meta.ignore");

        // Match or ignore NBT
        this.addConditionalButton(id++, x + 63, y + 29, 14, 14, containerStack, ItemPickupManager.TAG_NAME_TXFILTER_NBT, 60, 154, 60, 140, "nbt.ignore", "nbt.match");


        // Add the inventory filter settings buttons

        // Match or ignore this group of filters
        this.addConditionalButton(id++, x +  9, y + 105, 14, 14, containerStack, ItemPickupManager.TAG_NAME_INVFILTER_ENABLED, 60, 98, 60, 28, "filtergroup.disabled", "filtergroup.enabled");

        // Blacklist or Whitelist
        this.addConditionalButton(id++, x + 27, y + 105, 14, 14, containerStack, ItemPickupManager.TAG_NAME_INVFILTER_MODE, 60, 70, 60, 84, "blacklist", "whitelist");

        // Match or ignore damage/metadata
        this.addConditionalButton(id++, x + 45, y + 105, 14, 14, containerStack, ItemPickupManager.TAG_NAME_INVFILTER_META, 60, 112, 60, 126, "meta.match", "meta.ignore");

        // Match or ignore NBT
        this.addConditionalButton(id++, x + 63, y + 105, 14, 14, containerStack, ItemPickupManager.TAG_NAME_INVFILTER_NBT, 60, 154, 60, 140, "nbt.ignore", "nbt.match");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        int first = 0;
        if (button.id >= first && button.id < (first + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_SELECT_MODULE, button.id - first));
            return;
        }
        first += this.numModuleSlots;

        if (button.id >= first && button.id < (first + ItemPickupManager.NUM_PRESETS))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_CHANGE_PRESET, button.id - first));
            return;
        }
        first += ItemPickupManager.NUM_PRESETS;

        if (button.id >= first && button.id < (first + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_TOGGLE_TRANSPORT_SETTINGS, button.id - first));
            return;
        }
        first += 4;

        if (button.id >= first && button.id < (first + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_PICKUP_MANAGER, ItemPickupManager.GUI_ACTION_TOGGLE_INVENTORY_SETTINGS, button.id - first));
            return;
        }
    }
}
