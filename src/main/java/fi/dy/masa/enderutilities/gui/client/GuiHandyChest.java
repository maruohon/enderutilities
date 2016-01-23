package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.Optional;

import codechicken.nei.guihook.IGuiSlotDraw;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerHandyChest;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.ModRegistry;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;

@Optional.Interface(iface = "codechicken.nei.guihook.IGuiSlotDraw", modid = "NotEnoughItems")
public class GuiHandyChest extends GuiEnderUtilities implements IGuiSlotDraw
{
    protected static RenderItem itemRenderCustom = new RenderItemLargeStacks();
    protected TileEntityHandyChest tehc;
    protected ContainerHandyChest containerHC;
    protected int chestTier;

    public GuiHandyChest(ContainerHandyChest container, TileEntityHandyChest te)
    {
        super(container, 176, 249, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0));
        this.tehc = te;
        this.containerHC = container;
        this.chestTier = te.getStorageTier();
    }

    @Override
    public void initGui()
    {
        this.setGuiYSize();

        super.initGui();

        this.createButtons();
    }

    protected void setGuiYSize()
    {
        switch(this.chestTier)
        {
            case 0: this.ySize = 187; break;
            case 1: this.ySize = 213; break;
            case 2: this.ySize = 249; break;
            default:
        }
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
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handychest", new Object[0]), 8, 30, 0x404040);
        //this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 63, 84 + this.chestTier * 36, 0x404025);
        //this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards", new Object[0]), 98, 14, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTexture);

        int invSize = this.tehc.getItemInventory().getSizeInventory();

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(this.guiLeft + 101 + this.tehc.getSelectedModule() * 18, this.guiTop + 26, 244, 72, 10, 10);

        // Draw the hilight background for the selected module slot
        this.drawTexturedModalRect(this.guiLeft + 97 + this.tehc.getSelectedModule() * 18, this.guiTop + 7, 220, 100, 18, 18);

        int y = 77 + this.chestTier * 36;
        int x = 9;
        int mode = this.tehc.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            x = new int[] { 9, 27, 45, 117, 135, 153 }[mode];
        }

        // Draw the selection marker around the selected action button, ie. the "Quick Action"
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 238, 82, 14, 14);

        // The inventory is not accessible (because there is no valid Memory Card selected, or the item is not accessible)
        if (this.tehc.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < invSize; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xDisplayPosition - 1;
                y = this.guiTop + slot.yDisplayPosition - 1;
                this.drawTexturedModalRect(x, y, 220, 82, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerHC.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.containerHC.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 220, 100, 18, 18);
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
            if (this.tehc.getStackInSlot(i) == null)
            {
                this.drawTexturedModelRectFromIcon(this.guiLeft + 98 + i * 18, this.guiTop + 8, icon, 16, 16);
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
            this.buttonList.add(new GuiButtonIcon(i, x + 102 + i * 18, y + 27, 8, 8, 220, 72, this.guiTexture, 8, 0));
        }

        int yOff = 78 + this.chestTier * 36;
        int xOffs[] = new int[] { 9, 27, 45, 117, 135, 153 };
        String[] strs = new String[] {
                "enderutilities.gui.label.moveallitems",
                "enderutilities.gui.label.movematchingitems",
                "enderutilities.gui.label.leaveonefilledstack",
                "enderutilities.gui.label.fillstacks",
                "enderutilities.gui.label.movematchingitems",
                "enderutilities.gui.label.moveallitems"
        };

        for (int i = 0; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(i + 4, x + xOffs[i] + 1, y + yOff, 12, 12, 220, i * 12,
                    this.guiTexture, 12, 0, new String[] { I18n.format(strs[i], new Object[0]) }));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id < 4)
        {
            PacketHandler.INSTANCE.sendToServer(
                new MessageGuiAction(this.tehc.getWorldObj().provider.dimensionId,
                    this.tehc.xCoord, this.tehc.yCoord, this.tehc.zCoord,
                    ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                    TileEntityHandyChest.GUI_ACTION_SELECT_MODULE, button.id));
        }
        else if (button.id >= 4 && button.id < 10)
        {
            if (isShiftKeyDown() == true)
            {
                PacketHandler.INSTANCE.sendToServer(
                        new MessageGuiAction(this.tehc.getWorldObj().provider.dimensionId,
                            this.tehc.xCoord, this.tehc.yCoord, this.tehc.zCoord,
                            ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                            TileEntityHandyChest.GUI_ACTION_SET_QUICK_ACTION, button.id - 4));
            }
            else
            {
                PacketHandler.INSTANCE.sendToServer(
                    new MessageGuiAction(this.tehc.getWorldObj().provider.dimensionId,
                        this.tehc.xCoord, this.tehc.yCoord, this.tehc.zCoord,
                        ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC,
                        TileEntityHandyChest.GUI_ACTION_MOVE_ITEMS, button.id - 4));
            }
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
        if (slot.inventory == this.tehc.getItemInventory())
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
