package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.InventoryItem;

public class GuiInventorySwapper extends GuiEnderUtilities
{
    public static final int BTN_ID_FIRST_SELECT_MODULE  = 0;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 4;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 8;

    public ContainerInventorySwapper container;
    public InventoryItem inventory;
    public EntityPlayer player;
    public int numModuleSlots;
    public int firstModuleSlotX;
    public int firstModuleSlotY;
    public int firstArmorSlotX;
    public int firstArmorSlotY;

    public GuiInventorySwapper(ContainerInventorySwapper container)
    {
        super(container, 192, 194, "gui.container.inventoryswapper");
        this.player = container.player;
        this.container = container;
        this.inventory = container.inventoryItem;
        this.numModuleSlots = this.inventory.getSizeInventory();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);

        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 5 * 18;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(0).yDisplayPosition - 33;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(this.numModuleSlots + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(this.numModuleSlots + 36).yDisplayPosition;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper", new Object[0]), 8, 90, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.inventoryswapper.storagemodules", new Object[0]), 97, 59, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        /*GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.textureGuiBackground);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, this.backgroundU, this.backgroundV, this.xSize, this.ySize);

        this.bindTexture(this.textureGuiWidgets);

        // The inventory is not accessible (because there is no valid Memory Card selected)
        if (this.inventory.isItemInventoryAccessible() == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.container.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 0, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.container.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.container.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 0, 18, 18, 18);
        }

        // Memory Card slots are not accessible, because the opened bag isn't currently available
        // Draw the dark background icon over the disabled slots
        if (this.inventory.isModuleInventoryAccessible() == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, 0, 0, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.inventory.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, 0, 18, 18, 18);
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
            if (this.inventory.getStackInSlot(i) == null)
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
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 3 + i * 18, this.firstModuleSlotY + 18, 10, 10, 18, 0, this.guiTexture, 0, 10));
        }

        int x = this.guiLeft + 6;
        int y = this.guiTop + this.ySize - 60;

        // Add the Toggle Row buttons
        for (int i = 0; i < 4; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i, x, y + i * 18, 16, 16, 192, 36, this.guiTexture, 0, 16));
        }

        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, x + i * 18, y, 16, 16, 208, 36, this.guiTexture, 0, 16));
        }
    }
}
