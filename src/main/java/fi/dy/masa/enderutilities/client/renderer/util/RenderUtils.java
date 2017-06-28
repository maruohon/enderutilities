package fi.dy.masa.enderutilities.client.renderer.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class RenderUtils
{
    public static void renderEntityDebugBoundingBox(Entity entityIn, float partialTicks, boolean renderLook, boolean renderEyeHeight)
    {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        double x = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double y = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double z = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        x -= renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * (double)partialTicks;
        y -= renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * (double)partialTicks;
        z -= renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * (double)partialTicks;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.glLineWidth(1.0F);

        double entityRadius = entityIn.width / 2.0D;
        AxisAlignedBB bb = entityIn.getEntityBoundingBox();

        RenderGlobal.drawBoundingBox(bb.minX - entityIn.posX + x,
                                     bb.minY - entityIn.posY + y,
                                     bb.minZ - entityIn.posZ + z,
                                     bb.maxX - entityIn.posX + x,
                                     bb.maxY - entityIn.posY + y,
                                     bb.maxZ - entityIn.posZ + z,
                                     1.0F, 1.0F, 1.0F, 1.0F);

        if (renderEyeHeight && entityIn instanceof EntityLivingBase)
        {
            RenderGlobal.drawBoundingBox(x - entityRadius,
                                         y + entityIn.getEyeHeight() - 0.01D,
                                         z - entityRadius,
                                         x + entityRadius,
                                         y + entityIn.getEyeHeight() + 0.01D,
                                         z + entityRadius, 1.0F, 0.0F, 0.0F, 1.0F);
        }

        if (renderLook)
        {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            Vec3d look = entityIn.getLook(partialTicks);
            vertexbuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(x, y + entityIn.getEyeHeight(), z).color(0, 0, 255, 255).endVertex();
            vertexbuffer.pos(x + look.x * 2.0D, y + entityIn.getEyeHeight() + look.y * 2.0D, z + look.z * 2.0D).color(0, 0, 255, 255).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
}
