package fi.dy.masa.enderutilities.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge.Type;

@SideOnly(Side.CLIENT)
public class TileEntityRendererEnergyBridge extends TileEntitySpecialRenderer<TileEntityEnergyBridge>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");

    public void renderBeamVertical(double x, double y, double z, double yMin, double yMax, double radius, double rot, double flowSpeed, boolean powered)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        double tx1 = 0.0d, tx2 = 0.0d;
        double tz1 = 0.0d, tz2 = 0.0d;
        double angle = 0.0d;

        double vScale = yMax - yMin;
        double v1 = -rot * flowSpeed;
        double v2 = (vScale * 2.0d) + v1;

        int r_i = (powered ? 160 : 255);
        int g_i = (powered ? 255 : 160);
        int b_i = (powered ? 230 : 160);
        int r_o = (powered ? 210 : 255);
        int g_o = (powered ? 255 : 160);
        int b_o = (powered ? 230 : 160);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        this.bindTexture(TEXTURE);
        GlStateManager.disableFog();
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        // Beam (inner part)
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (int i = 0; i < 8; ++i)
        {
            tx1 = Math.sin(rot + angle) * radius;
            tz1 = Math.cos(rot + angle) * radius;
            angle += Math.PI / 4.0d;
            tx2 = Math.sin(rot + angle) * radius;
            tz2 = Math.cos(rot + angle) * radius;
            vertexBuffer.pos(tx1, yMin, tz1).tex(0.125, v1).color(r_i, g_i, b_i, 200).endVertex();
            vertexBuffer.pos(tx1, yMax, tz1).tex(0.125, v2).color(r_i, g_i, b_i, 200).endVertex();
            vertexBuffer.pos(tx2, yMax, tz2).tex(0.875, v2).color(r_i, g_i, b_i, 200).endVertex();
            vertexBuffer.pos(tx2, yMin, tz2).tex(0.875, v1).color(r_i, g_i, b_i, 200).endVertex();
        }

        tessellator.draw();

        // Glow (outer part)
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        v1 = -rot * flowSpeed * 3.0d;
        v2 = (vScale * 2.0d) + v1;
        radius *= 2.0d;
        rot = Math.PI / 8.0d;
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (int i = 0; i < 8; ++i)
        {
            tx1 = Math.sin(rot + angle) * radius;
            tz1 = Math.cos(rot + angle) * radius;
            angle += Math.PI / 4.0d;
            tx2 = Math.sin(rot + angle) * radius;
            tz2 = Math.cos(rot + angle) * radius;
            vertexBuffer.pos(tx1, yMin, tz1).tex(0.125, v1).color(r_o, g_o, b_o, 80).endVertex();
            vertexBuffer.pos(tx1, yMax, tz1).tex(0.125, v2).color(r_o, g_o, b_o, 80).endVertex();
            vertexBuffer.pos(tx2, yMax, tz2).tex(0.875, v2).color(r_o, g_o, b_o, 80).endVertex();
            vertexBuffer.pos(tx2, yMin, tz2).tex(0.875, v1).color(r_o, g_o, b_o, 80).endVertex();
        }

        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
    }

    @Override
    public void render(TileEntityEnergyBridge teeb, double x, double y, double z, float partialTicks, int destroyStage, float partial)
    {
        if (teeb.getIsActive() == false)
        {
            return;
        }

        TileEntityEnergyBridge.Type type = teeb.getType();
        double rot = (teeb.getWorld().getTotalWorldTime() % 100.0d) * Math.PI  / 50.0d + (Math.PI / 50.0d * partialTicks);
        x += 0.5d;
        z += 0.5d;

        BlockPos pos = teeb.getPos();
        // Energy Bridge Transmitter
        if (type == Type.TRANSMITTER)
        {
            this.renderBeamVertical(x, y, z, teeb.beamYMin - pos.getY(), 0.0d, 0.2d, rot, 3.0d, teeb.getIsPowered());
            this.renderBeamVertical(x, y, z, 1.0d, teeb.beamYMax - pos.getY(), 0.2d, rot, 3.0d, teeb.getIsPowered());
        }
        // Energy Bridge Receiver
        else if (type == Type.RECEIVER)
        {
            this.renderBeamVertical(x, y, z, teeb.beamYMin - pos.getY(), 0.0d, 0.2d, rot,  3.0d, teeb.getIsPowered());
            this.renderBeamVertical(x, y, z, 1.0d, teeb.beamYMax - pos.getY(), 0.2d, rot, -3.0d, teeb.getIsPowered());
        }
        // Energy Bridge Resonator
        else if (type == Type.RESONATOR)
        {
            EnumFacing dirFront = teeb.getFacing();
            EnumFacing dirSide = dirFront.rotateY();

            // From Resonator to Receiver
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5d * dirFront.getFrontOffsetX(), y + 0.5d, z + 0.5d * dirFront.getFrontOffsetZ());
            GlStateManager.rotate(90, -dirSide.getFrontOffsetX(), 0, -dirSide.getFrontOffsetZ());
            GlStateManager.translate(-x, -y, -z);
            this.renderBeamVertical(x, y, z, 0.0d, 2.0d, 0.2d, rot, 3.0d, teeb.getIsPowered());
            GlStateManager.popMatrix();

            // From resonator to next resonator
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.3d * dirSide.getFrontOffsetX() - 0.2d * dirFront.getFrontOffsetX(), y + 0.5d, z + 0.3d * dirSide.getFrontOffsetZ() - 0.2d * dirFront.getFrontOffsetZ());
            GlStateManager.rotate(90, dirFront.getFrontOffsetX(), 0, dirFront.getFrontOffsetZ());
            GlStateManager.rotate(45, -dirSide.getFrontOffsetX(), 0, -dirSide.getFrontOffsetZ());
            GlStateManager.translate(-x, -y, -z);
            this.renderBeamVertical(x, y, z, 0.0d, 4.2d, 0.14d, rot, 3.0d, teeb.getIsPowered());
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityEnergyBridge te)
    {
        return true;
    }
}
