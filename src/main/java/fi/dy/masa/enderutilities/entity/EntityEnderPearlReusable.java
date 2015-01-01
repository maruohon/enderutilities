package fi.dy.masa.enderutilities.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEnderPearlReusable extends EntityThrowable
{
    public float teleportDamage = 2.0f;
    public boolean canPickUp = true;
    public boolean letMeFly = false;

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
        double motionY = (double)(-MathHelper.sin((this.rotationPitch + this.func_70183_g()) / 180.0f * (float)Math.PI) * f);
        this.setThrowableHeading(motionX, motionY, motionZ, 2.0f, 0.2f);
    }

    public EntityEnderPearlReusable(World world, EntityLivingBase entity, boolean elite)
    {
        this(world, entity);
        this.teleportDamage = 1.0f;
        this.motionX *= 1.3d;
        this.motionY *= 1.3d;
        this.motionZ *= 1.3d;
    }

    public void setLetMeFly(boolean value)
    {
        this.letMeFly = value;
    }

    @SideOnly(Side.CLIENT)
    public EntityEnderPearlReusable(World world, double par2, double par4, double par6)
    {
        super(world, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    @Override
    protected void onImpact(MovingObjectPosition mop)
    {
        if (this.worldObj.isRemote == false && this.getThrower() != null)
        {
            Entity thrower = this.getThrower();

            // Don't collide with self, needed for Elite version of pearl, which the thrower is riding
            if (this.letMeFly == true && mop.typeOfHit == MovingObjectType.ENTITY && thrower == mop.entityHit)
            {
                return;
            }

            /*System.out.println("typeOfHit: " + mop.typeOfHit.toString());
            System.out.printf("blockN: x: %d y: %d z: %d\n", mop.blockX, mop.blockY, mop.blockZ);
            if (mop.entityHit != null)
            {
                System.out.printf("entityHit: x: %f y: %f z: %f\n", mop.entityHit.posX, mop.entityHit.posY, mop.entityHit.posZ);
            }
            if (mop.hitVec != null)
            {
                System.out.printf("hitVec: x: %f y: %f z: %f\n", mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
            }
            if (mop.hitInfo != null)
            {
                System.out.printf("hitInfo: %s\n", mop.hitInfo.toString());
            }*/

            if (mop.entityHit != null)
            {
                mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), 0.0f);
            }

            Entity bottom = EntityUtils.getBottomEntity(thrower);

            // If the player is "riding" an Ender Pearl (Elite version most likely)
            if (bottom instanceof EntityEnderPearlReusable && bottom.riddenByEntity != null && (this.letMeFly == false || bottom == this))
            {
                // Dismount the Ender Pearl ridden, if a regular pearl hits something, or when the actual ridden pearl hits something.
                // This allows throwing multiple Elite Pearls whil mid air, without the first ones dismounting the player when they land.
                bottom.riddenByEntity.mountEntity(null);

                // Elite pearl teleport needs to check that we are riding the pearl in question, to not dismount while a previous pearl impacts
                if (this.letMeFly == true)
                {
                    TeleportEntity.entityTeleportWithProjectile(thrower, this, mop, this.teleportDamage, true, true);
                }
            }

            // Regular pearl teleport
            if (this.letMeFly == false)
            {
                TeleportEntity.entityTeleportWithProjectile(thrower, this, mop, this.teleportDamage, true, true);
            }

            if (this.canPickUp == true)
            {
                int damage = (this.letMeFly == true ? 1 : 0);

                boolean success = false;
                // If the teleport was successful, try to add the pearl straight to the player's inventory
                if (thrower instanceof EntityPlayer)
                {
                    success = ((EntityPlayer)thrower).inventory.addItemStackToInventory(new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));
                }

                // Failed to add the pearl straight back to the player's inventory: spawn it in the world
                if (success == false)
                {
                    //PositionHelper pos = new PositionHelper(movingObjectPosition, this);
                    //EntityItem entityitem = new EntityItem(this.worldObj, pos.posX, pos.posY, pos.posZ, new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));

                    EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));

                    entityitem.motionX = 0.05d * this.worldObj.rand.nextGaussian();
                    entityitem.motionY = 0.05d * this.worldObj.rand.nextGaussian() + 0.2d;
                    entityitem.motionZ = 0.05d * this.worldObj.rand.nextGaussian();
                    entityitem.delayBeforeCanPickup = 0;

                    this.worldObj.spawnEntityInWorld(entityitem);
                }
            }

            this.setDead();
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }
}