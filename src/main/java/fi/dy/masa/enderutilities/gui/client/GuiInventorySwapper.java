package fi.dy.masa.enderutilities.gui.client;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class GuiInventorySwapper extends GuiEnderUtilities
{
    public static final int BTN_ID_FIRST_SELECT_MODULE  = 0;
    public static final int BTN_ID_FIRST_SELECT_PRESET  = 4;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 8;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 12;

    public ContainerInventorySwapper container;
    public InventoryItemModular inventory;
    public EntityPlayer player;
    public int numModuleSlots;
    public int firstModuleSlotX;
    public int firstModuleSlotY;
    public int firstInvSlotX;
    public int firstInvSlotY;
    public int firstArmorSlotX;
    public int firstArmorSlotY;

    public GuiInventorySwapper(ContainerInventorySwapper container)
    {
        super(container, 192, 199, "gui.container.inventoryswapper");
        this.player = container.player;
        this.container = container;
        this.inventory = container.inventoryItemModular;
        this.numModuleSlots = this.inventory.getModuleInventory().getSizeInventory();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);

        // These need to be drawn after super.drawScreen(), because the buttons get rendered after the drawGuiContainerBackgroundLayer()

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        this.bindTexture(this.guiTexture);

        // Draw the colored background for the selected module slot
        int index = this.inventory.getSelectedModuleIndex();
        if (index >= 0)
        {
            // Draw the selection border around the selected module's button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.guiTop + 64, 234, 66, 10, 10);
        }

        // Draw the selection border around the selected preset's button
        if (this.container.getModularItem() != null)
        {
            byte sel = NBTUtils.getByte(this.container.getModularItem(), ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + sel * 18, this.guiTop + 88, 234, 66, 10, 10);
        }

        // Draw the number icons over the preset selection buttons
        for (int i = 0; i < ItemInventorySwapper.NUM_PRESETS; i++)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + i * 18, this.guiTop + 88, 224, 66 + i * 10, 10, 10);
        }
        GL11.glEnable(GL11.GL_LIGHTING);

        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(0).xDisplayPosition;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(0).yDisplayPosition;
        this.firstInvSlotX   = this.guiLeft + this.container.getSlot(this.numModuleSlots).xDisplayPosition;
        this.firstInvSlotY   = this.guiTop  + this.container.getSlot(this.numModuleSlots).yDisplayPosition;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(this.numModuleSlots + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(this.numModuleSlots + 36).yDisplayPosition;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper", new Object[0]), 98, 4, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper.storagemodules", new Object[0]), 113, 35, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper.presets", new Object[0]), 113, 78, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTexture);

        // Memory Card slots are not accessible, because the opened bag isn't currently available
        // Draw the dark background icon over the disabled slots
        if (this.inventory.getModuleInventory().isUseableByPlayer(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, 192, 0, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.inventory.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, 192, 18, 18, 18);
            // Draw the selection border around the selected module's button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.guiTop + 64, 234, 66, 10, 10);
        }

        ItemStack stack = this.container.getModularItem();
        if (stack != null)
        {
            // Draw the colored background for the selected/enabled inventory slots
            byte selected = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            long mask = NBTUtils.getLong(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET + selected);
            long bit = 0x1;
            // Hotbar
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + 58, 192, 18, 18, 18);
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
                        this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + r * 18, 192, 18, 18, 18);
                    }
                    bit <<= 1;
                }
            }

            // Armor slots
            for (int r = 0; r < 4; r++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstArmorSlotX - 1, this.firstArmorSlotY - 1 + r * 18, 192, 18, 18, 18);
                }
                bit <<= 1;
            }
        }

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        IIcon icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0; icon != null && i < this.numModuleSlots; i++)
        {
            if (this.inventory.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModelRectFromIcon(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, icon, 16, 16);
            }
        }

        // Draw the background icon for empty player armor slots
        IInventory inv = this.player.inventory;
        // Note: We use the original actual inventory size for these!
        // Otherwise stuff would mess up when the bag is picked up with the cursor, since
        // the number of slots in the container doesn't change.
        for (int i = 0; i < 4; i++)
        {
            if (inv.getStackInSlot(39 - i) == null)
            {
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(this.firstArmorSlotX, this.firstArmorSlotY + i * 18, icon, 16, 16);
            }
        }

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8*/
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 3 + i * 18, this.firstModuleSlotY + 18, 10, 10, 210, 0, this.guiTexture, 0, 10));
        }

        // Add the preset selection buttons
        for (int i = 0; i < ItemInventorySwapper.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_PRESET + i, this.firstModuleSlotX + 3 + i * 18, this.guiTop + 88, 10, 10, 224, 36, this.guiTexture, 0, 10));
        }

        int x = this.guiLeft + 6;
        int y = this.guiTop + 117;

        // Add the Toggle Row buttons
        // Hotbar is first
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS, x, this.guiTop + 175, 16, 16, 192, 36, this.guiTexture, 0, 16));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i + 1, x, y + i * 18, 16, 16, 192, 36, this.guiTexture, 0, 16));
        }

        x = this.guiLeft + 24;
        y = this.guiTop + 99;
        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, x + i * 18, y, 16, 16, 208, 36, this.guiTexture, 0, 16));
        }

        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + 9, x, y - 17, 16, 16, 192, 132, this.guiTexture, 0, 16));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_SELECT_PRESET && button.id < (BTN_ID_FIRST_SELECT_PRESET + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_CHANGE_PRESET, button.id - BTN_ID_FIRST_SELECT_PRESET));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_ROWS && button.id < (BTN_ID_FIRST_TOGGLE_ROWS + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_ROWS, button.id - BTN_ID_FIRST_TOGGLE_ROWS));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_COLUMNS && button.id < (BTN_ID_FIRST_TOGGLE_COLUMNS + 10))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, ItemInventorySwapper.GUI_ACTION_TOGGLE_COLUMNS, button.id - BTN_ID_FIRST_TOGGLE_COLUMNS));
        }
    }
}
