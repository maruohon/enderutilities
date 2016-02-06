package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class GuiHandyBag extends InventoryEffectRenderer
{
    public static final int BTN_ID_FIRST_SELECT_MODULE = 0;
    public static final int BTN_ID_FIRST_MOVE_ITEMS    = 4;

    //protected static RenderItem itemRenderCustom = new RenderItemLargeStacks();
    protected EntityPlayer player;
    protected ContainerHandyBag container;
    protected InventoryItemModular invModular;
    protected ResourceLocation guiTexture;
    protected ResourceLocation guiTextureWidgets;
    protected float mouseXFloat;
    protected float mouseYFloat;
    protected int invSize;
    protected int numModuleSlots;
    protected int bagTier;
    protected int firstModuleSlotX;
    protected int firstModuleSlotY;
    protected int firstArmorSlotX;
    protected int firstArmorSlotY;

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container);
        this.player = container.player;
        this.container = container;
        this.invModular = container.inventoryItemModular;
        this.invSize = this.invModular.getSizeInventory();
        this.numModuleSlots = this.invModular.getModuleInventory().getSizeInventory();
        this.bagTier = this.container.getBagTier();

        this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.handybag." + this.bagTier);
        this.guiTextureWidgets = ReferenceTextures.getGuiTexture("gui.widgets");
        this.xSize = this.bagTier == 1 ? 256 : 176;
        this.ySize = 256;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 5 * 18;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(0).yDisplayPosition - 33;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(this.invSize + this.numModuleSlots + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(this.invSize + this.numModuleSlots + 36).yDisplayPosition;
        this.createButtons();

        /*if (ModRegistry.isModLoadedNEI() == false)
        {
            // Swap the RenderItem() instance for the duration of rendering the ItemStacks to the GUI
            RenderItem ri = this.setItemRender(itemRenderCustom);
            super.drawScreen(mouseX, mouseY, gameTicks);
            this.setItemRender(ri);
        }
        else
        {
            super.drawScreen(mouseX, mouseY, gameTicks);
        }*/

        super.drawScreen(mouseX, mouseY, gameTicks);

        this.drawTooltips(mouseX, mouseY);
        this.mouseXFloat = (float)mouseX;
        this.mouseYFloat = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        this.bindTexture(this.guiTextureWidgets);

        // The inventory is not accessible (because there is no valid Memory Card selected)
        if (this.invModular.isUseableByPlayer(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.container.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.container.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.container.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 18, 18, 18);
        }

        // Memory Card slots are not accessible, because the opened bag isn't currently available
        // Draw the dark background icon over the disabled slots
        if (this.invModular.getModuleInventory().isUseableByPlayer(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, 102, 0, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.invModular.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, 102, 18, 18, 18);
            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.firstModuleSlotY + 18, 120, 0, 10, 10);
        }

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        //this.bindTexture(TextureMap.locationBlocksTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            if (this.invModular.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, 240, 80, 16, 16);
            }
        }

        // Draw the background icon for empty player armor slots
        //IInventory inv = this.player.inventory;
        // Note: We use the original actual inventory size for these!
        // Otherwise stuff would mess up when the bag is picked up with the cursor, since
        // the number of slots in the container doesn't change.
        /*for (int i = 0; i < 4; i++)
        {
            if (inv.getStackInSlot(39 - i) == null)
            {
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(this.firstArmorSlotX, this.firstArmorSlotY + i * 18, icon, 16, 16);
            }
        }*/

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8

        int xOff = this.guiLeft + (this.bagTier == 1 ? 91 : 51);
        // Draw the player model
        GuiInventory.drawEntityOnScreen(xOff, this.guiTop + 82, 30, xOff - this.mouseXFloat, this.guiTop + 25 - this.mouseYFloat, this.mc.thePlayer);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int xOff = this.bagTier == 1 ? 40 : 0;
        this.fontRendererObj.drawString(I18n.format("container.crafting"), xOff + 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards"), xOff + 97, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag"), xOff + 8, 90, 0x404040);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        int numModules = this.invModular.getModuleInventory().getSizeInventory();
        for (int i = 0; i < numModules; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 4 + i * 18, this.firstModuleSlotY + 19, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        int x = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 2;
        int y = this.guiTop + this.container.getSlot(0).yDisplayPosition + 55;

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 0, x +   0, y + 0, 12, 12, 24,  0, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.moveallitems"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 1, x +  18, y + 0, 12, 12, 24, 12, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.quickstack",
                 "(" + I18n.format("enderutilities.gui.label.movematchingitems") + ")"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 2, x +  36, y + 0, 12, 12, 24, 24, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.leaveonefilledstack"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 3, x + 108, y + 0, 12, 12, 24, 36, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.fillstacks"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 4, x + 126, y + 0, 12, 12, 24, 48, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.restock",
                 "(" + I18n.format("enderutilities.gui.label.movematchingitems") + ")"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 5, x + 144, y + 0, 12, 12, 24, 60, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.moveallitems"));
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
        for (int i = 0; i < this.buttonList.size(); i++)
        {
            GuiButton button = (GuiButton)this.buttonList.get(i);

            // Mouse is over the button
            if ((button instanceof GuiButtonHoverText) && button.mousePressed(this.mc, mouseX, mouseY) == true)
            {
                this.drawHoveringText(((GuiButtonHoverText)button).getHoverStrings(), mouseX, mouseY, this.fontRendererObj);
            }
        }
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_MOVE_ITEMS && button.id <= (BTN_ID_FIRST_MOVE_ITEMS + 5))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_MOVE_ITEMS, button.id - BTN_ID_FIRST_MOVE_ITEMS));
        }
    }

    protected RenderItem setItemRender(RenderItem itemRenderIn)
    {
        RenderItem ri = itemRender;
        itemRender = itemRenderIn;
        return ri;
    }

    /*@Optional.Method(modid = "NotEnoughItems")
    @Override
    public void drawSlotItem(Slot slot, ItemStack stack, int x, int y, String quantity)
    {
        // Slot is in the bag's inventory, render using the smaller font for stack size
        if (slot.inventory == this.invModular)
        {
            itemRenderCustom.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRenderCustom.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
        else
        {
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
    }*/
}
