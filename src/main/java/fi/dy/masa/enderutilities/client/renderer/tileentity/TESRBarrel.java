package fi.dy.masa.enderutilities.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;

public class TESRBarrel extends TileEntitySpecialRenderer<TileEntityBarrel>
{
    private static final ResourceLocation TEXTURE_LOCK = ReferenceTextures.getResourceLocation("textures/gui/barrel_lock_icon.png");
    /** The rotation around the y-axis, when on the horizontal faces */
    private static final float[] MODEL_ROT_SIDE_Y = {    0f,    0f,  180f,    0f,  270f,   90f };
    private static final float[] LABEL_ROT_SIDE_Y = {    0f,    0f,    0f, -180f,   90f,  -90f };
    private RenderItem renderItem;
    private Minecraft mc;

    @Override
    public void renderTileEntityAt(TileEntityBarrel te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        this.mc = Minecraft.getMinecraft();
        BlockPos pos = te.getPos();

        if (this.mc.player.getDistanceSq(pos) < 900) // 30m
        {
            EnumFacing barrelFront = te.getFacing();
            x += 0.5;
            y += 0.5;
            z += 0.5;

            // Render a small lock icon if the barrel has been locked to an item type
            if (te.getInventoryBarrel().isSlotLocked(0) && this.mc.player.getDistanceSq(pos) < 100) // 10m
            {
                for (EnumFacing side : te.getLabeledFaces())
                {
                    int ambLight = this.getWorld().getCombinedLight(pos.offset(side), 0);
                    int lu = ambLight % 65536;
                    int lv = ambLight / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lu / 1.0F, (float) lv / 1.0F);

                    double posX = x + 0.502 * side.getFrontOffsetX();
                    double posY = y + 0.502 * side.getFrontOffsetY();
                    double posZ = z + 0.502 * side.getFrontOffsetZ();

                    this.renderLockIcon(posX, posY, posZ, side, barrelFront);
                }
            }

            if (te.renderStack.isEmpty())
            {
                return;
            }

            boolean fancy = this.mc.gameSettings.fancyGraphics;
            this.renderItem = this.mc.getRenderItem();
            this.mc.gameSettings.fancyGraphics = true;

            // Render the ItemStacks
            for (EnumFacing side : te.getLabeledFaces())
            {
                int ambLight = this.getWorld().getCombinedLight(pos.offset(side), 0);
                int lu = ambLight % 65536;
                int lv = ambLight / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lu / 1.0F, (float) lv / 1.0F);

                double posX = x + 0.502 * side.getFrontOffsetX();
                double posY = y + 0.502 * side.getFrontOffsetY();
                double posZ = z + 0.502 * side.getFrontOffsetZ();

                this.renderStack(te.renderStack, posX, posY, posZ, side, barrelFront);
            }

