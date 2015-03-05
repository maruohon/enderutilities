package fi.dy.masa.enderutilities.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EntityEndermanFighter extends EntityMob
{
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 6.199999809265137D, 0)).setSaved(false);
    /** Counter to delay the teleportation of an enderman towards the currently attacked target */
    private int teleportDelay;
    private Entity lastEntityToAttack;
    /** The target entity set by a Summon item */
    private Entity attackTargetCommanded;
    private UUID attackTargetUUID;
    /** The revenge target, if someone has attacked this entity */
    private Entity revengeTarget;
    private UUID revengeTargetUUID;
    private boolean isAggressive;
    private boolean isBeingControlled;
    private int timer;
    private int idleTimer;

    public EntityEndermanFighter(World world)
    {
        super(world);
        this.setSize(0.6f, 2.9f);
        this.stepHeight = 1.0f;
        this.attackTargetCommanded = null;
        this.attackTargetUUID = null;
        this.revengeTarget = null;
        this.revengeTargetUUID = null;
        this.isBeingControlled = false;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0d);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.4d);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0d);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte)0)); // isRaging; true when the entity is raging and not controlled by a summon item
        this.dataWatcher.addObject(18, new Byte((byte)0)); // isScreaming()
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        if (this.revengeTargetUUID != null)
        {
            nbt.setLong("RTUUIDM", this.revengeTargetUUID.getMostSignificantBits());
            nbt.setLong("RTUUIDL", this.revengeTargetUUID.getLeastSignificantBits());
        }

        if (this.attackTargetUUID != null)
        {
            nbt.setLong("ATUUIDM", this.attackTargetUUID.getMostSignificantBits());
            nbt.setLong("ATUUIDL", this.attackTargetUUID.getLeastSignificantBits());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("RTUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("RTUUIDL", Constants.NBT.TAG_LONG))
        {
            this.revengeTargetUUID = new UUID(nbt.getLong("RTUUIDM"), nbt.getLong("RTUUIDL"));
        }

        if (nbt.hasKey("ATUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("ATUUIDL", Constants.NBT.TAG_LONG))
        {
            this.attackTargetUUID = new UUID(nbt.getLong("ATUUIDM"), nbt.getLong("ATUUIDL"));
        }
    }

    public boolean isPlayerHoldingSummonItem(EntityPlayer player)
    {
        if (player.getCurrentEquippedItem() != null)
        {
            ItemStack stack = player.getCurrentEquippedItem();
            if (stack.getItem() == EnderUtilitiesItems.enderSword
                && ((ItemEnderSword)stack.getItem()).getSwordMode(stack) == ItemEnderSword.MODE_SUMMON)
            {
                return true;
            }
        }

        return false;
    }

    public void setTargetCommanded(Entity entity)
    {
        //System.out.println("setTargetCommanded(): " + entity);
        this.attackTargetCommanded = entity;

        if (entity != null)
        {
            this.attackTargetUUID = entity.getUniqueID();
        }
    }

    @Override
    public void setTarget(Entity entity)
    {
        //System.out.println("setTarget(): " + entity);
        super.setTarget(entity);

        if (entity == null)
        {
            this.setScreaming(false);
        }
        else if (entity instanceof EntityPlayer)
        {
            this.setScreaming(true);
            this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "mob.endermen.stare", 0.5f, 1.2f);
        }
    }

    @Override
    protected Entity findPlayerToAttack()
    {
        if (this.timer == 0)
        {
            EntityPlayer player = this.getClosestVulnerablePlayer(this.posX, this.posY, this.posZ, 64.0d);

            if (player != null && this.shouldAttackPlayer(player) == true)
            {
                this.isAggressive = true;
                this.setTarget(player);

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
        if (this.hasCustomNameTag() == true || player.isEntityAlive() == false
            || this.isPlayerHoldingSummonItem(player) == true || player.capabilities.disableDamage == true)
        {
            return false;
        }

        return this.isBeingControlled == false;
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

    private Entity getLivingEntityNearbyByUUID(UUID uuid, double bbRadius)
    {
        double r = bbRadius;
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.posX - r, this.posY - r, this.posZ - r, this.posX + r, this.posY + r, this.posZ + r);
        List<Entity> list = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);

        return EntityUtils.findEntityByUUID(list, uuid);
    }

    private void checkTargetsNotDead()
    {
        if (this.revengeTarget != null && this.revengeTarget.isEntityAlive() == false)
        {
            if (this.entityToAttack == this.revengeTarget)
            {
                this.setTarget(null);
            }

            //System.out.println("clearing revenge target");
            this.revengeTarget = null;
            this.revengeTargetUUID = null;
        }

        if (this.attackTargetCommanded != null && this.attackTargetCommanded.isEntityAlive() == false)
        {
            if (this.entityToAttack == this.attackTargetCommanded)
            {
                this.setTarget(null);
            }

            //System.out.println("clearing commanded target");
            this.attackTargetCommanded = null;
            this.attackTargetUUID = null;
        }
    }

    private void switchTargets()
    {
        if (this.isBeingControlled == true)
        {
            if (this.attackTargetCommanded == null && this.attackTargetUUID != null)
            {
                this.attackTargetCommanded = this.getLivingEntityNearbyByUUID(this.attackTargetUUID, 64.0d);
            }

            // This entity is being controlled and is currently attacking something other than the commanded target,
            // switch to the commanded target.
            if (this.attackTargetCommanded != null && this.entityToAttack != this.attackTargetCommanded)
            {
                //System.out.println("is controlled, switching to commanded target");
                this.setTarget(this.attackTargetCommanded);
            }
        }
        // Not controlled by a Summon item
        else
        {
            if (this.revengeTarget == null && this.revengeTargetUUID != null)
            {
                this.revengeTarget = this.getLivingEntityNearbyByUUID(this.revengeTargetUUID, 64.0d);
            }

            // This entity is NOT being controlled at the moment, has a revenge target but is not currently attacking it,
            // switch to the revenge target.
            if (this.revengeTarget != null && this.entityToAttack != this.revengeTarget)
            {
                //System.out.println("not controlled, switching to revenge target");
                this.setTarget(this.revengeTarget);
            }
            // Not controlled, has no revenge target and is attacking a commanded target, switch to the closest player. 
            else if (this.revengeTarget == null && this.entityToAttack == this.attackTargetCommanded)
            {
                //System.out.println("not controlled, switching to player");
                this.findPlayerToAttack();
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
                this.worldObj.spawnParticle("portal", x, y, z, vx, -this.rand.nextDouble(), vz);
            }

            super.onLivingUpdate();
            return;
        }

        // 1 second timer
        if (++this.timer >= 20)
        {
            this.timer = 0;
        }

        if (this.lastEntityToAttack != this.entityToAttack)
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(attackingSpeedBoostModifier);

            if (this.entityToAttack != null)
            {
                iattributeinstance.applyModifier(attackingSpeedBoostModifier);
            }

            this.lastEntityToAttack = this.entityToAttack;
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

            if (this.entityToAttack != null)
            {
                this.faceEntity(this.entityToAttack, 100.0f, 100.0f);

                // is a player and should attack him
                if (this.entityToAttack instanceof EntityPlayer)
                {
                    if (this.shouldAttackPlayer((EntityPlayer)this.entityToAttack) == true)
                    {
                        if (this.entityToAttack.getDistanceSqToEntity(this) < 16.0d && this.worldObj.rand.nextFloat() < 0.03f)
                        {
                            this.teleportRandomly();
                        }

                        this.setRaging(true);
                    }
                    else
                    {
                        //System.out.println("should not attack player");
                        this.setTarget(null);
                        this.setRaging(false);
                    }
                }
                else
                {
                    this.setRaging(false);
                }

                if (this.entityToAttack != null && this.entityToAttack.getDistanceSqToEntity(this) > 256.0d && this.teleportDelay++ >= 30 && this.teleportToEntity(this.entityToAttack))
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

        if (this.entityToAttack == null && this.isNoDespawnRequired() == false)
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
                    this.worldObj.spawnParticle("portal", x, y, z, vx, vy, vz);
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
        Vec3 vec3 = Vec3.createVectorHelper(this.posX - target.posX, this.boundingBox.minY + (this.height / 2.0d) - target.posY + (double)target.getEyeHeight(), this.posZ - target.posZ);
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
        int blockX = MathHelper.floor_double(this.posX);
        int blockY = MathHelper.floor_double(this.posY);
        int blockZ = MathHelper.floor_double(this.posZ);

        boolean foundValidLocation = false;

        if (this.worldObj.blockExists(blockX, blockY, blockZ))
        {
            boolean foundSolidFloor = false;

            while (foundSolidFloor == false && blockY > 0)
            {
                Block block = this.worldObj.getBlock(blockX, blockY - 1, blockZ);

                if (block.getMaterial().blocksMovement())
                {
                    foundSolidFloor = true;
                }
                else
                {
                    --this.posY;
                    --blockY;
                }
            }

            if (foundSolidFloor == true)
            {
                this.setPosition(this.posX, this.posY, this.posZ);

                if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && this.worldObj.isAnyLiquid(this.boundingBox) == false)
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
                this.worldObj.spawnParticle("portal", d7, d8, d9, (double)f, (double)f1, (double)f2);
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
        if (this.isEntityInvulnerable())
        {
            return false;
        }

        this.setScreaming(true);

        if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityLivingBase)
        {
            //System.out.println("setting revenge target");
            this.revengeTarget = (EntityLivingBase)source.getEntity();
            this.revengeTargetUUID = this.revengeTarget.getUniqueID();
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
