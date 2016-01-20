package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class RulerRenderer
{
    public Minecraft mc;
    public float partialTicksLast;
    List<BlockPosEU> positions;

    public RulerRenderer()
    {
        this.mc = Minecraft.getMinecraft();
        this.positions = new ArrayList<BlockPosEU>();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        EntityPlayer player = this.mc.thePlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.ruler)
        {
            stack = InventoryUtils.getFirstMatchingItem(player.inventory, EnderUtilitiesItems.ruler);
            if (stack == null)
            {
                return;
            }
        }

        ItemRuler item = (ItemRuler)stack.getItem();
        if (item.getRenderAllLocations(stack) == true)
        {
            int selected = item.getLocationSelection(stack);
            int count = item.getLocationCount(stack);

            for (int i = 0; i < count; i++)
            {
                // We render the selected location pair last
                if (i != selected)
                {
                    BlockPosEU posStart = item.getPosition(stack, i, ItemRuler.POS_START);
                    BlockPosEU posEnd = item.getPosition(stack, i, ItemRuler.POS_END);
                    this.render(player, posStart, posEnd, event.partialTicks);
                }
            }

            BlockPosEU posStart = item.getPosition(stack, selected, ItemRuler.POS_START);
            BlockPosEU posEnd = item.getPosition(stack, selected, ItemRuler.POS_END);
            this.render(player, posStart, posEnd, event.partialTicks);
        }
    }

    public void render(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        this.updatePositions(player, posStart, posEnd);
        this.renderPositions(player, posStart, posEnd, partialTicks);
        this.renderStartAndEndPositions(player, posStart, posEnd, partialTicks);
    }

    public void updatePositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd)
    {
        this.positions.clear();
    }

    public void renderPositions(EntityPlayer player, BlockPosEU posStart, BlockPosEU posEnd, float partialTicks)
    {
        GL11.glLineWidth(2.0f);
        for (int i = 0; i < this.positions.size(); i++)
        {
            BlockPosEU pos = this.positions.get(i);
            if (pos.equals(posStart) == false && (posEnd == null || posEnd.equals(pos) == false))
            {
                AxisAlignedBB aabb = BuildersWandRenderer.makeBlockBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
                RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFFFFFF);
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
}
