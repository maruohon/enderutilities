package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class RulerRenderer
{
    public Minecraft mc;
    public float partialTicks;
    public float partialTicksLast;
    Map<Integer, List<BlockPosEU>> positions;
    long timeLast; // FIXME debug

    public RulerRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new HashMap<Integer, List<BlockPosEU>>();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        this.partialTicks = event.partialTicks;

        this.renderAllPairs();

        this.partialTicksLast = this.partialTicks;

        if (System.currentTimeMillis() - this.timeLast > 5010)
        {
            this.timeLast = System.currentTimeMillis();
        }
    }

    public void renderAllPairs()
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
                // We render the selected location pair last
                if (i != selected)
                {
                    BlockPosEU posStart = item.getPosition(stack, i, ItemRuler.POS_START);
                    BlockPosEU posEnd = item.getPosition(stack, i, ItemRuler.POS_END);
                    this.renderPointPair(player, posStart, posEnd, this.partialTicks);
                }
            }
        }

        BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
        BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);
        this.renderPointPair(player, posStart, posEnd, this.partialTicks);

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    public void renderPointPair(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        if (posStart != null && posEnd != null && posStart.dimension != posEnd.dimension)
        {
            return;
        }

        // Only update the positions once per game tick
        if (this.partialTicks < this.partialTicksLast)
        {
            this.updatePositions(player, posStart, posEnd);
        }

        this.renderPositions(player, posStart, posEnd, partialTicks);
        this.renderStartAndEndPositions(player, posStart, posEnd, partialTicks);
    }

    public void renderPositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
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
                //System.out.println("rendering 1");
                BlockPosEU pos = column.get(i);
                if (pos.equals(posStart) == false && (posEnd == null || posEnd.equals(pos) == false))
                {
                    //System.out.println("rendering 2");
                    AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
                    RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFFFFFF);
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
            return;
        }

        if (posStart == null)
        {
            posStart = posEnd;
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY + 1d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }
        else if (posEnd == null)
        {
            posEnd = new BlockPosEU((int)player.posX, (int)(player.posY + 1d), (int)player.posZ, player.dimension, ForgeDirection.UP.ordinal());
        }

        BlockPosEU[] pos = new BlockPosEU[] { posStart, posEnd };
        int[] done = new int[] { 0, 0, 0 };

        for (int i = 0; i < 3; i++)
        {
            BlockPosAligner aligner = new BlockPosAligner(pos[0], pos[1], player);
            //int axis = aligner.getFurthestPointIndexOnLongestAxis(axisId);
            BlockPosEU aligned = aligner.getAlignedPointAlongLongestAxis();
            int furthest = aligner.furthestPoint;

            List<BlockPosEU> list = null;

            if (aligner.axisLength > 0)
            {
                // generate block positions here

                //List<BlockPosEU> list = this.getColumn(aligned, pos[furthest], aligner.longestAxis, includeStart, false);
                // The column starts from the nearest point, offset the start position by one
                boolean includeStart = aligned.equals(pos[0]) == false && aligned.equals(pos[1]) == false;
                list = this.getColumn(aligned, pos[furthest], aligner.longestAxis, includeStart, false);
                this.positions.put(aligner.longestAxis, list);
                done[aligner.longestAxis] = 1;

                //this.positions.put(aligner.longestAxis, list);
            }
            else
            {
                //this.positions.put(aligner.longestAxis, new ArrayList<BlockPosEU>());
            }

            if (System.currentTimeMillis() - this.timeLast > 5000)
            {
                String strAxis = "XYZ   ";
                strAxis = strAxis.substring(aligner.longestAxis, aligner.longestAxis + 1);
                String str = "axis: " + strAxis + " len: " + aligner.axisLength + " furthest: " + pos[furthest] + " - aligned: " + aligned;
                if (list == null || list.isEmpty())
                {
                    str = str + " empty";
                }
                EnderUtilities.logger.info(str);
            }

            /*if (aligned.equals(pos[furthest ^ 1]) == true)
            {
                this.positions.put(aligner.longestAxis, this.getColumn(aligned, pos[furthest], aligner.longestAxis, false, true));
            }
            else
            {
                this.positions.put(aligner.longestAxis, this.getColumn(aligned, pos[furthest], aligner.longestAxis, true, true));
            }*/

            pos[furthest] = aligned;
        }

        for (int i = 0; i < 3; i++)
        {
            if (done[i] == 0)
            {
                if (System.currentTimeMillis() - this.timeLast > 5000)
                {
                    String strAxis = "XYZ   ";
                    strAxis = strAxis.substring(i, i + 1);
                    System.out.println("clearing axis " + strAxis);
                }
                this.positions.remove(i);
            }
        }
    }

    public List<BlockPosEU> getColumn(BlockPosEU posNear, BlockPosEU posFar, int axis, boolean includeStart, boolean includeEnd)
    {
        List<BlockPosEU> list = new ArrayList<BlockPosEU>();

        int[] p1 = new int[] { posNear.posX, posNear.posY, posNear.posZ };
        int[] p2 = new int[] { posFar.posX, posFar.posY, posFar.posZ };
        int o1 = p1[axis];
        int o2 = p2[axis];
        int inc = p1[axis] < p2[axis] ? 1 : -1;

        if (includeStart == false)
        {
            p1[axis] += inc;
        }

        if (includeEnd == false)
        {
            p2[axis] -= inc;
        }

        /*if (Math.abs(o1 - o2) <= 0)
        {
            return list;
        }*/

        if (p1[axis] <= p2[axis])
        {
            if (System.currentTimeMillis() - this.timeLast > 5000)
            {
                //System.out.println("\ncolumn, p1 < p2: " + posNear + " - " + posFar);
            }
            for (int i = 0; i < 256 && p1[axis] <= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] += 1;
            }
        }
        else if (p1[axis] > p2[axis])
        {
            if (System.currentTimeMillis() - this.timeLast > 5000)
            {
                //System.out.println("\ncolumn, p1 > p2: " + posNear + " - " + posFar);
            }
            for (int i = 0; i < 256 && p1[axis] >= p2[axis]; i++)
            {
                list.add(new BlockPosEU(p1[0], p1[1], p1[2], posNear.dimension, posNear.face));
                p1[axis] -= 1;
            }
        }

        return list;
    }

    public class BlockPosAligner
    {
        public final int[] playerPos;
        public int longestAxis;
        public int axisLength;
        public int furthestPoint;
        public int[][] points;

        public BlockPosAligner(BlockPosEU p1, BlockPosEU p2, EntityPlayer player)
        {
            this.playerPos = new int[] { (int)(player.posX + 0.0d), (int)(player.posY - 1.0d), (int)(player.posZ + 0.0d) };
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
            this.furthestPoint = Math.abs(this.playerPos[axisId] - this.points[0][axisId]) > Math.abs(this.playerPos[axisId] - this.points[1][axisId]) ? 0 : 1;
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
