package fi.dy.masa.enderutilities.event;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.block.BlockPortalPanel;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class RenderEventHandler
{
    private static RenderEventHandler INSTANCE;
    public Minecraft mc;
    protected BuildersWandRenderer buildersWandRenderer;
    protected RulerRenderer rulerRenderer;

    protected BlockPos portalPanelPosLast = BlockPos.ORIGIN;
    protected EnumFacing panelFacingLast = EnumFacing.DOWN;
    protected List<AxisAlignedBB> panelBoxes;
    protected float partialTicks;

    public RenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
        this.buildersWandRenderer = new BuildersWandRenderer();
        this.rulerRenderer = new RulerRenderer();
        this.panelBoxes = new ArrayList<AxisAlignedBB>();
        INSTANCE = this;
    }

    public static RenderEventHandler getInstance()
    {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        this.renderItemExtras(this.mc.theWorld, this.mc.thePlayer, this.mc.thePlayer, event.getPartialTicks());

        if (Configs.buildersWandRenderForOtherPlayers)
        {
            for (EntityPlayer player : this.mc.theWorld.getPlayers(EntityPlayer.class, EntitySelectors.NOT_SPECTATING))
            {
                this.renderItemExtras(this.mc.theWorld, player, this.mc.thePlayer, event.getPartialTicks());
            }
        }
    }

    private void renderItemExtras(World world, EntityPlayer usingPlayer, EntityPlayer clientPlayer, float partialTicks)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(usingPlayer, EnderUtilitiesItems.buildersWand);

        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand)
        {
            this.buildersWandRenderer.renderSelectedArea(world, usingPlayer, stack, clientPlayer, partialTicks);
        }

        this.rulerRenderer.renderAllPositionPairs(usingPlayer, clientPlayer, partialTicks);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != ElementType.ALL)
        {
            return;
        }

        if ((this.mc.currentScreen instanceof GuiChat) == false && this.mc.thePlayer != null)
        {
            this.buildersWandRenderer.renderHud(this.mc.thePlayer);
            this.rulerRenderer.renderHud();
        }
    }

    @SubscribeEvent
    public void onBlockHilight(DrawBlockHighlightEvent event)
    {
        RayTraceResult trace = event.getTarget();

        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            IBlockState state = this.mc.theWorld.getBlockState(trace.getBlockPos());

            if (state.getBlock() == EnderUtilitiesBlocks.blockPortalPanel)
            {
                this.renderPortalPanelText(trace.getBlockPos(), event.getPartialTicks());
                this.renderPortalPanelHilight(event.getContext(), state, trace.getBlockPos(), event.getPartialTicks());
                //event.setCanceled(true);
            }
        }
    }

    private void renderPortalPanelText(BlockPos pos, float partialTicks)
    {
        IBlockState state = this.mc.theWorld.getBlockState(pos);

        if (state.getBlock() == EnderUtilitiesBlocks.blockPortalPanel)
        {
            TileEntity te = this.mc.theWorld.getTileEntity(pos);

            if (te instanceof TileEntityPortalPanel)
            {
                String name = ((TileEntityPortalPanel) te).getPanelDisplayName();
                EnumFacing facing = state.getValue(BlockPortalPanel.FACING);

                if (StringUtils.isBlank(name) == false && name.length() > 0)
                {
                    GlStateManager.alphaFunc(516, 0.1F);
                    this.renderPortalPanelText(name, this.mc.thePlayer, pos, facing, partialTicks);
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
        double frontOffset = 0.32D;

        if (facing.getAxis().isHorizontal())
        {
            int frontX = facing.getFrontOffsetX();
            int frontZ = facing.getFrontOffsetZ();

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

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    protected void renderPortalPanelHilight(RenderGlobal context, IBlockState state, BlockPos pos, float partialTicks)
    {
        EnumFacing facing = state.getValue(BlockEnderUtilities.FACING);

        if (pos.equals(this.portalPanelPosLast) == false || facing != this.panelFacingLast)
        {
            this.updatePanelBoxBounds(pos, facing);
            this.portalPanelPosLast = pos;
            this.panelFacingLast = facing;
        }

        this.partialTicks = partialTicks;
    }

    public AxisAlignedBB getSelectedBoundingBox()
    {
        int index = EntityUtils.getPointedBox(this.mc.getRenderViewEntity(), 6d, this.panelBoxes, this.partialTicks);

        if (index >= 0 && index < this.panelBoxes.size())
        {
            return this.panelBoxes.get(index);
        }

        return PositionUtils.ZERO_BB;
    }

    private void updatePanelBoxBounds(BlockPos pos, EnumFacing facing)
    {
        this.panelBoxes.clear();

        Vec3d reference = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // The button AABBs are defined in the NORTH orientation
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_1.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_2.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_3.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_4.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_5.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_6.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_7.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_8.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.BUTTON_M.offset(pos), reference, EnumFacing.NORTH, facing));
        this.panelBoxes.add(PositionUtils.rotateBoxAroundPoint(BlockPortalPanel.PANEL_BOUNDS_BASE.offset(pos), reference, EnumFacing.NORTH, facing));
    }
}
