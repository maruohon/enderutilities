package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.common.Optional;

import codechicken.nei.guihook.IGuiSlotDraw;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerCreationStation;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.ModRegistry;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;

@Optional.Interface(iface = "codechicken.nei.guihook.IGuiSlotDraw", modid = "NotEnoughItems")
public class GuiCreationStation extends GuiEnderUtilities implements IGuiSlotDraw
{
    protected static RenderItem itemRenderCustom = new RenderItemLargeStacks();
    protected TileEntityCreationStation tecs;
    protected ContainerCreationStation containerCS;
    protected ResourceLocation guiTextureWidgets;
    public static final int[] ACTION_BUTTON_POSX = new int[] { 41, 59, 77, 149, 167, 185 };

    public GuiCreationStation(ContainerCreationStation container, TileEntityCreationStation te)
    {
        super(container, 240, 256, "gui.container.creationstation");
        this.tecs = te;
        this.containerCS = container;
        this.guiTextureWidgets = ReferenceTextures.getGuiTexture("gui.widgets");
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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.creationstation", new Object[0]), 80, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int invSize = this.tecs.getItemInventory().getSizeInventory();

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(this.guiLeft + 204, this.guiTop + 105 + this.tecs.getSelectedModule() * 18, 120, 0, 10, 10);

        // Draw the hilight background for the selected module slot
        this.drawTexturedModalRect(this.guiLeft + 215, this.guiTop + 101 + this.tecs.getSelectedModule() * 18, 102, 18, 18, 18);

        int x = 9;
        int y = 156;
        int mode = this.tecs.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            x = ACTION_BUTTON_POSX[mode];
        }

        // Draw the selection marker around the selected action button, ie. the "Quick Action"
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 120, 10, 14, 14);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.tecs.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < invSize; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background icon over the disabled crafting slots (left side)
            for (int i = 31; i <= 39; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background icon over the disabled crafting slots (right side)
            for (int i = 41; i <= 49; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerCS.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.containerCS.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 18, 18, 18);
        }

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon over empty storage module slots
        IIcon icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0; icon != null && i < 4; i++)
        {
            if (this.tecs.getStackInSlot(i) == null)
            {
                this.drawTexturedModelRectFromIcon(this.guiLeft + 216, this.guiTop + 102 + i * 18, icon, 16, 16);
            }
        }

        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8*/
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Add the Memory Card selection buttons
        for (int i = 0; i < 4; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + 205, y + 106 + i * 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        String[] strs = new String[] {
                "enderutilities.gui.label.moveallitems",
                "enderutilities.gui.label.movematchingitems",
                "enderutilities.gui.label.leaveonefilledstack",
                "enderutilities.gui.label.fillstacks",
                "enderutilities.gui.label.movematchingitems",
                "enderutilities.gui.label.moveallitems"
        };

        // Add the quick-move-items buttons
        for (int i = 0; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(i + 4, x + ACTION_BUTTON_POSX[i] + 1, y + 157, 12, 12, 24, i * 12,
                    this.guiTextureWidgets, 12, 0, new String[] { I18n.format(strs[i], new Object[0]) }));
        }

        // Add the left side crafting grid buttons
        this.buttonList.add(new GuiButtonIcon(10, x + 58, y + 88, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0));
        this.buttonList.add(new GuiButtonIcon(12, x + 71, y + 88, 8, 8, 0, 16, this.guiTextureWidgets, 8, 0));
        this.buttonList.add(new GuiButtonIcon(14, x + 84, y + 88, 8, 8, 0,  8, this.guiTextureWidgets, 8, 0));

        // Add the right side crafting grid buttons
        this.buttonList.add(new GuiButtonIcon(11, x + 148, y + 88, 8, 8, 0,  8, this.guiTextureWidgets, 8, 0));
        this.buttonList.add(new GuiButtonIcon(13, x + 161, y + 88, 8, 8, 0, 16, this.guiTextureWidgets, 8, 0));
        this.buttonList.add(new GuiButtonIcon(15, x + 174, y + 88, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0));

        // Add the left and right side furnace mode buttons
        this.buttonList.add(new GuiButtonIcon(16, x +   9, y + 71, 14, 14, 60, 0, this.guiTextureWidgets, 14, 0));
        this.buttonList.add(new GuiButtonIcon(17, x + 217, y + 71, 14, 14, 60, 0, this.guiTextureWidgets, 14, 0));

        // Add the recipe recall buttons
        for (int i = 0; i < 5; i++)
        {
            this.buttonList.add(new GuiButtonIcon(18 + i, x +  29, y + 33 + i * 11, 8, 8, 0, 32 + i * 8, this.guiTextureWidgets, 8, 0));
            this.buttonList.add(new GuiButtonIcon(23 + i, x + 203, y + 33 + i * 11, 8, 8, 0, 32 + i * 8, this.guiTextureWidgets, 8, 0));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id <= 3)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityCreationStation.GUI_ACTION_SELECT_MODULE, button.id));
        }
        else if (button.id >= 4 && button.id <= 9)
        {
            if (isShiftKeyDown() == true)
            {
                PacketHandler.INSTANCE.sendToServer(
                        new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                            this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                            TileEntityCreationStation.GUI_ACTION_SET_QUICK_ACTION, button.id - 4));
            }
            else
            {
                PacketHandler.INSTANCE.sendToServer(
                    new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                        this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                        TileEntityCreationStation.GUI_ACTION_MOVE_ITEMS, button.id - 4));
            }
        }
        // Clear crafting grid buttons
        else if (button.id == 10 && button.id == 11)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID, button.id - 10));
        }
        // Crafting grid mode buttons
        else if (button.id >= 12 && button.id <= 15)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID, button.id - 12));
        }
        // Toggle furnace mode buttons
        else if (button.id >= 16 && button.id <= 17)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityCreationStation.GUI_ACTION_TOGGLE_FURNACE_MODE, button.id - 16));
        }
        // Recipe recall buttons
        else if (button.id >= 18 && button.id <= 27)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tecs.getWorldObj().provider.dimensionId,
                    this.tecs.xCoord, this.tecs.yCoord, this.tecs.zCoord, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityCreationStation.GUI_ACTION_RECALL_RECIPE, button.id - 18));
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
        // Slot is in the external inventory, render using the smaller font for stack size
        if (slot.inventory == this.tecs.getItemInventory())
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
