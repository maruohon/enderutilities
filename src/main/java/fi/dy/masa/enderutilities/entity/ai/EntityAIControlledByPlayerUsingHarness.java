package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;

public class EntityAIControlledByPlayerUsingHarness extends EntityAIBase
{
    public EntityLiving entity;
    public float maxSpeed;
    public float currentSpeed;

    public EntityAIControlledByPlayerUsingHarness(EntityLiving living, float maxSpeed)
    {
        this.entity = living;
        this.setMutexBits(7);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.currentSpeed = this.maxSpeed;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        if (this.entity.riddenByEntity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)this.entity.riddenByEntity;
            this.moveEntity2(player);
        }
    }

    @Override
    public boolean shouldExecute()
    {
        Entity rider = this.entity.riddenByEntity;
        return this.entity.isEntityAlive() && rider != null && rider instanceof EntityPlayer && ((EntityPlayer)rider).inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness));
    }

    public void moveEntity1(EntityPlayer player)
    {
        float f = MathHelper.wrapAngleTo180_float(player.rotationYaw - this.entity.rotationYaw) * 0.5F;
        f = MathHelper.clamp_float(f, -5.0F, 5.0F);

        this.entity.rotationYaw = MathHelper.wrapAngleTo180_float(this.entity.rotationYaw + f);

        int x = MathHelper.floor_double(this.entity.posX);
        int y = MathHelper.floor_double(this.entity.posY);
        int z = MathHelper.floor_double(this.entity.posZ);
        float f1 = this.currentSpeed;
        float f2 = 0.91F;

        if (this.entity.onGround)
        {
            f2 *= this.entity.worldObj.getBlock(x, y - 1, z).slipperiness;
        }

        float f3 = 0.16277136F / (f2 * f2 * f2);
        float f4 = MathHelper.sin(this.entity.rotationYaw * (float)Math.PI / 180.0F);
        float f5 = MathHelper.cos(this.entity.rotationYaw * (float)Math.PI / 180.0F);
        float f6 = this.entity.getAIMoveSpeed() * f3;
        float f7 = Math.max(f1, 1.0F);
        f7 = f6 / f7;
        float f8 = f1 * f7;
        float f9 = -(f8 * f4);
        float f10 = f8 * f5;

        if (MathHelper.abs(f9) > MathHelper.abs(f10))
        {
            if (f9 < 0.0F)
            {
                f9 -= this.entity.width / 2.0F;
            }

            if (f9 > 0.0F)
            {
                f9 += this.entity.width / 2.0F;
            }

            f10 = 0.0F;
        }
        else
        {
            f9 = 0.0F;

            if (f10 < 0.0F)
            {
                f10 -= this.entity.width / 2.0F;
            }

            if (f10 > 0.0F)
            {
                f10 += this.entity.width / 2.0F;
            }
        }

        int l = MathHelper.floor_double(this.entity.posX + (double)f9);
        int i1 = MathHelper.floor_double(this.entity.posZ + (double)f10);
        PathPoint pathpoint = new PathPoint(MathHelper.floor_float(this.entity.width + 1.0F), MathHelper.floor_float(this.entity.height + player.height + 1.0F), MathHelper.floor_float(this.entity.width + 1.0F));

        if (x != l || z != i1)
        {
            Block block = this.entity.worldObj.getBlock(x, y, z);
            boolean flag = ! this.isStairsOrSlab(block) && (block.getMaterial() != Material.air || ! this.isStairsOrSlab(this.entity.worldObj.getBlock(x, y - 1, z)));

            if (flag && PathFinder.func_82565_a(this.entity, l, y, i1, pathpoint, false, false, true) == 0 && PathFinder.func_82565_a(this.entity, x, y + 1, z, pathpoint, false, false, true) == 1 && PathFinder.func_82565_a(this.entity, l, y + 1, i1, pathpoint, false, false, true) == 1)
            {
                this.entity.getJumpHelper().setJumping();
            }
        }

        this.entity.moveEntityWithHeading(0.0f, f1);
    }

    public void moveEntity2(EntityPlayer player)
    {
        this.entity.prevRotationYaw = this.entity.rotationYaw = player.rotationYaw % 360.0F;
        this.entity.rotationPitch = (player.rotationPitch * 0.5F) % 360.0F;
        this.entity.rotationYawHead = this.entity.renderYawOffset = this.entity.rotationYaw;
        float strafe = player.moveStrafing * 0.5F;
        float forward = player.moveForward;

        if (forward <= 0.0F)
        {
            forward *= 0.25F;
        }

        this.entity.stepHeight = 1.0F;
        this.entity.jumpMovementFactor = this.entity.getAIMoveSpeed() * 0.1F;

        if (this.entity.worldObj.isRemote == false)
        {
            this.entity.setAIMoveSpeed((float)this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            this.entity.moveEntityWithHeading(strafe, forward);
        }

        this.entity.prevLimbSwingAmount = this.entity.limbSwingAmount;
        double d1 = this.entity.posX - this.entity.prevPosX;
        double d0 = this.entity.posZ - this.entity.prevPosZ;
        float f4 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        this.entity.limbSwingAmount += (f4 - this.entity.limbSwingAmount) * 0.4F;
        this.entity.limbSwing += this.entity.limbSwingAmount;
    }

    public boolean isStairsOrSlab(Block block)
    {
        return block.getRenderType() == 10 || block instanceof BlockSlab;
    }
}
