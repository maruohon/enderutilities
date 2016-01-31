package fi.dy.masa.enderutilities.client.renderer.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

public class RenderItemLargeStacks extends RenderItem
{
    @Override
    public void renderItemOverlayIntoGUI(FontRenderer fontRenderer, TextureManager textureManager, ItemStack stack, int x, int y, String str)
    {
        if (stack != null)
        {
            //System.out.println("plop - stack: " + stack + " str: " + str);
            if (stack.stackSize > 1 || str != null)
            {
                String s1 = str == null ? String.valueOf(stack.stackSize) : str;
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_BLEND);

                //if (stack.stackSize >= 100)
                {
                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, 0.0d);
                    GL11.glScaled(0.5d, 0.5d, 0.5d);
                    fontRenderer.drawStringWithShadow(s1, (31 - fontRenderer.getStringWidth(s1)), 23, 16777215);
                    GL11.glPopMatrix();
                }
                /*else
                {
                    fontRenderer.drawStringWithShadow(s1, x + 19 - 2 - fontRenderer.getStringWidth(s1), y + 6 + 3, 16777215);
                }*/

                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            if (stack.getItem().showDurabilityBar(stack))
            {
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int j1 = (int)Math.round(13.0D - health * 13.0D);
                int k = (int)Math.round(255.0D - health * 255.0D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_BLEND);
                Tessellator tessellator = Tessellator.instance;
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                this.renderQuadCustom(tessellator, x + 2, y + 13, 13, 2, 0);
                this.renderQuadCustom(tessellator, x + 2, y + 13, 12, 1, i1);
                this.renderQuadCustom(tessellator, x + 2, y + 13, j1, 1, l);
                //GL11.glEnable(GL11.GL_BLEND); // Forge: Disable Bled because it screws with a lot of things down the line.
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private void renderQuadCustom(Tessellator tessellator, int x, int y, int z, int p_77017_5_, int p_77017_6_)
    {
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(p_77017_6_);
        tessellator.addVertex((double)(x + 0), (double)(y + 0), 0.0D);
        tessellator.addVertex((double)(x + 0), (double)(y + p_77017_5_), 0.0D);
        tessellator.addVertex((double)(x + z), (double)(y + p_77017_5_), 0.0D);
        tessellator.addVertex((double)(x + z), (double)(y + 0), 0.0D);
        tessellator.draw();
    }
}
