package fi.dy.masa.enderutilities.client.renderer.item;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;

public class BuildersWandRenderer
{
    public Minecraft mc;

    public BuildersWandRenderer()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        ItemStack stack = this.mc.thePlayer.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != EnderUtilitiesItems.buildersWand)
        {
            return;
        }

        World world = this.mc.theWorld;
        EntityPlayer player = this.mc.thePlayer;
        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();

        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPushMatrix();
        //GL11.glTranslated(-player.posX, -player.posY, -player.posZ);

        switch(wand.getMode(stack))
        {
            case WALLS:
            case CUBE:
                break;
            default:
                this.renderOutlinesTargeted(world, player, stack, event.partialTicks);
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    public AxisAlignedBB makeBoundingBox(int x, int y, int z, double partialTicks, EntityPlayer player)
    {
        double offset1 = 0.000d;
        double offset2 = 1.000d;

        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return AxisAlignedBB.getBoundingBox(x - offset1 - dx, y - offset1 - dy, z - offset1 - dz, x + offset2 - dx, y + offset2 - dy, z + offset2 - dz);
    }

    public void renderOutlinesTargeted(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        BlockPosEU targeted = ((ItemBuildersWand)stack.getItem()).blockPos1.get(player.getUniqueID());

        MovingObjectPosition mop = this.mc.objectMouseOver;
        if (targeted == null && mop != null && mop.typeOfHit == MovingObjectType.BLOCK)
        {
            targeted = new BlockPosEU(mop.blockX, mop.blockY, mop.blockZ, mop.sideHit);
        }

        if (targeted == null)
        {
            return;
        }

        List<BlockPosEU> positions = ((ItemBuildersWand)stack.getItem()).getBlockPositions(stack, targeted, world, player);

        GL11.glLineWidth(6.0f);
        // Render the targeted position (first in the list) in a different color
        if (positions.size() > 0)
        {
            BlockPosEU pos = positions.get(0);
            AxisAlignedBB aabb = this.makeBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFF1111);
        }

        GL11.glLineWidth(2.0f);
        for (int i = 1; i < positions.size(); i++)
        {
            BlockPosEU pos = positions.get(i);
            AxisAlignedBB aabb = this.makeBoundingBox(pos.posX, pos.posY, pos.posZ, partialTicks, player);
            //RenderGlobal.drawOutlinedBoundingBox(aabb, 0x99FF99);
            RenderGlobal.drawOutlinedBoundingBox(aabb, 0xFFFFFF);
        }
    }
}
