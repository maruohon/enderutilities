package fi.dy.masa.enderutilities.entity;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;

public class EntityEndermanFighter extends EntityMob
{
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 6.199999809265137D, 0)).setSaved(false);
    /** Counter to delay the teleportation of an enderman towards the currently attacked target */
    private int teleportDelay;
    private Entity lastEntityToAttack;
    private boolean isAggressive;
    private int timer;

    public EntityEndermanFighter(World world)
    {
        super(world);
        this.setSize(0.6f, 2.9f);
        this.stepHeight = 1.0f;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0d);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.4d);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(9.0d);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte)0)); // isRaging; true when the entity is raging and not controlled by a summon item
        this.dataWatcher.addObject(18, new Byte((byte)0)); // isScreaming()
    }

    /*@Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        super.writeEntityToNBT(p_70014_1_);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        super.readEntityFromNBT(p_70037_1_);
    }*/

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

    public EntityPlayer getClosestVulnerablePlayer(double x, double y, double z, double distance)
    {
        double d4 = -1.0D;
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
                    d6 = distance * 0.800000011920929D;
                }

                if (playerTmp.isInvisible())
                {
                    float f = playerTmp.getArmorVisibility();

                    if (f < 0.1F)
                    {
                        f = 0.1F;
                    }

                    d6 *= (double)(0.7F * f);
                }

                if ((distance < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    player = playerTmp;
                }
            }
        }

        return player;
    }

    @Override
    protected Entity findPlayerToAttack()
    {
        if (this.timer == 0)
        {
            EntityPlayer player = this.getClosestVulnerablePlayer(this.posX, this.posY, this.posZ, 32.0d);

            if (player != null)
            {
                if (this.shouldAttackPlayer(player) == true)
                {
                    this.isAggressive = true;
                    this.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "mob.endermen.stare", 1.0F, 1.0F);
                    this.setScreaming(true);
                    return player;
                }
            }
        }

        return null;
    }

    private boolean shouldAttackPlayer(EntityPlayer player)
    {
        // The fighters always attack player that are not holding an Ender Sword in the Summon mode
        if (this.isPlayerHoldingSummonItem(player) == true)
        {
            return false;
        }

        for (int i = 0; i < this.worldObj.playerEntities.size(); ++i)
        {
            EntityPlayer playerTmp = (EntityPlayer)this.worldObj.playerEntities.get(i);

            // If there is a player holding an Ender Sword in Summon mode within 32 blocks, then this fighter won't attack players
            if (playerTmp.isEntityAlive() == true && this.isPlayerHoldingSummonItem(playerTmp) == true
                && playerTmp.getDistanceSq(this.posX, this.posY, this.posZ) < 1024.0d)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onLivingUpdate()
    {
        // 1 second timer
        if (++this.timer >= 20)
        {
            this.timer = 0;
        }

        if (this.isWet())
        {
            this.attackEntityFrom(DamageSource.drown, 1.0F);
        }

        if (this.lastEntityToAttack != this.entityToAttack)
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            iattributeinstance.removeModifier(attackingSpeedBoostModifier);

            if (this.entityToAttack != null)
            {
                iattributeinstance.applyModifier(attackingSpeedBoostModifier);
            }
        }

        this.lastEntityToAttack = this.entityToAttack;

        for (int i = 0; i < 2; ++i)
        {
            double x = this.posX + (this.rand.nextDouble() - 0.5d) * (double)this.width;
            double y = this.posY + this.rand.nextDouble() * (double)this.height - 0.25d;
            double z = this.posZ + (this.rand.nextDouble() - 0.5d) * (double)this.width;
            double vx = (this.rand.nextDouble() - 0.5d) * 2.0d;
            double vz = (this.rand.nextDouble() - 0.5d) * 2.0d;
            this.worldObj.spawnParticle("portal", x, y, z, vx, -this.rand.nextDouble(), vz);
        }

        /*if (this.worldObj.isDaytime() && this.worldObj.isRemote == false)
        {
            float f = this.getBrightness(1.0F);

            if (f > 0.5F && this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX),
                    MathHelper.floor_double(this.posY),
                    MathHelper.floor_double(this.posZ))
                && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F)
            {
                this.entityToAttack = null;
                this.setScreaming(false);
                this.isAggressive = false;
                this.teleportRandomly();
            }
        }*/

        if (this.isWet() || this.isBurning())
        {
            this.entityToAttack = null;
            this.setScreaming(false);
            this.isAggressive = false;
            this.teleportRandomly();
        }

        if (this.isScreaming() && this.isAggressive == false && this.rand.nextInt(100) == 0)
        {
            this.setScreaming(false);
        }

        this.isJumping = false;

        if (this.entityToAttack != null)
        {
            this.faceEntity(this.entityToAttack, 100.0f, 100.0f);
        }

        if (this.worldObj.isRemote == false && this.isEntityAlive() == true)
        {
            if (this.entityToAttack != null)
            {
                if (this.entityToAttack instanceof EntityPlayer && this.shouldAttackPlayer((EntityPlayer)this.entityToAttack))
                {
                    if (this.entityToAttack.getDistanceSqToEntity(this) < 16.0D)
                    {
                        this.teleportRandomly();
                    }

                    this.teleportDelay = 0;
                    this.setRaging(true);
                }
                else if (this.entityToAttack.getDistanceSqToEntity(this) > 256.0D && this.teleportDelay++ >= 30 && this.teleportToEntity(this.entityToAttack))
                {
                    this.teleportDelay = 0;
                    this.setRaging(false);
                }
                else
                {
                    this.setRaging(false);
                }
            }
            else
            {
                this.setScreaming(false);
                this.setRaging(false);
                this.teleportDelay = 0;
            }
        }

        super.onLivingUpdate();
    }

    /**
     * Teleport the enderman to a random nearby position
     */
    protected boolean teleportRandomly()
    {
        double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double d1 = this.posY + (double)(this.rand.nextInt(64) - 32);
        double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(d0, d1, d2);
    }

    /**
     * Teleport the enderman to another entity
     */
    protected boolean teleportToEntity(Entity p_70816_1_)
    {
        Vec3 vec3 = Vec3.createVectorHelper(this.posX - p_70816_1_.posX, this.boundingBox.minY + (double)(this.height / 2.0F) - p_70816_1_.posY + (double)p_70816_1_.getEyeHeight(), this.posZ - p_70816_1_.posZ);
        vec3 = vec3.normalize();
        double d0 = 16.0D;
        double d1 = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3.xCoord * d0;
        double d2 = this.posY + (double)(this.rand.nextInt(16) - 8) - vec3.yCoord * d0;
        double d3 = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3.zCoord * d0;
        return this.teleportTo(d1, d2, d3);
    }

    /**
     * Teleport the enderman
     */
    protected boolean teleportTo(double p_70825_1_, double p_70825_3_, double p_70825_5_)
    {
        EnderTeleportEvent event = new EnderTeleportEvent(this, p_70825_1_, p_70825_3_, p_70825_5_, 0);
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
                double d6 = (double)i / ((double)short1 - 1.0D);
                float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double d7 = oldX + (this.posX - oldX) * d6 + (this.rand.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                double d8 = oldY + (this.posY - oldY) * d6 + this.rand.nextDouble() * (double)this.height;
                double d9 = oldZ + (this.posZ - oldZ) * d6 + (this.rand.nextDouble() - 0.5D) * (double)this.width * 2.0D;
                this.worldObj.spawnParticle("portal", d7, d8, d9, (double)f, (double)f1, (double)f2);
            }

            this.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 1.0F, 1.0F);
            this.playSound("mob.endermen.portal", 1.0F, 1.0F);
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
        return null;
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    }

    @Override
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
    {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else
        {
            this.setScreaming(true);

            if (p_70097_1_ instanceof EntityDamageSource && p_70097_1_.getEntity() instanceof EntityPlayer)
            {
                this.isAggressive = true;
            }

            if (p_70097_1_ instanceof EntityDamageSourceIndirect)
            {
                this.isAggressive = false;

                for (int i = 0; i < 64; ++i)
                {
                    if (this.teleportRandomly())
                    {
                        return true;
                    }
                }

                return super.attackEntityFrom(p_70097_1_, p_70097_2_);
            }
            else
            {
                return super.attackEntityFrom(p_70097_1_, p_70097_2_);
            }
        }
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
