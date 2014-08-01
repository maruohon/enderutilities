package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.entity.ExtendedPlayer;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;

public class PlayerEventHandler
{
	private Container containerLast;

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
					if (player.openContainer != player.inventoryContainer)
					{
						// Allow access from anywhere with the Ender Bag (bypassing the distance checks)
						event.setResult(Result.ALLOW);
					}

					NBTTagCompound nbt = player.getCurrentEquippedItem().getTagCompound();
					// On container closing, release the chunk loading ticket
					if (nbt != null && nbt.hasKey("IsOpen") == true && nbt.getBoolean("IsOpen") == true &&
						player.openContainer != this.containerLast && player.openContainer == player.inventoryContainer)
					{
						ExtendedPlayer ep = ExtendedPlayer.get(player);
						if (ep != null && ep.getTicket() != null)
						{
							// TODO: add a delay to try to minimize chunk/dimension loading/unloading?
							ForgeChunkManager.releaseTicket(ep.getTicket());
							ep.setTicket(null);
						}

						nbt.setBoolean("IsOpen", false);
						player.getCurrentEquippedItem().setTagCompound(nbt);
					}

					this.containerLast = player.openContainer;
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)event.entity) == null)
		{
			ExtendedPlayer.register((EntityPlayer)event.entity);
		}
	}
}
