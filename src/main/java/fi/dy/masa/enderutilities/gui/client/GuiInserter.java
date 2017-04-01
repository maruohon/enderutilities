package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityInserter;

public class GuiInserter extends GuiEnderUtilities implements IButtonStateCallback
{
    private final TileEntityInserter tef;

    public GuiInserter(ContainerEnderUtilities container, TileEntityInserter te)
    {
        super(container, 176, te.isFiltered() ? 197 : 141, "gui.container.inserter_" + (te.isFiltered() ? "filtered" : "normal"));

        this.tef = te;

        if (te.isFiltered())
        {
            this.infoArea = new InfoArea(150, 19, 17, 17, "enderutilities.gui.infoarea.inserter_filtered");
        }
        else
        {
            this.infoArea = new InfoArea(153, 5, 17, 17, "enderutilities.gui.infoarea.inserter_normal");
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();

        if (this.tef.isFiltered())
        {
            this.createButtons();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        if (this.tef.isFiltered())
        {
            this.fontRendererObj.drawString(I18n.format("enderutilities.container.inserter_filtered"), 8, 6, 0x404040);
            this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 105, 0x404040);
        }
        else
        {
            this.fontRendererObj.drawString(I18n.format("enderutilities.container.inserter_normal"), 8, 6, 0x404040);
            this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 49, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        /*this.buttonList.add(new GuiButtonStateCallback(BTN_ID_TOGGLE_CREATIVE, x + 162, y + 6, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  40, "enderutilities.gui.label.msu.creative.disabled"),
                ButtonState.createTranslate(0, 112, "enderutilities.gui.label.msu.creative.enabled")));*/
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        /*if (button.id == BTN_ID_TOGGLE_CREATIVE)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.temsu.getWorld().provider.getDimension(), this.temsu.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityMSU.GUI_ACTION_TOGGLE_CREATIVE, 0));
        }*/
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
