package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class GuiHandyBag extends InventoryEffectRenderer
{
    protected EntityPlayer player;
    protected ContainerHandyBag container;
    protected InventoryItemModular inventory;
    protected ResourceLocation guiTexture;
    protected float mouseXFloat;
    protected float mouseYFloat;
    protected int backgroundU;
    protected int backgroundV;
    protected int invSize;
    protected int numModuleSlots;
    protected int firstModuleSlotX;
    protected int firstModuleSlotY;

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container);
        this.player = container.player;
        this.container = container;
        this.inventory = container.inventory;
        this.invSize = this.inventory.getSizeInventory();
        this.numModuleSlots = this.inventory.getStorageModuleSlotCount();

        this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.handybag." + container.getBagTier());
        this.xSize = 176;
        this.ySize = 256;
        this.backgroundU = 40;
        this.backgroundV = 0;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.firstModuleSlotX = this.guiLeft + this.container.getSlot(this.invSize - this.numModuleSlots).xDisplayPosition;
        this.firstModuleSlotY = this.guiTop + this.container.getSlot(this.invSize - this.numModuleSlots).yDisplayPosition;
        this.createButtons();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        this.createButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
        this.mouseXFloat = (float)mouseX;
        this.mouseYFloat = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, this.backgroundU, this.backgroundV, this.xSize, this.ySize);

        int xOff = this.guiLeft;
        int yOff = this.guiTop;

        // The inventory is not accessible (because there is no valid Memory Card selected)
        if (this.inventory.isItemInventoryAccessible() == false)
        {
            // Draw the dark background icon over the inventory slots
            xOff = this.guiLeft + this.container.getSlot(0).xDisplayPosition - 1;
            yOff = this.guiTop + this.container.getSlot(0).yDisplayPosition - 1;
            for (int row = 0; row < 3; row++)
            {
                for (int column = 0; column < 9; column++)
                {
                    this.drawTexturedModalRect(xOff + column * 18, yOff + row * 18, 0, 0, 18, 18);
                }
            }
        }

        // Draw the colored background for the selected module slot
        this.drawTexturedModalRect(this.firstModuleSlotX + this.inventory.getSelectedStorageModule() * 18, this.firstModuleSlotY, 1, 19, 16, 16);

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        IIcon icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0, slotNum = this.invSize - this.numModuleSlots; icon != null && i < this.numModuleSlots; i++, slotNum++)
        {
            if (this.inventory.getStackInSlot(slotNum) == null)
            {
                this.drawTexturedModelRectFromIcon(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, icon, 16, 16);
            }
        }

        // Draw the background icon for empty player armor slots
        IInventory inv = this.player.inventory;
        xOff = this.guiLeft + this.container.getSlot(this.invSize + 36).xDisplayPosition;
        yOff = this.guiTop + this.container.getSlot(this.invSize + 36).yDisplayPosition;
        for (int i = 0; i < 4; i++)
        {
            if (inv.getStackInSlot(39 - i) == null)
            {
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(xOff, yOff + i * 18, icon, 16, 16);
            }
        }

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8

        // Draw the player model
        GuiInventory.func_147046_a(this.guiLeft + 51, this.guiTop + 82, 30, this.guiLeft + 51 - this.mouseXFloat, this.guiTop + 25 - this.mouseYFloat, this.mc.thePlayer);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag.storagemodules", new Object[0]), 97, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag", new Object[0]), 8, 90, 0x404040);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        for (int i = 0; i < this.inventory.getStorageModuleSlotCount(); i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, this.firstModuleSlotX + 3 + i * 18, this.firstModuleSlotY + 18, 10, 10, 18, 0, this.guiTexture, 0, 10));
        }
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id < this.inventory.getStorageModuleSlotCount())
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_SELECT_MODULE, button.id));
        }
    }
}
