package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.Collection;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;

public class GuiHandyBag extends GuiContainerLargeStacks
{
    public static final int BTN_ID_FIRST_SELECT_MODULE = 0;
    public static final int BTN_ID_FIRST_MOVE_ITEMS    = 4;

    protected final EntityPlayer player;
    protected final ContainerHandyBag container;
    protected final InventoryItemModular invModular;
    protected final int invSize;
    protected final int numModuleSlots;
    protected final int bagTier;

    protected float oldMouseX;
    protected float oldMouseY;
    protected int firstModuleSlotX;
    protected int firstModuleSlotY;
    protected int firstArmorSlotX;
    protected int firstArmorSlotY;
    private boolean hasActivePotionEffects;

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container, 256, 256, "gui.container.handybag." + container.getBagTier());

        this.player = container.player;
        this.container = container;
        this.invModular = container.inventoryItemModular;
        this.invSize = this.invModular.getSlots();
        this.numModuleSlots = this.invModular.getModuleInventory().getSlots();
        this.bagTier = this.container.getBagTier();

        this.scaledStackSizeTextTargetInventories.add(this.invModular);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.firstModuleSlotX  = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 5 * 18;
        this.firstModuleSlotY  = this.guiTop  + this.container.getSlot(0).yDisplayPosition - 33;
        this.firstArmorSlotX   = this.guiLeft + this.container.getSlot(this.invSize + this.numModuleSlots + 36).xDisplayPosition;
        this.firstArmorSlotY   = this.guiTop  + this.container.getSlot(this.invSize + this.numModuleSlots + 36).yDisplayPosition;

        this.createButtons();
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

        if (this.hasActivePotionEffects == true)
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
        if (this.invModular.isUseableByPlayer(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.invSize; i++)
            {
                Slot slot = this.container.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 0, 18, 18);
            }
        }
        // Draw the colored background for the selected slot (for swapping), if any
        else if (this.container.getSelectedSlot() != -1)
        {
            Slot slot = this.container.getSlot(this.container.getSelectedSlot());
            this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition - 1, this.guiTop + slot.yDisplayPosition - 1, 102, 18, 18, 18);
        }

        // Memory Card slots are not accessible, because the opened bag isn't currently available
        // Draw the dark background icon over the disabled slots
        if (this.invModular.getModuleInventory().isUseableByPlayer(this.player) == false)
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

        int xOff = this.guiLeft + (this.bagTier == 1 ? 91 : 51);
        // Draw the player model
        GuiInventory.drawEntityOnScreen(xOff, this.guiTop + 82, 30, xOff - this.oldMouseX, this.guiTop + 25 - this.oldMouseY, this.mc.thePlayer);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int xOff = this.bagTier == 1 ? 40 : 0;
        this.fontRendererObj.drawString(I18n.format("container.crafting"), xOff + 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.gui.label.memorycards"), xOff + 97, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag"), xOff + 8, 90, 0x404040);
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

        int x = this.guiLeft + this.container.getSlot(0).xDisplayPosition + 2;
        int y = this.guiTop + this.container.getSlot(0).yDisplayPosition + 55;

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 0, x +   0, y + 0, 12, 12, 24,  0, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.moveallitems"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 1, x +  18, y + 0, 12, 12, 24, 12, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.quickstack",
                 "(" + I18n.format("enderutilities.gui.label.movematchingitems") + ")"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 2, x +  36, y + 0, 12, 12, 24, 24, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.leaveonefilledstack"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 3, x + 108, y + 0, 12, 12, 24, 36, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.fillstacks"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 4, x + 126, y + 0, 12, 12, 24, 48, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.restock",
                 "(" + I18n.format("enderutilities.gui.label.movematchingitems") + ")"));

        this.buttonList.add(new GuiButtonHoverText(BTN_ID_FIRST_MOVE_ITEMS + 5, x + 144, y + 0, 12, 12, 24, 60, this.guiTextureWidgets, 12, 0,
                "enderutilities.gui.label.moveallitems"));
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
        for (int i = 0; i < this.buttonList.size(); i++)
        {
            GuiButton button = (GuiButton)this.buttonList.get(i);

            // Mouse is over the button
            if ((button instanceof GuiButtonHoverText) && button.mousePressed(this.mc, mouseX, mouseY) == true)
            {
                this.drawHoveringText(((GuiButtonHoverText)button).getHoverStrings(), mouseX, mouseY, this.fontRendererObj);
            }
        }
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
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
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_MOVE_ITEMS, button.id - BTN_ID_FIRST_MOVE_ITEMS));
        }
    }

    protected void updateActivePotionEffects()
    {
        boolean hasVisibleEffect = false;
        for(PotionEffect potioneffect : this.mc.thePlayer.getActivePotionEffects()) {
            Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
            if(potion.shouldRender(potioneffect)) { hasVisibleEffect = true; break; }
        }
        if (!this.mc.thePlayer.getActivePotionEffects().isEmpty() && hasVisibleEffect)
        {
            this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
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

        Collection<PotionEffect> collection = this.mc.thePlayer.getActivePotionEffects();

        if (!collection.isEmpty())
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 33;

            if (collection.size() > 5)
            {
                l = 132 / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : this.mc.thePlayer.getActivePotionEffects())
            {
                Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                if(!potion.shouldRender(potioneffect)) continue;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(inventoryBackground);
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
                String s = Potion.getDurationString(potioneffect);
                this.fontRendererObj.drawStringWithShadow(s, (float)(i + 10 + 18), (float)(j + 6 + 10), 8355711);
                j += l;
            }
        }
    }
}
