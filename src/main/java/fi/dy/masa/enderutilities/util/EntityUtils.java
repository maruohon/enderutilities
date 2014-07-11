package fi.dy.masa.enderutilities.util;

import java.util.List;
import java.util.UUID;

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
}
