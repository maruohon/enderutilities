package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Area3D;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BuildersWandRenderer
{
    protected final Minecraft mc;
    protected final List<BlockPosStateDist> positions;
    protected float partialTicksLast;
    protected BlockPosEU posStartLast;

    public BuildersWandRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new ArrayList<BlockPosStateDist>();
    }

    public void renderSelectedArea(World world, EntityPlayer usingPlayer, ItemStack stack, EntityPlayer clientPlayer, float partialTicks)
    {
        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
        BlockPosEU posStart = wand.getPosition(stack, ItemBuildersWand.POS_START);
        Mode mode = Mode.getMode(stack);

        if (posStart == null)
        {
            RayTraceResult rayTraceResult = EntityUtils.getRayTraceFromPlayer(world, usingPlayer, false);

            if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)
            {
                return;
            }

            // Don't allow targeting the top face of blocks while sneaking
            // This should make sneak building a platform a lot less annoying
            if (usingPlayer.isSneaking() == true && rayTraceResult.sideHit == EnumFacing.UP && mode != Mode.REPLACE)
            {
                return;
            }

            // In Replace mode we want to target the pointed block, not the empty space adjacent to it
            if (mode == Mode.REPLACE)
            {
                posStart = new BlockPosEU(rayTraceResult.getBlockPos(), usingPlayer.dimension, rayTraceResult.sideHit);
            }
            else
            {
                posStart = new BlockPosEU(rayTraceResult.getBlockPos().offset(rayTraceResult.sideHit), usingPlayer.dimension, rayTraceResult.sideHit);
            }
        }

        if (posStart == null || usingPlayer.dimension != posStart.dimension)
        {
            return;
        }

        BlockPosEU posEnd = mode.isAreaMode() || (mode == Mode.WALLS || mode == Mode.CUBE) ? wand.getPosition(stack, ItemBuildersWand.POS_END) : null;

        // If the start position hasn't been set but is ray traced, then get a "floating" end position for it
        if (posEnd == null && mode.isAreaMode())
        {
            posEnd = wand.getTransformedEndPosition(stack, mode, posStart);
        }

        if (partialTicks < this.partialTicksLast || posStart.equals(this.posStartLast) == false)
        {
            this.positions.clear();

            if (mode == Mode.CUBE || mode == Mode.WALLS)
            {
                // We use the walls mode block positions for cube rendering as well, to save on rendering burden
                wand.getBlockPositionsWalls(stack, world, this.positions, posStart, posEnd);
            }
            else if (mode.isAreaMode() == false)
            {
                wand.getBlockPositions(stack, world, usingPlayer, this.positions, posStart);
            }
        }

        this.posStartLast = posStart;

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();

        boolean renderGhostBlocks = wand.getRenderGhostBlocks(stack, mode);

        if (renderGhostBlocks == true)
        {
            this.renderGhostBlocks(clientPlayer, partialTicks);
        }

        GlStateManager.disableTexture2D();

        if (renderGhostBlocks == false && mode.isAreaMode() == false)
        {
            this.renderBlockOutlines(mode, clientPlayer, posStart, posEnd, partialTicks);
        }

        if (mode == Mode.STACK)
        {
            this.renderStackedArea(Area3D.getAreaFromNBT(wand.getAreaTag(stack)), clientPlayer, posStart, posEnd, partialTicks);
            this.renderStartAndEndPositions(mode, clientPlayer, posStart, posEnd, partialTicks, 0x22, 0xFF, 0x22);
        }
        else
        {
            this.renderStartAndEndPositions(mode, clientPlayer, posStart, posEnd, partialTicks);
        }

        // In "Move, to" mode we also render the "Move, from" area
        if (mode == Mode.MOVE_DST)
        {
            posStart = wand.getPosition(stack, Mode.MOVE_SRC, true);
            posEnd = wand.getPosition(stack, Mode.MOVE_SRC, false);
            this.renderStartAndEndPositions(Mode.MOVE_SRC, clientPlayer, posStart, posEnd, partialTicks, 0xFF, 0x55, 0x55);
        }

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);

        this.partialTicksLast = partialTicks;
    }

    private void renderBlockOutlines(Mode mode, EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        GlStateManager.glLineWidth(2.0f);
        float expand = mode == Mode.REPLACE ? 0.001f : 0f;

        for (int i = 0; i < this.positions.size(); i++)
        {
            BlockPosEU pos = this.positions.get(i);

            if (pos.equals(posStart) == false && pos.equals(posEnd) == false)
            {
                AxisAlignedBB aabb = createAABB(pos.posX, pos.posY, pos.posZ, expand, partialTicks, player);
                RenderGlobal.drawSelectionBoundingBox(aabb, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    private void renderStartAndEndPositions(Mode mode, EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        this.renderStartAndEndPositions(mode, player, posStart, posEnd, partialTicks, 0xFF, 0xFF, 0xFF);
    }

    private void renderStartAndEndPositions(Mode mode, EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks, int r, int g, int b)
    {
        // Draw the area bounding box
        if (posStart != null && posEnd != null && mode.isAreaMode())
        {
            AxisAlignedBB aabb = createEnclosingAABB(posStart, posEnd, player, partialTicks);
            RenderGlobal.drawSelectionBoundingBox(aabb, r / 255f, g / 255f, b / 255f, 0xCC / 255f);
        }

        float expand = mode == Mode.REPLACE || mode == Mode.REPLACE_3D || mode.hasTwoPlacableCorners() ? 0.001f : 0f;

        if (posStart != null)
        {
            // Render the targeted position in a different (hilighted) color
            GlStateManager.glLineWidth(3.0f);
            AxisAlignedBB aabb = createAABB(posStart.posX, posStart.posY, posStart.posZ, expand, partialTicks, player);
            RenderGlobal.drawSelectionBoundingBox(aabb, 1.0f, 0x11 / 255f, 0x11 / 255f, 1.0f);
        }

        if (posEnd != null && (mode.isAreaMode() || mode == Mode.WALLS || mode == Mode.CUBE))
        {
            // Render the end position in a different (hilighted) color
            GlStateManager.glLineWidth(3.0f);
            AxisAlignedBB aabb = createAABB(posEnd.posX, posEnd.posY, posEnd.posZ, expand, partialTicks, player);
            RenderGlobal.drawSelectionBoundingBox(aabb, 0x11 / 255f, 0x11 / 255f, 1.0f, 1.0f);
        }
    }

    private void renderGhostBlocks(EntityPlayer player, float partialTicks)
    {
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        if (partialTicks < this.partialTicksLast)
        {
            for (int i = 0; i < this.positions.size(); i++)
            {
                BlockPosStateDist pos = this.positions.get(i);
                pos.setSquaredDistance(pos.getSquaredDistanceFrom(dx, dy, dz));
            }

            Collections.sort(this.positions);
            Collections.reverse(this.positions);
        }

        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1f, 1f, 1f, 1f);

        for (int i = 0; i < this.positions.size(); i++)
        {
            BlockPosStateDist posEU = this.positions.get(i);

            if (posEU.blockInfo != null && posEU.blockInfo.blockStateActual != null)
            {
                IBlockState stateActual = posEU.blockInfo.blockStateActual;
                BlockPos pos = posEU.toBlockPos();

                GlStateManager.pushMatrix();
                GlStateManager.enableCull();
                GlStateManager.enableDepth();
                GlStateManager.translate(posEU.posX - dx, posEU.posY - dy, posEU.posZ - dz);

                GlStateManager.color(1f, 1f, 1f, 1f);

                // Existing block
                if (this.mc.theWorld.isAirBlock(pos) == false)
                {
                    GlStateManager.translate(-0.001, -0.001, -0.001);
                    GlStateManager.scale(1.002, 1.002, 1.002);
                }

                // Translucent ghost block rendering
                if (Configs.buildersWandUseTranslucentGhostBlocks)
                {
                    IBakedModel model = this.mc.getBlockRendererDispatcher().getModelForState(stateActual);
                    int alpha = ((int)(Configs.buildersWandGhostBlockAlpha * 0xFF)) << 24;

                    GlStateManager.enableBlend();
                    GlStateManager.enableTexture2D();

                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.colorMask(false, false, false, false);
                    this.renderModel(stateActual, model, pos, alpha);

                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.depthFunc(GL11.GL_LEQUAL);
                    this.renderModel(stateActual, model, pos, alpha);

                    GlStateManager.disableBlend();
                }
                // Normal fully opaque ghost block rendering
                else
                {
                    GlStateManager.rotate(-90, 0, 1, 0);
                    Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(stateActual, 0.9f);
                }

                GlStateManager.popMatrix();
            }
        }
    }

    private void renderStackedArea(Area3D area, EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        if (posStart == null || posEnd == null)
        {
            return;
        }

        int xp = area.getXPos();
        int yp = area.getYPos();
        int zp = area.getZPos();
        int xn = area.getXNeg();
        int yn = area.getYNeg();
        int zn = area.getZNeg();
        int sx = Math.abs(posEnd.posX - posStart.posX) + 1;
        int sy = Math.abs(posEnd.posY - posStart.posY) + 1;
        int sz = Math.abs(posEnd.posZ - posStart.posZ) + 1;
        AxisAlignedBB originalBox = createEnclosingAABB(posStart, posEnd, player, partialTicks);

        GlStateManager.glLineWidth(2.0f);

        // Non-empty area on at least one axis
        if ((xp + xn + yp + yn + zp + zn) != 0)
        {
            for (int y = -yn; y <= yp; y++)
            {
                for (int x = -xn; x <= xp; x++)
                {
                    for (int z = -zn; z <= zp; z++)
                    {
                        AxisAlignedBB aabb = originalBox.offset(x * sx, y * sy, z * sz);

                        if (x != 0 || y != 0 || z != 0)
                        {
                            RenderGlobal.drawSelectionBoundingBox(aabb, 1f, 1f, 1f, 0xCC / 255f);
                        }
                        else
                        {
                            RenderGlobal.drawSelectionBoundingBox(aabb, 0.5f, 1f, 0.5f, 0xCC / 255f);
                        }
                    }
                }
            }
        }
    }

    public void renderHud(EntityPlayer player)
    {
        ItemStack stack = this.mc.thePlayer.getHeldItemMainhand();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.buildersWand)
        {
            return;
        }

        List<String> lines = new ArrayList<String>();

        this.getText(lines, stack, player);

        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int scaledY = scaledResolution.getScaledHeight();
        int lineHeight = this.mc.fontRendererObj.FONT_HEIGHT + 2;
        int y = scaledY - (lineHeight * lines.size());

        this.renderText(4, y, lines);
    }

    private void renderText(int posX, int posY, List<String> lines)
    {
        int y = posY;
        boolean useTextBackground = true;
        boolean useFontShadow = true;
        int textBgColor = 0x80000000;
        FontRenderer fontRenderer = this.mc.fontRendererObj;

        for (String line : lines)
        {
            if (useTextBackground)
            {
                Gui.drawRect(posX - 2, y - 2, posX + fontRenderer.getStringWidth(line) + 2, y + fontRenderer.FONT_HEIGHT, textBgColor);
            }

            if (useFontShadow)
            {
                this.mc.ingameGUI.drawString(fontRenderer, line, posX, y, 0xFFFFFFFF);
            }
            else
            {
                fontRenderer.drawString(line, posX, y, 0xFFFFFFFF);
            }

            y += fontRenderer.FONT_HEIGHT + 2;
        }
    }

    private void getText(List<String> lines, ItemStack stack, EntityPlayer player)
    {
        ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
        Mode mode = Mode.getMode(stack);
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
        String preIta = TextFormatting.ITALIC.toString();
        String strNo = I18n.format("enderutilities.tooltip.item.no");
        String strYes = I18n.format("enderutilities.tooltip.item.yes");
        int index = wand.getSelectionIndex(stack);
        String str;

        if (mode == Mode.REPLACE_3D)
        {
            int index1 = index;

            str = I18n.format("enderutilities.tooltip.item.build.target");
            index = wand.getSelectionIndex(stack, true);
            lines.add(str + String.format(" [%s%d/%d%s]: %s%s", preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS,
                    rst, this.getBlockTypeName(wand, stack, index, true), rst));

            str = I18n.format("enderutilities.tooltip.item.build.replacement");
            lines.add(str + String.format(" [%s%d/%d%s]: %s%s", preGreen, (index1 + 1), ItemBuildersWand.MAX_BLOCKS,
                    rst, this.getBlockTypeName(wand, stack, index1), rst));

            str = I18n.format("enderutilities.tooltip.item.build.bindmode");

            if (wand.getBindModeEnabled(stack, mode))
            {
                lines.add(String.format("%s: %s%s%s", str, preGreen, strYes, rst));
            }
            else
            {
                lines.add(String.format("%s: %s%s%s", str, preRed, strNo, rst));
            }
        }
        else if (mode.isAreaMode())
        {
            if (mode == Mode.COPY || mode == Mode.PASTE)
            {
                str = I18n.format("enderutilities.tooltip.item.template");
                String name = wand.getTemplateName(stack, mode);
                lines.add(String.format("%s [%s%d/%d%s]: %s%s%s", str, preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS, rst, preIta, name, rst));
            }
            else
            {
                str = I18n.format("enderutilities.tooltip.item.area");
                lines.add(String.format("%s: [%s%d/%d%s]", str, preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS, rst));
            }

            str = I18n.format("enderutilities.tooltip.item.rotation");

            EnumFacing facing = wand.getAreaFacing(stack, mode);
            String strFacing = facing != null ? preGreen + facing.toString().toLowerCase() : preRed + "N/A";

            if (wand.isMirrored(stack))
            {
                String mirror = wand.getMirror(stack) == Mirror.FRONT_BACK ? "x" : "z";
                str = String.format("%s: %s%s - %s: %s%s%s", str, strFacing, rst,
                        I18n.format("enderutilities.tooltip.item.mirror"), preGreen, mirror, rst);
            }
            else
            {
                str = String.format("%s: %s%s - %s: %s%s%s", str, strFacing, rst,
                        I18n.format("enderutilities.tooltip.item.mirror"), preRed, strNo, rst);
            }

            lines.add(str);

            if (mode == Mode.DELETE)
            {
                str = I18n.format("enderutilities.tooltip.item.entities");
                str += ": " + (wand.getAffectEntities(stack) ? preGreen + strYes : preRed + strNo) + rst;

                lines.add(str);
            }
            else if (mode == Mode.PASTE || mode == Mode.MOVE_DST || mode == Mode.STACK)
            {
                str = I18n.format("enderutilities.tooltip.item.replace");
                str += ": " + (wand.getReplaceExisting(stack, mode) ? preGreen + strYes : preRed + strNo) + rst;

                if (mode != Mode.MOVE_DST)
                {
                    str += " - " + I18n.format("enderutilities.tooltip.item.entities");
                    str += ": " + (wand.getAffectEntities(stack) ? preGreen + strYes : preRed + strNo) + rst;
                }

                lines.add(str);
            }
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.block");

            if (index >= 0)
            {
                lines.add(str + String.format(" [%s%d/%d%s]: %s%s", preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS,
                        rst, this.getBlockTypeName(wand, stack, index), rst));
            }
            else
            {
                lines.add(str + ": " + this.getBlockTypeName(wand, stack, index) + rst);
            }

            if (mode != Mode.WALLS && mode != Mode.CUBE)
            {
                if (mode != Mode.REPLACE)
                {
                    str = I18n.format("enderutilities.tooltip.item.area.flipped");
                    str += ": " + (wand.getAreaFlipped(stack) ? preGreen + wand.getAreaFlipAxis(stack, EnumFacing.UP) : preRed + strNo) + rst;

                    str += " - " + I18n.format("enderutilities.tooltip.item.move");
                    lines.add(str + ": " + (wand.getMovePosition(stack, mode) ? preGreen + strYes : preRed + strNo) + rst);
                }

                if (mode == Mode.COLUMN || mode == Mode.LINE || mode == Mode.PLANE || mode == Mode.EXTEND_AREA)
                {
                    str = I18n.format("enderutilities.tooltip.item.continuethrough");
                    lines.add(str + ": " + (wand.getContinueThrough(stack, mode) ? preGreen + strYes : preRed + strNo) + rst);
                }
                else if (mode == Mode.EXTEND_CONTINUOUS || (mode == Mode.REPLACE && wand.getReplaceModeIsArea(stack)))
                {
                    str = I18n.format("enderutilities.tooltip.item.builderswand.allowdiagonals");
                    lines.add(str + ": " + (wand.getAllowDiagonals(stack, mode) ? preGreen + strYes : preRed + strNo) + rst);
                }
            }
        }

        String modeName = mode.getDisplayName();
        if (mode == Mode.REPLACE)
        {
            if (wand.getReplaceModeIsArea(stack))
            {
                modeName += " (" + I18n.format("enderutilities.tooltip.item.area") + ")";
            }
            else
            {
                modeName += " (" + I18n.format("enderutilities.tooltip.item.single") + ")";
            }
        }

        int modeId = mode.ordinal() + 1;
        int maxModeId = Mode.getModeCount(player);
        str = I18n.format("enderutilities.tooltip.item.mode");
        String strMode = String.format("%s [%s%d/%d%s]: %s%s%s", str, preGreen, modeId, maxModeId, rst, preGreen, modeName, rst);

        lines.add(strMode);
    }

    private String getBlockTypeName(ItemBuildersWand wand, ItemStack stack, int index)
    {
        return this.getBlockTypeName(wand, stack, index, false);
    }

    private String getBlockTypeName(ItemBuildersWand wand, ItemStack stack, int index, boolean secondary)
    {
        if (index >= 0)
        {
            BlockInfo blockInfo = wand.getSelectedFixedBlockType(stack, secondary);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);
                if (blockStack != null && blockStack.getItem() != null)
                {
                    return TextFormatting.GREEN.toString() + blockStack.getDisplayName();
                }
            }

            return TextFormatting.RED.toString() + "N/A";
        }
        else
        {
            if (index == ItemBuildersWand.BLOCK_TYPE_TARGETED)
            {
                return TextFormatting.AQUA.toString() + I18n.format("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                return TextFormatting.AQUA.toString() + I18n.format("enderutilities.tooltip.item.blocktype.adjacent");
            }
        }
    }

    private AxisAlignedBB createEnclosingAABB(BlockPosEU posStart, BlockPosEU posEnd, EntityPlayer player, float partialTicks)
    {
        int minX = Math.min(posStart.posX, posEnd.posX);
        int minY = Math.min(posStart.posY, posEnd.posY);
        int minZ = Math.min(posStart.posZ, posEnd.posZ);
        int maxX = Math.max(posStart.posX, posEnd.posX) + 1;
        int maxY = Math.max(posStart.posY, posEnd.posY) + 1;
        int maxZ = Math.max(posStart.posZ, posEnd.posZ) + 1;

        return createAABB(minX, minY, minZ, maxX, maxY, maxZ, 0, partialTicks, player);
    }

    public static AxisAlignedBB createAABB(int x, int y, int z, double expand, double partialTicks, EntityPlayer player)
    {
        return createAABB(x, y, z, x + 1, y + 1, z + 1, expand, partialTicks, player);
    }

    public static AxisAlignedBB createAABB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, double expand, double partialTicks, EntityPlayer player)
    {
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return new AxisAlignedBB(   minX - dx - expand, minY - dy - expand, minZ - dz - expand,
                                    maxX - dx + expand, maxY - dy + expand, maxZ - dz + expand);
    }

    private void renderModel(final IBlockState state, final IBakedModel model, final BlockPos pos, final int alpha)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        for (final EnumFacing facing : EnumFacing.values())
        {
            this.renderQuads(state, pos, buffer, model.getQuads(state, facing, 0), alpha);
        }

        this.renderQuads(state, pos, buffer, model.getQuads(state, null, 0), alpha);
        tessellator.draw();
    }

    private void renderQuads(final IBlockState state, final BlockPos pos, final VertexBuffer buffer, final List<BakedQuad> quads, final int alpha)
    {
        int i = 0;
        for (final int j = quads.size(); i < j; ++i)
        {
            final BakedQuad quad = quads.get(i);
            final int color = quad.getTintIndex() == -1 ? alpha | 0xffffff : this.getTint(state, pos, alpha, quad.getTintIndex());
            LightUtil.renderQuadColor(buffer, quad, color);
        }
    }

    private int getTint(final IBlockState state, final BlockPos pos, final int alpha, final int tintIndex)
    {
        return alpha | this.mc.getBlockColors().colorMultiplier(state, null, pos, tintIndex);
    }
}
