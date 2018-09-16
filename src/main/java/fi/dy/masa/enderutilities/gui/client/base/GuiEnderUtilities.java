package fi.dy.masa.enderutilities.gui.client.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar.ScrollbarAction;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerCustomSlotClick;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.util.InventoryUtils;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiEnderUtilities extends GuiContainer
{
    protected static final int GUI_ACTION_SLOT_MIDDLE_CLICK = 1;
    protected final ContainerEnderUtilities container;
    protected final EntityPlayer player;
    protected final ResourceLocation guiTextureWidgets;
    protected ResourceLocation guiTexture;
    protected int backgroundU;
    protected int backgroundV;
    protected InfoArea infoArea;

    public GuiEnderUtilities(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container);
        this.container = container;
        this.player = container.player;
        this.xSize = xSize;
        this.ySize = ySize;
        this.guiTexture = ReferenceTextures.getGuiTexture(textureName);
        this.guiTextureWidgets = ReferenceTextures.getGuiTexture("gui.widgets");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMouseInputEventPre(MouseInputEvent.Pre event)
    {
        // Handle the mouse input inside all of the mod's GUIs via the event and then cancel the event,
        // so that some mods like Inventory Sorter don't try to sort the Ender Utilities inventories.
        // Using priority LOW should still allow even older versions of Item Scroller to work,
        // since it uses normal priority.
        if (event.getGui() instanceof GuiEnderUtilities)
        {
            try
            {
                event.getGui().handleMouseInput();
                event.setCanceled(true);
            }
            catch (IOException e)
            {
                EnderUtilities.logger.warn("Exception while executing handleMouseInput() on {}", event.getGui().getClass().getName());
            }
        }
    }

    @SubscribeEvent
    public static void onPotionShiftEvent(GuiScreenEvent.PotionShiftEvent event)
    {
        // Disable the potion shift in all my GUIs
        if (event.getGui() instanceof GuiEnderUtilities)
        {
            event.setCanceled(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, this.backgroundU, this.backgroundV, this.xSize, this.ySize);

        if (this.infoArea != null)
        {
            this.infoArea.render(this, this.guiTextureWidgets);
        }

        this.bindTexture(this.guiTexture);
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
        for (int i = 0; i < this.buttonList.size(); i++)
        {
            GuiButton button = this.buttonList.get(i);

            // Mouse is over the button
            if ((button instanceof GuiButtonHoverText) && button.mousePressed(this.mc, mouseX, mouseY))
            {
                this.drawHoveringText(((GuiButtonHoverText) button).getHoverStrings(), mouseX, mouseY, this.fontRenderer);
            }
        }

        // Draw the colored background for the selected slot (for swapping), if any
        if (this.container instanceof ContainerCustomSlotClick)
        {
            int selectedSlot = ((ContainerCustomSlotClick) this.container).getSelectedSlot();

            if (selectedSlot != -1)
            {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                this.bindTexture(this.guiTextureWidgets);
                Slot slot = this.container.getSlot(selectedSlot);
                this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 90, 18, 18);
            }
        }

        // Info text has been set, show it if the mouse is over the designated info area
        if (this.infoArea != null && this.infoArea.isMouseOver(mouseX, mouseY, this.guiLeft, this.guiTop))
        {
            this.drawHoveringText(this.infoArea.getInfoLines(), mouseX, mouseY, this.fontRenderer);
        }

        // Added in vanilla 1.12
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int l = 0; l < this.buttonList.size(); ++l)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(l);

            if (guibutton.mousePressed(this.mc, mouseX, mouseY))
            {
                // Vanilla GUI only plays the click sound for the left click, we do it for other buttons here
                if (mouseButton != 0)
                {
                    guibutton.playPressSound(this.mc.getSoundHandler());
                }

                this.actionPerformedWithButton(guibutton, mouseButton);
            }
        }
    }

    // This method is overridden simply to remove the getHasStack() check from the hovered slot,
    // as we also need to get the pick block events for empty slots in some cases.
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
        {
            this.mc.player.closeScreen();
        }

        this.checkHotbarKeys(keyCode);

        Slot slot = this.getSlotUnderMouse();

        if (slot != null)
        {
            if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode))
            {
                this.handleMouseClick(slot, slot.slotNumber, 0, ClickType.CLONE);
            }
            else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode))
            {
                this.handleMouseClick(slot, slot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int dWheel = Mouse.getEventDWheel();

        if (dWheel != 0)
        {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            for (int i = 0; i < this.buttonList.size(); i++)
            {
                GuiButton button = this.buttonList.get(i);

                if (button.mousePressed(this.mc, mouseX, mouseY))
                {
                    this.actionPerformedWithButton(button, 10 + dWheel / 120);
                    break;
                }
            }
        }
        else
        {
            super.handleMouseInput();
        }
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        // Custom: middle click on a slot, with a modifier key active
        if (type == ClickType.CLONE && (isShiftKeyDown() || isCtrlKeyDown() || isAltKeyDown()))
        {
            if (slotIn != null)
            {
                slotId = slotIn.slotNumber;
            }

            int action = HotKeys.KEYCODE_MIDDLE_CLICK | HotKeys.getActiveModifierMask();

            // Send a packet to the server
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, BlockPos.ORIGIN,
                    ReferenceGuiIds.GUI_ID_CONTAINER_GENERIC, action, slotId));
        }
        else
        {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }

    /**
     * Called when a mouse action is performed. Wheel actions have a value (dWheel / 120) + 10.
     * @param guiButton
     * @param mouseButton
     * @throws IOException
     */
    protected void actionPerformedWithButton(GuiButton guiButton, int mouseButton) throws IOException
    {
    }

    public void scrollbarAction(int scrollbarId, ScrollbarAction action, int position)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    /**
     * Draw the background colors/icons for locked slots from a "lockable inventory"
     * @param inv
     */
    protected void drawLockedSlotBackgrounds(ItemStackHandlerLockable inv)
    {
        this.bindTexture(this.guiTextureWidgets);

        int invSize = inv.getSlots();

        // Draw the colored background icon for locked/"templated" slots
        for (int slotNum = 0; slotNum < invSize; slotNum++)
        {
            Slot slot = this.inventorySlots.getSlot(slotNum);

            if (inv.isSlotLocked(slotNum))
            {
                int x = this.guiLeft + slot.xPos;
                int y = this.guiTop + slot.yPos;
                int v = 18;
                ItemStack stackSlot = inv.getStackInSlot(slotNum);

                // Empty locked slots are in a different color
                if (stackSlot.isEmpty())
                {
                    v = 36;
                }
                // Non-matching item in a locked slot
                else if (InventoryUtils.areItemStacksEqual(stackSlot, inv.getTemplateStackInSlot(slotNum)) == false)
                {
                    v = 72;
                }

                this.drawTexturedModalRect(x - 1, y - 1, 102, v, 18, 18);
            }
        }
    }

    /**
     * Draw the template stacks from a "lockable inventory" for otherwise empty slots
     * @param inv
     */
    protected void drawTemplateStacks(ItemStackHandlerLockable inv)
    {
        // Draw a faint version of the template item for empty locked slots
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        int invSize = inv.getSlots();

        for (int slotNum = 0; slotNum < invSize; slotNum++)
        {
            Slot slot = this.inventorySlots.getSlot(slotNum);

            if (inv.isSlotLocked(slotNum) && inv.getStackInSlot(slotNum).isEmpty())
            {
                ItemStack stack = inv.getTemplateStackInSlot(slotNum);

                if (stack.isEmpty() == false)
                {
                    int x = this.guiLeft + slot.xPos;
                    int y = this.guiTop + slot.yPos;
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.enableBlend();
                    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
                }
            }
        }

        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    public static class InfoArea
    {
        private final int posX;
        private final int posY;
        private final int width;
        private final int height;
        private final String infoText;
        private final Object[] args;
        private int u;
        private int v;

        public InfoArea(int x, int y, int width, int height, String infoTextKey, Object... args)
        {
            this.posX = x;
            this.posY = y;
            this.width = width;
            this.height = height;
            this.infoText = infoTextKey;
            this.args = args;

            // Default texture locations on the widgets sheet
            this.u = 134;

            if (width == 11)
            {
                this.v = 66;
            }
            else if (width == 18)
            {
                this.v = 48;
            }
        }

        public void setUV(int u, int v)
        {
            this.u = u;
            this.v = v;
        }

        public List<String> getInfoLines()
        {
            List<String> lines = new ArrayList<String>();
            ItemEnderUtilities.addTranslatedTooltip(this.infoText, lines, false, this.args);
            return lines;
        }

        public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop)
        {
            return mouseX >= guiLeft + this.posX && mouseX < guiLeft + this.posX + this.width &&
                   mouseY >= guiTop + this.posY && mouseY < guiTop + this.posY + this.height;
        }

        public void render(GuiEnderUtilities gui, ResourceLocation texture)
        {
            gui.bindTexture(texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            gui.drawTexturedModalRect(gui.guiLeft + this.posX, gui.guiTop + this.posY, this.u, this.v, this.width, this.height);
        }
    }
}
