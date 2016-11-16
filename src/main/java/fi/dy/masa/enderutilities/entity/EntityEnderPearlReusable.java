package fi.dy.masa.enderutilities.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.base.EntityThrowableEU;
import fi.dy.masa.enderutilities.entity.base.IItemData;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEnderPearlReusable extends EntityThrowableEU implements IItemData
{
    private static final DataParameter<Boolean> IS_ELITE_PEARL = EntityDataManager.<Boolean>createKey(EntityEnderPearlReusable.class, DataSerializers.BOOLEAN);
    public float teleportDamage;
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
        if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode)
        {
            this.canPickUp = false;
        }
    }

    public EntityEnderPearlReusable(World world, EntityLivingBase entity, boolean isElitePearl)
    {
        this(world, entity);

        this.isElite = isElitePearl;
        this.teleportDamage = isElitePearl ? 1.0f : 2.0f;
        this.getDataManager().set(IS_ELITE_PEARL, Boolean.valueOf(this.isElite));
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.getDataManager().register(IS_ELITE_PEARL, Boolean.valueOf(false));
    }

    @Override
    protected void kill()
    {
        // Failed to add the pearl straight back to the thrower's inventory: drop the item in the world
        if (this.returnToPlayersInventory() == false)
        {
            this.dropAsItem();
        }

        super.kill();
    }

    @Override
    public void onUpdate()
    {
        // The pearl has been dismounted, try to return the item to the thrower's inventory, or drop it as an item
        if (this.getEntityWorld().isRemote == false && this.isElite && this.isBeingRidden() == false)
        {
            // Failed to add the pearl straight back to the thrower's inventory: drop the item in the world
            if (this.returnToPlayersInventory() == false)
            {
                this.dropAsItem();
            }

            this.setDead();
            return;
        }

        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult rayTraceResult)
    {
        Entity thrower = this.getThrower();

        // Thrower not found, drop the item if applicable and bail out
        if (thrower == null)
        {
            if (this.getEntityWorld().isRemote == false && this.canPickUp)
            {
                this.dropAsItem();
            }

            this.setDead();
            return;
        }

        // Don't collide with the thrower or the entities in the 'stack' with the thrower
        if (this.getEntityWorld().isRemote == false && rayTraceResult.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            if (EntityUtils.doesEntityStackContainEntity(rayTraceResult.entityHit, thrower))
            {
                return;
            }

            if (rayTraceResult.entityHit instanceof EntityPlayerMP && ((EntityPlayerMP)rayTraceResult.entityHit).isSpectator())
            {
                return;
            }

            if (rayTraceResult.entityHit instanceof EntityLivingBase)
            {
                rayTraceResult.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), 0.0f);
            }
        }
        // Don't collide with blocks without a collision box
        else if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.getBlockPos() != null)
        {
            IBlockState state = this.getEntityWorld().getBlockState(rayTraceResult.getBlockPos());
            AxisAlignedBB aabb = state.getCollisionBoundingBox(this.getEntityWorld(), rayTraceResult.getBlockPos());
            if (aabb == Block.NULL_AABB)
            {
                return;
            }
        }

        // A regular pearl lands, teleport the thrower
        if (this.isElite == false)
        {
            // If the thrower is currently riding an elite pearl, unmount the pearl
            Entity bottom = thrower.getLowestRidingEntity();
            if (bottom instanceof EntityEnderPearlReusable && bottom.isBeingRidden())
            {
                bottom.removePassengers();
            }

            if (this.getEntityWorld().isRemote == false)
            {
                TeleportEntity.entityTeleportWithProjectile(thrower, this, rayTraceResult, this.teleportDamage, true, true);
            }
        }
        // An Elite pearl lands, which is still being ridden by something (see above)
        else if (this.isBeingRidden())
        {
            Entity entity = this.getPassengers().get(0);
            this.removePassengers();

            if (this.getEntityWorld().isRemote == false)
            {
                TeleportEntity.entityTeleportWithProjectile(entity, this, rayTraceResult, this.teleportDamage, true, true);
            }
        }

        // Try to add the pearl straight back to the player's inventory
        if (this.getEntityWorld().isRemote == false && this.returnToPlayersInventory() == false)
        {
            this.dropAsItem();
        }

        this.setDead();
    }

    /**
     * Tries to return the pearl back to the thrower's inventory.
     * @return false if adding the item to the player's inventory failed and dropAsItem should be called. true if the entity can just be killed now.
     */
    public boolean returnToPlayersInventory()
    {
        if (this.canPickUp == false || this.getEntityWorld().isRemote)
        {
            return true;
        }

        Entity thrower = this.getThrower();
        int damage = (this.isElite ? 1 : 0);

        // Tried to, but failed to add the pearl straight back to the thrower's inventory
        if (thrower instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) thrower;
            if (player.inventory.addItemStackToInventory(new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage)) == false)
            {
                return false;
            }

            // This fixes the otherwise one behind lagging count on the hotbar... wtf?!
            player.sendContainerToPlayer(player.openContainer);
        }

        return true;
    }

    public void dropAsItem()
    {
        if (this.isDead)
        {
            return;
        }

        int damage = (this.isElite ? 1 : 0);
        EntityItem entityitem = new EntityItem(this.getEntityWorld(), this.posX, this.posY, this.posZ, new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, damage));

        entityitem.motionX = 0.05d * this.getEntityWorld().rand.nextGaussian();
        entityitem.motionY = 0.05d * this.getEntityWorld().rand.nextGaussian() + 0.2d;
        entityitem.motionZ = 0.05d * this.getEntityWorld().rand.nextGaussian();
        entityitem.setDefaultPickupDelay();

        this.getEntityWorld().spawnEntityInWorld(entityitem);
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
        this.getDataManager().set(IS_ELITE_PEARL, Boolean.valueOf(this.isElite));
    }

    @Override
    public int getItemMetadata(Entity entity)
    {
        return this.getDataManager().get(IS_ELITE_PEARL).booleanValue() ? 1 : 0;
    }

    @Override
    public NBTTagCompound getTagCompound(Entity entity)
    {
        return null;
    }
}