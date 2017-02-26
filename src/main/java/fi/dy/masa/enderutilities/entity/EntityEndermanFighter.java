package fi.dy.masa.enderutilities.entity;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EntityEndermanFighter extends EntityMob
{
    private static final DataParameter<Boolean> RAGING = EntityDataManager.<Boolean>createKey(EntityEndermanFighter.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SCREAMING = EntityDataManager.<Boolean>createKey(EntityEndermanFighter.class, DataSerializers.BOOLEAN);
    private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier ATTACKING_SPEED_BOOST = (new AttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.2d, 0)).setSaved(false);
    private EntityLivingBase assignedTarget;
    private EntityLivingBase revengeTarget;
    private UUID assignedTargetUUID;
    private UUID revengeTargetUUID;
    private boolean isBeingControlled;
    private int lastCreepySound;

    public EntityEndermanFighter(World world)
    {
        super(world);
        this.setSize(0.6f, 2.9f);
        this.stepHeight = 1.0f;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(6, new EntityEndermanFighter.AIDespawn(this, 200));

        this.targetTasks.addTask(1, new EntityEndermanFighter.AIAttackAssignedTarget(this, true));
        this.targetTasks.addTask(2, new EntityEndermanFighter.AIAttackRevengeTarget(this, false));
        this.targetTasks.addTask(3, new EntityEndermanFighter.AIAttackClosestPlayer(this, false));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0d);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35d);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0d);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0d);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        this.getDataManager().register(RAGING, Boolean.valueOf(false)); // isRaging; true when the entity is raging and not controlled by a summon item
        this.getDataManager().register(SCREAMING, Boolean.valueOf(false)); // isScreaming()
    }

    @Override
    public float getEyeHeight()
    {
        return 2.55f;
    }

    @Override
    protected boolean canDropLoot()
    {
        return false;
    }

    public void playEndermanSound()
    {
        if (this.ticksExisted >= this.lastCreepySound + 400)
        {
            this.lastCreepySound = this.ticksExisted;

            if (this.isSilent() == false)
            {
                this.world.playSound(this.posX, this.posY + this.getEyeHeight(), this.posZ,
                        SoundEvents.ENTITY_ENDERMEN_SCREAM, this.getSoundCategory(), 2.5F, 1.0F, false);
            }
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (SCREAMING.equals(key) && this.isScreaming() && this.world.isRemote)
        {
            this.playEndermanSound();
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target)
    {
        super.setAttackTarget(target);

        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (target == null)
        {
            this.setScreaming(false);
            iattributeinstance.removeModifier(ATTACKING_SPEED_BOOST);
        }
        else
        {
            if (iattributeinstance.hasModifier(ATTACKING_SPEED_BOOST) == false)
            {
                iattributeinstance.applyModifier(ATTACKING_SPEED_BOOST);
            }

            if (target instanceof EntityPlayer && this.isScreaming() == false)
            {
                this.setScreaming(true);
            }
        }
    }

    private boolean isPlayerHoldingSummonItem(EntityPlayer player)
    {
        if (player.getHeldItemMainhand() != null)
        {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() == EnderUtilitiesItems.enderSword &&
                ItemEnderSword.SwordMode.fromStack(stack) == ItemEnderSword.SwordMode.SUMMON)
            {
                return true;
            }
        }

        return false;
    }

    public void setAssignedTarget(@Nullable EntityLivingBase target)
    {
        this.assignedTarget = target;

        if (target != null)
        {
            this.assignedTargetUUID = target.getUniqueID();
        }
        else
        {
            this.assignedTargetUUID = null;
        }
    }

    @Override
    public void setRevengeTarget(@Nullable EntityLivingBase target)
    {
        this.revengeTarget = target;

        if (target != null)
        {
            this.revengeTargetUUID = target.getUniqueID();
        }
        else
        {
            this.revengeTargetUUID = null;
        }
    }

    public @Nullable EntityLivingBase getAssignedTarget()
    {
        if (this.assignedTarget == null)
        {
            if (this.assignedTargetUUID != null)
            {
                this.assignedTarget = (EntityLivingBase) this.getEntityFromUUID(this.assignedTargetUUID);
            }
        }
        else if (this.assignedTarget.isEntityAlive() == false)
        {
            this.assignedTarget = null;
            this.assignedTargetUUID = null;
        }

        return this.assignedTarget;
    }

    public @Nullable EntityLivingBase getRevengeTarget()
    {
        if (this.revengeTarget == null)
        {
            if (this.revengeTargetUUID != null)
            {
                this.revengeTarget = (EntityLivingBase) this.getEntityFromUUID(this.revengeTargetUUID);
            }
        }
        else if (this.revengeTarget.isEntityAlive() == false)
        {
            this.revengeTarget = null;
            this.revengeTargetUUID = null;
        }

        return this.revengeTarget;
    }

    public boolean isWithinTargetingDistance(@Nonnull Entity entity)
    {
        return this.getDistanceSqToEntity(entity) <= 1024d;
    }

    public boolean isBeingControlled()
    {
        return this.isBeingControlled;
    }

    private @Nullable Entity getEntityFromUUID(UUID uuid)
    {
        if (this.getEntityWorld() instanceof WorldServer)
        {
            return ((WorldServer) this.getEntityWorld()).getEntityFromUuid(uuid);
        }

        return null;
    }

    protected @Nullable EntityPlayer findPlayerToAttack()
    {
        //if (this.timer == 0)
        {
            EntityPlayer player = this.getClosestVulnerablePlayer(this.posX, this.posY, this.posZ, 64.0d);

            if (player != null && this.shouldAttackPlayer(player))
            {
                return player;
            }
        }

        return null;
    }

    protected @Nullable EntityPlayer getClosestVulnerablePlayer(double x, double y, double z, double distance)
    {
        double closest = -1.0d;
        EntityPlayer player = null;
        List<EntityPlayer> players = this.getEntityWorld().getPlayers(EntityPlayer.class, EntitySelectors.NOT_SPECTATING);

        for (EntityPlayer playerTmp : players)
        {
            if (playerTmp.capabilities.disableDamage == false && playerTmp.isEntityAlive() && this.isPlayerHoldingSummonItem(playerTmp) == false)
            {
                double distTmp = playerTmp.getDistanceSq(x, y, z);
                double distSight =playerTmp.isSneaking() ? distance * 0.8d : distance;

                if (playerTmp.isInvisible())
                {
                    float f = playerTmp.getArmorVisibility();

                    if (f < 0.1f)
                    {
                        f = 0.1f;
                    }

                    distSight *= (0.7d * f);
                }

                if ((distance < 0.0d || distTmp < (distSight * distSight)) && (closest == -1.0d || distTmp < closest))
                {
                    closest = distTmp;
                    player = playerTmp;
                }
            }
        }

        return player;
    }

    public boolean shouldAttackPlayer(@Nonnull EntityPlayer player)
    {
        // The fighters attack players that are not holding an Ender Sword in the Summon mode, unless they have been renamed
        // (this allows having them around without them teleporting out and attacking unless you are always holding and Ender Sword...)
        if (this.isBeingControlled || this.hasCustomName() || player.isEntityAlive() == false
            || player.capabilities.disableDamage || this.isPlayerHoldingSummonItem(player))
        {
            return false;
        }

        return true;
    }

    private void updateIsBeingControlled()
    {
        for (int i = 0; i < this.getEntityWorld().playerEntities.size(); ++i)
        {
            EntityPlayer playerTmp = (EntityPlayer)this.getEntityWorld().playerEntities.get(i);

            // If there is a player holding an Ender Sword in Summon mode within 32 blocks, then this fighter won't attack players
            if (playerTmp.isEntityAlive() && playerTmp.getDistanceSq(this.posX, this.posY, this.posZ) < 1024.0d
                && this.isPlayerHoldingSummonItem(playerTmp))
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
        AxisAlignedBB bb = new AxisAlignedBB(this.posX - r, this.posY - r, this.posZ - r, this.posX + r, this.posY + r, this.posZ + r);
        List<EntityLivingBase> list = this.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, bb);

        return EntityUtils.findEntityByUUID(list, uuid);
    }

    public static void summonFighters(World world, EntityLivingBase target, int amount)
    {
        if (target instanceof EntityEndermanFighter)
        {
            return;
        }

        double r = 16.0d;
        double x = target.posX;
        double y = target.posY;
        double z = target.posZ;
        int numReTargeted = 0;

        AxisAlignedBB bb = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);
        List<EntityEndermanFighter> list = world.getEntitiesWithinAABB(EntityEndermanFighter.class, bb);

        for (EntityEndermanFighter fighter : list)
        {
            if (fighter.getAssignedTarget() == null && fighter.hasCustomName() == false)
            {
                fighter.setAssignedTarget(target);
                numReTargeted++;
            }
        }

        if (numReTargeted >= amount)
        {
            return;
        }

        int count = numReTargeted;

        for ( ; count < amount; count++)
        {
            EntityEndermanFighter fighter = new EntityEndermanFighter(world);

            for (int i = 0; i < 16; i++)
            {
                x = target.posX - 5.0d + world.rand.nextFloat() * 10.0d;
                y = target.posY - 2.0d + world.rand.nextFloat() * 4.0d;
                z = target.posZ - 5.0d + world.rand.nextFloat() * 10.0d;

                BlockPos pos = new BlockPos(x, (int)(y - 1), z);
                IBlockState state = world.getBlockState(pos);

                fighter.setPosition(x, (int)y, z);

                if (world.getCollisionBoxes(fighter, fighter.getEntityBoundingBox()).isEmpty() &&
                    world.containsAnyLiquid(fighter.getEntityBoundingBox()) == false && state.isSideSolid(world, pos, EnumFacing.UP))
                {
                    for (int j = 0; j < 16; ++j)
                    {
                        float vx = (world.rand.nextFloat() - 0.5F) * 0.2F;
                        float vy = (world.rand.nextFloat() - 0.5F) * 0.2F;
                        float vz = (world.rand.nextFloat() - 0.5F) * 0.2F;
                        world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, vy, vz);
                    }

                    fighter.setAssignedTarget(target);
                    world.spawnEntity(fighter);
                    world.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, fighter.getSoundCategory(), 1.0F, 1.0F);

                    break;
                }
            }
        }
    }

    @Override
    public void onLivingUpdate()
    {
        if (this.world.isRemote)
        {
            for (int i = 0; i < 2; ++i)
            {
                double x = this.posX + (this.rand.nextDouble() - 0.5d) * (double)this.width;
                double y = this.posY + this.rand.nextDouble() * (double)this.height - 0.25d;
                double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (double)this.width;
                double vx = (this.rand.nextDouble() - 0.5d) * 2.0d;
                double vz = (this.rand.nextDouble() - 0.5d) * 2.0d;
                this.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, -this.rand.nextDouble(), vz);
            }
        }

        this.isJumping = false;
        super.onLivingUpdate();
    }

    @Override
    protected void updateAITasks()
    {
        if (this.isAIDisabled() == false)
        {
            if (this.isWet())
            {
                this.attackEntityFrom(DamageSource.drown, 1.0F);
            }

            if ((this.ticksExisted & 0x7) == 0)
            {
                this.updateIsBeingControlled();
            }
        }

        super.updateAITasks();
    }

    protected boolean teleportRandomly()
    {
        double x = this.posX + (this.rand.nextDouble() - 0.5d) * 32.0d;
        double y = this.posY + (this.rand.nextInt(8) - 4);
        double z = this.posZ + (this.rand.nextDouble() - 0.5d) * 32.0d;
        return this.teleportTo(x, y, z);
    }

    /*protected boolean teleportToEntity(Entity target)
    {
        Vec3d vec3 = new Vec3d(this.posX - target.posX, this.posY + (this.height / 2.0d) - target.posY + (double)target.getEyeHeight(), this.posZ - target.posZ);
        vec3 = vec3.normalize();
        double d = 16.0d;
        double x = this.posX + (this.rand.nextDouble() - 0.5d) * (d / 2) - vec3.xCoord * d;
        double y = this.posY + (this.rand.nextInt((int)d) - (int)(d / 2)) - vec3.yCoord * d;
        double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (d / 2) - vec3.zCoord * d;

        return this.teleportTo(x, y, z);
    }*/

    protected boolean teleportTo(double x, double y, double z)
    {
        EnderTeleportEvent event = new EnderTeleportEvent(this, x, y, z, 0);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return false;
        }

        boolean success = this.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (success)
        {
            this.world.playSound(null, this.prevPosX, this.prevPosY, this.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        }

        return success;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        if (this.isAIDisabled())
        {
            return null;
        }

        return this.isScreaming() ? SoundEvents.ENTITY_ENDERMEN_SCREAM : SoundEvents.ENTITY_ENDERMEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound()
    {
        return SoundEvents.ENTITY_ENDERMEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_ENDERMEN_DEATH;
    }

    @Override
    protected Item getDropItem()
    {
        return null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }

        this.setScreaming(true);

        /*if (source instanceof EntityDamageSource && source.getEntity() instanceof EntityLivingBase)
        {
            this.setRevengeTarget((EntityLivingBase) source.getEntity());
        }*/

        if (source.getEntity() == null)
        {
            for (int i = 0; i < 64; ++i)
            {
                if (this.teleportRandomly())
                {
                    return true;
                }
            }
        }

        return super.attackEntityFrom(source, damage);
    }

    public boolean isRaging()
    {
        return this.getDataManager().get(RAGING).booleanValue();
    }

    public void setRaging(boolean value)
    {
        this.getDataManager().set(RAGING, Boolean.valueOf(value));
    }

    public boolean isScreaming()
    {
        return this.getDataManager().get(SCREAMING).booleanValue();
    }

    public void setScreaming(boolean value)
    {
        this.getDataManager().set(SCREAMING, Boolean.valueOf(value));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        if (this.assignedTargetUUID != null)
        {
            nbt.setLong("ATUUIDM", this.assignedTargetUUID.getMostSignificantBits());
            nbt.setLong("ATUUIDL", this.assignedTargetUUID.getLeastSignificantBits());
        }

        if (this.revengeTargetUUID != null)
        {
            nbt.setLong("RTUUIDM", this.revengeTargetUUID.getMostSignificantBits());
            nbt.setLong("RTUUIDL", this.revengeTargetUUID.getLeastSignificantBits());
        }

        nbt.setBoolean("Raging", this.isRaging());
        nbt.setBoolean("Screaming", this.isScreaming());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("ATUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("ATUUIDL", Constants.NBT.TAG_LONG))
        {
            this.assignedTargetUUID = new UUID(nbt.getLong("ATUUIDM"), nbt.getLong("ATUUIDL"));
        }

        if (nbt.hasKey("RTUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("RTUUIDL", Constants.NBT.TAG_LONG))
        {
            this.revengeTargetUUID = new UUID(nbt.getLong("RTUUIDM"), nbt.getLong("RTUUIDL"));
        }

        this.setRaging(nbt.getBoolean("Raging"));
        this.setScreaming(nbt.getBoolean("Screaming"));
    }

    private abstract class AIAttackTarget extends EntityAITarget
    {
        protected final EntityEndermanFighter fighter;
        protected final boolean shouldBeControlled;

        public AIAttackTarget(EntityEndermanFighter fighter, boolean shouldBeControlled)
        {
            super(fighter, false, false);

            this.setMutexBits(1);
            this.fighter = fighter;
            this.shouldBeControlled = shouldBeControlled;
        }

        @Override
        public boolean shouldExecute()
        {
            if (this.fighter.hasCustomName() || this.fighter.isBeingControlled() != this.shouldBeControlled)
            {
                return false;
            }

            EntityLivingBase target = this.getTarget();
            return target != null && target.isEntityAlive();
        }

        @Override
        public boolean continueExecuting()
        {
            if (this.shouldExecute() && super.continueExecuting())
            {
                this.fighter.faceEntity(this.getTarget(), 10.0F, 10.0F);
                return true;
            }

            return false;
        }

        @Override
        public void startExecuting()
        {
            super.startExecuting();

            this.fighter.setAttackTarget(this.getTarget());
        }

        @Nullable
        protected abstract EntityLivingBase getTarget();
    }

    class AIAttackAssignedTarget extends AIAttackTarget
    {
        public AIAttackAssignedTarget(EntityEndermanFighter fighter, boolean shouldBeControlled)
        {
            super(fighter, shouldBeControlled);
        }

        @Override
        @Nullable
        protected EntityLivingBase getTarget()
        {
            return this.fighter.getAssignedTarget();
        }
    }

    class AIAttackRevengeTarget extends AIAttackTarget
    {
        public AIAttackRevengeTarget(EntityEndermanFighter fighter, boolean shouldBeControlled)
        {
            super(fighter, shouldBeControlled);
        }

        @Override
        public void startExecuting()
        {
            super.startExecuting();

            this.fighter.setRaging(true);
        }

        @Override
        public void resetTask()
        {
            super.resetTask();

            this.fighter.setRaging(false);
        }

        @Override
        @Nullable
        protected EntityLivingBase getTarget()
        {
            return this.fighter.getRevengeTarget();
        }
    }

    class AIAttackClosestPlayer extends AIAttackRevengeTarget
    {
        public AIAttackClosestPlayer(EntityEndermanFighter fighter, boolean shouldBeControlled)
        {
            super(fighter, shouldBeControlled);
        }

        @Override
        @Nullable
        protected EntityLivingBase getTarget()
        {
            return this.fighter.findPlayerToAttack();
        }
    }

    class AIDespawn extends EntityAIBase
    {
        protected final EntityEndermanFighter fighter;
        protected final int delay;
        protected int timer;

        public AIDespawn(EntityEndermanFighter fighter, int delay)
        {
            this.fighter = fighter;
            this.delay = delay;
        }

        @Override
        public boolean shouldExecute()
        {
            return this.fighter.isNoDespawnRequired() == false && this.fighter.getAttackTarget() == null;
        }

        @Override
        public void updateTask()
        {
            if (++this.timer >= this.delay)
            {
                Random rand = this.fighter.getRNG();

                if (rand.nextFloat() < 0.03f)
                {
                    for (int i = 0; i < 16; ++i)
                    {
                        float vx = (rand.nextFloat() - 0.5f) * 0.2f;
                        float vy = (rand.nextFloat() - 0.5f) * 0.2f;
                        float vz = (rand.nextFloat() - 0.5f) * 0.2f;
                        double x = this.fighter.posX + (rand.nextDouble() - 0.5d) * (double)this.fighter.width * 2.0d;
                        double y = this.fighter.posY + rand.nextDouble() * (double)this.fighter.height;
                        double z = this.fighter.posZ + (rand.nextDouble() - 0.5d) * (double)this.fighter.width * 2.0d;
                        this.fighter.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, vy, vz);
                    }

                    this.fighter.getEntityWorld().playSound(null, this.fighter.posX, this.fighter.posY, this.fighter.posZ,
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.fighter.getSoundCategory(), 0.7f, 1.0f);

                    this.fighter.setDead();
                }
            }
        }

        @Override
        public void resetTask()
        {
            this.timer = 0;
        }
    }
}
