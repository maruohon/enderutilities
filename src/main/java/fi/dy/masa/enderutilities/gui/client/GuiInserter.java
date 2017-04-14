package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
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
            this.infoArea = new InfoArea(151, 7, 17, 17, "enderutilities.gui.infoarea.inserter_filtered");
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

        this.createButtons();
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

        String str = String.valueOf(this.tef.getBaseItemHandler().getInventoryStackLimit());
        this.fontRendererObj.drawString(str, 106 - this.fontRendererObj.getStringWidth(str) / 2, 24, 0x404040);
        str = String.valueOf(this.tef.getUpdateDelay());
        this.fontRendererObj.drawString(str, 133 - this.fontRendererObj.getStringWidth(str) / 2, 24, 0x404040);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonHoverText(0, x + 102, y + 35, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.stacklimit"));
        this.buttonList.add(new GuiButtonHoverText(1, x + 129, y + 35, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.delay.inserter"));

        this.buttonList.add(new GuiButtonStateCallback(2, x + 153, y + 29, 14, 14, 14, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(60, 224, "enderutilities.gui.label.redstone.ignored"),
                ButtonState.createTranslate(60, 196, "enderutilities.gui.label.redstone.low"),
                ButtonState.createTranslate(60, 210, "enderutilities.gui.label.redstone.high")));

        if (this.tef.isFiltered())
        {
            this.buttonList.add(new GuiButtonStateCallback(10, x +  9, y + 30, 14, 14, 14, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(60, 70, "enderutilities.gui.label.blacklist"),
                    ButtonState.createTranslate(60, 84, "enderutilities.gui.label.whitelist")));

            this.buttonList.add(new GuiButtonStateCallback(11, x + 27, y + 30, 14, 14, 14, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(60, 126, "enderutilities.gui.label.meta.ignore"),
                    ButtonState.createTranslate(60, 112, "enderutilities.gui.label.meta.match")));

            this.buttonList.add(new GuiButtonStateCallback(12, x + 45, y + 30, 14, 14, 14, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(60, 154, "enderutilities.gui.label.nbt.ignore"),
                    ButtonState.createTranslate(60, 140, "enderutilities.gui.label.nbt.match")));
        }
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int dim = this.tef.getWorld().provider.getDimension();
        int amount = 0;

        if (mouseButton == 0 || mouseButton == 11)
        {
            amount = 1;
        }
        else if (mouseButton == 1 || mouseButton == 9)
        {
            amount = -1;
        }

        if (button.id == 0)
        {
            if (GuiScreen.isShiftKeyDown()) { amount *= 16; }
            else if (GuiScreen.isCtrlKeyDown()) { amount *= 64; }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityInserter.GuiAction.CHANGE_STACK_LIMIT.ordinal(), amount));
        }
        else if (button.id == 1)
        {
            if (mouseButton == 2)
            {
                amount = GuiScreen.isShiftKeyDown() ? 72000 : -72000;
            }
            else
            {
                if (GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown()) { amount *= 1000; }
                else if (GuiScreen.isShiftKeyDown()) { amount *= 10; }
                else if (GuiScreen.isCtrlKeyDown()) { amount *= 100; }
            }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityInserter.GuiAction.CHANGE_DELAY.ordinal(), amount));
        }
        if (button.id == 2)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityInserter.GuiAction.CHANGE_REDSTONE_MODE.ordinal(), amount));
        }
        else if (button.id >= 10 && button.id <= 12)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityInserter.GuiAction.CHANGE_FILTERS.ordinal(), 1 << (button.id - 10)));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        if (callbackId == 2)
        {
            return this.tef.getRedstoneModeOrdinal();
        }
        else if (callbackId == 10)
        {
            return this.tef.isFilterSettingEnabled(TileEntityInserter.FilterSetting.IS_WHITELIST) ? 1 : 0;
        }
        else if (callbackId == 11)
        {
            return this.tef.isFilterSettingEnabled(TileEntityInserter.FilterSetting.MATCH_META) ? 1 : 0;
        }
        else if (callbackId == 12)
        {
            return this.tef.isFilterSettingEnabled(TileEntityInserter.FilterSetting.MATCH_NBT) ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
