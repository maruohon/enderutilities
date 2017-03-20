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
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemHandyBag.PickupMode;
import fi.dy.masa.enderutilities.item.ItemHandyBag.RestockMode;
import fi.dy.masa.enderutilities.item.ItemHandyBag.ShiftMode;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiHandyBag extends GuiContainerLargeStacks implements IButtonStateCallback
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
            "enderutilities.gui.label.sortitems"
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
    private final boolean baublesLoaded;
    public static final ResourceLocation RESOURCES_BAUBLES_BUTTON
            = new ResourceLocation(ModRegistry.MODID_BAUBLES.toLowerCase(), "textures/gui/expanded_inventory.png");

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

        if (this.containerHB.getBagTier() == 1)
        {
            int x = (this.width - this.xSize) / 2;
            int y = (this.height - this.ySize) / 2;
            int[] xPos = new int[] { 115 - 1, 21 - 1, 227 - 1 };

            for (int i = 0; i < 3; i++)
            {
                if (this.isMaskActiveForSection(i, "LockMask"))
                {
                    // Draw the hilight border for active Block-Quick-Actions buttons
                    this.drawTexturedModalRect(x + xPos[i], y + 90, 120, 24, 10, 10);
                }
            }
        }

        // Draw the shift-click double-tap mode's effective mode indication, if applicable
        ItemStack modularStack = this.containerHB.inventoryItemModular.getModularItemStack();

        if (modularStack != null && ShiftMode.getEffectiveMode(modularStack) == ShiftMode.TO_BAG)
        {
            int x = this.guiLeft + this.offsetXTier + 64;
            this.drawTexturedModalRect(x, this.guiTop + 157, 154, 0, 12, 12);
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
        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_FIRST_MODES + 0, x - 1, y, 8, 8, 8, 0,
                this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 48, "enderutilities.gui.label.bag.disabled"),
                ButtonState.createTranslate(0,  0, "enderutilities.gui.label.bag.enabled")));
        // Pickup mode toggle
        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_FIRST_MODES + 1, x + 23, y, 8, 8, 8, 0,
                this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 40, "enderutilities.gui.label.pickupmode.disabled"),
                ButtonState.createTranslate(0, 56, "enderutilities.gui.label.pickupmode.matching"),
                ButtonState.createTranslate(0, 64, "enderutilities.gui.label.pickupmode.all")));
        // Restock mode toggle
        this.buttonList.add(new GuiButtonStateCallback(BTN_ID_FIRST_MODES + 2, x + 11, y, 8, 8, 8, 0,
                this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 40, "enderutilities.gui.label.restockmode.disabled"),
                ButtonState.createTranslate(0, 80, "enderutilities.gui.label.restockmode.hotbar"),
                ButtonState.createTranslate(0, 72, "enderutilities.gui.label.restockmode.all")));
        this.buttonList.add(new GuiButtonStateCallback(23, x + 35, y + 0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  16, ShiftMode.TO_BAG.getUnlocName()),
                ButtonState.createTranslate(0,  96, ShiftMode.INV_HOTBAR.getUnlocName()),
                ButtonState.createTranslate(0, 104, ShiftMode.DOUBLE_TAP.getUnlocName())));

        if (this.bagTier == 0)
        {
            // Add the sort button
            this.buttonList.add(new GuiButtonHoverText(10, x + 74, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            // Sort player inventory
            this.buttonList.add(new GuiButtonHoverText(13, x + 74, y + 70, 8, 8, 0, 24,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems.player"));
            this.buttonList.add(new GuiButtonStateCallback(20, x + 62, y + 0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.updateitems.disabled"),
                    ButtonState.createTranslate(0, 88, "enderutilities.gui.label.updateitems.enabled")));
        }
        else
        {
            // Add the sort buttons
            this.buttonList.add(new GuiButtonHoverText(11, x -  17, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            this.buttonList.add(new GuiButtonHoverText(10, x +  53, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            this.buttonList.add(new GuiButtonHoverText(12, x + 165, y +  0, 8, 8, 0, 24, this.guiTextureWidgets, 8, 0, BUTTON_STRINGS[6]));
            // Sort player inventory
            this.buttonList.add(new GuiButtonHoverText(13, x +  74, y + 70, 8, 8, 0, 24,
                    this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sortitems.player"));

            // Add the section locking buttons
            this.buttonList.add(new GuiButtonStateCallback(15, x -  29, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0,  0, "enderutilities.gui.label.blockquickactions.disabled"),
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.blockquickactions.enabled")));
            this.buttonList.add(new GuiButtonStateCallback(14, x +  65, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0,  0, "enderutilities.gui.label.blockquickactions.disabled"),
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.blockquickactions.enabled")));
            this.buttonList.add(new GuiButtonStateCallback(16, x + 177, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0,  0, "enderutilities.gui.label.blockquickactions.disabled"),
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.blockquickactions.enabled")));

            // Add the update-items-in-section buttons
            this.buttonList.add(new GuiButtonStateCallback(21, x -  41, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.updateitems.disabled"),
                    ButtonState.createTranslate(0, 88, "enderutilities.gui.label.updateitems.enabled")));
            this.buttonList.add(new GuiButtonStateCallback(20, x +  77, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.updateitems.disabled"),
                    ButtonState.createTranslate(0, 88, "enderutilities.gui.label.updateitems.enabled")));
            this.buttonList.add(new GuiButtonStateCallback(22, x + 189, y +  0, 8, 8, 8, 0, this.guiTextureWidgets, this,
                    ButtonState.createTranslate(0, 40, "enderutilities.gui.label.updateitems.disabled"),
                    ButtonState.createTranslate(0, 88, "enderutilities.gui.label.updateitems.enabled")));
        }

        if (this.baublesLoaded)
        {
            // The texture comes from the Baubles expanded inventory texture
            this.buttonList.add(new GuiButtonHoverText(BTN_ID_BAUBLES, this.guiLeft + 68 + this.offsetXTier, this.guiTop + 15,
                    10, 10, 190, 48, RESOURCES_BAUBLES_BUTTON, 10, 0, "Baubles"));
        }
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
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
            int data = button.id - BTN_ID_FIRST_MODES;

            if (mouseButton == 1)
            {
                data |= 0x8000;
            }

            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_MODES, data));
        }
        else if (button.id >= 20 && button.id <= 22)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_UPDATE, button.id - 20));
        }
        else if (button.id == 23)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_SHIFTCLICK, mouseButton));
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
                String s1 = I18n.format(potion.getName());
                int amp = potioneffect.getAmplifier();

                if (amp >= 1 && amp <= 3)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level." + (amp + 1));
                }

                this.fontRendererObj.drawStringWithShadow(s1, (float)(i + 10 + 18), (float)(j + 6), 16777215);
                String s = Potion.getPotionDurationString(potioneffect, 1.0F);
                this.fontRendererObj.drawStringWithShadow(s, (float)(i + 10 + 18), (float)(j + 6 + 10), 8355711);
                j += l;
            }
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        ItemStack stack = this.containerHB.getContainerItem();

        if (stack != null)
        {
            // Locked mode
            if (callbackId == BTN_ID_FIRST_MODES)
            {
                return NBTUtils.getBoolean(stack, "HandyBag", "DisableOpen") ? 0 : 1;
            }
            // Pickup mode
            else if (callbackId == BTN_ID_FIRST_MODES + 1)
            {
                PickupMode mode = ItemHandyBag.PickupMode.fromStack(stack);
                if (mode == PickupMode.ALL) { return 2; }
                else if (mode == PickupMode.MATCHING) { return 1; }
                return 0; // blocked/none
            }
            // Restock mode
            else if (callbackId == BTN_ID_FIRST_MODES + 2)
            {
                RestockMode mode = ItemHandyBag.RestockMode.fromStack(stack);
                if (mode == RestockMode.ALL) { return 2; }
                else if (mode == RestockMode.HOTBAR) { return 1; }
                return 0; // blocked/none
            }
            // Block quick actions
            else if (callbackId >= 14 && callbackId <= 16)
            {
                return this.isMaskActiveForSection(callbackId - 14, "LockMask") ? 1 : 0;
            }
            // Update items
            else if (callbackId >= 20 && callbackId <= 22)
            {
                return this.isMaskActiveForSection(callbackId - 20, "UpdateMask") ? 1 : 0;
            }
            // Shift-click behaviour
            else if (callbackId == 23)
            {
                return MathHelper.clamp(NBTUtils.getByte(stack, "HandyBag", "ShiftMode") & 0x3, 0, 2);
            }
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }

    private boolean isMaskActiveForSection(int section, String tagName)
    {
        ItemStack stack = null;
        int selected = this.invModular.getSelectedModuleIndex();

        if (selected >= 0 && section >= 0 && section <= 2)
        {
            stack = this.invModular.getModuleInventory().getStackInSlot(selected);

            if (stack != null)
            {
                long[] masks = new long[] { 0x1FFFFFFL, 0x1FFF8000000L, 0x7FFE0000000000L };
                long lockMask = NBTUtils.getLong(stack, "HandyBag", tagName);

                if ((lockMask & masks[section]) == masks[section])
                {
                    return true;
                }
            }
        }

        return false;
    }
}
