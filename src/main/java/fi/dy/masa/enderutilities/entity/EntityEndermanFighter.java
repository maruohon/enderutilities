package fi.dy.masa.enderutilities.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

import fi.dy.masa.enderutilities.entity.base.IEntityDoubleTargeting;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EntityEndermanFighter extends EntityMob implements IEntityDoubleTargeting
{
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 0.15d, 0)).setSaved(false);
    /** Counter to delay the teleportation of an enderman towards the currently attacked target */
    private int teleportDelay;
    private Entity lastEntityToAttack;
    /** The primary target entity, set by a Summon item */
    private EntityLivingBase primaryTarget;
    /** The revenge target, if someone has attacked this entity */
    private EntityLivingBase secondaryTarget;
    private UUID primaryTargetUUID;
    private UUID secondaryTargetUUID;
    private boolean activeTargetIsPrimary;
    private boolean isAggressive;
    private boolean isBeingControlled;
    private int timer;
    private int idleTimer;

    public EntityEndermanFighter(World world)
    {
        super(world);
        this.setSize(0.6f, 2.9f);
        this.stepHeight = 1.0f;
        this.primaryTarget = null;
        this.secondaryTarget = null;
        this.primaryTargetUUID = null;
        this.secondaryTargetUUID = null;
        this.isBeingControlled = false;
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, false));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        //this.targetTasks.addTask(2, new EntityEnderman.AIFindPlayer());
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0d);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35d);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7.0d);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0d);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte)0)); // isRaging; true when the entity is raging and not controlled by a summon item
        this.dataWatcher.addObject(18, new Byte((byte)0)); // isScreaming()
    }

    @Override
    public float getEyeHeight()
    {
        return 2.55f;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        if (this.primaryTargetUUID != null)
        {
            nbt.setLong("PTUUIDM", this.primaryTargetUUID.getMostSignificantBits());
            nbt.setLong("PTUUIDL", this.primaryTargetUUID.getLeastSignificantBits());
        }

        if (this.secondaryTargetUUID != null)
        {
            nbt.setLong("STUUIDM", this.secondaryTargetUUID.getMostSignificantBits());
            nbt.setLong("STUUIDL", this.secondaryTargetUUID.getLeastSignificantBits());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("PTUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("PTUUIDL", Constants.NBT.TAG_LONG))
        {
            this.primaryTargetUUID = new UUID(nbt.getLong("PTUUIDM"), nbt.getLong("PTUUIDL"));
        }

        if (nbt.hasKey("STUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("STUUIDL", Constants.NBT.TAG_LONG))
        {
            this.secondaryTargetUUID = new UUID(nbt.getLong("STUUIDM"), nbt.getLong("STUUIDL"));
        }
    }

    public boolean isPlayerHoldingSummonItem(EntityPlayer player)
    {
        if (player.getCurrentEquippedItem() != null)
        {
            ItemStack stack = player.getCurrentEquippedItem();
            if (stack.getItem() == EnderUtilitiesItems.enderSword &&
                ItemEnderSword.SwordMode.fromStack(stack) == ItemEnderSword.SwordMode.SUMMON)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setPrimaryTarget(EntityLivingBase livingBase)
    {
        //System.out.println("setPrimaryTarget(): " + livingBase);
        this.primaryTarget = livingBase;

        if (livingBase != null)
        {
            this.primaryTargetUUID = livingBase.getUniqueID();
        }
        else
        {
            this.primaryTargetUUID = null;
        }
    }

    @Override
    public void setSecondaryTarget(EntityLivingBase livingBase)
    {
        //System.out.println("setSecondaryTarget(): " + livingBase);
        this.secondaryTarget = livingBase;

        if (livingBase != null)
        {
            this.secondaryTargetUUID = livingBase.getUniqueID();
        }
        else
        {
            this.secondaryTargetUUID = null;
        }
    }

    @Override
    public EntityLivingBase getPrimaryTarget()
    {
        return this.primaryTarget;
    }

    @Override
    public EntityLivingBase getSecondaryTarget()
    {
        return this.secondaryTarget;
    }

    @Override
    public void setActiveTarget(boolean primaryIsActive)
    {
        this.activeTargetIsPrimary = primaryIsActive;
    }

    @Override
    public boolean getPrimaryTargetIsActive()
    {
        return this.activeTargetIsPrimary;
    }

    @Override
    public EntityLivingBase getActiveTargetEntity()
    {
        return this.activeTargetIsPrimary == true ? this.primaryTarget : this.secondaryTarget;
    }

    @Override
    public void setAttackTarget(EntityLivingBase livingBase)
    {
        //System.out.println("setAttackTarget(): " + livingBase);
        super.setAttackTarget(livingBase);

        if (livingBase == null)
        {
            this.setScreaming(false);
        }
        else if (livingBase instanceof EntityPlayer)
        {
            this.setScreaming(true);
            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "mob.endermen.stare", 0.5f, 1.2f);
        }
    }

    protected EntityPlayer findPlayerToAttack()
    {
        //if (this.timer == 0)
        {
            EntityPlayer player = this.getClosestVulnerablePlayer(this.posX, this.posY, this.posZ, 64.0d);

            if (player != null && this.shouldAttackPlayer(player) == true)
            {
                return player;
            }
        }

        return null;
    }

    public EntityPlayer getClosestVulnerablePlayer(double x, double y, double z, double distance)
    {
        double d4 = -1.0d;
        EntityPlayer player = null;

        for (int i = 0; i < this.worldObj.playerEntities.size(); ++i)
        {
            EntityPlayer playerTmp = (EntityPlayer)this.worldObj.playerEntities.get(i);

            if (playerTmp.capabilities.disableDamage == false && playerTmp.isEntityAlive() == true
                && this.isPlayerHoldingSummonItem(playerTmp) == false)
            {
                double d5 = playerTmp.getDistanceSq(x, y, z);
                double d6 = distance;

                if (playerTmp.isSneaking())
                {
                    d6 = distance * 0.8d;
                }

                if (playerTmp.isInvisible())
                {
                    float f = playerTmp.getArmorVisibility();

                    if (f < 0.1f)
                    {
                        f = 0.1f;
                    }

                    d6 *= (double)(0.7f * f);
                }

                if ((distance < 0.0d || d5 < d6 * d6) && (d4 == -1.0d || d5 < d4))
                {
                    d4 = d5;
                    player = playerTmp;
                }
            }
        }

        return player;
    }

    public boolean shouldAttackPlayer(EntityPlayer player)
    {
        // The fighters attack players that are not holding an Ender Sword in the Summon mode, unless they have been renamed
        // (this allows having them around without them teleporting out and attacking unless you are always holding and Ender Sword...)
        if (this.isBeingControlled == true || this.hasCustomName() == true || player.isEntityAlive() == false
            || player.capabilities.disableDamage == true || this.isPlayerHoldingSummonItem(player) == true)
        {
            return false;
        }

        return true;
    }

    private void updateIsBeingControlled()
    {
        for (int i = 0; i < this.worldObj.playerEntities.size(); ++i)
        {
            EntityPlayer playerTmp = (EntityPlayer)this.worldObj.playerEntities.get(i);

            // If there is a player holding an Ender Sword in Summon mode within 32 blocks, then this fighter won't attack players
            if (playerTmp.isEntityAlive() == true && playerTmp.getDistanceSq(this.posX, this.posY, this.posZ) < 1024.0d
                && this.isPlayerHoldingSummonItem(playerTmp) == true)
            {
                this.isBeingControlled = true;
                return;
            }
        }

        this.isBeingControlled = false;
    }

    public EntityLivingBase getLivingEntityNearbyByUUID(UUID uuid, double bbRadius)
    {
        double r = bbRadius;
        AxisAlignedBB bb = AxisAlignedBB.fromBounds(this.posX - r, this.posY - r, this.posZ - r, this.posX + r, this.posY + r, this.posZ + r);
        List<EntityLivingBase> list = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);

        return EntityUtils.findEntityByUUID(list, uuid);
    }

    private void checkTargetsNotDead()
    {
        EntityLivingBase primary = this.getPrimaryTarget();
        if (primary != null && primary.isEntityAlive() == false)
        {
            //System.out.println("primary target is dead, clearing...");
            this.setPrimaryTarget(null);

            if (this.getAttackTarget() == primary)
            {
                this.setAttackTarget(null);
            }
        }

        EntityLivingBase secondary = this.getSecondaryTarget();
        if (secondary != null && secondary.isEntityAlive() == false)
        {
            //System.out.println("secondary target is dead, clearing...");
            this.setSecondaryTarget(null);

            if (this.getAttackTarget() == secondary)
            {
                this.setAttackTarget(null);
            }
        }
    }

    private void switchTargets()
    {
        if (this.isBeingControlled == true)
        {
            if (this.getPrimaryTarget() == null && this.primaryTargetUUID != null)
            {
                this.setPrimaryTarget(this.getLivingEntityNearbyByUUID(this.primaryTargetUUID, 64.0d));
            }

            // This entity is being controlled and is currently attacking something other than the commanded target,
            // switch to the commanded target.
            if (this.getPrimaryTarget() != null && this.getAttackTarget() != this.getPrimaryTarget())
            {
                //System.out.println("is being controlled, switching to primary target");
                this.setAttackTarget(this.getPrimaryTarget());
            }
        }
        // Not controlled by a Summon item
        else
        {
            if (this.getSecondaryTarget() == null && this.secondaryTargetUUID != null)
            {
                this.setSecondaryTarget(this.getLivingEntityNearbyByUUID(this.secondaryTargetUUID, 64.0d));
            }

            // This entity is NOT being controlled at the moment, has a revenge target but is not currently attacking it,
            // switch to the revenge target.
            if (this.getSecondaryTarget() != null && this.getAttackTarget() != this.getSecondaryTarget())
            {
                //System.out.println("not being controlled, switching to secondary target");
                this.setAttackTarget(this.getSecondaryTarget());
            }
            // Not controlled, has no revenge target and is attacking a commanded target, switch to the closest player. 
            else if (this.getSecondaryTarget() == null && this.getAttackTarget() == this.getPrimaryTarget())
            {
                //System.out.println("not being controlled, switching to closest player");
                EntityPlayer player = this.findPlayerToAttack();
                if (player != null)
                {
                    this.isAggressive = true;
                    this.setAttackTarget(player);
                }
            }
        }
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.worldObj.isRemote == true)
        {
            for (int i = 0; i < 2; ++i)
            {
                double x = this.posX + (this.rand.nextDouble() - 0.5d) * (double)this.width;
                double y = this.posY + this.rand.nextDouble() * (double)this.height - 0.25d;
                double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (double)this.width;
                double vx = (this.rand.nextDouble() - 0.5d) * 2.0d;
                double vz = (this.rand.nextDouble() - 0.5d) * 2.0d;
                this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, -this.rand.nextDouble(), vz);
            }

            this.isJumping = false;
            super.onLivingUpdate();
            return;
        }

        // 1 second timer
        if (++this.timer >= 20)
        {
            this.timer = 0;
        }

        if (this.lastEntityToAttack != this.getAttackTarget())
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(attackingSpeedBoostModifier);

            if (this.getAttackTarget() != null)
            {
                iattributeinstance.applyModifier(attackingSpeedBoostModifier);
            }

            this.lastEntityToAttack = this.getAttackTarget();
        }

        if (this.isWet())
        {
            this.attackEntityFrom(DamageSource.drown, 1.0f);
        }

        if (this.isWet() || this.isBurning())
        {
            this.teleportRandomly();
        }

        if (this.isScreaming() && this.isAggressive == false && this.rand.nextInt(100) == 0)
        {
            this.setScreaming(false);
        }

        this.isJumping = false;

        if (this.isEntityAlive() == true)
        {
            this.checkTargetsNotDead();
            this.updateIsBeingControlled();
            this.switchTargets();

            if (this.getAttackTarget() != null)
            {
                this.faceEntity(this.getAttackTarget(), 100.0f, 100.0f);

                // is a player and should attack him
                if (this.getAttackTarget() instanceof EntityPlayer)
                {
                    if (this.shouldAttackPlayer((EntityPlayer)this.getAttackTarget()) == true)
                    {
                        if (this.getAttackTarget().getDistanceSqToEntity(this) < 16.0d && this.worldObj.rand.nextFloat() < 0.03f)
                        {
                            this.teleportRandomly();
                        }

                        this.setRaging(true);
                    }
                    else
                    {
                        //System.out.println("should not attack player");
                        this.setAttackTarget(null);
                        this.setRaging(false);
                    }
                }
                else
                {
                    this.setRaging(false);
                }

                if (this.getAttackTarget() != null && this.getAttackTarget().getDistanceSqToEntity(this) > 256.0d && this.teleportDelay++ >= 30 && this.teleportToEntity(this.getAttackTarget()))
                {
                    this.teleportDelay = 0;
                }
            }
            // No target entity set at the moment
            else
            {
                this.setScreaming(false);
                this.teleportDelay = 0;
            }
        }

        if (this.getAttackTarget() == null && this.isNoDespawnRequired() == false)
        {
            // Despawn ("teleport away") the entity sometime after 10 seconds of not attacking anything
            if (++this.idleTimer >= 200 && this.worldObj.rand.nextFloat() < 0.03f)
            {
                for (int i = 0; i < 16; ++i)
                {
                    float vx = (this.rand.nextFloat() - 0.5f) * 0.2f;
                    float vy = (this.rand.nextFloat() - 0.5f) * 0.2f;
                    float vz = (this.rand.nextFloat() - 0.5f) * 0.2f;
                    double x = this.posX + (this.rand.nextDouble() - 0.5d) * (double)this.width * 2.0d;
                    double y = this.posY + this.rand.nextDouble() * (double)this.height;
                    double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (double)this.width * 2.0d;
                    this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, vy, vz);
                }

                this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "mob.endermen.portal", 0.7f, 1.0f);

                this.setDead();
            }
        }
        else
        {
            this.idleTimer = 0;
        }

        super.onLivingUpdate();
    }

    /**
     * Teleport the enderman to a random nearby position
     */
    protected boolean teleportRandomly()
    {
        double d0 = this.posX + (this.rand.nextDouble() - 0.5d) * 32.0d;
        double d1 = this.posY + (this.rand.nextInt(8) - 4);
        double d2 = this.posZ + (this.rand.nextDouble() - 0.5d) * 32.0d;
        return this.teleportTo(d0, d1, d2);
    }

    /**
     * Teleport the enderman to another entity
     */
    protected boolean teleportToEntity(Entity target)
    {
        Vec3 vec3 = new Vec3(this.posX - target.posX, this.posY + (this.height / 2.0d) - target.posY + (double)target.getEyeHeight(), this.posZ - target.posZ);
        vec3 = vec3.normalize();
        double d = 16.0d;
        double x = this.posX + (this.rand.nextDouble() - 0.5d) * (d / 2) - vec3.xCoord * d;
        double y = this.posY + (this.rand.nextInt((int)d) - (int)(d / 2)) - vec3.yCoord * d;
        double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (d / 2) - vec3.zCoord * d;
        return this.teleportTo(x, y, z);
    }

    /**
     * Teleport the enderman
     */
    protected boolean teleportTo(double x, double y, double z)
    {
        EnderTeleportEvent event = new EnderTeleportEvent(this, x, y, z, 0);
        if (MinecraftForge.EVENT_BUS.post(event)){
            return false;
        }

        double oldX = this.posX;
        double oldY = this.posY;
        double oldZ = this.posZ;
        this.posX = event.targetX;
        this.posY = event.targetY;
        this.posZ = event.targetZ;

        boolean foundValidLocation = false;
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);

        if (this.worldObj.isBlockLoaded(pos))
        {
            boolean foundSolidFloor = false;

            while (foundSolidFloor == false && pos.getY() > 0)
            {
                BlockPos pos1 = pos.down();
                Block block = this.worldObj.getBlockState(pos1).getBlock();

                if (block.getMaterial().blocksMovement())
                {
                    foundSolidFloor = true;
                }
                else
                {
                    --this.posY;
                    pos = pos1;
                }
            }

            if (foundSolidFloor == true)
            {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.worldObj.isAnyLiquid(this.getEntityBoundingBox()) == false)
                {
                    foundValidLocation = true;
                }
            }
        }

        if (foundValidLocation == false)
        {
            this.setPosition(oldX, oldY, oldZ);
            return false;
        }
        else
        {
            short short1 = 128;

            for (int i = 0; i < short1; ++i)
            {
                double d6 = (double)i / ((double)short1 - 1.0d);
                float f = (this.rand.nextFloat() - 0.5f) * 0.2f;
                float f1 = (this.rand.nextFloat() - 0.5f) * 0.2f;
                float f2 = (this.rand.nextFloat() - 0.5f) * 0.2f;
                double d7 = oldX + (this.posX - oldX) * d6 + (this.rand.nextDouble() - 0.5d) * (double)this.width * 2.0d;
                double d8 = oldY + (this.posY - oldY) * d6 + this.rand.nextDouble() * (double)this.height;
                double d9 = oldZ + (this.posZ - oldZ) * d6 + (this.rand.nextDouble() - 0.5d) * (double)this.width * 2.0d;
                this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, (double)f, (double)f1, (double)f2);
            }

            this.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 0.7f, 1.0f);

            return true;
        }
    }

    @Override
    protected String getLivingSound()
    {
        return this.isScreaming() ? "mob.endermen.scream" : "mob.endermen.idle";
    }

    @Override
    protected String getHurtSound()
    {
        return "mob.endermen.hit";
    }

    @Override
    protected String getDeathSound()
    {
        return "mob.endermen.death";
    }

    @Override
    protected Item getDropItem()
    {
        return Item.getItemById(0);
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int looting)
    {
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }

        this.setScreaming(true);

        if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityLivingBase)
        {
            //System.out.println("setting secondary target");
            this.setSecondaryTarget((EntityLivingBase)source.getEntity());
            this.isAggressive = true;
        }

        if (source instanceof EntityDamageSourceIndirect)
        {
            this.isAggressive = false;

            for (int i = 0; i < 64; ++i)
            {
                if (this.teleportRandomly() == true)
                {
                    return true;
                }
            }
        }

        return super.attackEntityFrom(source, damage);
    }

    public boolean isRaging()
    {
        return this.dataWatcher.getWatchableObjectByte(16) > 0;
    }

    public void setRaging(boolean value)
    {
        this.dataWatcher.updateObject(16, Byte.valueOf((byte)(value ? 1 : 0)));
    }

    public boolean isScreaming()
    {
        return this.dataWatcher.getWatchableObjectByte(18) > 0;
    }

    public void setScreaming(boolean value)
    {
        this.dataWatcher.updateObject(18, Byte.valueOf((byte)(value ? 1 : 0)));
    }
}
