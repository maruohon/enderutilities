package fi.dy.masa.enderutilities.client.renderer.item;

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
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

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
        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glLineWidth(1.0f);
        GL11.glPushMatrix();
        //GL11.glTranslated(-player.posX, -player.posY, -player.posZ);

        switch(wand.getMode(stack))
        {
            case EXTEND_TARGETED:
            case EXTEND_SAME:
            case EXTEND_FIXED:
                this.renderModeExtend(world, player, stack, event.partialTicks);
                break;
            case COLUMN:
                this.renderModeColumn(world, player, stack, event.partialTicks);
                break;
            case LINE:
                this.renderModeLine(world, player, stack, event.partialTicks);
                break;
            case PLANE:
                this.renderModePlane(world, player, stack, event.partialTicks);
                break;
            case WALLS:
                this.renderModeWalls(world, player, stack, event.partialTicks);
                break;
            case CUBE:
                this.renderModeCube(world, player, stack, event.partialTicks);
                break;
            default:
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
    }

    public AxisAlignedBB makeBoundingBox(int x, int y, int z, double partialTicks, EntityPlayer player)
    {
        double offset1 = 0.005d;
        double offset2 = 1.005d;

        //double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        //double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        //double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return AxisAlignedBB.getBoundingBox(x - offset1 - dx, y - offset1 - dy, z - offset1 - dz, x + offset2 - dx, y + offset2 - dy, z + offset2 - dz);
    }

    public void renderModeExtend(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
    }

    public void renderModeColumn(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
    }

    public void renderModeLine(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        MovingObjectPosition mop = this.mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
        {
            return;
        }

        AxisAlignedBB aabb = this.makeBoundingBox(mop.blockX, mop.blockY, mop.blockZ, partialTicks, player);
        ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
        dir = dir.getRotation(ForgeDirection.UP);

        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();

        // FIXME testing code
        int max = 6;
        int step = 255 / max;
        for (int i = 0; i < max; i++)
        {
            RenderGlobal.drawOutlinedBoundingBox(aabb.offset(dir.offsetX, dir.offsetY, dir.offsetZ), (i * step) << 16 | (i * step) << 8 | (i * step));
        }
    }

    public void renderModePlane(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        MovingObjectPosition mop = this.mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
        {
            return;
        }

        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
    }

    public void renderModeWalls(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        MovingObjectPosition mop = this.mc.objectMouseOver;
        if ((mop == null || mop.typeOfHit != MovingObjectType.BLOCK) &&
            EnderUtilities.proxy.isAltKeyDown() == false)
        {
            return;
        }

        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
    }

    public void renderModeCube(World world, EntityPlayer player, ItemStack stack, float partialTicks)
    {
        MovingObjectPosition mop = this.mc.objectMouseOver;
        if ((mop == null || mop.typeOfHit != MovingObjectType.BLOCK) &&
            EnderUtilities.proxy.isAltKeyDown() == false)
        {
            return;
        }

        ItemBuildersWand wand = (ItemBuildersWand)stack.getItem();
    }
}
