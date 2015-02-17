package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
        this.setMutexBits(7);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        // Don't block other AI tasks when not running
        this.setMutexBits(0);
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
            this.moveEntity(player);
        }
    }

    @Override
    public boolean shouldExecute()
    {
        Entity rider = this.entity.riddenByEntity;
        return this.entity.isEntityAlive() && rider != null && rider instanceof EntityPlayer && ((EntityPlayer)rider).inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness));
    }

    /**
     * Moves the ridden entity based on player input
     * @param player
     */
    public void moveEntity(EntityPlayer player)
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
