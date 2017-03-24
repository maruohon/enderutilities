package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerBarrel;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;

public class GuiBarrel extends GuiContainerLargeStacks implements IButtonStateCallback
{
    private final TileEntityBarrel tebarrel;
    private final IItemHandler upgradeInv;

    public GuiBarrel(ContainerBarrel container, TileEntityBarrel te)
    {
        super(container, 176, 175, "gui.container.barrel");

        this.tebarrel = te;
        this.infoArea = new InfoArea(153, 21, 17, 17, "enderutilities.gui.infoarea.barrel", Integer.valueOf(Configs.barrelCapacityUpgradeStacksPer));

        this.upgradeInv = te.getUpgradeInventory();
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
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.barrel"), 8, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 80, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the Creative mode button highlight when it's active
        if (this.tebarrel.isCreative())
        {
            this.drawTexturedModalRect(x + 161, y + 5, 176, 0, 10, 10);
        }

        for (int i = 0; i < 3; i++)
        {
            // Draw the upgrade slot backgrounds for empty slots
            if (this.upgradeInv.getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(x + 62 + i * 18, y + 59, 176, 10 + i * 16, 16, 16);
            }
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonStateCallback(1, x + 162, y + 6, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  40, "enderutilities.gui.label.msu.creative.disabled"),
                ButtonState.createTranslate(0, 112, "enderutilities.gui.label.msu.creative.enabled")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 1 && this.player.capabilities.isCreativeMode)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.tebarrel.getWorld().provider.getDimension(), this.tebarrel.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, 1, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        if (callbackId == 1)
        {
            return this.tebarrel.isCreative() ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
