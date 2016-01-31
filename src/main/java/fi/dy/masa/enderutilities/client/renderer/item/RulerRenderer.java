package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.InventoryUtils;

@SideOnly(Side.CLIENT)
public class RulerRenderer
{
    public Minecraft mc;
    public float partialTicks;
    public float partialTicksLast;
    Map<Integer, List<BlockPosEU>> positions;
    public String modeStrDim;
    public String modeStrDiff;
    public static int[] colors = new int[] { 0x70FFFF, 0xFF70FF, 0xFFFF70, 0xA401CD, 0x1C1CC3, 0xD9850C, 0x13A43C, 0xED2235};

    public RulerRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new HashMap<Integer, List<BlockPosEU>>();
        this.modeStrDim = StatCollector.translateToLocal("enderutilities.tooltip.item.dimensions");
        this.modeStrDiff = StatCollector.translateToLocal("enderutilities.tooltip.item.difference");
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        this.partialTicks = event.partialTicks;

        this.renderAllPositionPairs();

        this.partialTicksLast = this.partialTicks;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.type != ElementType.ALL)
        {
            return;
        }

        this.renderHud();
    }

    public void renderHud()
    {
        EntityPlayer player = this.mc.thePlayer;
        if (player == null)
        {
            return;
        }

        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.ruler)
        {
            stack = InventoryUtils.getFirstMatchingItem(player.inventory, EnderUtilitiesItems.ruler);
            if (stack == null || ((ItemRuler)stack.getItem()).getRenderWhenUnselected(stack) == false)
            {
                return;
            }
        }

        ItemRuler item = (ItemRuler)stack.getItem();
        int selected = item.getLocationSelection(stack);

        BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
        BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);

        if (posStart == null && posEnd == null)
        {
            return;
        }

        if (posStart == null)
        {
            posStart = posEnd;
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }
        else if (posEnd == null)
        {
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }

        if ((posStart != null && posStart.dimension != player.dimension) || (posEnd != null && posEnd.dimension != player.dimension))
        {
            return;
        }

        int lenX = Math.abs(posStart.posX - posEnd.posX);
        int lenY = Math.abs(posStart.posY - posEnd.posY);
        int lenZ = Math.abs(posStart.posZ - posEnd.posZ);
        String modeStr = this.modeStrDiff;

        if (item.getDistanceMode(stack) == ItemRuler.DISTANCE_MODE_DIMENSIONS)
        {
            lenX += 1;
            lenY += 1;
            lenZ += 1;
            modeStr = this.modeStrDim;
        }

        ScaledResolution scaledResolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);

        //int scaledX = scaledResolution.getScaledWidth();
        int scaledY = scaledResolution.getScaledHeight();
        int x = 0;
        int y = scaledY - 16;

        //System.out.println("scX: " + scaledX + " scY: " + scaledY);
        //this.mc.ingameGUI.drawString(this.mc.fontRenderer, modeStr,        x + 10, y + 10, 0xffffffff);
        //this.mc.ingameGUI.drawString(this.mc.fontRenderer, modeStr + " X: " + lenX + ", Y: " + lenY + ", Z: " + lenZ, x + 10, y, 0xFF427CFF);
        this.mc.fontRenderer.drawString(modeStr + " X: " + lenX + ", Y: " + lenY + ", Z: " + lenZ, x + 10, y, 0xFF70FFFF, true);
        //this.mc.ingameGUI.drawString(this.mc.fontRenderer, "  X: " + lenX, x + 10, y + 18, 0xffffffff);
        //this.mc.ingameGUI.drawString(this.mc.fontRenderer, "  Y: " + lenY, x + 10, y + 26, 0xffffffff);
        //this.mc.ingameGUI.drawString(this.mc.fontRenderer, "  Z: " + lenZ, x + 10, y + 34, 0xffffffff);
    }

    public void renderAllPositionPairs()
    {
        EntityPlayer player = this.mc.thePlayer;
        if (player == null)
        {
            return;
        }

        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.ruler)
        {
            stack = InventoryUtils.getFirstMatchingItem(player.inventory, EnderUtilitiesItems.ruler);
            if (stack == null || ((ItemRuler)stack.getItem()).getRenderWhenUnselected(stack) == false)
            {
                return;
            }
        }

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();

        ItemRuler item = (ItemRuler)stack.getItem();
        int selected = item.getLocationSelection(stack);

        if (item.getRenderAllLocations(stack) == true)
        {
            int count = item.getLocationCount(stack);

            for (int i = 0; i < count; i++)
            {
                int color = i < colors.length ? colors[i] : 0x70FFFF;

                // We render the selected location pair last
                if (i != selected && item.getAlwaysRenderLocation(stack, i) == true)
                {
                    BlockPosEU posStart = item.getPosition(stack, i, ItemRuler.POS_START);
                    BlockPosEU posEnd = item.getPosition(stack, i, ItemRuler.POS_END);
                    this.renderPointPair(player, posStart, posEnd, color, this.partialTicks);
                }
            }
        }

        // Render the currently selected point pair in white
        BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
        BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);
        this.renderPointPair(player, posStart, posEnd, 0xFFFFFF, this.partialTicks);

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    public void renderPointPair(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, int color, float partialTicks)
    {
        if ((posStart != null && posStart.dimension != player.dimension) || (posEnd != null && posEnd.dimension != player.dimension))
        {
            return;
        }

        // Only update the positions once per game tick
        //if (this.partialTicks < this.partialTicksLast)
        {
            this.updatePositions(player, posStart, posEnd);
        }

        this.renderPositions(player, posStart, posEnd, color, partialTicks);
        this.renderStartAndEndPositions(player, posStart, posEnd, partialTicks);
    }

    public void renderPositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, int color, float partialTicks)
    {
        GL11.glLineWidth(2.0f);
        for (int a = 0; a < 3; a++)
        {
            List<BlockPosEU> column = this.positions.get(a);
            if (column == null)
            {
                continue;
            }

            for (int i = 0; i < column.size(); i++)
            {
                BlockPosEU pos = column.get(i);
                //if (pos.equals(posStart) == false && (posEnd == null || posEnd.equals(pos) == false))
                {
                    AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
                    RenderGlobal.drawOutlinedBoundingBox(aabb, color);
                }
            }
        }
    }

    public void renderStartAndEndPositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        if (posStart != null)
        {
            // Render the start position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(posStart.posX, posStart.posY, posStart.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF1111);
        }

        if (posEnd != null)
        {
            // Render the end position in a different (hilighted) color
            GL11.glLineWidth(3.0f);
            AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(posEnd.posX, posEnd.posY, posEnd.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0x1111FF);
        }
    }

    public void updatePositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd)
    {
        if (posStart == null && posEnd == null)
        {
            for (int i = 0; i < 3; i++)
            {
                this.positions.remove(i);
            }
            return;
        }

        if (posStart == null)
        {
            posStart = posEnd;
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }
        else if (posEnd == null)
        {
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY - 1.6d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }

        BlockPosEU[] pos = new BlockPosEU[] { posStart, posEnd };
        int[] done = new int[] { 0, 0, 0 };

        for (int i = 0; i < 3; i++)
        {
            BlockPosAligner aligner = new BlockPosAligner(pos[0], pos[1], player);
            BlockPosEU aligned = aligner.getAlignedPointAlongLongestAxis();
            int furthest = aligner.furthestPoint;

            if (aligner.axisLength > 0)
            {
                // We don't want to include duplicate positions
                boolean includeStart = aligned.equals(pos[0]) == false && aligned.equals(pos[1]) == false;
                this.positions.put(aligner.longestAxis, this.getColumn(aligned, pos[furthest], aligner.longestAxis, includeStart, false));
                done[aligner.longestAxis] = 1;
            }

            pos[furthest] = aligned;
        }

        for (int i = 0; i < 3; i++)
        {
            if (done[i] == 0)
            {
                this.positions.remove(i);
            }
        }
    }

    public List<BlockPosEU> getColumn(BlockPosEU posNear, BlockPosEU posFar, int axis, boolean includeStart, boolean includeEnd)
    {
        List<BlockPosEU> list = new ArrayList<BlockPosEU>();

        int[] p1 = new int[] { posNear.posX, posNear.posY, posNear.posZ };
        int[] p2 = new int[] { posFar.posX, posFar.posY, posFar.posZ };
        int inc = p1[axis] < p2[axis] ? 1 : -1;

        if (includeStart == false)
        {
            p1[axis] += inc;
        }

        if (includeEnd == false)
        {
            p2[axis] -= inc;
        }

        int maxLength = 160;

        if (p1[axis] <= p2[axis])
        {
            for (int i = 0; i < maxLength && p1[axis] <= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] += 1;
            }
        }
        else if (p1[axis] > p2[axis])
        {
            for (int i = 0; i < maxLength && p1[axis] >= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] -= 1;
            }
        }

        return list;
    }

    public class BlockPosAligner
    {
        public final double[] playerPos;
        public int longestAxis;
        public int axisLength;
        public int furthestPoint;
        public int[][] points;

        public BlockPosAligner(BlockPosEU p1, BlockPosEU p2, EntityPlayer player)
        {
            this.playerPos = new double[] { player.posX, player.posY - 1.0d, player.posZ };
            this.points = new int[][] {
                { p1.posX, p1.posY, p1.posZ },
                { p2.posX, p2.posY, p2.posZ }
            };
        }

        public int getLongestAxisLength()
        {
            this.getLongestAxis();
            return this.axisLength;
        }

        public int getLongestAxis()
        {
            int longest = 0;
            int length = Math.abs(this.points[0][0] - this.points[1][0]);

            for (int i = 1; i < 3; i++)
            {
                int tmp = Math.abs(this.points[0][i] - this.points[1][i]);
                if (tmp > length)
                {
                    longest = i;
                    length = tmp;
                }
            }

            this.longestAxis = longest;
            this.axisLength = length;

            return longest;
        }

        public int getFurthestPointIndexOnLongestAxis()
        {
            int axisId = this.getLongestAxis();
            double len0 = Math.abs(this.playerPos[axisId] - (this.points[0][axisId] + 0.5d));
            double len1 = Math.abs(this.playerPos[axisId] - (this.points[1][axisId] + 0.5d));
            this.furthestPoint = len0 > len1 ? 0 : 1;
            return this.furthestPoint;
        }

        public BlockPosEU getAlignedPointAlongLongestAxis()
        {
            int far = this.getFurthestPointIndexOnLongestAxis();
            int near = far ^ 0x1;
            int[] p = new int[] { this.points[far][0], this.points[far][1], this.points[far][2] };
            p[this.longestAxis] = this.points[near][this.longestAxis];

            return new BlockPosEU(p[0], p[1], p[2]); 
        }
    }
}
