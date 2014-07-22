package fi.dy.masa.enderutilities.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class FMLPlayerEventHandler
{
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
	{
		if (event != null && event.player != null)
		{
			if (event.player.riddenByEntity != null)
			{
				event.player.riddenByEntity.mountEntity(null);
			}
			//event.player.mountEntity(null);
		}
	}
}
