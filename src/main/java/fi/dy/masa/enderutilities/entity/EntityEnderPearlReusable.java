package fi.dy.masa.enderutilities.entity;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEnderPearlReusable extends EntityThrowable
{
	public float teleportDamage = 2.0f;
	public boolean canPickUp = true;

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
		this.setThrowableHeading(motionX, motionY, motionZ, 2.0f, 1.0f);
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
	protected void onImpact(MovingObjectPosition movingObjectPosition)
	{
		if (this.worldObj.isRemote == false && this.getThrower() != null && this.getThrower() instanceof EntityPlayerMP)
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP)this.getThrower();

			if (entityplayermp.playerNetServerHandler.func_147362_b().isChannelOpen() && entityplayermp.worldObj == this.worldObj)
			{
				if (movingObjectPosition.entityHit != null)
				{
					movingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0f);
				}

				TeleportEntity.playerTeleportSelfWithProjectile(entityplayermp, this, movingObjectPosition, this.teleportDamage, true, true);
			}

			if (this.canPickUp == true)
			{
				EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ,
							new ItemStack(EnderUtilitiesItems.enderPearlReusable, 1, 0));

				Random r = new Random();
				entityitem.motionX = 0.05d * r.nextGaussian();
				entityitem.motionY = 0.05d * r.nextGaussian() + 0.2d;
				entityitem.motionZ = 0.05d * r.nextGaussian();
				entityitem.delayBeforeCanPickup = 20;
				this.worldObj.spawnEntityInWorld(entityitem);
			}

			this.setDead();
		}
	}
}