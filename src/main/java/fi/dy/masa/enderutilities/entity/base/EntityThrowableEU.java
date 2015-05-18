package fi.dy.masa.enderutilities.entity.base;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.util.EntityUtils;

public abstract class EntityThrowableEU extends EntityThrowable
{
    public int blockX;
    public int blockY;
    public int blockZ;
    public Block inBlock;
    public EntityLivingBase thrower;
    public UUID throwerUUID;
    public int ticksInGround;
    public int ticksInAir;

    public EntityThrowableEU(World world)
    {
        super(world);
    }

    public EntityThrowableEU(World world, EntityLivingBase entity)
    {
        super(world, entity);

        this.setThrower(entity);
        this.setSize(0.25F, 0.25F);

        this.setLocationAndAngles(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);

        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        this.posY -= 0.10000000149011612d;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);

        this.setPosition(this.posX, this.posY, this.posZ);

        float f = 0.4f;
        double motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f);
        double motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f);
        double motionY = (double)(-MathHelper.sin((this.rotationPitch + this.getInaccuracy()) / 180.0f * (float)Math.PI) * f);

        this.setThrowableHeading(motionX, motionY, motionZ, this.getVelocity(), 1.0f);
    }

    @Override
    protected float getVelocity()
    {
        return 2.0f;
    }

    @Override
    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        this.onEntityUpdate();

        if (this.throwableShake > 0)
        {
            --this.throwableShake;
        }

        if (this.inGround)
        {
            if (this.worldObj.getBlockState(new BlockPos(this.blockX, this.blockY, this.blockZ)).getBlock() == this.inBlock)
            {
                if (++this.ticksInGround >= 1200)
                {
                    this.setDead();
                }

                return;
            }

            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }
        else
        {
            ++this.ticksInAir;
        }

        Vec3 vec3 = new Vec3(this.posX, this.posY, this.posZ);
        Vec3 vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        MovingObjectPosition mopImpact = this.worldObj.rayTraceBlocks(vec3, vec31);

        vec3 = new Vec3(this.posX, this.posY, this.posZ);
        vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        if (mopImpact != null)
        {
            vec31 = new Vec3(mopImpact.hitVec.xCoord, mopImpact.hitVec.yCoord, mopImpact.hitVec.zCoord);
        }

        if (this.worldObj.isRemote == false)
        {
            EntityLivingBase thrower = this.getThrower();
            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double distance = 0.0d;

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entityIter = (Entity)list.get(j);

                // This line fixes the elite pearl going through blocks:
                // The entity collision with the riding player would override the block collision.
                // We are ignoring any collisions with any of the entities in the "stack" the player is in, in case he is riding or being ridden by other entities.
                if (entityIter.canBeCollidedWith() && (thrower == null || EntityUtils.doesEntityStackContainEntity(entityIter, thrower) == false))
                {
                    double s = 0.1d;
                    AxisAlignedBB axisalignedbb = entityIter.getEntityBoundingBox().expand(s, s, s);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null)
                    {
                        double distanceTmp = vec3.distanceTo(movingobjectposition1.hitVec);

                        if (distanceTmp < distance || distance == 0.0d)
                        {
                            entity = entityIter;
                            distance = distanceTmp;
                        }
                    }
                }
            }

            if (entity != null)
            {
                mopImpact = new MovingObjectPosition(entity);
            }
        }

        if (mopImpact != null)
        {
            if (mopImpact.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.worldObj.getBlockState(mopImpact.getBlockPos()).getBlock() == Blocks.portal)
            {
                this.setInPortal();
            }
            else
            {
                this.onImpact(mopImpact);
            }
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f1) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float motionFactor = 0.99F;

        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
            {
                float f4 = 0.25F;
                this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f4, this.posY - this.motionY * (double)f4, this.posZ - this.motionZ * (double)f4, this.motionX, this.motionY, this.motionZ);
            }

            motionFactor = 0.8F;
        }

        this.motionX *= (double)motionFactor;
        this.motionY *= (double)motionFactor;
        this.motionZ *= (double)motionFactor;
        this.motionY -= (double)this.getGravityVelocity();
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setShort("xTile", (short)this.blockX);
        nbt.setShort("yTile", (short)this.blockY);
        nbt.setShort("zTile", (short)this.blockZ);
        nbt.setByte("inTile", (byte)Block.getIdFromBlock(this.inBlock));
        nbt.setByte("shake", (byte)this.throwableShake);
        nbt.setByte("inGround", (byte)(this.inGround ? 1 : 0));

        if (this.throwerUUID != null)
        {
            nbt.setLong("ownerUUIDM", this.throwerUUID.getMostSignificantBits());
            nbt.setLong("ownerUUIDL", this.throwerUUID.getLeastSignificantBits());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.blockX = nbt.getShort("xTile");
        this.blockY = nbt.getShort("yTile");
        this.blockZ = nbt.getShort("zTile");
        this.inBlock = Block.getBlockById(nbt.getByte("inTile") & 255);
        this.throwableShake = nbt.getByte("shake") & 255;
        this.inGround = nbt.getByte("inGround") == 1;

        if (nbt.hasKey("ownerUUIDM", Constants.NBT.TAG_LONG) && nbt.hasKey("ownerUUIDL", Constants.NBT.TAG_LONG))
        {
            this.throwerUUID = new UUID(nbt.getLong("ownerUUIDM"), nbt.getLong("ownerUUIDL"));
        }
    }

    public void setThrower(EntityLivingBase entity)
    {
        this.thrower = entity;
        this.throwerUUID = entity.getUniqueID();
    }

    public EntityLivingBase getThrower()
    {
        if (this.thrower == null && this.throwerUUID != null)
        {
            this.thrower = this.worldObj.getPlayerEntityByUUID(this.throwerUUID);
        }

        return this.thrower;
    }
}
