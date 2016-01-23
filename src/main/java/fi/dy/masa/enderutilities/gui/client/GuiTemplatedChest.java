package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.inventory.ContainerTemplatedChest;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityTemplatedChest;

public class GuiTemplatedChest extends GuiEnderUtilities
{
    protected TileEntityTemplatedChest tetc;
    protected int chestTier;

    public GuiTemplatedChest(ContainerTemplatedChest container, TileEntityTemplatedChest te)
    {
        super(container, 176, 176, "gui.container." + te.getTEName() + "." + (te.getStorageTier() < 3 ? te.getStorageTier() : 0));
        this.tetc = te;
        this.chestTier = te.getStorageTier();
    }

    @Override
    public void initGui()
    {
        this.setGuiYSize();

        super.initGui();
    }

    protected void setGuiYSize()
    {
        switch(this.chestTier)
        {
            case 0: this.ySize = 140; break;
            case 1: this.ySize = 176; break;
            case 2: this.ySize = 234; break;
            default:
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        int y = 47;

        switch(this.chestTier)
        {
            case 0: y =  47; break;
            case 1: y =  83; break;
            case 2: y = 137; break;
            default:
        }

        this.fontRendererObj.drawString(I18n.format("enderutilities.container.templatedchest", new Object[0]), 8, 15, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, y, 0x404025);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        this.bindTexture(this.guiTexture);

        int invSize = this.tetc.getSizeInventory();

        // Draw the colored background icon for locked/"templated" slots
        long mask = this.tetc.getTemplateMask();
        long bit = 0x1;

        for (int i = 0; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                int x = this.guiLeft + slot.xDisplayPosition;
                int y = this.guiTop + slot.yDisplayPosition;
                int u = 0;

                // Empty locked slots are in a different color
                if (this.tetc.getStackInSlot(i) == null)
                {
                    u = 18;
                }

                this.drawTexturedModalRect(x - 1, y - 1, 176, u, 18, 18);
            }
        }

        // Draw a faint version of the template item for empty locked slots
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        bit = 0x1;
        for (int i = 0; i < invSize; i++, bit <<= 1)
        {
            Slot slot = this.inventorySlots.getSlot(i);
            if ((mask & bit) != 0)
            {
                int x = this.guiLeft + slot.xDisplayPosition;
                int y = this.guiTop + slot.yDisplayPosition;

                ItemStack stack = this.tetc.getStackInSlot(i);
                if (stack == null)
                {
                    stack = this.tetc.getTemplateStack(i);
                    if (stack != null)
                    {
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glEnable(GL11.GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_BLEND);
                        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
                    }
                }
            }
        }

        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void drawTooltips(int mouseX, int mouseY)
    {
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        x += 152;
        y += 6;

        // Hovering over the info icon
        if (mouseX >= x && mouseX <= x + 17 && mouseY >= y && mouseY <= y + 17)
        {
            List<String> list = new ArrayList<String>();
            ItemEnderUtilities.addTooltips("enderutilities.gui.label.templatedchest.info", list, false);
            this.drawHoveringText(list, mouseX, mouseY, this.fontRendererObj);
        }
    }
}
