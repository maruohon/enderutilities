package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;

public class BuildersWandRenderer
{
    protected final Minecraft mc;
    protected final List<BlockPosStateDist> positions;
    protected float partialTicksLast;

    public BuildersWandRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new ArrayList<BlockPosStateDist>();
    }

    public void renderSelectedArea(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
        BlockPosEU posStart = wand.getPosition(stack, ItemBuildersWand.POS_START);
        Mode mode = Mode.getMode(stack);

        RayTraceResult rayTraceResult = this.mc.objectMouseOver;
        if (posStart == null && rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            // Don't allow targeting the top face of blocks while sneaking
            // This should make sneak building a platform a lot less annoying
            if (player.isSneaking() == true && rayTraceResult.sideHit == EnumFacing.UP)
            {
                return;
            }

            // In Replace mode we want to target the pointed block, not the empty space adjacent to it
            if (mode == Mode.REPLACE)
            {
                posStart = new BlockPosEU(rayTraceResult.getBlockPos(), player.dimension, rayTraceResult.sideHit);
            }
            else
            {
                posStart = new BlockPosEU(rayTraceResult.getBlockPos().offset(rayTraceResult.sideHit), player.dimension, rayTraceResult.sideHit);
            }
        }

        if (posStart == null || player.dimension != posStart.dimension)
        {
            return;
        }

        BlockPosEU posEnd = mode.isAreaMode() || (mode == Mode.WALLS || mode == Mode.CUBE) ?
                wand.getPosition(stack, ItemBuildersWand.POS_END) : null;

        if (partialTicks < this.partialTicksLast)
        {
            this.positions.clear();

            if (mode == Mode.CUBE || mode == Mode.WALLS)
            {
                // We use the walls mode block positions for cube rendering as well, to save on rendering burden
                wand.getBlockPositionsWalls(stack, world, this.positions, posStart, posEnd);
            }
            else if (mode.isAreaMode() == false)
            {
                wand.getBlockPositions(stack, world, player, this.positions, posStart);
            }
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();

        boolean renderGhostBlocks = wand.getRenderGhostBlocks(stack, mode);

        if (renderGhostBlocks == true)
        {
            this.renderGhostBlocks(player, partialTicks);
        }

        GlStateManager.disableTexture2D();

        if (renderGhostBlocks == false && mode.isAreaMode() == false)
        {
            this.renderBlockOutlines(player, posStart, posEnd, partialTicks);
        }

        this.renderStartAndEndPositions(mode, player, posStart, posEnd, partialTicks);

        // In "Move, to" mode we also render the "Move, from" area
        if (mode == Mode.MOVE_DST)
        {
            posStart = wand.getPosition(stack, Mode.MOVE_SRC, true);
            posEnd = wand.getPosition(stack, Mode.MOVE_SRC, false);
            this.renderStartAndEndPositions(Mode.MOVE_SRC, player, posStart, posEnd, partialTicks, 0xFF, 0x55, 0x55);
        }

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);

        this.partialTicksLast = partialTicks;
    }

    private void renderBlockOutlines(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        GL11.glLineWidth(2.0f);
        for (int i = 0; i < this.positions.size(); i++)
        {
            BlockPosEU pos = this.positions.get(i);
            if (pos.equals(posStart) == false && (posEnd == null || posEnd.equals(pos) == false))
            {
                AxisAlignedBB aabb = makeBlockBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
                RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF, 0xFF, 0xFF, 0xFF);
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
            int minX = Math.min(posStart.posX, posEnd.posX);
            int minY = Math.min(posStart.posY, posEnd.posY);
            int minZ = Math.min(posStart.posZ, posEnd.posZ);
            int maxX = Math.max(posStart.posX, posEnd.posX) + 1;
            int maxY = Math.max(posStart.posY, posEnd.posY) + 1;
            int maxZ = Math.max(posStart.posZ, posEnd.posZ) + 1;
            AxisAlignedBB aabb = makeBoundingBox(minX, minY, minZ, maxX, maxY, maxZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, r, g, b, 0xCC);
        }

        if (posStart != null)
        {
            // Render the targeted position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = makeBlockBoundingBox(posStart.posX, posStart.posY, posStart.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF, 0x11, 0x11, 0xFF);
        }

        if (posEnd != null && (mode.isAreaMode() || mode == Mode.WALLS || mode == Mode.CUBE))
        {
            // Render the end position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = makeBlockBoundingBox(posEnd.posX, posEnd.posY, posEnd.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0x11, 0x11, 0xFF, 0xFF);
        }
    }

    private void renderGhostBlocks(EntityPlayer player, float partialTicks)
    {
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1f, 1f, 1f, 1f);

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

        for (int i = 0; i < this.positions.size(); i++)
        {
            BlockPosStateDist pos = this.positions.get(i);

            if (pos.blockInfo != null && pos.blockInfo.blockStateActual != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableCull();
                GlStateManager.enableDepth();
                GlStateManager.translate(pos.posX - dx + 0.0d, pos.posY - dy + 0.0d, pos.posZ - dz + 1.0d);
                GlStateManager.translate(0, 0, -1);
                GlStateManager.rotate(-90, 0, 1, 0);

                Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(pos.blockInfo.blockStateActual, 1.0f);

                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
    }

    public void renderHud()
    {
        ItemStack stack = this.mc.thePlayer.getHeldItemMainhand();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.buildersWand)
        {
            return;
        }

        List<String> lines = new ArrayList<String>();

        this.getText(lines, stack);

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

    private void getText(List<String> lines, ItemStack stack)
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

        if (mode.isAreaMode())
        {
            if (mode == Mode.DELETE || mode == Mode.MOVE_SRC || mode == Mode.MOVE_DST)
            {
                str = I18n.format("enderutilities.tooltip.item.area");
                lines.add(String.format("%s: [%s%d/%d%s]", str, preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS, rst));
            }
            else
            {
                str = I18n.format("enderutilities.tooltip.item.template");
                String name = wand.getTemplateName(stack, mode);
                lines.add(String.format("%s [%s%d/%d%s]: %s%s%s", str, preGreen, (index + 1), ItemBuildersWand.MAX_BLOCKS, rst, preIta, name, rst));
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
        int maxModeId = Mode.values().length;
        str = I18n.format("enderutilities.tooltip.item.mode");
        String strMode = String.format("%s [%s%d/%d%s]: %s%s%s", str, preGreen, modeId, maxModeId, rst, preGreen, modeName, rst);

        if (mode == Mode.PASTE || mode == Mode.MOVE_DST)
        {
            strMode += " - " + I18n.format("enderutilities.tooltip.item.replace");
            strMode += ": " + (wand.getReplaceExisting(stack, mode) ? preGreen + strYes : preRed + strNo) + rst;
        }
        else if (mode == Mode.DELETE)
        {
            strMode += " - " + I18n.format("enderutilities.tooltip.item.entities");
            strMode += ": " + (wand.getRemoveEntities(stack) ? preGreen + strYes : preRed + strNo) + rst;
        }

        lines.add(strMode);
    }

    private String getBlockTypeName(ItemBuildersWand wand, ItemStack stack, int index)
    {
        if (index >= 0)
        {
            BlockInfo blockInfo = wand.getSelectedFixedBlockType(stack);
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

    public static AxisAlignedBB makeBlockBoundingBox(int x, int y, int z, double partialTicks, EntityPlayer player)
    {
        double offset1 = 0.000d;
        double offset2 = 1.000d;

        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return new AxisAlignedBB(x - offset1 - dx, y - offset1 - dy, z - offset1 - dz, x + offset2 - dx, y + offset2 - dy, z + offset2 - dz);
    }

    public static AxisAlignedBB makeBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, double partialTicks, EntityPlayer player)
    {
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return new AxisAlignedBB(minX - dx, minY - dy, minZ - dz, maxX - dx, maxY - dy, maxZ - dz);
    }
}
