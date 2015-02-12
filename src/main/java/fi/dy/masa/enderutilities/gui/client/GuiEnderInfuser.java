package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.resources.I18n;
import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;

public class GuiEnderInfuser extends GuiEnderUtilitiesInventory
{
    private TileEntityEnderInfuser teef;

    public GuiEnderInfuser(ContainerEnderInfuser container, TileEntityEnderInfuser te)
    {
        super(container, te);
        this.teef = te;
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomInventoryName() ? this.te.getInventoryName() : I18n.format(this.te.getInventoryName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, 84, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        //itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
    }

    @Override
    public void initGui()
    {
        super.initGui();
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
    }
}
