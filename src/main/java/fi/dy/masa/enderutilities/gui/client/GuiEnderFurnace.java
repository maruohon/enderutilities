package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import fi.dy.masa.enderutilities.inventory.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GuiEnderFurnace extends GuiTileEntityInventory
{
    public ContainerEnderFurnace containerEnderFurnace;
    public GuiButtonIcon buttonMode;
    public GuiButtonIcon buttonOutput;
    public TileEntityEnderFurnace teef;

    public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
    {
        super(container, 176, 166, "gui.container." + te.getTEName(), te);
        this.containerEnderFurnace = container;
        this.teef = te;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 4, 0x404025);

        s = I18n.format("enderutilities.gui.label.outputbuffer", new Object[0]) + ": " + this.containerEnderFurnace.outputBufferAmount;
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
        if (this.containerEnderFurnace.outputToEnderChest == true)
        {
            this.drawTexturedModalRect(x + 114, y + 34, 176, 78, 24, 16);
        }

        // Draw the burn progress flame
        if (this.teef.isBurningLast == true)
        {
            int uOffset = (this.teef.fastMode == true ? 14 : 0);
            int h = this.containerEnderFurnace.fuelProgress * 13 / 100;

            this.drawTexturedModalRect(x + 34, y + 36 + 12 - h, 176 + uOffset, 12 - h, 14, h + 1);
        }

        // Draw the smelting progress arrow
        if (this.containerEnderFurnace.smeltingProgress > 0)
        {
            int vOffset = 0;
            int w = this.containerEnderFurnace.smeltingProgress * 24 / 100;

            if (this.teef.isBurningLast == true)
            {
                vOffset = (this.teef.fastMode == true ? 32 : 16);
            }

            this.drawTexturedModalRect(x + 57, y + 34, 176, 14 + vOffset, w, 16);
        }

        itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
    }

    protected void createButtons()
    {
        int modeOffset = (this.teef.fastMode == true ? 16 : 0);
        int outputOffset = (this.containerEnderFurnace.outputToEnderChest == true ? 16 : 0);

        this.buttonMode = new GuiButtonIcon(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, 200, 14 + modeOffset, this.guiTexture);
        this.buttonOutput = new GuiButtonIcon(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, 200, 46 + outputOffset, this.guiTexture);
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
    public void updateScreen()
    {
        super.updateScreen();
        this.createButtons();
    }

    @Override
    protected void actionPerformed(GuiButton btn)
    {
        super.actionPerformed(btn);

        PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.te.getWorldObj().provider.dimensionId, this.te.xCoord, this.te.yCoord, this.te.zCoord,
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, btn.id, 0));
    }
}
