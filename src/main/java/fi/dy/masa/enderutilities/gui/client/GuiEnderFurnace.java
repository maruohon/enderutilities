package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.inventory.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class GuiEnderFurnace extends GuiContainerLargeStacks
{
    public ContainerEnderFurnace containerEnderFurnace;
    public TileEntityEnderFurnace teef;

    public GuiEnderFurnace(ContainerEnderFurnace container, TileEntityEnderFurnace te)
    {
        super(container, 176, 166, "gui.container." + te.getTEName());
        this.containerEnderFurnace = container;
        this.teef = te;
        this.scaledStackSizeTextTargetInventories.add(container.inventory);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.teef.hasCustomName() ? this.teef.getName() : I18n.format(this.teef.getName(), new Object[0]);
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 5, 0x404025);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, this.ySize - 96 + 4, 0x404025);

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

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Item.getItemFromBlock(Blocks.ender_chest)), x + 145, y + 34);
    }

    protected void createButtons()
    {
        int modeOffset = (this.teef.fastMode == true ? 16 : 0);
        int outputOffset = (this.containerEnderFurnace.outputToEnderChest == true ? 16 : 0);

        this.buttonList.clear();

        this.buttonList.add(new GuiButtonHoverText(0, this.guiLeft + 10, this.guiTop + 53, 16, 16, 200, 14 + modeOffset, this.guiTexture,
            new String[] { I18n.format("enderutilities.gui.label.slowfasttoggle", new Object[0]) }));

        this.buttonList.add(new GuiButtonHoverText(1, this.guiLeft + 145, this.guiTop + 53, 16, 16, 200, 46 + outputOffset, this.guiTexture,
            new String[] { I18n.format("enderutilities.gui.label.outputtoenderchest", new Object[0]) }));
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
        this.createButtons();
        super.drawScreen(mouseX, mouseY, gameTicks);
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException
    {
        super.actionPerformed(btn);

        PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(this.teef.getWorld().provider.getDimensionId(), this.teef.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, btn.id, 0));
    }
}
