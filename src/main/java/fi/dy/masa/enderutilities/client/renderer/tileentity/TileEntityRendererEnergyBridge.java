package fi.dy.masa.enderutilities.client.renderer.tileentity;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

@SideOnly(Side.CLIENT)
public class TileEntityRendererEnergyBridge extends TileEntitySpecialRenderer
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");

    public void renderBeamVertical(double x, double y, double z, double yMin, double yMax, double radius, double rot, double flowSpeed)
    {
        Tessellator tessellator = Tessellator.instance;
        double tx1 = 0.0d, tx2 = 0.0d;
        double tz1 = 0.0d, tz2 = 0.0d;
        double angle = 0.0d;

        double vScale = yMax - yMin;
        double v1 = -rot * flowSpeed;
        double v2 = (vScale * 2.0d) + v1;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        this.bindTexture(TEXTURE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        OpenGlHelper.glBlendFunc(770, 1, 1, 0);

        // Beam (inner part)
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(150, 255, 220, 200);

        for (int i = 0; i < 8; ++i)
        {
            tx1 = Math.sin(rot + angle) * radius;
            tz1 = Math.cos(rot + angle) * radius;
            angle += Math.PI / 4.0d;
            tx2 = Math.sin(rot + angle) * radius;
            tz2 = Math.cos(rot + angle) * radius;
            tessellator.addVertexWithUV(tx1, yMin, tz1, 0.125, v1);
            tessellator.addVertexWithUV(tx1, yMax, tz1, 0.125, v2);
            tessellator.addVertexWithUV(tx2, yMax, tz2, 0.875, v2);
            tessellator.addVertexWithUV(tx2, yMin, tz2, 0.875, v1);
        }

        tessellator.draw();

        // Glow (outer part)
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDepthMask(false);

        v1 = -rot * flowSpeed * 3.0d;
        v2 = (vScale * 2.0d) + v1;
        radius = 0.4d;
        rot = Math.PI / 8.0d;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(210, 230, 255, 80);

        for (int i = 0; i < 8; ++i)
        {
            tx1 = Math.sin(rot + angle) * radius;
            tz1 = Math.cos(rot + angle) * radius;
            angle += Math.PI / 4.0d;
            tx2 = Math.sin(rot + angle) * radius;
            tz2 = Math.cos(rot + angle) * radius;
            tessellator.addVertexWithUV(tx1, yMin, tz1, 0.125, v1);
            tessellator.addVertexWithUV(tx1, yMax, tz1, 0.125, v2);
            tessellator.addVertexWithUV(tx2, yMax, tz2, 0.875, v2);
            tessellator.addVertexWithUV(tx2, yMin, tz2, 0.875, v1);
        }

        tessellator.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    public void renderTileEntityAt(TileEntityEnergyBridge teeb, double x, double y, double z, float pTicks)
    {
        if (teeb.isActive == false)
        {
            return;
        }

        double rot = (teeb.getWorldObj().getTotalWorldTime() % 100.0d) * Math.PI  / 50.0d + (Math.PI / 50.0d * pTicks);

        // Energy Bridge Transmitter
        if (teeb.meta == 0 && teeb.isMaster == true)
        {
            this.renderBeamVertical(x + 0.5d, y, z + 0.5d, teeb.beamYMin - teeb.yCoord, 0.0d, 0.2d, rot, 1.0d);
            this.renderBeamVertical(x + 0.5d, y, z + 0.5d, 1.0d, teeb.beamYMax - teeb.yCoord, 0.2d, rot, 1.0d);
        }
        // Energy Bridge Receiver
        else if (teeb.meta == 1 && teeb.isMaster == true)
        {
            this.renderBeamVertical(x + 0.5d, y, z + 0.5d, teeb.beamYMin - teeb.yCoord, 0.0d, 0.2d, rot,  3.0d);
            this.renderBeamVertical(x + 0.5d, y, z + 0.5d, 1.0d, teeb.beamYMax - teeb.yCoord, 0.2d, rot, -3.0d);
        }
        // Energy Bridge Resonator
        else if (teeb.meta == 2)
        {
        }
    }

    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f)
    {
        this.renderTileEntityAt((TileEntityEnergyBridge)te, x, y, z, f);
    }
}
