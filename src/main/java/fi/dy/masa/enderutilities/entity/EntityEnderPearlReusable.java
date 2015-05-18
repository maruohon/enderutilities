package fi.dy.masa.enderutilities.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.base.EntityThrowableEU;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEnderPearlReusable extends EntityThrowableEU implements IItemData
{
    public float teleportDamage = 2.0f;
    public boolean canPickUp = true;
    public boolean isElite = false;

    public EntityEnderPearlReusable(World world)
    {
        super(world);
    }

    public EntityEnderPearlReusable(World world, EntityLivingBase entity)
    {
        super(world, entity);

        // Don't drop the items when in creative mode, since currently I can't decrease (or change at all) the stackSize when in creative mode (wtf?)
        if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode == true)
        {
            this.canPickUp = false;
        }

        this.setLocationAndAngles(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);

        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        this.posY -= 0.10000000149011612d;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);

        this.setPosition(this.posX, this.posY, this.posZ);

        float f = 0.4f;
        double motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f);
        double motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f);
        double motionY = (double)(-MathHelper.sin((this.rotationPitch + this.getInaccuracy()) / 180.0f * (float)Math.PI) * f);
        this.setThrowableHeading(motionX, motionY, motionZ, 2.0f, 0.2f);
    }

    public EntityEnderPearlReusable(World world, EntityLivingBase entity, boolean isElitePearl)
    {
        this(world, entity);

        this.isElite = isElitePearl;
        this.dataWatcher.updateObject(6, (this.isElite ? (short)1 : (short)0));

        if (isElitePearl == true)
        {
            this.teleportDamage = 1.0f;
            this.motionX *= 1.3d;
            this.motionY *= 1.3d;
            this.motionZ *= 1.3d;
        }
    }

    @Override
    protected void entityInit()
    {
        this.dataWatcher.addObject(6, Short.valueOf((this.isElite ? (short)1 : (short)0)));
    }

    @Override
    protected void onImpact(MovingObjectPosition mop)
    {
        if (this.worldObj.isRemote == false)
        {
            Entity thrower = this.getThrower();

            // Don't collide with self or the entities in the 'stack' with self
            if (thrower == null || (mop.typeOfHit == MovingObjectType.ENTITY && EntityUtils.doesEntityStackContainEntity(mop.entityHit, thrower) == true))
            {
                return;
            }

            if (mop.entityHit != null && mop.entityHit instanceof EntityLivingBase)
            {
                mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), 0.0f);
            }

            // A regular pearl lands, teleport the thrower
            if (this.isElite == false)
            {
                // If the thrower is currently riding an elite pearl, unmount the pearl
                Entity bottom = EntityUtils.getBottomEntity(thrower);
                if (bottom instanceof EntityEnderPearlReusable && bottom.riddenByEntity != null)
                {
                    bottom.riddenByEntity.mountEntity(null);
                }

                TeleportEntity.entityTeleportWithProjectile(thrower, this, mop, this.teleportDamage, true, true);
            }
            // An Elite pearl lands, which is still being ridden by something
            else if (this.riddenByEntity != null)
            {
                Entity entity = this.riddenByEntity;
                this.riddenByEntity.mountEntity(null);
                TeleportEntity.entityTeleportWithProjectile(entity, this, mop, this.teleportDamage, true, true);
            }

            if (this.canPickUp == true)
            {
                int damage = (this.isElite == true ? 1 : 0);

                boolean success = false;
                // If the teleport was successful, try to add the pearl straight to the player's inventory
                if (thrower instanceof EntityPlayer)
                {
                    success = ((EntityPlayer)thrower).inventory.addItemStackToInventory(new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));
                }

                // Failed to add the pearl straight back to the player's inventory: spawn it in the world
                if (success == false)
                {
                    EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));

                    entityitem.motionX = 0.05d * this.worldObj.rand.nextGaussian();
                    entityitem.motionY = 0.05d * this.worldObj.rand.nextGaussian() + 0.2d;
                    entityitem.motionZ = 0.05d * this.worldObj.rand.nextGaussian();
                    entityitem.setPickupDelay(0);

                    this.worldObj.spawnEntityInWorld(entityitem);
                }
            }

            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        nbt.setBoolean("Elite", this.isElite);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        this.isElite = nbt.getBoolean("Elite");
        this.dataWatcher.updateObject(6, (this.isElite ? (short)1 : (short)0));
    }

    @Override
    public int getItemDamage(Entity entity)
    {
        return this.dataWatcher.getWatchableObjectShort(6);
    }

    @Override
    public NBTTagCompound getTagCompound(Entity entity)
    {
        return null;
    }
}