package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStackerAdvanced;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;

public class GuiQuickStackerAdvanced extends GuiEnderUtilities
{
    private final ContainerQuickStackerAdvanced containerQSA;
    private final IItemHandler inventoryModules;

    public GuiQuickStackerAdvanced(ContainerQuickStackerAdvanced container, TileEntityQuickStackerAdvanced te)
    {
        super(container, 220, 244, "gui.container.quickstacker.advanced");
        this.containerQSA = container;
        this.inventoryModules = te.getBaseItemHandler();
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
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.quickstacker.advanced", new Object[0]), 8, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        int index = this.containerQSA.activeModulesMask;

        for (int i = 0, bit = 1; i < TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS; i++)
        {
            if (this.inventoryModules.getStackInSlot(i) == null)
            {
                // Draw the background icon over empty Link Crystal slots
                this.drawTexturedModalRect(x + 7, y + 18 + i * 18, 240, 32, 16, 16);
            }
            else if ((index & bit) != 0)
            {
                // Draw the green background for active Link Crystal slots
                this.drawTexturedModalRect(x + 6, y + 17 + i * 18, 102, 54, 18, 18);
                // Draw the selection border around the active Link Crystals' selection button
                this.drawTexturedModalRect(x + 25, y + 21 + i * 18, 120, 0, 10, 10);
            }

            bit <<= 1;
        }

        // Draw the selection border around the selected preset's button
        index = this.containerQSA.selectedPreset;
        this.drawTexturedModalRect(x + 205, y + 165 + index * 18, 120, 0, 10, 10);
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        super.drawTooltips(mouseX, mouseY);

        Slot slot = this.getSlotUnderMouse();
        // Hovering over the tool slot
        if (slot != null && slot == this.inventorySlots.getSlot(0) && slot.getHasStack() == false)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.transportitemsslot", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected void addConditionalButton(int id, int x, int y, int w, int h, ContainerQuickStackerAdvanced container, int u1, int v1, int u2, int v2, String s1, String s2)
    {
        // TODO
        if (id == 0)
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
        for (int i = 0; i < TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 26, y + 22 + i * 18, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Add the preset selection buttons
        for (int i = 0; i < TileEntityQuickStackerAdvanced.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 206, y + 166 + i * 13, 8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        // Add the column selection buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(id++, x + 44 + i * 18, y + 146, 14, 14, 60, 42, this.guiTextureWidgets, 14, 0));
        }

        // Add the row selection buttons
        for (int i = 0, yOff = 0; i < 4; i++)
        {
            if (i == 3) yOff = 4;
            this.buttonList.add(new GuiButtonIcon(id++, x + 26, y + yOff + 164 + i * 18, 14, 14, 60, 28, this.guiTextureWidgets, 14, 0));
        }

        // Add the Link Crystals toggle button
        this.addConditionalButton(id++, x + 8, y + 164, 14, 14, this.containerQSA, 60, 56, 60, 182, "use.uselinkcrystaltargets", "use.areaplayer");

        // Add the filter settings buttons for group 1

        // Match or ignore this group of filters
        this.addConditionalButton(id++, x + 43, y + 19, 14, 14, this.containerQSA, 60, 98, 60, 28, "filtergroup.disabled", "filtergroup.enabled");

        // Blacklist or Whitelist
        this.addConditionalButton(id++, x + 61, y + 19, 14, 14, this.containerQSA, 60, 70, 60, 84, "blacklist", "whitelist");

        // Match or ignore damage/metadata
        this.addConditionalButton(id++, x + 79, y + 19, 14, 14, this.containerQSA, 60, 112, 60, 126, "meta.match", "meta.ignore");

        // Match or ignore NBT
        this.addConditionalButton(id++, x + 97, y + 19, 14, 14, this.containerQSA, 60, 154, 60, 140, "nbt.ignore", "nbt.match");


        // Add the filter settings buttons for group 2

        // Match or ignore this group of filters
        this.addConditionalButton(id++, x + 43, y + 77, 14, 14, this.containerQSA, 60, 98, 60, 28, "filtergroup.disabled", "filtergroup.enabled");

        // Blacklist or Whitelist
        this.addConditionalButton(id++, x + 61, y + 77, 14, 14, this.containerQSA, 60, 70, 60, 84, "blacklist", "whitelist");

        // Match or ignore damage/metadata
        this.addConditionalButton(id++, x + 79, y + 77, 14, 14, this.containerQSA, 60, 112, 60, 126, "meta.match", "meta.ignore");

        // Match or ignore NBT
        this.addConditionalButton(id++, x + 97, y + 77, 14, 14, this.containerQSA, 60, 154, 60, 140, "nbt.ignore", "nbt.match");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        int first = 0;
        int dim = this.containerQSA.te.getWorld().provider.getDimension();
        BlockPos pos = this.containerQSA.te.getPos();

        if (button.id >= first && button.id < (first + TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityQuickStackerAdvanced.GUI_ACTION_SELECT_MODULE, button.id - first));
            return;
        }
        first += TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS;

        if (button.id >= first && button.id < (first + TileEntityQuickStackerAdvanced.NUM_PRESETS))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityQuickStackerAdvanced.GUI_ACTION_CHANGE_PRESET, button.id - first));
            return;
        }
        first += TileEntityQuickStackerAdvanced.NUM_PRESETS;

        if (button.id >= first && button.id < (first + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_SETTINGS_1, button.id - first));
            return;
        }
        first += 4;

        if (button.id >= first && button.id < (first + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityQuickStackerAdvanced.GUI_ACTION_TOGGLE_SETTINGS_2, button.id - first));
            return;
        }
    }
}
