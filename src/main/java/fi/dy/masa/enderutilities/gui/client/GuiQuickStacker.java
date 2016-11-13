package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStacker;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiQuickStacker extends GuiEnderUtilities
{
    public static final int BTN_ID_FIRST_SELECT_PRESET  = 0;
    public static final int BTN_ID_FIRST_TOGGLE_ROWS    = 4;
    public static final int BTN_ID_FIRST_TOGGLE_COLUMNS = 8;

    private final ContainerQuickStacker containerQS;
    private int firstInvSlotX;
    private int firstInvSlotY;

    public GuiQuickStacker(ContainerQuickStacker container)
    {
        super(container, 192, 126, "gui.container.quickstacker");

        this.infoArea = new InfoArea(6, 6, 17, 17, "enderutilities.gui.label.quickstacker.info");
        this.containerQS = container;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstInvSlotX = this.guiLeft + this.containerQS.getSlot(0).xDisplayPosition;
        this.firstInvSlotY = this.guiTop  + this.containerQS.getSlot(0).yDisplayPosition;

        this.createButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.quickstacker", new Object[0]), 28, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.slotpresets", new Object[0]) + ":", 60, 135, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        ItemStack stack = this.containerQS.getContainerItem();
        if (stack != null)
        {
            // Draw the selection border around the selected preset's button
            byte sel = NBTUtils.getByte(stack, ItemQuickStacker.TAG_NAME_CONTAINER, ItemQuickStacker.TAG_NAME_PRESET_SELECTION);
            this.drawTexturedModalRect(this.firstInvSlotX + 93 + sel * 18, this.firstInvSlotY - 29, 120, 0, 10, 10);

            // Draw the colored background for the selected/enabled inventory slots
            final long mask = ItemQuickStacker.getEnabledSlotsMask(stack);
            long bit = 0x1;

            // Hotbar
            for (int c = 0; c < 9; c++)
            {
                if ((mask & bit) != 0)
                {
                    this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + 58, 102, 18, 18, 18);
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
                        this.drawTexturedModalRect(this.firstInvSlotX - 1 + c * 18, this.firstInvSlotY - 1 + r * 18, 102, 18, 18, 18);
                    }
                    bit <<= 1;
                }
            }

            // Offhand slot
            bit = 1L << 40;
            if ((mask & bit) != 0)
            {
                this.drawTexturedModalRect(this.firstInvSlotX - 1 - 18, this.firstInvSlotY - 1 - 18, 102, 18, 18, 18);
            }
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the preset selection buttons
        for (int i = 0; i < ItemQuickStacker.NUM_PRESETS; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_PRESET + i, this.firstInvSlotX + 94 + i * 18, this.firstInvSlotY - 28,
                    8, 8, 0, 128 + i * 8, this.guiTextureWidgets, 8, 0));
        }

        // Add the Toggle Row buttons
        // Hotbar is first
        this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS, this.firstInvSlotX - 17, this.firstInvSlotY + 59,
                14, 14, 60, 28, this.guiTextureWidgets, 14, 0));

        for (int i = 0; i < 3; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_ROWS + i + 1, this.firstInvSlotX - 17, this.firstInvSlotY + 1 + i * 18,
                    14, 14, 60, 28, this.guiTextureWidgets, 14, 0));
        }

        // Add the Toggle Column buttons
        for (int i = 0; i < 9; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_TOGGLE_COLUMNS + i, this.firstInvSlotX + 1 + i * 18, this.firstInvSlotY - 17,
                    14, 14, 60, 42, this.guiTextureWidgets, 14, 0));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_PRESET && button.id < (BTN_ID_FIRST_SELECT_PRESET + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_QUICK_STACKER, ItemQuickStacker.GUI_ACTION_CHANGE_PRESET, button.id - BTN_ID_FIRST_SELECT_PRESET));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_ROWS && button.id < (BTN_ID_FIRST_TOGGLE_ROWS + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_QUICK_STACKER, ItemQuickStacker.GUI_ACTION_TOGGLE_ROWS, button.id - BTN_ID_FIRST_TOGGLE_ROWS));
        }
        else if (button.id >= BTN_ID_FIRST_TOGGLE_COLUMNS && button.id < (BTN_ID_FIRST_TOGGLE_COLUMNS + 10))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_QUICK_STACKER, ItemQuickStacker.GUI_ACTION_TOGGLE_COLUMNS, button.id - BTN_ID_FIRST_TOGGLE_COLUMNS));
        }
    }
}
