package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import fi.dy.masa.enderutilities.inventory.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiEnderUtilitiesInventory
{
    private ContainerEnderFurnace container;
    private GuiButtonIcon buttonMode;
    private GuiButtonIcon buttonOutput;
    private TileEntityEnderFurnace teef;

    public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
    {
        super(container, te);
        this.container = container;
        this.teef = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomName() ? this.te.getName() : I18n.format(this.te.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 4, 0x404025);

        s = I18n.format("enderutilities.gui.label.outputbuffer", new Object[0]) + ": " + this.container.outputBufferAmount;
        this.fontRendererObj.drawString(s, 60, 58, 0x404025);

        if (this.teef.getOwnerName() != null)
        {
            s = I18n.format(this.teef.getOwnerName(), new Object[0]);
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
        if (this.container.outputToEnderChest == true)
        {
            this.drawTexturedModalRect(x + 114, y + 34, 176, 78, 24, 16);
        }

        // Draw the burn progress flame
        if (this.teef.isBurningLast == true)
        {
            int uOffset = (this.teef.fastMode == true ? 14 : 0);
            int h = this.container.fuelProgress * 13 / 100;

            this.drawTexturedModalRect(x + 34, y + 36 + 12 - h, 176 + uOffset, 12 - h, 14, h + 1);
        }

        // Draw the smelting progress arrow
        if (this.container.smeltingProgress > 0)
        {
            int vOffset = 0;
            int w = this.container.smeltingProgress * 24 / 100;

            if (this.teef.isBurningLast == true)
            {
                vOffset = (this.teef.fastMode == true ? 32 : 16);
            }

            this.drawTexturedModalRect(x + 57, y + 34, 176, 14 + vOffset, w, 16);
        }

        itemRender.renderItemAndEffectIntoGUI(new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
    }

    protected void createButtons()
    {
        int modeOffset = (this.teef.fastMode == true ? 16 : 0);
        int outputOffset = (this.container.outputToEnderChest == true ? 16 : 0);

        this.buttonMode = new GuiButtonIcon(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, this.guiTexture, 200, 14 + modeOffset);
        this.buttonOutput = new GuiButtonIcon(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, this.guiTexture, 200, 46 + outputOffset);
        this.buttonList.clear();
        this.buttonList.add(this.buttonMode);
        this.buttonList.add(this.buttonOutput);
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Hovering over slow/fast mode button
        if (mouseX >= x + 10 && mouseX <= x + 25 && mouseY >= y + 53 && mouseY <= y + 68)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.slowfasttoggle", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
        // Hovering over the output-to-ender-chest button
        else if (mouseX >= x + 145 && mouseX <= x + 160 && mouseY >= y + 53 && mouseY <= y + 68)
        {
            List<String> list = new ArrayList<String>();
            list.add(I18n.format("enderutilities.gui.label.outputtoenderchest", new Object[0]));
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.createButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        this.createButtons();
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException
    {
        super.actionPerformed(btn);

        BlockPos pos = this.te.getPos();
        PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.te.getWorld().provider.getDimensionId(), pos.getX(), pos.getY(), pos.getZ(),
                ReferenceGuiIds.GUI_ID_ENDER_FURNACE, btn.id, (short)0));
    }
}
