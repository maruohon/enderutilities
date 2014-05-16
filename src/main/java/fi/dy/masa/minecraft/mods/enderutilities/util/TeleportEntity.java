package fi.dy.masa.minecraft.mods.enderutilities.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

public class TeleportEntity
{
	public static boolean transferEntityToDimension(EntityLiving entity, int dim)
	{
		TeleportEntity.transferEntityToDimension(entity, dim, entity.posX, entity.posY, entity.posZ);
		return true;
	}

	public static boolean transferEntityToDimension(EntityLiving entitySrc, int dimDst, double x, double y, double z)
	{
/*
		// FIXME debug
		if (dimDst == 1)
		{
			entitySrc.travelToDimension(1);
			return true;
		}
*/
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
