package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EntityAIControlledByPlayerUsingHarness extends EntityAIBase
{
    public EntityLiving entity;
    public float maxSpeed;

    public EntityAIControlledByPlayerUsingHarness(EntityLiving living, float maxSpeed)
    {
        this.entity = living;
        this.maxSpeed = maxSpeed;
    }

    @Override
    public boolean isInterruptible()
    {
        return false;
    }

    @Override
    public void startExecuting()
    {
        this.setMutexBits(7); // Block most other AI tasks while riding a mob. Mostly just the swim task is allowed.
    }

    @Override
    public void resetTask()
    {
        this.setMutexBits(0); // Don't block other AI tasks when not running
    }

    @Override
    public void updateTask()
    {
        Entity top = EntityUtils.getTopEntity(this.entity);
        if (top instanceof EntityPlayer)
        {
            this.moveEntity(this.entity, (EntityPlayer)top);
        }
    }

    @Override
    public boolean shouldExecute()
    {
        Entity rider = EntityUtils.getTopEntity(this.entity);
        return this.entity.isEntityAlive() && rider != null && rider instanceof EntityPlayer && ((EntityPlayer)rider).inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness));
    }

    /**
     * Moves the ridden entity based on player input
     * @param player
     */
    public void moveEntity(EntityLiving entity, EntityPlayer player)
    {
        entity.prevRotationYaw = entity.rotationYaw = player.rotationYaw % 360.0F;
        entity.rotationPitch = (player.rotationPitch * 0.5F) % 360.0F;
        entity.rotationYawHead = entity.renderYawOffset = entity.rotationYaw;
        float strafe = player.moveStrafing * 0.5F;
        float forward = player.moveForward;

        if (forward <= 0.0F)
        {
            forward *= 0.25F;
        }

        entity.stepHeight = 1.0F;
        entity.jumpMovementFactor = entity.getAIMoveSpeed() * 0.1F;

        if (entity.worldObj.isRemote == false)
        {
            //float speed = (float)entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
            float speed = this.maxSpeed;
            entity.setAIMoveSpeed(speed);
            entity.moveEntityWithHeading(strafe, forward);
        }

        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        double d1 = entity.posX - entity.prevPosX;
        double d0 = entity.posZ - entity.prevPosZ;
        float f4 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        entity.limbSwingAmount += (f4 - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }

    public boolean isStairsOrSlab(Block block)
    {
        return block.getRenderType() == 10 || block instanceof BlockSlab;
    }
}
