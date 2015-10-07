package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.nei.guihook.IGuiSlotDraw;
import cpw.mods.fml.common.Optional;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.ModRegistry;

@Optional.Interface(iface = "codechicken.nei.guihook.IGuiSlotDraw", modid = "NotEnoughItems")
public class GuiHandyBag extends InventoryEffectRenderer implements IGuiSlotDraw
{
    public static final int BTN_ID_FIRST_SELECT_MODULE = 0;
    public static final int BTN_ID_FIRST_MOVE_ITEMS    = 4;

    protected static RenderItem itemRenderCustom = new RenderItemLargeStacks();
    protected EntityPlayer player;
    protected ContainerHandyBag container;
    protected InventoryItemModular inventory;
    protected ResourceLocation textureGuiBackground;
    protected ResourceLocation textureGuiWidgets;
    protected float mouseXFloat;
    protected float mouseYFloat;
    protected int backgroundU;
    protected int backgroundV;
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
        this.inventory = container.inventoryItemModular;
        this.invSize = this.inventory.getSizeInventory();
        this.numModuleSlots = this.inventory.getModuleInventory().getSizeInventory();
        this.bagTier = this.container.getBagTier();

        this.textureGuiBackground = ReferenceTextures.getGuiTexture("gui.container.handybag." + this.bagTier);
        this.textureGuiWidgets = ReferenceTextures.getGuiTexture("gui.container.handybag.0");
        this.xSize = this.bagTier == 1 ? 256 : 176;
        this.ySize = 256;
        this.backgroundU = this.bagTier == 1 ? 0 : 40;
        this.backgroundV = 0;
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

        if (ModRegistry.isModLoadedNEI() == false)
        {
            // Swap the RenderItem() instance for the duration of rendering the ItemStacks to the GUI
            RenderItem ri = this.setItemRender(itemRenderCustom);
            super.drawScreen(mouseX, mouseY, gameTicks);
            this.setItemRender(ri);
        }
        else
        {
            super.drawScreen(mouseX, mouseY, gameTicks);
        }

        this.drawTooltips(mouseX, mouseY);
        this.mouseXFloat = (float)mouseX;
        this.mouseYFloat = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
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
        int index = this.inventory.getSelectedStorageModuleIndex();
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
        // TODO end of to-be-removed code in 1.8

        int xOff = this.guiLeft + (this.bagTier == 1 ? 91 : 51);
        // Draw the player model
        GuiInventory.func_147046_a(xOff, this.guiTop + 82, 30, xOff - this.mouseXFloat, this.guiTop + 25 - this.mouseYFloat, this.mc.thePlayer);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int xOff = this.bagTier == 1 ? 40 : 0;
        this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), xOff + 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag.storagemodules", new Object[0]), xOff + 97, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag", new Object[0]), xOff + 8, 90, 0x404040);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        int numModules = this.inventory.getModuleInventory().getSizeInventory();
        for (int i = 0; i < numModules; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 3 + i * 18, this.firstModuleSlotY + 18, 10, 10, 18, 0, this.textureGuiWidgets, 0, 10));
        }

        int x = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 1;
        int y = this.guiTop + this.container.getSlot(0).yDisplayPosition + 54;
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 0, x +   0, y + 0, 14, 14, 214, 14, this.textureGuiWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 1, x +  18, y + 0, 14, 14, 214,  0, this.textureGuiWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 2, x +  36, y + 0, 14, 14, 214, 70, this.textureGuiWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 3, x + 108, y + 0, 14, 14, 214, 28, this.textureGuiWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 4, x + 126, y + 0, 14, 14, 214, 42, this.textureGuiWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_MOVE_ITEMS + 5, x + 144, y + 0, 14, 14, 214, 56, this.textureGuiWidgets, 14, 0));
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
        List<String> list = new ArrayList<String>();

        // Move all items to bag
        if (((GuiButton)this.buttonList.get(4)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.moveallitems", new Object[0]));
        }
        // Move matching items to bag
        else if (((GuiButton)this.buttonList.get(5)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.quickstack", new Object[0]));
            list.add("(" + I18n.format("enderutilities.gui.label.movematchingitems", new Object[0]) + ")");
        }
        // Leave one stack of each item type and fill that stack
        else if (((GuiButton)this.buttonList.get(6)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.leaveonefilledstack", new Object[0]));
        }
        // Fill stacks from bag
        else if (((GuiButton)this.buttonList.get(7)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.fillstacks", new Object[0]));
        }
        // Move matching items from bag
        else if (((GuiButton)this.buttonList.get(8)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.restock", new Object[0]));
            list.add("(" + I18n.format("enderutilities.gui.label.movematchingitems", new Object[0]) + ")");
        }
        // Move all items from bag
        else if (((GuiButton)this.buttonList.get(9)).mousePressed(this.mc, mouseX, mouseY))
        {
            list.add(I18n.format("enderutilities.gui.label.moveallitems", new Object[0]));
        }

        this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_MOVE_ITEMS && button.id <= (BTN_ID_FIRST_MOVE_ITEMS + 5))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, 0, 0, 0,
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_MOVE_ITEMS, button.id - BTN_ID_FIRST_MOVE_ITEMS));
        }
    }

    protected RenderItem setItemRender(RenderItem itemRenderIn)
    {
        RenderItem ri = itemRender;
        itemRender = itemRenderIn;
        return ri;
    }

    @Optional.Method(modid = "NotEnoughItems")
    @Override
    public void drawSlotItem(Slot slot, ItemStack stack, int x, int y, String quantity)
    {
        // Slot is in the bag's inventory, render using the smaller font for stack size
        if (slot.inventory == this.inventory)
        {
            itemRenderCustom.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRenderCustom.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
        else
        {
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y, quantity);
        }
    }
}
