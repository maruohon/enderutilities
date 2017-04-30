package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import org.lwjgl.input.Mouse;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.gui.client.base.GuiArea;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar.ScrollbarAction;
import fi.dy.masa.enderutilities.inventory.container.ContainerJSU;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityJSU;

public class GuiJSU extends GuiContainerLargeStacks
{
    private final ContainerJSU containerJSU;
    private final ScrollBar scrollBar;
    private final GuiArea areaInventory;

    public GuiJSU(ContainerJSU container, TileEntityJSU te)
    {
        super(container, 192, 220, "gui.container.jsu");

        this.containerJSU = container;
        this.areaInventory = new GuiArea(7, 16, 177, 108);
        this.scrollBar = new ScrollBar(0, 172, 17, 192, 0, 12, 106, ((TileEntityJSU.INV_SIZE / 9) - 5), this);
        this.scaledStackSizeTextInventories.add(container.inventory);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.jsu"), 8, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 129, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the scroll bar
        this.scrollBar.render(x, y, mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - this.guiLeft;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.guiTop;

        if (Mouse.getEventDWheel() != 0 && this.areaInventory.isMouseOver(mouseX, mouseY))
        {
            this.scrollBar.handleMouseInput(mouseX, mouseY);
        }
        else if ((Mouse.getEventButton() != 0 && Mouse.isButtonDown(0) == false) ||
                this.scrollBar.handleMouseInput(mouseX, mouseY) == false)
        {
            super.handleMouseInput();
        }
    }

    @Override
    public void scrollbarAction(int scrollbarId, ScrollbarAction action, int position)
    {
        int row = ((TileEntityJSU.INV_SIZE / 9) - 6) * this.scrollBar.getPosition() / this.scrollBar.getMaxPosition();

        // Change the scroll position locally
        this.containerJSU.performGuiAction(this.player, ContainerJSU.GUI_ACTION_SCROLL_SET, row);

        // Send a packet to the server
        PacketHandler.INSTANCE.sendToServer(
            new MessageGuiAction(0, BlockPos.ORIGIN,
                ReferenceGuiIds.GUI_ID_CONTAINER_GENERIC, ContainerJSU.GUI_ACTION_SCROLL_SET, row));
    }
}
