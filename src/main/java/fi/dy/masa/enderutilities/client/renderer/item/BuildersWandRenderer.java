package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class BuildersWandRenderer
{
    public Minecraft mc;
    public float partialTicksLast;
    List<BlockPosStateDist> positions;

    public BuildersWandRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new ArrayList<BlockPosStateDist>();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(this.mc.thePlayer, EnderUtilitiesItems.buildersWand);
        if (stack == null || stack.getItem() != EnderUtilitiesItems.buildersWand)
        {
            return;
        }

        this.renderSelectedArea(this.mc.theWorld, this.mc.thePlayer, stack, event.getPartialTicks());
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

    public void renderSelectedArea(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        ItemBuildersWand item = (ItemBuildersWand)stack.getItem();
        BlockPosEU posStart = item.getPosition(stack, ItemBuildersWand.POS_START);

        RayTraceResult rayTraceResult = this.mc.objectMouseOver;
        if (posStart == null && rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            // Don't allow targeting the top face of blocks while sneaking
            // This should make sneak building a platform a lot less annoying
            if (player.isSneaking() == true && rayTraceResult.sideHit == EnumFacing.UP)
            {
                return;
            }

            posStart = new BlockPosEU(rayTraceResult.getBlockPos().offset(rayTraceResult.sideHit), player.dimension, rayTraceResult.sideHit);
        }

        if (posStart == null || player.dimension != posStart.dimension)
        {
            return;
        }

        Mode mode = Mode.getMode(stack);
        BlockPosEU posEnd = (mode == Mode.WALLS || mode == Mode.CUBE || mode == Mode.COPY  || mode == Mode.PASTE) ?
                item.getPosition(stack, ItemBuildersWand.POS_END) : null;

        if (partialTicks < this.partialTicksLast)
        {
            this.positions.clear();

            if (mode == Mode.CUBE || mode == Mode.WALLS)
            {
                // We use the walls mode block positions for cube rendering as well, to save on rendering burden
                item.getBlockPositionsWalls(stack, world, this.positions, posStart, posEnd);
            }
            else if (mode == Mode.COPY || mode == Mode.PASTE)
            {
            }
            else
            {
                item.getBlockPositions(stack, world, player, this.positions, posStart);
            }
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();

        boolean renderGhostBlocks = NBTUtils.getBoolean(stack, ItemBuildersWand.WRAPPER_TAG_NAME, ItemBuildersWand.TAG_NAME_GHOST_BLOCKS);

        if (renderGhostBlocks == true)
        {
            this.renderGhostBlocks(player, partialTicks);
        }

        GlStateManager.disableTexture2D();

        if (renderGhostBlocks == false)
        {
            this.renderBlockOutlines(player, posStart, posEnd, partialTicks);
        }

        this.renderStartAndEndPositions(mode, player, posStart, posEnd, partialTicks);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);

        this.partialTicksLast = partialTicks;
    }

    public void renderBlockOutlines(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
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

    public void renderStartAndEndPositions(Mode mode, EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        // Draw the area bounding box
        if (posStart != null && posEnd != null && (mode == Mode.COPY || mode == Mode.PASTE))
        {
            int minX = Math.min(posStart.posX, posEnd.posX);
            int minY = Math.min(posStart.posY, posEnd.posY);
            int minZ = Math.min(posStart.posZ, posEnd.posZ);
            int maxX = Math.max(posStart.posX, posEnd.posX) + 1;
            int maxY = Math.max(posStart.posY, posEnd.posY) + 1;
            int maxZ = Math.max(posStart.posZ, posEnd.posZ) + 1;
            AxisAlignedBB aabb = makeBoundingBox(minX, minY, minZ, maxX, maxY, maxZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF, 0xFF, 0xFF, 0xCC);
        }

        if (posStart != null)
        {
            // Render the targeted position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = makeBlockBoundingBox(posStart.posX, posStart.posY, posStart.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF, 0x11, 0x11, 0xFF);
        }

        if (posEnd != null && (mode == Mode.WALLS || mode == Mode.CUBE || mode == Mode.COPY || mode == Mode.PASTE))
        {
            // Render the end position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = makeBlockBoundingBox(posEnd.posX, posEnd.posY, posEnd.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0x11, 0x11, 0xFF, 0xFF);
        }
    }

    public void renderGhostBlocks(EntityPlayer player, float partialTicks)
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
            BlockInfo blockInfo = pos.blockInfo;

            if (blockInfo != null && blockInfo.block != null)
            {
                IBlockState state = blockInfo.block.getStateFromMeta(blockInfo.blockMeta);

                GlStateManager.pushMatrix();
                GlStateManager.enableCull();
                GlStateManager.enableDepth();
                GlStateManager.translate(pos.posX - dx + 0.0d, pos.posY - dy + 0.0d, pos.posZ - dz + 1.0d);
                GlStateManager.translate(0, 0, -1);
                GlStateManager.rotate(-90, 0, 1, 0);

                Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, 1.0f);

                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
    }
}
