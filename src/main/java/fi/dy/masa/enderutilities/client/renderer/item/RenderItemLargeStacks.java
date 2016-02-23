package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemLargeStacks extends RenderItem
{
    protected Container container;
    protected List<IInventory> targetInventories;

    public RenderItemLargeStacks(TextureManager textureManager, ModelManager modelManager)
    {
        super(textureManager, modelManager);
    }

    public void setContainer(Container container)
    {
        this.container = container;
    }

    public void setScaledTextInventories(List<IInventory> invs)
    {
        this.targetInventories = invs;
    }

    @Override
    public void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack stack, int xPosition, int yPosition, String text)
    {
        if (stack == null)
        {
            return;
        }

        if (stack.stackSize != 1 || text != null)
        {
            String str = text == null ? String.valueOf(stack.stackSize) : text;

            if (text == null && stack.stackSize < 1)
            {
                str = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
            }

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();

            //if (this.shouldRenderStackSizeAsScaled(stack) == true)
            if (this.shouldRenderStackSizeAsScaled(xPosition, yPosition) == true)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(xPosition, yPosition, 0.0d);
                GlStateManager.scale(0.5d, 0.5d, 0.5d);
                fontRenderer.drawStringWithShadow(str, (31 - fontRenderer.getStringWidth(str)), 23, 0xFFFFFF);
                GlStateManager.popMatrix();
            }
            else
            {
                fontRenderer.drawStringWithShadow(str, (float)(xPosition + 17 - fontRenderer.getStringWidth(str)), (float)(yPosition + 9), 0xFFFFFF);
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }

        if (stack.getItem().showDurabilityBar(stack))
        {
            double health = stack.getItem().getDurabilityForDisplay(stack);
            int j = (int)Math.round(13.0D - health * 13.0D);
            int i = (int)Math.round(255.0D - health * 255.0D);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            this.draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
            this.draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
            this.draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);

            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    /**
     * A hacky way to find out if the ItemStack being rendered is part of the inventories
     * for which we want to render the stackSize as scaled...
     */
    protected boolean shouldRenderStackSizeAsScaled(ItemStack stack)
    {
        for (Slot slot : this.container.inventorySlots)
        {
            if (slot.getStack() == stack)
            {
                return this.targetInventories.contains(slot.inventory);
            }
        }

        return false;
    }

    /**
     * A hacky way to find out if the ItemStack being rendered is part of the inventories
     * for which we want to render the stackSize as scaled...
     */
    protected boolean shouldRenderStackSizeAsScaled(int slotX, int slotY)
    {
        for (Slot slot : this.container.inventorySlots)
        {
            if (slot.xDisplayPosition == slotX && slot.yDisplayPosition == slotY)
            {
                return this.targetInventories.contains(slot.inventory);
            }
        }

        return false;
    }

    protected void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        renderer.pos(x +     0, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x +     0, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y + height, 0.0d).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y +      0, 0.0d).color(red, green, blue, alpha).endVertex();

        Tessellator.getInstance().draw();
    }
}
