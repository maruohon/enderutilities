package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerCreationStation;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class GuiCreationStation extends GuiContainerLargeStacks implements IButtonStateCallback
{
    public static final int[] ACTION_BUTTON_POSX = new int[] { 41, 59, 77, 149, 167, 185 };
    public static final int[] CRAFTING_BUTTON_POSX = new int[] { 44, 57, 70, 186, 173, 160 };
    public static final String[] BUTTON_STRINGS = new String[] {
            "enderutilities.gui.label.moveallitemsexcepthotbar",
            "enderutilities.gui.label.movematchingitemsexcepthotbar",
            "enderutilities.gui.label.leaveonefilledstack",
            "enderutilities.gui.label.fillstacks",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.moveallitems"
    };

    private final TileEntityCreationStation tecs;
    private final ContainerCreationStation containerCS;
    private final int invSize;

    public GuiCreationStation(ContainerCreationStation container, TileEntityCreationStation te)
    {
        super(container, 240, 256, "gui.container.creationstation");

        this.infoArea = new InfoArea(223, 88, 11, 11, "enderutilities.gui.infoarea.creationstation");
        this.tecs = te;
        this.containerCS = container;
        this.invSize = container.inventory.getSlots();
        this.scaledStackSizeTextInventories.add(this.tecs.getFurnaceInventory());
        this.scaledStackSizeTextInventories.add(container.inventory);
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
        this.fontRenderer.drawString(I18n.format("enderutilities.container.creationstation"), 80, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int modeMask = this.tecs.getModeMask();

        // Draw the selection marker around the selected module's button
        this.drawTexturedModalRect(this.guiLeft + 204, this.guiTop + 105 + this.tecs.getSelectedModuleSlot() * 18, 120, 0, 10, 10);

        // Draw the hilight background for the selected module slot
        this.drawTexturedModalRect(this.guiLeft + 215, this.guiTop + 101 + this.tecs.getSelectedModuleSlot() * 18, 102, 18, 18, 18);

        int x = 9;
        int y = 156;
        int mode = this.tecs.getQuickMode();
        if (mode >= 0 && mode <= 5)
        {
            x = ACTION_BUTTON_POSX[mode];
        }

        // Draw the selection marker around the selected action button, ie. the "Quick Action"
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + y, 120, 10, 14, 14);

        // Draw the selection marker around selected crafting mode buttons
        mode = modeMask & 0xFF;
        for (int i = 0, bit = 0x1; i < 6; i++, bit <<= 1)
        {
            if ((mode & bit) != 0)
            {
                this.drawTexturedModalRect(this.guiLeft + CRAFTING_BUTTON_POSX[i], this.guiTop + 88, 120, 0, 10, 10);
            }
        }

        // Draw the selection border around the selected crafting preset buttons
        mode = (modeMask >> 8) & 0x7;
        this.drawTexturedModalRect(this.guiLeft +  27, this.guiTop + 32 + mode * 11, 120, 0, 10, 10);
        mode = (modeMask >> 11) & 0x7;
        this.drawTexturedModalRect(this.guiLeft + 203, this.guiTop + 32 + mode * 11, 120, 0, 10, 10);

        // Draw the hilight border around the last interacted on crafting grid, which will be used for JEI recipe filling
        x = this.containerCS.getLastInteractedCraftingGridId() == 1 ? 146 : 38;
        this.drawTexturedModalRect(this.guiLeft + x, this.guiTop + 31, 184, 200, 56, 56);

        // The inventory is not accessible (because there is no valid Memory Card selected, or something else...)
        if (this.tecs.isInventoryAccessible(this.container.getPlayer()) == false)
        {
            // Draw the dark background over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xPos - 1;
                y = this.guiTop + slot.yPos - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background over the disabled crafting slots (left side)
            for (int i = 31; i <= 39; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xPos - 1;
                y = this.guiTop + slot.yPos - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }

            // Draw the dark background over the disabled crafting slots (right side)
            for (int i = 41; i <= 49; i++)
            {
                Slot slot = this.inventorySlots.getSlot(i);
                x = this.guiLeft + slot.xPos - 1;
                y = this.guiTop + slot.yPos - 1;
                this.drawTexturedModalRect(x, y, 102, 0, 18, 18);
            }
        }

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < this.tecs.getMemoryCardInventory().getSlots(); i++)
        {
            if (this.tecs.getMemoryCardInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(this.guiLeft + 216, this.guiTop + 102 + i * 18, 240, 80, 16, 16);
            }
        }

        // Left side furnace progress icons

        boolean isFast = (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_FAST) != 0;

        // Draw the burn progress flame
        int vOff = (isFast ? 34 : 20);
        int h = (this.containerCS.fuelProgress & 0x7F) * 13 / 100;
        this.drawTexturedModalRect(this.guiLeft + 9, this.guiTop + 30 + 12 - h, 134, vOff + 13 - h, 14, h + 1);

        // Draw the smelting progress arrow
        if ((this.containerCS.smeltProgress & 0xFF) > 0)
        {
            vOff = isFast ? 10 : 0;
            int w = (this.containerCS.smeltProgress & 0xFF) * 11 / 100;
            this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 11, 134, vOff, w, 10);
        }

        // Right side furnace progress icons

        isFast = (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_FAST) != 0;

        // Draw the burn progress flame
        vOff = (isFast ? 34 : 20);
        h = ((this.containerCS.fuelProgress >> 8) & 0x7F) * 13 / 100;
        this.drawTexturedModalRect(this.guiLeft + 217, this.guiTop + 30 + 12 - h, 134, vOff + 13 - h, 14, h + 1);

        // Draw the smelting progress arrow
        if ((this.containerCS.smeltProgress >> 8) > 0)
        {
            vOff = isFast ? 10 : 0;
            int w = ((this.containerCS.smeltProgress >> 8) & 0x7F) * 11 / 100;
            this.drawTexturedModalRect(this.guiLeft + 203 + 10 - w, this.guiTop + 11, 144 + 10 - w, vOff, w, 10);
        }

        // Draw the red or purple background under non-matching crafting grid slots
        for (int invId = 0; invId < 2; invId++)
        {
            if (this.tecs.getShowRecipe(invId))
            {
                for (int slotNum = 0; slotNum < 9; slotNum++)
                {
                    ItemStack gridStack = this.containerCS.getCraftMatrixWrapper(invId).getStackInSlot(slotNum);
                    ItemStack recipeStack = this.containerCS.getRecipeItem(invId, slotNum);

                    if (InventoryUtils.areItemStacksEqual(gridStack, recipeStack) == false)
                    {
                        Slot slot = this.containerCS.getSlot(31 + invId * 10 + slotNum);
                        x = this.guiLeft + slot.xPos - 1;
                        y = this.guiTop + slot.yPos - 1;

                        // Missing items, red background
                        if (gridStack == null)
                        {
                            this.bindTexture(this.guiTextureWidgets);

                            GlStateManager.disableLighting();
                            this.drawTexturedModalRect(x, y, 102, 72, 18, 18);
                            GlStateManager.enableLighting();

                            RenderHelper.enableGUIStandardItemLighting();
                            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                            GlStateManager.enableRescaleNormal();
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
                            this.zLevel = 100.0F;
                            itemRender.zLevel = 100.0F;
                            GlStateManager.enableDepth();
                            GlStateManager.enableBlend();
                            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                            this.itemRender.renderItemAndEffectIntoGUI(recipeStack, x + 1, y + 1);

                            this.itemRender.zLevel = 0.0F;
                            this.zLevel = 0.0F;

                            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, recipeStack, x + 1, y + 1, "0");
                        }
                        // Extra items, purple background
                        else if (recipeStack == null)
                        {
                            this.bindTexture(this.guiTextureWidgets);
                            GlStateManager.disableLighting();
                            this.drawTexturedModalRect(x, y, 102, 36, 18, 18);
                            GlStateManager.enableLighting();
                            //this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.getTextureManager(), recipeStack, x, y, "+");
                        }
                        // Wrong items, red background
                        else
                        {
                            this.bindTexture(this.guiTextureWidgets);
                            GlStateManager.disableLighting();
                            this.drawTexturedModalRect(x, y, 102, 72, 18, 18);
                            GlStateManager.enableLighting();
                        }
                    }
                }
            }
        }

        RenderHelper.enableGUIStandardItemLighting();
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

        // Add the quick-move-items buttons
        for (int i = 0; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(i + 4, x + ACTION_BUTTON_POSX[i] + 1, y + 157, 12, 12, 24, i * 12,
                    this.guiTextureWidgets, 12, 0, BUTTON_STRINGS[i]));
        }

        // Crafting grid clear buttons
        this.buttonList.add(new GuiButtonHoverText(10, x +  84, y + 89, 8, 8, 0,  8,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.clearcraftinggrid"));
        this.buttonList.add(new GuiButtonHoverText(11, x + 148, y + 89, 8, 8, 0,  8,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.clearcraftinggrid"));

        // Add other left side crafting grid buttons
        this.buttonList.add(new GuiButtonStateCallback(12, x + 45, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 32, "enderutilities.gui.label.useoredictionary.disabled"),
                ButtonState.createTranslate(0, 32, "enderutilities.gui.label.useoredictionary.enabled")));
        this.buttonList.add(new GuiButtonStateCallback(13, x + 58, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 24, "enderutilities.gui.label.leaveoneitemongrid.disabled"),
                ButtonState.createTranslate(0, 24, "enderutilities.gui.label.leaveoneitemongrid.enabled")));
        this.buttonList.add(new GuiButtonStateCallback(14, x + 71, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 16, "enderutilities.gui.label.useitemsfrominventory.disabled"),
                ButtonState.createTranslate(0, 16, "enderutilities.gui.label.useitemsfrominventory.enabled")));

        // Add other right side crafting grid buttons
        this.buttonList.add(new GuiButtonStateCallback(15, x + 161, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 16, "enderutilities.gui.label.useitemsfrominventory.disabled"),
                ButtonState.createTranslate(0, 16, "enderutilities.gui.label.useitemsfrominventory.enabled")));
        this.buttonList.add(new GuiButtonStateCallback(16, x + 174, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 24, "enderutilities.gui.label.leaveoneitemongrid.disabled"),
                ButtonState.createTranslate(0, 24, "enderutilities.gui.label.leaveoneitemongrid.enabled")));
        this.buttonList.add(new GuiButtonStateCallback(17, x + 187, y + 89, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 32, "enderutilities.gui.label.useoredictionary.disabled"),
                ButtonState.createTranslate(0, 32, "enderutilities.gui.label.useoredictionary.enabled")));

        // Add the sort button for the station inventory
        this.buttonList.add(new GuiButtonHoverText(30, x + 116, y + 90, 8, 8, 0, 24,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems"));

        // Add the sort button for the player inventory
        this.buttonList.add(new GuiButtonHoverText(31, x + 116, y + 162, 8, 8, 0, 24,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems.player"));

        // Add the left and right side furnace mode buttons
        this.buttonList.add(new GuiButtonStateCallback(18, x +   9, y + 71, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60,  0, "enderutilities.gui.label.furnace.slow"),
                ButtonState.createTranslate(60, 14, "enderutilities.gui.label.furnace.fast")));
        this.buttonList.add(new GuiButtonStateCallback(19, x + 217, y + 71, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60,  0, "enderutilities.gui.label.furnace.slow"),
                ButtonState.createTranslate(60, 14, "enderutilities.gui.label.furnace.fast")));

        // Add the recipe recall buttons
        for (int i = 0; i < 5; i++)
        {
            this.buttonList.add(new GuiButtonIcon(20 + i, x +  28, y + 33 + i * 11, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
            this.buttonList.add(new GuiButtonIcon(25 + i, x + 204, y + 33 + i * 11, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        switch (callbackId)
        {
            case 12: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_CRAFTING_OREDICT) != 0 ? 1 : 0;
            case 13: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_CRAFTING_KEEPONE) != 0 ? 1 : 0;
            case 14: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_CRAFTING_AUTOUSE) != 0 ? 1 : 0;
            case 15: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_CRAFTING_AUTOUSE) != 0 ? 1 : 0;
            case 16: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_CRAFTING_KEEPONE) != 0 ? 1 : 0;
            case 17: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_CRAFTING_OREDICT) != 0 ? 1 : 0;
            case 18: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_LEFT_FAST) != 0 ? 1 : 0;
            case 19: return (this.containerCS.modeMask & TileEntityCreationStation.MODE_BIT_RIGHT_FAST) != 0 ? 1 : 0;
        }
        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        super.actionPerformed(button);

        boolean valid = true;
        int action = 0;
        int element = 0;

        if (button.id >= 0 && button.id <= 3)
        {
            action = TileEntityCreationStation.GUI_ACTION_SELECT_MODULE;
            element = button.id - 0;
        }
        else if (button.id >= 4 && button.id <= 9)
        {
            if (isShiftKeyDown() || mouseButton != 0)
            {
                action = TileEntityCreationStation.GUI_ACTION_SET_QUICK_ACTION;
            }
            else
            {
                action = TileEntityCreationStation.GUI_ACTION_MOVE_ITEMS;
            }

            element = button.id - 4;
        }
        // Clear crafting grid buttons
        else if (button.id == 10 || button.id == 11)
        {
            action = TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID;
            element = button.id - 10;
        }
        // Crafting grid mode buttons and furnace mode buttons
        else if (button.id >= 12 && button.id <= 19)
        {
            action = TileEntityCreationStation.GUI_ACTION_TOGGLE_MODE;
            element = button.id - 12;
        }
        // Recipe recall buttons
        else if (button.id >= 20 && button.id <= 29)
        {
            // Left click: Load recipe or load items from recipe into crafting grid, or if sneaking, store the recipe
            if (mouseButton == 0)
            {
                if (isShiftKeyDown())
                {
                    action = TileEntityCreationStation.GUI_ACTION_RECIPE_STORE;
                }
                else
                {
                    action = TileEntityCreationStation.GUI_ACTION_RECIPE_LOAD;
                }

                element = button.id - 20;
            }
            // Right click: Clear crafting grid
            else if (mouseButton == 1)
            {
                action = TileEntityCreationStation.GUI_ACTION_CLEAR_CRAFTING_GRID;
                element = (button.id - 20) / 5;
            }
            // Middle click: Remove recipe
            else if (mouseButton == 2)
            {
                action = TileEntityCreationStation.GUI_ACTION_RECIPE_CLEAR;
                element = button.id - 20;
            }
        }
        // Sort buttons
        else if (button.id >= 30 && button.id <= 31)
        {
            action = TileEntityCreationStation.GUI_ACTION_SORT_ITEMS;
            element = button.id - 30;
        }
        else
        {
            valid = false;
        }

        if (valid)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tecs.getWorld().provider.getDimension(),
                    this.tecs.getPos(), ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, action, element));
        }
    }
}
