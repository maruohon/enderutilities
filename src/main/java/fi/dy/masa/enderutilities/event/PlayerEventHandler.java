package fi.dy.masa.enderutilities.event;

import net.minecraftforge.event.entity.player.PlayerEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerEventHandler
{
	@SubscribeEvent
	public void onStartStracking(PlayerEvent.StartTracking event)
	{
		if (event.entity != null && event.target != null && event.entity.worldObj.isRemote == false)
		{
			// Remount the entity if the player starts tracking an entity he is supposed to be riding already
			if (event.entity.ridingEntity == event.target)
			{
				event.entity.mountEntity(event.target);
			}
		}
	}
}
