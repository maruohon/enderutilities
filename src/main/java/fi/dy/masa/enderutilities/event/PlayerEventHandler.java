package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;

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

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerOpenContainerEvent event)
	{
		if (event != null && event.entityPlayer != null && event.entityPlayer.worldObj != null && event.entityPlayer.worldObj.isRemote == false)
		{
			EntityPlayer player = event.entityPlayer;
			if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() != null)
			{
				if (player.getCurrentEquippedItem().getItem() == EnderUtilitiesItems.enderBag)
				{
					// Allow access from anywhere with the Ender Bag (bypassing the distance checks)
					event.setResult(Result.ALLOW);
				}
			}
		}
	}
}
