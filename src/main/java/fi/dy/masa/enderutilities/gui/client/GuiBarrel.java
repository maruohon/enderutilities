package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
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
import fi.dy.masa.enderutilities.util.EUStringUtils;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiBarrel extends GuiContainerLargeStacks implements IButtonStateCallback
{
    private final TileEntityBarrel tebarrel;
    private final IItemHandler upgradeInv;

    public GuiBarrel(ContainerBarrel container, TileEntityBarrel te)
    {
        super(container, 176, 175, "gui.container.barrel");

        this.tebarrel = te;
        this.infoArea = new InfoArea(160, 17, 11, 11, "enderutilities.gui.infoarea.barrel", Integer.valueOf(Configs.barrelCapacityUpgradeStacksPer));

        this.upgradeInv = container.getUpgradeInventory();
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
        this.fontRenderer.drawString(I18n.format("enderutilities.container.barrel"), 8, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 80, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        for (int i = 0; i < this.upgradeInv.getSlots(); i++)
        {
            // Draw the upgrade slot backgrounds for empty slots
            if (this.upgradeInv.getStackInSlot(i).isEmpty())
            {
                this.drawTexturedModalRect(x + 44 + i * 18, y + 59, 176, i * 16, 16, 16);
            }
        }

        // Draw the Creative mode button highlight when it's active
        if (this.tebarrel.isCreative())
        {
            this.bindTexture(this.guiTextureWidgets);
            this.drawTexturedModalRect(x + 161, y + 5, 120, 24, 10, 10);
        }

        this.drawLockedSlotBackgrounds(this.tebarrel.getInventoryBarrel());
        this.drawTemplateStacks(this.tebarrel.getInventoryBarrel());
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
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityBarrel.GUI_ACTION_TOGGLE_CREATIVE_MODE, 0));
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

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event)
    {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiBarrel && event.getEntityPlayer() != null &&
            event.getEntityPlayer().openContainer instanceof ContainerBarrel)
        {
            ContainerBarrel container = (ContainerBarrel) event.getEntityPlayer().openContainer;
            GuiBarrel gui = (GuiBarrel) Minecraft.getMinecraft().currentScreen;
            Slot slot = gui.getSlotUnderMouse();

            if (slot != null && slot.getHasStack() && container.getCustomInventorySlotRange().contains(slot.slotNumber))
            {
                String size = EUStringUtils.formatNumberWithKSeparators(slot.getStack().getCount());
                event.getToolTip().add(size + " " + I18n.format("enderutilities.tooltip.item.items"));
            }
        }
    }
}
