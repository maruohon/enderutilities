package fi.dy.masa.enderutilities.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EntityAIControlledByPlayerUsingHarness extends EntityAIBase
{
    public final EntityLiving entity;
    public final float maxSpeed;

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
        //System.out.printf("%s - startExecuting()\n", this.getClass().getSimpleName());
        this.setMutexBits(7); // Block most other AI tasks while riding a mob. Mostly just the swim task is allowed.
    }

    @Override
    public void resetTask()
    {
        //System.out.printf("%s - resetTask()\n", this.getClass().getSimpleName());
        this.setMutexBits(0); // Don't block other AI tasks when not running
    }

    @Override
    public void updateTask()
    {
        //System.out.printf("%s - updateTask()\n", this.getClass().getSimpleName());
        Entity top = EntityUtils.getTopEntity(this.entity);
        if (top instanceof EntityPlayer)
        {
            this.moveEntity(this.entity, (EntityPlayer)top);
        }
    }

    @Override
    public boolean shouldExecute()
    {
        //System.out.printf("%s - shouldExecute()\n", this.getClass().getSimpleName());
        Entity rider = EntityUtils.getTopEntity(this.entity);
        return this.entity.isEntityAlive() && rider != null && rider instanceof EntityPlayer &&
                ((EntityPlayer)rider).inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.MOB_HARNESS));
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

        if (entity.getEntityWorld().isRemote == false)
        {
            //float speed = (float)entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
            float speed = this.maxSpeed;
            entity.setAIMoveSpeed(speed);
            // FIXME 1.12 update: What is this new field exactly?
            entity.func_191986_a(strafe, forward, player.field_191988_bg);
        }

        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        double d1 = entity.posX - entity.prevPosX;
        double d0 = entity.posZ - entity.prevPosZ;
        float f4 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        entity.limbSwingAmount += (f4 - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }
}
