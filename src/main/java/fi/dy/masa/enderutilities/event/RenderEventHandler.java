package fi.dy.masa.enderutilities.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class RenderEventHandler
{
    public Minecraft mc;
    public float partialTicksLast;
    protected BuildersWandRenderer buildersWandRenderer;
    protected RulerRenderer rulerRenderer;

    public RenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
        this.buildersWandRenderer = new BuildersWandRenderer();
        this.rulerRenderer = new RulerRenderer();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(this.mc.thePlayer, EnderUtilitiesItems.buildersWand);
        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand)
        {
            this.buildersWandRenderer.renderSelectedArea(this.mc.theWorld, this.mc.thePlayer, stack, event.getPartialTicks());
        }

        this.rulerRenderer.renderAllPositionPairs(event.getPartialTicks());
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != ElementType.ALL)
        {
            return;
        }

        this.buildersWandRenderer.renderHud();
        this.rulerRenderer.renderHud();
    }

    @SubscribeEvent
    public void onBlockHilight(DrawBlockHighlightEvent event)
    {
        RayTraceResult trace = event.getTarget();

        this.renderPortalPanelText(trace, event.getPartialTicks());
    }

    private void renderPortalPanelText(RayTraceResult trace, float partialTicks)
    {
        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = trace.getBlockPos();
            IBlockState state = this.mc.theWorld.getBlockState(pos);

            if (state.getBlock() == EnderUtilitiesBlocks.blockPortalPanel)
            {
                TileEntity te = this.mc.theWorld.getTileEntity(pos);

                if (te instanceof TileEntityPortalPanel)
                {
                    String name = ((TileEntityPortalPanel) te).getDisplayName();
                    EnumFacing facing = ((TileEntityPortalPanel) te).getFacing();

                    if (name != null && name.length() > 0)
                    {
                        GlStateManager.alphaFunc(516, 0.1F);
                        this.renderPortalPanelText(name, this.mc.thePlayer, pos, facing, partialTicks);
                    }
                }
            }
        }
    }

    protected void renderPortalPanelText(String text, EntityPlayer player, BlockPos pos, EnumFacing facing, float partialTicks)
    {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        float angleH = 0f;
        float angleV = 0f;

        if (facing.getAxis().isHorizontal())
        {
            int frontX = facing.getFrontOffsetX();
            int frontZ = facing.getFrontOffsetZ();
            double frontOffset = 0.2D;

            if (facing == EnumFacing.NORTH || facing == EnumFacing.WEST)
            {
                frontX = -frontX;
                frontZ = -frontZ;
                frontOffset = 1.0D - frontOffset;
            }

            x += frontX * frontOffset + frontZ * 0.5D;
            z += frontZ * frontOffset + frontX * 0.5D;

            y += 1.25D;
            angleH = facing.getHorizontalAngle() + 180f;
        }
        else
        {
            double frontOffset = 0.2D;
            if (facing == EnumFacing.DOWN)
            {
                frontOffset = 1.0D - frontOffset;
                x += 0.5D;
                z -= 0.25D;
            }
            else
            {
                x += 0.5D;
                z += 1.25D;
            }

            y += frontOffset;
            angleV = facing.getFrontOffsetY() * -90f;
        }

        this.renderLabel(text, x - dx, y - dy, z - dz, angleH, angleV);
    }

    protected void renderLabel(String text, double x, double y, double z, float angleH, float angleV)
    {
        boolean flag = false; // sneaking
        FontRenderer fontrenderer = this.mc.fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-angleH, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-angleV, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        //GlStateManager.scale(-0.75F, -0.75F, 0.75F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int strLenHalved = fontrenderer.getStringWidth(text) / 2;

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(-strLenHalved - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos(-strLenHalved - 1,  8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos( strLenHalved + 1,  8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos( strLenHalved + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();

        fontrenderer.drawString(text, -strLenHalved, 0, 0x20FFFFFF);
        GlStateManager.enableDepth();

        GlStateManager.depthMask(true);
        fontrenderer.drawString(text, -strLenHalved, 0, flag ? 0x20FFFFFF : 0xFFFFFFFF);

        //GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
