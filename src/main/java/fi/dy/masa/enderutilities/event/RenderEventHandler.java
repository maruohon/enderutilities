package fi.dy.masa.enderutilities.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.client.renderer.util.RenderUtils;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.entity.EntityChair;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.item.block.ItemBlockPlacementProperty;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.ItemType;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import fi.dy.masa.enderutilities.util.PlacementProperties.PlacementProperty;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class RenderEventHandler
{
    private static RenderEventHandler INSTANCE;
    public Minecraft mc;
    protected BuildersWandRenderer buildersWandRenderer;
    protected RulerRenderer rulerRenderer;

    protected BlockPos pointedPosLast = BlockPos.ORIGIN;
    protected EnumFacing pointedBlockFacingLast = EnumFacing.DOWN;
    protected float partialTicks;

    public RenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
        this.buildersWandRenderer = new BuildersWandRenderer();
        this.rulerRenderer = new RulerRenderer();
        INSTANCE = this;
    }

    public static RenderEventHandler getInstance()
    {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        this.renderItemExtras(this.mc.world, this.mc.player, this.mc.player, event.getPartialTicks());

        if (Configs.buildersWandRenderForOtherPlayers)
        {
            for (EntityPlayer player : this.mc.world.getPlayers(EntityPlayer.class, EntitySelectors.NOT_SPECTATING))
            {
                this.renderItemExtras(this.mc.world, player, this.mc.player, event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != ElementType.ALL)
        {
            return;
        }

        if ((this.mc.currentScreen instanceof GuiChat) == false && this.mc.player != null)
        {
            this.buildersWandRenderer.renderHud(this.mc.player);
            this.rulerRenderer.renderHud();
            this.renderPlacementPropertiesHud(this.mc.player);
        }
    }

    @SubscribeEvent
    public void onBlockHilight(DrawBlockHighlightEvent event)
    {
        RayTraceResult trace = event.getTarget();

        if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            World world = this.mc.world;
            BlockPos pos = trace.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block == EnderUtilitiesBlocks.PORTAL_PANEL || block == EnderUtilitiesBlocks.INSERTER)
            {
                state = state.getActualState(world, pos);
                this.updatePointedBlockHilight(world, trace.getBlockPos(), state, (BlockEnderUtilities) block, event.getPartialTicks());
            }

            if (block == EnderUtilitiesBlocks.PORTAL_PANEL)
            {
                this.renderPortalPanelText(this.mc.world, trace.getBlockPos(), (BlockEnderUtilities) block, this.mc.player, event.getPartialTicks());
            }
        }
    }

    private void renderItemExtras(World world, EntityPlayer usingPlayer, EntityPlayer clientPlayer, float partialTicks)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(usingPlayer, EnderUtilitiesItems.BUILDERS_WAND);

        if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.BUILDERS_WAND)
        {
            this.buildersWandRenderer.renderSelectedArea(world, usingPlayer, stack, clientPlayer, partialTicks);
        }

        stack = EntityUtils.getHeldItemOfType(usingPlayer, EnderUtilitiesItems.CHAIR_WAND);

        if (stack.isEmpty() == false)
        {
            List<EntityChair> chairs = world.getEntities(EntityChair.class, Predicates.alwaysTrue());

            for (Entity entity : chairs)
            {
                RenderUtils.renderEntityDebugBoundingBox(entity, partialTicks, false, false);
            }
        }

        this.rulerRenderer.renderAllPositionPairs(usingPlayer, clientPlayer, partialTicks);
    }

    private void renderPortalPanelText(World world, BlockPos pos, BlockEnderUtilities block, EntityPlayer player, float partialTicks)
    {
        IBlockState state = world.getBlockState(pos);

        TileEntityPortalPanel te = BlockEnderUtilitiesTileEntity.getTileEntitySafely(world, pos, TileEntityPortalPanel.class);

        if (te != null)
        {
            EnumFacing facing = state.getValue(block.propFacing);
            Integer elementId = block.getPointedElementId(world, pos, facing, player);
            String name;

            if (elementId != null && elementId >= 0 && elementId <= 7)
            {
                name = te.getTargetDisplayName(elementId);
            }
            else
            {
                name = te.getPanelDisplayName();
            }

            if (StringUtils.isBlank(name) == false && name.length() > 0)
            {
                this.renderPortalPanelText(name, player, pos, facing, partialTicks);
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
        FontRenderer fontrenderer = this.mc.fontRenderer;

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
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
        BufferBuilder vertexbuffer = tessellator.getBuffer();
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

    /**
     * Renders text on the screen, with the given offset from the screen edge from the specified corner.<br>
     * <b>NOTE: Only BOTTOM_LEFT is currently implemented!!</b>
     * @param lines
     * @param offsetX
     * @param offsetY
     * @param align
     * @param useTextBackground
     * @param useFontShadow
     * @param mc
     */
    public static void renderText(List<String> lines, int offsetX, int offsetY, HudAlignment align,
            boolean useTextBackground, boolean useFontShadow, Minecraft mc)
    {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scaledY = scaledResolution.getScaledHeight();
        int lineHeight = mc.fontRenderer.FONT_HEIGHT + 2;
        int posX = offsetX;
        int posY = offsetY;

        switch (align)
        {
            // TODO Add all the others, if needed some time...
            case TOP_LEFT:
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                break;

            case BOTTOM_LEFT:
                posY = scaledY - (lineHeight * lines.size()) - offsetY;
        }

        int textBgColor = 0x80000000;
        FontRenderer fontRenderer = mc.fontRenderer;

        for (String line : lines)
        {
            if (useTextBackground)
            {
                Gui.drawRect(posX - 2, posY - 2, posX + fontRenderer.getStringWidth(line) + 2, posY + fontRenderer.FONT_HEIGHT, textBgColor);
            }

            if (useFontShadow)
            {
                mc.ingameGUI.drawString(fontRenderer, line, posX, posY, 0xFFFFFFFF);
            }
            else
            {
                fontRenderer.drawString(line, posX, posY, 0xFFFFFFFF);
            }

            posY += fontRenderer.FONT_HEIGHT + 2;
        }
    }

    private void renderPlacementPropertiesHud(EntityPlayer player)
    {
        ItemStack stack = player.getHeldItemMainhand();

        if (stack.isEmpty() || (stack.getItem() instanceof ItemBlockEnderUtilities) == false)
        {
            stack = player.getHeldItemOffhand();
        }

        if (stack.isEmpty() == false && stack.getItem() instanceof ItemBlockPlacementProperty)
        {
            ItemBlockPlacementProperty item = (ItemBlockPlacementProperty) stack.getItem();

            if (item.hasPlacementProperty(stack))
            {
                renderText(this.getPlacementPropertiesText(item, stack, player), 4, 0, HudAlignment.BOTTOM_LEFT, true, true, this.mc);
            }
        }
    }

    private List<String> getPlacementPropertiesText(ItemBlockPlacementProperty item, ItemStack stack, EntityPlayer player)
    {
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        List<String> lines = new ArrayList<String>();
        PlacementProperties props = PlacementProperties.getInstance();
        UUID uuid = player.getUniqueID();
        PlacementProperty pp = item.getPlacementProperty(stack);
        boolean nbtSensitive = pp.isNBTSensitive();
        ItemType type = new ItemType(stack, nbtSensitive);
        int index = props.getPropertyIndex(uuid, type);
        int count = pp.getPropertyCount();

        for (int i = 0; i < count; i++)
        {
            Pair<String, Integer> pair = pp.getProperty(i);

            if (pair != null)
            {
                String key = pair.getLeft();
                String pre = (i == index) ? "> " : "  ";
                String name = I18n.format("enderutilities.placement_properties." + key);
                int value = props.getPropertyValue(uuid, type, key, pair.getRight());
                String valueName = pp.getPropertyValueName(key, value);

                if (valueName == null)
                {
                    valueName = String.valueOf(value);
                }
                else
                {
                    valueName = I18n.format("enderutilities.placement_properties.valuenames." + key + "." + valueName);
                }

                lines.add(String.format("%s%s: %s%s%s", pre, name, preGreen, valueName, rst));
            }
        }

        return lines;
    }

    public <T> AxisAlignedBB getPointedHilightBox(BlockEnderUtilities block)
    {
        Map<T, AxisAlignedBB> boxMap = block.getHilightBoxMap();
        T key = EntityUtils.getPointedBox(this.mc.getRenderViewEntity(), 6d, boxMap, this.partialTicks);

        if (key != null)
        {
            return boxMap.get(key);
        }

        return PositionUtils.ZERO_BB;
    }

    protected void updatePointedBlockHilight(World world, BlockPos pos, IBlockState state, BlockEnderUtilities block, float partialTicks)
    {
        EnumFacing facing = state.getValue(block.propFacing);

        if (pos.equals(this.pointedPosLast) == false || facing != this.pointedBlockFacingLast)
        {
            block.updateBlockHilightBoxes(world, pos, facing);
            this.pointedPosLast = pos;
            this.pointedBlockFacingLast = facing;
        }

        this.partialTicks = partialTicks;
    }

    public enum HudAlignment
    {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
