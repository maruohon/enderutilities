package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.gui.client.base.GuiContainerLargeStacks;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonIcon;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerNullifier;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.item.ItemNullifier;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class GuiNullifier extends GuiContainerLargeStacks implements IButtonStateCallback
{
    private final ContainerNullifier containerN;
    private final InventoryItem inventoryItem;

    public GuiNullifier(ContainerNullifier container)
    {
        super(container, 176, 151, "gui.container.nullifier");

        this.containerN = container;
        this.inventoryItem = container.inventoryItem;
        this.scaledStackSizeTextInventories.add(this.inventoryItem);
        this.infoArea = new InfoArea(153, 5, 17, 17, "enderutilities.gui.infoarea.nullifier");
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
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.nullifier"), 8, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 58, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTextureWidgets);

        // The inventory is not accessible (because the item is not accessible)
        if (this.inventoryItem.isAccessibleBy(this.player) == false)
        {
            // Draw the dark background icon over the disabled inventory slots
            for (int i = 0; i < this.inventoryItem.getSlots(); i++)
            {
                Slot slot = this.containerN.getSlot(i);
                this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 102, 0, 18, 18);
            }
        }

        ItemStack stack = this.containerN.getContainerItem();

        // Draw the selection around the selected slot's button
        if (stack != null)
        {
            int slot =  NBTUtils.getByte(stack, ItemNullifier.TAG_NAME_CONTAINER, ItemNullifier.TAG_NAME_SLOT_SELECTION);
            this.drawTexturedModalRect(this.guiLeft + 11 + slot * 18, this.guiTop + 42, 120, 24, 10, 10);
        }
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        for (int i = 0; i < 5; i++)
        {
            this.buttonList.add(new GuiButtonIcon(i, x + 12 + i * 18, y + 43, 8, 8, 0, 0, this.guiTextureWidgets, 8, 0));
        }

        // Locked mode toggle
        this.buttonList.add(new GuiButtonStateCallback(5, x + 102, y + 32, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0,  0, "enderutilities.gui.label.item.enabled"),
                ButtonState.createTranslate(0, 48, "enderutilities.gui.label.item.disabled")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button.id >= 0 && button.id <= 4)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_NULLIFIER, ItemNullifier.GUI_ACTION_SELECT_SLOT, button.id));
        }
        else if (button.id == 5)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                ReferenceGuiIds.GUI_ID_NULLIFIER, ItemNullifier.GUI_ACTION_TOGGLE_DISABLED, 0));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        if (callbackId == 5)
        {
            ItemStack stack = this.containerN.getContainerItem();
            //System.out.printf("stack: %s - %s\n", stack, stack.getTagCompound());

            return stack != null && NBTUtils.getBoolean(stack, ItemNullifier.TAG_NAME_CONTAINER, ItemNullifier.TAG_NAME_DISABLED) ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
