package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.Collection;
import com.google.common.collect.Ordering;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemHandyBag.PickupMode;
import fi.dy.masa.enderutilities.item.ItemHandyBag.RestockMode;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiHandyBag extends GuiContainerLargeStacks implements IButtonCallback
{
    public static final int BTN_ID_FIRST_SELECT_MODULE = 0;
    public static final int BTN_ID_FIRST_MOVE_ITEMS    = 4;
    public static final int BTN_ID_FIRST_SORT          = 10;
    public static final int BTN_ID_FIRST_BLOCK         = 14;
    public static final int BTN_ID_FIRST_MODES         = 17;
    public static final int BTN_ID_BAUBLES             = 100;

    private static final String[] BUTTON_STRINGS = new String[] {
            "enderutilities.gui.label.moveallitemsexcepthotbar",
            "enderutilities.gui.label.movematchingitemsexcepthotbar",
            "enderutilities.gui.label.leaveonefilledstack",
            "enderutilities.gui.label.fillstacks",
            "enderutilities.gui.label.movematchingitems",
            "enderutilities.gui.label.moveallitems",
            "enderutilities.gui.label.sortitems",
            "enderutilities.gui.label.blockquickactions",
            "enderutilities.tooltip.item.bag.enabled",
            "enderutilities.tooltip.item.pickupmode",
            "enderutilities.tooltip.item.restockmode"
    };

    private final ContainerHandyBag containerHB;
    private final InventoryItemModular invModular;
    private final int invSize;
    private final int numModuleSlots;
    private final int bagTier;
    private final int offsetXTier;
    private float oldMouseX;
    private float oldMouseY;
    private int firstModuleSlotX;
    private int firstModuleSlotY;
    private boolean hasActivePotionEffects;
    private int[] lastPos = new int[2];
    private boolean baublesLoaded;
    public static final ResourceLocation baublesButton = new ResourceLocation(ModRegistry.MODID_BAUBLES.toLowerCase(), "textures/gui/expanded_inventory.png");

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container, container.getBagTier() == 1 ? 256 : 176, 256, "gui.container.handybag." + container.getBagTier());

        this.containerHB = container;
        this.invModular = container.inventoryItemModular;
        this.invSize = this.invModular.getSlots();
        this.numModuleSlots = this.invModular.getModuleInventory().getSlots();
        this.bagTier = this.containerHB.getBagTier();
        this.offsetXTier = this.bagTier == 1 ? 40 : 0;
        this.baublesLoaded = ModRegistry.isModLoadedBaubles();

        this.scaledStackSizeTextInventories.add(this.invModular);
    }

    private void updatePositions()
    {
        this.firstModuleSlotX  = this.guiLeft + this.containerHB.getSlot(0).xPos + 5 * 18;
        this.firstModuleSlotY  = this.guiTop  + this.containerHB.getSlot(0).yPos - 33;

        this.createButtons();

        this.lastPos[0] = this.guiLeft;
        this.lastPos[1] = this.guiTop;
    }

    private boolean needsPositionUpdate()
    {
        return this.lastPos[0] != this.guiLeft || this.lastPos[1] != this.guiTop;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.updatePositions();

        this.updateActivePotionEffects();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        this.updateActivePotionEffects();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);

        if (this.needsPositionUpdate())
        {
            this.updatePositions();
        }

        if (this.hasActivePotionEffects)
        {
            this.drawActivePotionEffects();
        }

        this.drawTooltips(mouseX, mouseY);
        this.oldMouseX = (float)mouseX;
        this.oldMouseY = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        this.bindTexture(this.guiTextureWidgets);

        // The inventory is not accessible (because there is no valid Memory Card selected)
        if (this.invModular.isAccessibleBy(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.containerHB.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.containerHB.getSelectedSlot() != -1)
        {
            Slot slot = this.containerHB.getSlot(this.containerHB.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 18, 18, 18);
        }

        // Memory Card slots are not accessible, because the opened bag isn't currently available
        // Draw the dark background icon over the disabled slots
        if (this.invModular.getModuleInventory().isAccessibleBy(this.player) == false)
        {
            for (int i = 0; i < this.numModuleSlots; i++)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX - 1 + i * 18, this.firstModuleSlotY - 1, 102, 0, 18, 18);
            }
        }

        // Draw the colored background for the selected module slot
        int index = this.invModular.getSelectedModuleIndex();
        if (index >= 0)
        {
            this.drawTexturedModalRect(this.firstModuleSlotX - 1 + index * 18, this.firstModuleSlotY - 1, 102, 18, 18, 18);
            // Draw the selection border around the selected memory card module's selection button
            this.drawTexturedModalRect(this.firstModuleSlotX + 3 + index * 18, this.firstModuleSlotY + 18, 120, 0, 10, 10);
        }

        // Draw the background icon over empty storage module slots
        for (int i = 0; i < this.numModuleSlots; i++)
        {
            if (this.invModular.getModuleInventory().getStackInSlot(i) == null)
            {
                this.drawTexturedModalRect(this.firstModuleSlotX + i * 18, this.firstModuleSlotY, 240, 80, 16, 16);
            }
        }

        // Draw the hilight border for active Block-Quick-Actions buttons
        ItemStack stack = null;
        int selected = this.invModular.getSelectedModuleIndex();
        if (selected >= 0)
        {
            stack = this.invModular.getModuleInventory().getStackInSlot(selected);
        }

        if (stack != null)
        {
            int x = (this.width - this.xSize) / 2;
            int y = (this.height - this.ySize) / 2;
            long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
            int[] xPos = new int[] { 112 - 1, 21 - 1, 227 - 1 };
            long lockMask = NBTUtils.getLong(stack, "HandyBag", "LockMask");

            for (int i = 0; i < 3; i++)
            {
                if ((lockMask & masks[i]) == masks[i])
                {
                    this.drawTexturedModalRect(x + xPos[i], y + 90, 120, 24, 10, 10);
                }
            }
        }

        int xOff = this.guiLeft + 51 + this.offsetXTier;
        // Draw the player model
        GuiInventory.drawEntityOnScreen(xOff, this.guiTop + 82, 30, xOff - this.oldMouseX, this.guiTop + 25 - this.oldMouseY, this.mc.player);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int xOff = this.offsetXTier;
        this.fontRendererObj.drawString(I18n.format("container.crafting"), xOff + 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards"), xOff + 99, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag"), xOff + 8, 5, 0x404040);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        // Add the Memory Card selection buttons
        int numModules = this.invModular.getModuleInventory().getSlots();
        for (int i = 0; i < numModules; i++)
        {
            this.buttonList.add(new GuiButtonIcon(BTN_ID_FIRST_SELECT_MODULE + i, this.firstModuleSlotX + 4 + i * 18, this.firstModuleSlotY + 19, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        int x = this.guiLeft + this.containerHB.getSlot(0).xPos + 2;
        int y = this.guiTop + this.containerHB.getSlot(0).yPos + 55;

        // Add the quick move items buttons
        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 0, x +   0, y + 0, 12, 12, 24,  0, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.moveallitemsexcepthotbar",
                "enderutilities.gui.label.holdshifttoincludehotbar"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 1, x +  18, y + 0, 12, 12, 24, 12, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.movematchingitemsexcepthotbar",
                "enderutilities.gui.label.holdshifttoincludehotbar"));

        int[] xOff = new int[] { 36, 108, 126, 144 };

        for (int i = 2; i < 6; i++)
        {
            this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + i, x + xOff[i - 2], y,
                12, 12, 24, i * 12, this.guiTextureWidgets, 12, 0, BUTTON_STRINGS[i]));
        }

        y = this.guiTop + this.containerHB.getSlot(0).yPos - 11;

        // Locked mode toggle
        this.buttonList.add(new GuiButtonCallback(BTN_ID_FIRST_MODES + 0, x + 17, y, 8, 8, 0,  0, this.guiTextureWidgets, 8, 0, this, BUTTON_STRINGS[8]));
        // Pickup mode toggle
        this.buttonList.add(new GuiButtonCallback(BTN_ID_FIRST_MODES + 1, x + 41, y, 8, 8, 0, 40, this.guiTextureWidgets, 8, 0, this, BUTTON_STRINGS[9]));
        // Restock mode toggle
        this.buttonList.add(new GuiButtonCallback(BTN_ID_FIRST_MODES + 2, x + 29, y, 8, 8, 0, 40, this.guiTextureWidgets, 8, 0, this, BUTTON_STRINGS[10]));

        if (this.bagTier == 0)
        {
            // Add the sort button
            this.buttonList.add(new GuiButtonHoverText(10, x + 74, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            // Sort player inventory
            this.buttonList.add(new GuiButtonHoverText(13, x + 74, y + 70, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
        }
        else
        {
            // Add the sort buttons
            this.buttonList.add(new GuiButtonHoverText(11, x -  17, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            this.buttonList.add(new GuiButtonHoverText(10, x +  74, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            this.buttonList.add(new GuiButtonHoverText(12, x + 165, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            // Sort player inventory
            this.buttonList.add(new GuiButtonHoverText(13, x +  74, y + 70, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));

            // Add the section locking buttons
            this.buttonList.add(new GuiButtonHoverText(15, x -  29, y +  0, 8, 8, 0, 40, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[7]));
            this.buttonList.add(new GuiButtonHoverText(14, x +  62, y +  0, 8, 8, 0, 40, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[7]));
            this.buttonList.add(new GuiButtonHoverText(16, x + 177, y +  0, 8, 8, 0, 40, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[7]));
        }

        if (this.baublesLoaded)
        {
            // The texture comes from the Baubles expanded inventory texture
            this.buttonList.add(new GuiButtonIcon(BTN_ID_BAUBLES, this.guiLeft + 68 + this.offsetXTier, this.guiTop + 15, 10, 10, 190, 48, baublesButton, 10, 0));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= BTN_ID_FIRST_SELECT_MODULE && button.id < (BTN_ID_FIRST_SELECT_MODULE + this.numModuleSlots))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_SELECT_MODULE, button.id - BTN_ID_FIRST_SELECT_MODULE));
        }
        else if (button.id >= BTN_ID_FIRST_MOVE_ITEMS && button.id <= (BTN_ID_FIRST_MOVE_ITEMS + 5))
        {
            int value = button.id - BTN_ID_FIRST_MOVE_ITEMS;
            if (GuiScreen.isShiftKeyDown())
            {
                value |= 0x8000;
            }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_MOVE_ITEMS, value));
        }
        else if (button.id >= BTN_ID_FIRST_SORT && button.id < (BTN_ID_FIRST_SORT + 4))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_SORT_ITEMS, button.id - BTN_ID_FIRST_SORT));
        }
        else if (button.id >= BTN_ID_FIRST_BLOCK && button.id < (BTN_ID_FIRST_BLOCK + 3))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_BLOCK, button.id - BTN_ID_FIRST_BLOCK));
        }
        else if (button.id >= BTN_ID_FIRST_MODES && button.id < (BTN_ID_FIRST_MODES + 3))
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_MODES, button.id - BTN_ID_FIRST_MODES));
        }
        else if (button.id == 100 && this.baublesLoaded)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                    ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_OPEN_BAUBLES, 0));
        }
    }

    protected void updateActivePotionEffects()
    {
        boolean hasVisibleEffect = false;
        for(PotionEffect potioneffect : this.mc.player.getActivePotionEffects()) {
            Potion potion = potioneffect.getPotion();
            if(potion.shouldRender(potioneffect)) { hasVisibleEffect = true; break; }
        }
        if (this.mc.player.getActivePotionEffects().isEmpty() == false && hasVisibleEffect)
        {
            if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent(this)))
            {
                this.guiLeft = (this.width - this.xSize) / 2;
            }
            else
            {
                this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
            }

            this.hasActivePotionEffects = true;
        }
        else
        {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
    }

    private void drawActivePotionEffects()
    {
        int i = this.guiLeft - 124;
        int j = this.guiTop;

        Collection<PotionEffect> collection = this.mc.player.getActivePotionEffects();

        if (!collection.isEmpty())
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 33;

            if (collection.size() > 5)
            {
                l = 132 / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : Ordering.natural().sortedCopy(collection))
            {
                Potion potion = potioneffect.getPotion();
                if(!potion.shouldRender(potioneffect)) continue;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
                this.drawTexturedModalRect(i, j, 0, 166, 140, 32);

                if (potion.hasStatusIcon())
                {
                    int i1 = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(i + 6, j + 7, 0 + i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }

                potion.renderInventoryEffect(i, j, potioneffect, mc);
                if (!potion.shouldRenderInvText(potioneffect)) { j += l; continue; }
                String s1 = I18n.format(potion.getName(), new Object[0]);

                if (potioneffect.getAmplifier() == 1)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.2", new Object[0]);
                }
                else if (potioneffect.getAmplifier() == 2)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.3", new Object[0]);
                }
                else if (potioneffect.getAmplifier() == 3)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.4", new Object[0]);
                }

                this.fontRendererObj.drawStringWithShadow(s1, (float)(i + 10 + 18), (float)(j + 6), 16777215);
                String s = Potion.getPotionDurationString(potioneffect, 1.0F);
                this.fontRendererObj.drawStringWithShadow(s, (float)(i + 10 + 18), (float)(j + 6 + 10), 8355711);
                j += l;
            }
        }
    }

    @Override
    public int getButtonU(int callbackId, int defaultU)
    {
        return defaultU;
    }

    @Override
    public int getButtonV(int callbackId, int defaultV)
    {
        ItemStack stack = this.containerHB.getContainerItem();
        if (stack == null)
        {
            return defaultV;
        }

        // Locked mode
        if (callbackId == BTN_ID_FIRST_MODES)
        {
            return NBTUtils.getBoolean(stack, "HandyBag", "DisableOpen") ? 48 : 0;
        }
        // Pickup mode
        else if (callbackId == BTN_ID_FIRST_MODES + 1)
        {
            PickupMode mode = ItemHandyBag.PickupMode.fromStack(stack);
            if (mode == PickupMode.ALL) { return 64; }
            else if (mode == PickupMode.MATCHING) { return 56; }
            return 40; // blocked/none
        }
        // Restock mode
        else if (callbackId == BTN_ID_FIRST_MODES + 2)
        {
            RestockMode mode = ItemHandyBag.RestockMode.fromStack(stack);
            if (mode == RestockMode.ALL) { return 72; }
            else if (mode == RestockMode.HOTBAR) { return 80; }
            return 40; // blocked/none
        }

        return defaultV;
    }
}
