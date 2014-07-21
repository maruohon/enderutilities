package fi.dy.masa.enderutilities.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class EntityUtils
{
	public static EntityPlayerMP findPlayerFromUUID(UUID uuid)
	{
		if (uuid == null) { return null; }

		MinecraftServer mcs = MinecraftServer.getServer();
		if (mcs == null) { return null; }

		List<EntityPlayerMP> playerList = mcs.getConfigurationManager().playerEntityList;

		for (EntityPlayerMP player : playerList)
		{
			if (player.getUniqueID().equals(uuid) == true)
			{
				return player;
			}
		}

		return null;
	}

	public static Entity findEntityByUUID(List<Entity> list, UUID uuid)
	{
		if (uuid == null) { return null; }

		for (Entity entity : list)
		{
			if (entity.getUniqueID().equals(uuid) == true)
			{
				return entity;
			}
		}

		return null;
	}

	public static Entity getBottomEntity(Entity entity)
	{
		Entity ent;

		for (ent = entity; ent != null; ent = ent.ridingEntity)
		{
			if (ent.ridingEntity == null)
			{
				break;
			}
		}

		return ent;
	}

	public static Entity getTopEntity(Entity entity)
	{
		Entity ent;

		for (ent = entity; ent != null; ent = ent.riddenByEntity)
		{
			if (ent.riddenByEntity == null)
			{
				break;
			}
		}

		return ent;
	}

	// Check if there are any players on this entity 'stack' (pile of mounted entities)
	public static boolean doesEntityStackHavePlayers(Entity entity)
	{
		Entity ent;

		for (ent = entity; ent != null; ent = ent.ridingEntity)
		{
			if (ent instanceof EntityPlayer)
			{
				return true;
			}
		}

		for (ent = entity.riddenByEntity; ent != null; ent = ent.riddenByEntity)
		{
			if (ent instanceof EntityPlayer)
			{
				return true;
			}
		}

		return false;
	}
}
