package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerMemoryChest;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class GuiMemoryChest extends GuiEnderUtilities implements IButtonStateCallback
{
    public static final int BTN_ID_TOGGLE_LOCK = 1;

    private final TileEntityMemoryChest temc;
    private final int chestTier;

    public GuiMemoryChest(ContainerMemoryChest container, TileEntityMemoryChest te)
    {
        super(container, 176, 176, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0));

        this.infoArea = new InfoArea(160, 5, 11, 11, "enderutilities.gui.infoarea." + te.getTEName());
        this.temc = te;
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
            case 0: this.ySize = 140; break;
            case 1: this.ySize = 176; break;
            case 2: this.ySize = 234; break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int y = 47;

        switch(this.chestTier)
        {
            case 0: y =  47; break;
            case 1: y =  83; break;
            case 2: y = 137; break;
            default:
        }

        String str = this.temc.hasCustomName() ? this.temc.getName() : I18n.format(this.temc.getName());
        this.fontRenderer.drawString(str, 8, 15, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, y, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.drawLockedSlotBackgrounds(this.temc.getInventory());
        this.drawTemplateStacks(this.temc.getInventory());
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        String strPublic = I18n.format("enderutilities.gui.label.public") + " (" +
                I18n.format("enderutilities.tooltip.item.owner") + ": " + this.temc.getOwnerName() + ")";
        String strPrivate = I18n.format("enderutilities.gui.label.private") + " (" +
                I18n.format("enderutilities.tooltip.item.owner") + ": " + this.temc.getOwnerName() + ")";

        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_TOGGLE_LOCK, x + 138, y + 15, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.create(0, 0, strPublic),
                ButtonState.create(0, 48, strPrivate)));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == BTN_ID_TOGGLE_LOCK)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.temc.getWorld().provider.getDimension(), this.temc.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityMemoryChest.GUI_ACTION_TOGGLE_LOCKED, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        // Locked mode
        if (callbackId == BTN_ID_TOGGLE_LOCK)
        {
            return this.temc.isPublic() ? 0 : 1;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
