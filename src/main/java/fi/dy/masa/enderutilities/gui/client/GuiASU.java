package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.inventory.container.ContainerASU;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class GuiASU extends GuiContainerLargeStacks
{
    private final TileEntityASU teasu;

    public GuiASU(ContainerASU container, TileEntityASU te)
    {
        super(container, 176, 195, "gui.container.asu");

        this.teasu = te;
        this.infoArea = new InfoArea(160, 5, 11, 11, "enderutilities.gui.infoarea.asu");
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
        this.fontRenderer.drawString(I18n.format("enderutilities.container.asu"), 8, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 102, 0x404040);

        String str = I18n.format("enderutilities.gui.label.slot.amount.num", this.teasu.getBaseItemHandler().getSlots());
        this.fontRenderer.drawString(str, 20, 18, 0x404040);

        str = I18n.format("enderutilities.gui.label.stack.limit.num", this.teasu.getBaseItemHandler().getInventoryStackLimit());
        this.fontRenderer.drawString(str, 20, 30, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the slot backgrounds according to how many slots this tier has
        int invSize = this.teasu.getInvSize();
        int rows = Math.max((int) (Math.ceil((double) invSize / 9)), 1);

        for (int row = 0; row < rows; row++)
        {
            int rowLen = MathHelper.clamp(invSize - (row * 9), 1, 9);
            this.drawTexturedModalRect(x + 7, y + 42 + row * 18, 7, 111, rowLen * 18, 18);
        }

        this.drawLockedSlotBackgrounds(this.teasu.getInventoryASU());
        this.drawTemplateStacks(this.teasu.getInventoryASU());
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonHoverText(0, x + 8, y + 18, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.slot.amount"));

        this.buttonList.add(new GuiButtonHoverText(1, x + 8, y + 30, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.stacklimit"));
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int dim = this.teasu.getWorld().provider.getDimension();
        int amount = 0;

        if (mouseButton == 0 || mouseButton == 11)
        {
            amount = 1;
        }
        else if (mouseButton == 1 || mouseButton == 9)
        {
            amount = -1;
        }

        if (button.id == 0 || button.id == 1)
        {
            if (button.id == 1 && mouseButton == 2)
            {
                amount = GuiScreen.isShiftKeyDown() ? TileEntityASU.MAX_STACK_SIZE : -TileEntityASU.MAX_STACK_SIZE;
            }

            if (button.id == 1)
            {
                if (GuiScreen.isShiftKeyDown()) { amount *= 16; }
                if (GuiScreen.isCtrlKeyDown())  { amount *= 64; }
            }
            else
            {
                if (GuiScreen.isShiftKeyDown()) { amount *= 9; }
                if (GuiScreen.isCtrlKeyDown())  { amount *= 3; }
            }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.teasu.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, button.id, amount));
        }
    }
}
