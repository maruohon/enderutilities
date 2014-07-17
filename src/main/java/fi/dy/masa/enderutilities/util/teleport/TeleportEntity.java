package fi.dy.masa.enderutilities.util.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.FMLLog;
import fi.dy.masa.enderutilities.util.ItemNBTHelperTarget;

public class TeleportEntity
{
	public static void addEnderSoundsAndParticles(double x, double y, double z, World world)
	{
		world.playSoundEffect(x, y, z, "mob.endermen.portal", 0.8F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);

		// Spawn some particles
		for (int i = 0; i < 32; i++)
		{
			double offX = 0.0d;
			double offY = 0.0d;
			double offZ = 0.0d;

			double velX = (world.rand.nextFloat() - 0.5d) * 2.0d;
			double velY = (world.rand.nextFloat() - 0.5d) * 2.0d;
			double velZ = (world.rand.nextFloat() - 0.5d) * 2.0d;
			world.spawnParticle("portal", x + offX, y + offY, z + offZ, -velX, -velY, -velZ);
		}
	}

	public static boolean canTeleportEntity(Entity entity)
	{
		// TODO Add a blacklist for entities

		// Don't allow teleporting entities that are riding something
		// Note, that this means that we ARE allowed to teleport entities that are _ridden_ by something
		if (entity != null)
		{
			return entity.ridingEntity == null;
		}

		return false;
	}

	public static void teleportEntityRandomly(EntityLivingBase entity, double maxDist)
	{
		if (entity == null)
		{
			return;
		}

		// Sound and particles on the original location
		TeleportEntity.addEnderSoundsAndParticles(entity.posX, entity.posY, entity.posZ, entity.worldObj);

		// Do the actual teleportation only on the server side
		if (entity.worldObj.isRemote == true)
		{
			return;
		}

		double deltaYaw = 0.0d;
		double deltaPitch = 0.0d;
		double x = 0.0d;
		double y = 0.0d;
		double z = 0.0d;
		//maxDist *= Math.random();
		maxDist = maxDist - (Math.random() * maxDist / 2.0d);

		// Try to find a free spot (non-colliding with blocks)
		for (int i = 0; i < 10; i++)
		{
			deltaYaw = ((Math.random() * 360.0f) / 180.0d) * Math.PI;
			//deltaPitch = ((90.0d - (Math.random() * 180.0d)) / 180.0d) * Math.PI; // free range on the y-direction
			deltaPitch = ((Math.random() * 90.0d) / 180.0d) * Math.PI; // only from the same level upwards
			x = entity.posX;
			y = entity.posY;
			z = entity.posZ;
			x += Math.cos(deltaPitch) * Math.cos(deltaYaw) * maxDist;
			z += Math.cos(deltaPitch) * Math.sin(deltaYaw) * maxDist;
			y += Math.sin(deltaPitch) * maxDist;

			if (entity.worldObj.getBlock((int)x, (int)y, (int)z) == Blocks.air &&
				entity.worldObj.getBlock((int)x, (int)y + 1, (int)z) == Blocks.air)
			//if (entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty() == true)
			{
				entity.setPositionAndUpdate(x, y, z);
				//entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

				// Sound and particles on the new, destination location.
				//TODO: Since this only happens on the server side, we currently get no particles here. Maybe add custom packets for effects?
				TeleportEntity.addEnderSoundsAndParticles(x, y, z, entity.worldObj);
				return;
			}
		}
	}

	public static boolean lassoTeleportEntity(ItemStack stack, EntityLiving entity, EntityPlayer player, int dimSrc)
	{
		if (entity.riddenByEntity != null || entity.ridingEntity != null)
		{
			return false;
		}

		ItemNBTHelperTarget target = new ItemNBTHelperTarget();
		if (target.readFromNBT(stack.getTagCompound()) == false)
		{
			return false;
		}

		double x = target.posX + 0.5d;
		double y = target.posY;
		double z = target.posZ + 0.5d;

		return TeleportEntity.teleportEntity(entity, player, dimSrc, target.dimension, x, y, z);
	}

