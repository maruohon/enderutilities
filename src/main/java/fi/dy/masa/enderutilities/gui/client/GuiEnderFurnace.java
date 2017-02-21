package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiContainerLargeStacks implements IButtonStateCallback
{
    private final ContainerEnderFurnace containerEF;
    private final TileEntityEnderFurnace teef;

    public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
    {
        super(container, 176, 166, "gui.container." + te.getTEName());
        this.containerEF = container;
        this.teef = te;
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
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.teef.hasCustomName() ? this.teef.getName() : I18n.format(this.teef.getName());
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, 0x404025);

        if (this.teef.getOwnerName() != null)
        {
            s = I18n.format(this.teef.getOwnerName());
            this.fontRendererObj.drawString(s, this.xSize - this.fontRendererObj.getStringWidth(s) - 6, 20, 0x404025);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // 1: Outputting to Ender Chest, draw the regular arrow instead of the crossed over arrow
        if (this.containerEF.outputToEnderChest)
        {
            this.drawTexturedModalRect(x + 114, y + 34, 176, 78, 24, 16);
        }

        // Draw the burn progress flame
        if (this.teef.isBurningLast)
        {
            int uOffset = (this.teef.fastMode ? 14 : 0);
            int h = this.containerEF.fuelProgress * 13 / 100;

            this.drawTexturedModalRect(x + 34, y + 36 + 12 - h, 176 + uOffset, 12 - h, 14, h + 1);
        }

        // Draw the smelting progress arrow
        if (this.containerEF.smeltingProgress > 0)
        {
            int vOffset = 0;
            int w = this.containerEF.smeltingProgress * 24 / 100;

            if (this.teef.isBurningLast)
            {
                vOffset = (this.teef.fastMode ? 32 : 16);
            }

            this.drawTexturedModalRect(x + 57, y + 34, 176, 14 + vOffset, w, 16);
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Item.getItemFromBlock(Blocks.ENDER_CHEST)), x + 145, y + 34);
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        this.buttonList.add(new GuiButtonStateCallback(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, 16, 0, this.guiTexture, this,
                ButtonState.createTranslate(200, 14, "enderutilities.gui.label.furnace.slow"),
                ButtonState.createTranslate(200, 30, "enderutilities.gui.label.furnace.fast")));

        this.buttonList.add(new GuiButtonStateCallback(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, 16, 0, this.guiTexture, this,
                ButtonState.createTranslate(200, 46, "enderutilities.gui.label.outputtoenderchest.disabled"),
                ButtonState.createTranslate(200, 62, "enderutilities.gui.label.outputtoenderchest.enabled")));
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        if (callbackId == 0)
        {
            return this.teef.fastMode ? 1 : 0;
        }

        return this.containerEF.outputToEnderChest ? 1 : 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException
    {
        super.actionPerformed(btn);

        PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.teef.getWorld().provider.getDimension(), this.teef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, btn.id, 0));
    }
}
