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
		List<EntityPlayerMP> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

		for (EntityPlayerMP player : playerList)
		{
			if (player.getUniqueID().equals(uuid) == true)
			{
				return player;
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
