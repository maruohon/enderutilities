package fi.dy.masa.minecraft.mods.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.FMLLog;

public class TeleportEntity
{
	public static void addEnderSoundsAndParticles(EntityLiving entity)
	{
		double entX = entity.posX;
		double entY = entity.posY;
		double entZ = entity.posZ;

		World world = entity.worldObj;
		world.playSoundEffect(entX, entY, entZ, "mob.endermen.portal", 0.8F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);

		// Spawn some particles
		for (int i = 0; i < 20; i++)
		{
			double offX = (Math.random() - 0.5d) * 1.0d;
			double offY = (Math.random() - 0.5d) * 1.0d;
			double offZ = (Math.random() - 0.5d) * 1.0d;

			double velX = (Math.random() - 0.5d) * 1.0d;
			double velY = (Math.random() - 0.5d) * 1.0d;
			double velZ = (Math.random() - 0.5d) * 1.0d;
			world.spawnParticle("portal", entX + offX, entY + offY, entZ + offZ, velX, velY, velZ);
		}
	}

	public static void teleportEntityRandomly(EntityLiving entity, double maxDist)
	{
		double deltaYaw = 0.0d;
		double deltaPitch = 0.0d;
		double x = 0.0d;
		double y = 0.0d;
		double z = 0.0d;
		maxDist *= Math.random();

		// Try to find a free spot (non-colliding with blocks)
		for (int i = 0; i < 10; i++)
		{
			deltaYaw = (Math.random() * 360.0f) / (2.0d / Math.PI);
			deltaPitch = ((90.0d - (Math.random() * 180.0d)) / (2.0d * Math.PI));
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
				TeleportEntity.addEnderSoundsAndParticles(entity);
				entity.setPosition(x, y, z);
				//entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
				return;
			}
		}
	}

	public static void lassoTeleportEntity(ItemStack stack, EntityLiving entity, int dimSrc)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || ! nbt.hasKey("x") || ! nbt.hasKey("y") || ! nbt.hasKey("z") || ! nbt.hasKey("dim")
				|| entity.riddenByEntity != null || entity.ridingEntity != null)
		{
			return;
		}
		double x = (double)nbt.getInteger("x") + 0.5d;
		double y = (double)nbt.getInteger("y");
		double z = (double)nbt.getInteger("z") + 0.5d;
		int dimDst = nbt.getInteger("dim");

		TeleportEntity.teleportEntity(entity, dimSrc, dimDst, x, y, z);
	}

	public static void teleportEntity(EntityLiving entity, int dimSrc, int dimDst, double x, double y, double z)
	{
		if (entity == null || entity.worldObj.isRemote != false || entity.isDead == true)
		{
			return;
		}

		MinecraftServer minecraftserver = MinecraftServer.getServer();
		WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);
		//WorldServer worldServerDst = DimensionManager.getWorld(dimDst);
		if (worldServerDst == null)
		{
			FMLLog.warning("[Ender Utilities] teleportEntity(): worldServerDst == null");
			return;
		}

		System.out.println("Is loaded: " + worldServerDst.getChunkProvider().chunkExists((int)x >> 4, (int)z >> 4)); // FIXME debug

		IChunkProvider chunkProvider = worldServerDst.getChunkProvider();
		if (chunkProvider != null && chunkProvider.chunkExists((int)x >> 4, (int)z >> 4) == false)
		{
			//worldServerDst.theChunkProviderServer.loadChunk((int)x >> 4, (int)z >> 4);
			chunkProvider.loadChunk((int)x >> 4, (int)z >> 4);
		}

		entity.setMoveForward(0.0f);
		entity.getNavigator().clearPathEntity();

		TeleportEntity.addEnderSoundsAndParticles(entity);

		// FIXME Check for chunk loaded etc.
		if (dimSrc != dimDst)
		{
			TeleportEntity.transferEntityToDimension(entity, dimDst, x, y, z);
		}
		else
		{
			entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
		}
	}

	public static boolean transferEntityToDimension(EntityLiving entitySrc, int dimDst, double x, double y, double z)
	{
		if (entitySrc != null && entitySrc.worldObj.isRemote == false && entitySrc.isDead == false)
		{
			int dimSrc = entitySrc.dimension;

			if (dimSrc == dimDst)
			{
				return false;
			}

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
				entityDst.setVelocity(0.0d, 0.0d, 0.0d);
				worldServerDst.spawnEntityInWorld(entityDst);
			}

			// FIXME debug: this actually kills the original entity, commenting it will make clones
			entitySrc.isDead = true;

			entitySrc.worldObj.theProfiler.endSection();
			worldServerSrc.resetUpdateEntityTick();
			worldServerDst.resetUpdateEntityTick();
			entitySrc.worldObj.theProfiler.endSection();
			return true;
		}
		return false;
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