            // Render the fullness bar (if enabled)
            if (Configs.barrelRenderFullnessBar)
            {
                for (EnumFacing side : te.getLabeledFaces())
                {
                    int ambLight = this.getWorld().getCombinedLight(pos.offset(side), 0);
                    int lu = ambLight % 65536;
                    int lv = ambLight / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lu / 1.0F, (float) lv / 1.0F);

                    double posX = x + 0.502 * side.getFrontOffsetX();
                    double posY = y + 0.502 * side.getFrontOffsetY();
                    double posZ = z + 0.502 * side.getFrontOffsetZ();

                    this.renderFullnessBar(te.cachedFullness, posX, posY, posZ, side, barrelFront);
                }
            }

            if (this.mc.player.getDistanceSq(pos) < 100) // 10m
            {
                // Render the stored item count text
                for (EnumFacing side : te.getLabeledFaces())
                {
                    int ambLight = this.getWorld().getCombinedLight(pos.offset(side), 0);
                    int lu = ambLight % 65536;
                    int lv = ambLight / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lu / 1.0F, (float) lv / 1.0F);

                    double posX = x + 0.502 * side.getFrontOffsetX();
                    double posY = y + 0.502 * side.getFrontOffsetY();
                    double posZ = z + 0.502 * side.getFrontOffsetZ();

                    this.renderText(te.cachedStackSizeString, posX, posY, posZ, side, barrelFront);
                }
            }

            GlStateManager.enableLighting();
            GlStateManager.enableLight(0);
            GlStateManager.enableLight(1);
            GlStateManager.enableColorMaterial();
            GlStateManager.colorMaterial(1032, 5634);
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableNormalize();
            GlStateManager.disableBlend();

            this.mc.gameSettings.fancyGraphics = fancy;
        }
    }

    protected void renderStack(ItemStack stack, double posX, double posY, double posZ, EnumFacing side, EnumFacing barrelFront)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, posZ);

        if (side == EnumFacing.UP || side == EnumFacing.DOWN)
        {
            GlStateManager.rotate(MODEL_ROT_SIDE_Y[barrelFront.getIndex()], 0, 1, 0);
            GlStateManager.rotate(-90f * side.getFrontOffsetY(), 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(MODEL_ROT_SIDE_Y[side.getIndex()], 0, 1, 0);
        }

        //GlStateManager.translate(-0.25, 0.2, 0); // This offset is currently added to the renderItemIntoGUI arguments
        GlStateManager.scale(0.55f, 0.55f, 1);

        if (this.renderItem.shouldRenderItemIn3D(stack))
        {
            GlStateManager.scale(1 / 16f, -1 / 16f, 0.0001);
        }
        else
        {
            GlStateManager.scale(1 / 16f, -1 / 16f, 0.01);
            GlStateManager.translate(0, 0, -100);
        }

        // The following rendering stuff within this method has been taken from Storage Drawers by jaquadro, found here:
        // https://github.com/jaquadro/StorageDrawers/blob/e5719be6d64f757a1b58c25e8ca5bc64074c6b9e/src/com/jaquadro/minecraft/storagedrawers/client/renderer/TileEntityDrawersRenderer.java#L180

        // At the time GL_LIGHT* are configured, the coordinates are transformed by the modelview
        // matrix. The transformations used in `RenderHelper.enableGUIStandardItemLighting` are
        // suitable for the orthographic projection used by GUI windows, but they are just a little
        // bit off when rendering a block in 3D and squishing it flat. An additional term is added
        // to account for slightly different shading on the half-size "icons" in 1x2 and 2x2
        // drawers due to the extreme angles caused by flattening the block (as noted below).

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.6f, 2.6f, 1);
        GlStateManager.rotate(171.6f, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(84.9f, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();

        // TileEntitySkullRenderer alters both of these options on, but does not restore them.
        GlStateManager.enableCull();
        // This extra enable toggle is to fix a render glitch with items in Item Frames on screen,
        // and then hovering over a stack in an inventory... Rendering... always lovely...
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableRescaleNormal();

        // GL_POLYGON_OFFSET is used to offset flat icons toward the viewer (-Z) in screen space,
        // so they always appear on top of the drawer's front space.
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);

        // DIRTY HACK: Fool GlStateManager into thinking GL_RESCALE_NORMAL is enabled, but disable
        // it using popAttrib This prevents RenderItem from enabling it again.
        //
        // Normals are transformed by the inverse of the modelview and projection matrices that
        // excludes the translate terms. When put through the extreme Z scale used to flatten the
        // block, this makes them point away from the drawer face at a very sharp angle. These
        // normals are no longer unit scale (normalized), and normalizing them via
        // GL_RESCALE_NORMAL causes a loss of precision that results in the normals pointing
        // directly away from the face, which is visible as the block faces having identical
        // (dark) shading.

        GlStateManager.pushAttrib();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popAttrib();

        this.renderItem.renderItemIntoGUI(stack, -8, -7);

        GlStateManager.disableBlend(); // Clean up after RenderItem
        GlStateManager.enableAlpha();  // Restore world render state after RenderItem
        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
    }

    private void renderText(String text, double x, double y, double z, EnumFacing side, EnumFacing barrelFront)
    {
        FontRenderer fontRenderer = this.mc.fontRenderer;
        int strLenHalved = fontRenderer.getStringWidth(text) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        if (side == EnumFacing.UP || side == EnumFacing.DOWN)
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[barrelFront.getIndex()], 0, 1, 0);
            GlStateManager.rotate(90f * side.getFrontOffsetY(), 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[side.getIndex()], 0, 1, 0);
        }

        GlStateManager.translate(0, 0.48, 0);
        GlStateManager.scale(-0.01F, -0.01F, 0.01F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.doPolygonOffset(-1, -20);

        this.getFontRenderer().drawString(text, -strLenHalved, 0, 0xFFFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void renderFullnessBar(float fullness, double x, double y, double z, EnumFacing side, EnumFacing barrelFront)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        if (side == EnumFacing.UP || side == EnumFacing.DOWN)
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[barrelFront.getIndex()], 0, 1, 0);
            GlStateManager.rotate(90f * side.getFrontOffsetY(), 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[side.getIndex()], 0, 1, 0);
        }

        GlStateManager.translate(-0.3, -0.43, -0.001);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int r_b = 0x03;
        int g_b = 0x03;
        int b_b = 0x20;

        buffer.pos(  0,    0, 0).color(r_b, g_b, b_b, 255).endVertex();
        buffer.pos(  0, 0.08, 0).color(r_b, g_b, b_b, 255).endVertex();
        buffer.pos(0.6, 0.08, 0).color(r_b, g_b, b_b, 255).endVertex();
        buffer.pos(0.6,    0, 0).color(r_b, g_b, b_b, 255).endVertex();

        int r_f = 0x20;
        int g_f = 0x90;
        int b_f = 0xF0;
        float e = fullness * 0.57f;

        buffer.pos(0.585    , 0.065, -0.001).color(r_f, g_f, b_f, 255).endVertex();
        buffer.pos(0.585    , 0.015, -0.001).color(r_f, g_f, b_f, 255).endVertex();
        buffer.pos(0.585 - e, 0.015, -0.001).color(r_f, g_f, b_f, 255).endVertex();
        buffer.pos(0.585 - e, 0.065, -0.001).color(r_f, g_f, b_f, 255).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void renderLockIcon(double x, double y, double z, EnumFacing side, EnumFacing barrelFront)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        if (side == EnumFacing.UP || side == EnumFacing.DOWN)
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[barrelFront.getIndex()], 0, 1, 0);
            GlStateManager.rotate(90f * side.getFrontOffsetY(), 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(LABEL_ROT_SIDE_Y[side.getIndex()], 0, 1, 0);
        }

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.translate(0.365, 0.33, -0.001);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableRescaleNormal();
        GlStateManager.disableLighting();

        this.mc.getTextureManager().bindTexture(TEXTURE_LOCK);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(  0,   0, 0).tex(0, 0).endVertex();
        buffer.pos(  0, 0.15, 0).tex(0, 1).endVertex();
        buffer.pos(0.15, 0.15, 0).tex(1, 1).endVertex();
        buffer.pos(0.15,   0, 0).tex(1, 0).endVertex();

        tessellator.draw();

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }
}
