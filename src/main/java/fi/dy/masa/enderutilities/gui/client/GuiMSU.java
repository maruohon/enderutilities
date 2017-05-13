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
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerMSU;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;
import fi.dy.masa.enderutilities.util.EUStringUtils;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiMSU extends GuiContainerLargeStacks implements IButtonStateCallback
{
    public static final int BTN_ID_TOGGLE_CREATIVE = 1;

    private final TileEntityMSU temsu;
    private final int tier;

    public GuiMSU(ContainerMSU container, TileEntityMSU te)
    {
        super(container, 176, 139, "gui.container.msu");

        this.temsu = te;
        this.tier = te.getStorageTier();
        this.infoArea = new InfoArea(148, 5, 11, 11, "enderutilities.gui.infoarea.msu");
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
        this.fontRenderer.drawString(I18n.format("enderutilities.container.msu." + this.tier), 8, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 46, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the bundle variant's slot background
        if (this.tier == 1)
        {
            this.drawTexturedModalRect(x + 7, y + 22, 7, 56, 9 * 18, 18);
        }

        this.bindTexture(this.guiTextureWidgets);

        // Draw the Creative mode button highlight when it's active
        if (this.temsu.isCreative())
        {
            this.drawTexturedModalRect(x + 161, y + 5, 120, 24, 10, 10);
        }

        this.drawLockedSlotBackgrounds(this.temsu.getInventoryMSU());
        this.drawTemplateStacks(this.temsu.getInventoryMSU());

        /* The selected slot index isn't currently synced to the client... And it's probably too minor a feature to bother with anyway.
        int selectedSlot = ((ContainerMSU) this.container).getSelectedSlot();

        if (selectedSlot != -1)
        {
            Slot slot = this.container.getSlot(selectedSlot);
            this.bindTexture(this.guiTextureWidgets);
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 18, 18, 18);
        }
        */
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_TOGGLE_CREATIVE, x + 162, y + 6, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  40, "enderutilities.gui.label.msu.creative.disabled"),
                ButtonState.createTranslate(0, 112, "enderutilities.gui.label.msu.creative.enabled")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == BTN_ID_TOGGLE_CREATIVE)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.temsu.getWorld().provider.getDimension(), this.temsu.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, TileEntityMSU.GUI_ACTION_TOGGLE_CREATIVE, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        if (callbackId == BTN_ID_TOGGLE_CREATIVE)
        {
            return this.temsu.isCreative() ? 1 : 0;
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
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMSU && event.getEntityPlayer() != null &&
            event.getEntityPlayer().openContainer instanceof ContainerMSU)
        {
            ContainerMSU container = (ContainerMSU) event.getEntityPlayer().openContainer;
            GuiMSU gui = (GuiMSU) Minecraft.getMinecraft().currentScreen;
            Slot slot = gui.getSlotUnderMouse();

            if (slot != null && slot.getHasStack() && container.getCustomInventorySlotRange().contains(slot.slotNumber))
            {
                String size = EUStringUtils.formatNumberWithKSeparators(slot.getStack().stackSize);
                event.getToolTip().add(size + " " + I18n.format("enderutilities.tooltip.item.items"));
            }
        }
    }
}