	public static boolean teleportEntity(EntityLiving entity, EntityPlayer player, int dimSrc, int dimDst, double x, double y, double z)
	{
		if (entity == null || entity.isDead == true)
		{
			return false;
		}

		// Sound and particles on the original location
		TeleportEntity.addEnderSoundsAndParticles(entity.posX, entity.posY, entity.posZ, entity.worldObj);

		if (entity.worldObj.isRemote == false)
		{
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);
			//WorldServer worldServerDst = DimensionManager.getWorld(dimDst);
			if (worldServerDst == null)
			{
				FMLLog.warning("[Ender Utilities] teleportEntity(): worldServerDst == null");
				return false;
			}

			//System.out.println("Is loaded: " + worldServerDst.getChunkProvider().chunkExists((int)x >> 4, (int)z >> 4)); // FIXME debug

			IChunkProvider chunkProvider = worldServerDst.getChunkProvider();
			if (chunkProvider == null)
			{
				return false;
			}

			if (chunkProvider.chunkExists((int)x >> 4, (int)z >> 4) == false)
			{
				//worldServerDst.theChunkProviderServer.loadChunk((int)x >> 4, (int)z >> 4);
				chunkProvider.loadChunk((int)x >> 4, (int)z >> 4);
			}

			entity.setMoveForward(0.0f);
			entity.getNavigator().clearPathEntity();

			if (dimSrc != dimDst)
			{
				return TeleportEntity.transferEntityToDimension(entity, dimDst, x, y, z);
			}
			else
			{
				entity.setPositionAndUpdate(x, y, z);
			}
		}

		// Final position
		TeleportEntity.addEnderSoundsAndParticles(x, y, z, entity.worldObj);

		return true;
	}

	public static boolean transferEntityToDimension(EntityLiving entitySrc, int dimDst, double x, double y, double z)
	{
		if (entitySrc == null || entitySrc.worldObj.isRemote == true || entitySrc.isDead == true || entitySrc.dimension == dimDst)
		{
			return false;
		}

		int dimSrc = entitySrc.dimension;

		entitySrc.worldObj.theProfiler.startSection("changeDimension");

		//WorldServer worldServerSrc = DimensionManager.getWorld(dimSrc);
		//WorldServer worldServerDst = DimensionManager.getWorld(dimDst);
		MinecraftServer minecraftserver = MinecraftServer.getServer();
		WorldServer worldServerSrc = minecraftserver.worldServerForDimension(dimSrc);
		WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);

		if (worldServerSrc == null || worldServerDst == null)
		{
			FMLLog.warning("[Ender Utilities] transferEntityToDimension(): worldServer[Src|Dst] == null");
			return false;
		}

		entitySrc.dimension = dimDst;
		entitySrc.worldObj.removeEntity(entitySrc);
		entitySrc.isDead = false;

		entitySrc.worldObj.theProfiler.startSection("reposition");
		TeleportEntity.transferEntityToWorld(entitySrc, dimSrc, worldServerSrc, worldServerDst);

		entitySrc.worldObj.theProfiler.endStartSection("reloading");
		Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);

		if (entityDst != null && entityDst.isEntityAlive() == true)
		{
			entityDst.copyDataFrom(entitySrc, true);
			entityDst.setLocationAndAngles(x, y, z, entitySrc.rotationYaw, entitySrc.rotationPitch);
			entityDst.motionX = 0.0d;
			entityDst.motionY = 0.0d;
			entityDst.motionZ = 0.0d;
			worldServerDst.spawnEntityInWorld(entityDst);
		}

		// FIXME debug: this actually kills the original entity, commenting it will make clones
		entitySrc.isDead = true;

		entitySrc.worldObj.theProfiler.endSection();
		worldServerSrc.resetUpdateEntityTick();
		worldServerDst.resetUpdateEntityTick();
		entitySrc.worldObj.theProfiler.endSection();

		TeleportEntity.addEnderSoundsAndParticles(x, y, z, entityDst.worldObj);

		return true;
	}

	public static boolean transferEntityToDimension(EntityLiving entity, int dim)
	{
		TeleportEntity.transferEntityToDimension(entity, dim, entity.posX, entity.posY, entity.posZ);
		return true;
	}

	private static void transferEntityToWorld(Entity entity, int dimSrc, WorldServer worldServerSrc, WorldServer worldServerDst)
	{
		worldServerSrc.theProfiler.startSection("placing");
		double x = entity.posX;
		double y = entity.posY;
		double z = entity.posZ;

		if (entity.isEntityAlive() == true)
		{
			x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
			z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);

			worldServerDst.spawnEntityInWorld(entity);
			entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

			// FIXME: Afaik this will kill mobs that can't naturally spawn due to gamerules, maybe take it out?
			worldServerDst.updateEntityWithOptionalForce(entity, false);
		}

		worldServerSrc.theProfiler.endSection();
		entity.setWorld(worldServerDst);
	}
}
