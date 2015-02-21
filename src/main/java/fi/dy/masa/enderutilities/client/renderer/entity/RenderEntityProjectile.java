package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import fi.dy.masa.enderutilities.entity.IItemData;

public class RenderEntityProjectile extends Render
{
    private Item item;

    public RenderEntityProjectile(RenderManager renderManager, Item item)
    {
        super(renderManager);
        this.item = item;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float brightness)
    {
        IIcon iicon;
        if (entity instanceof IItemData)
        {
            iicon = this.item.getIconFromDamage(((IItemData)entity).getItemDamage(entity));
        }
        else
        {
            iicon = this.item.getIconFromDamage(0);
        }

        if (iicon != null)
        {
            GlStateManager.pushMatrix();
            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            this.bindEntityTexture(entity);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();

            this.drawQuad(tessellator, iicon);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return TextureMap.locationItemsTexture;
    }

    private void drawQuad(Tessellator tessellator, WorldRenderer worldRenderer, IIcon iicon)
    {
        float minU = iicon.getMinU();
        float maxU = iicon.getMaxU();
        float minV = iicon.getMinV();
        float maxV = iicon.getMaxV();

        GL11.glRotatef(180.0f - this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);

        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(-0.5d, -0.25d, 0.0d, (double)minU, (double)maxV);
        worldRenderer.addVertexWithUV( 0.5d, -0.25d, 0.0d, (double)maxU, (double)maxV);
        worldRenderer.addVertexWithUV( 0.5d,  0.75d, 0.0d, (double)maxU, (double)minV);
        worldRenderer.addVertexWithUV(-0.5d,  0.75d, 0.0d, (double)minU, (double)minV);
        tessellator.draw();
    }
}
