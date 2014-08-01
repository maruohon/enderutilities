package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.entity.ExtendedPlayer;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.IChunkLoadingItem;

public class PlayerEventHandler
{
	private Container containerLast;
	private int unloadDelay = 0;

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
					if (nbt != null && nbt.hasKey("IsActive") == true && nbt.getBoolean("IsActive") == true &&
						player.openContainer != this.containerLast && player.openContainer == player.inventoryContainer)
					{
						this.unloadDelay = 120 * 20; // 120 second delay before unloading

						nbt.setBoolean("IsActive", false);
						player.getCurrentEquippedItem().setTagCompound(nbt);
					}

					this.containerLast = player.openContainer;
				}
			}

			if (this.unloadDelay > 0)
			{
				if (--this.unloadDelay == 0)
				{
					ExtendedPlayer ep = ExtendedPlayer.get(player);
					ItemStack stack = player.getCurrentEquippedItem();
					if (ep != null && ep.getTicket() != null && (stack == null || stack.getItem() instanceof IChunkLoadingItem == false ||
						stack.getTagCompound() == null || stack.getTagCompound().getBoolean("IsActive") == false))
					{
						ForgeChunkManager.releaseTicket(ep.getTicket());
						ep.setTicket(null);
					}
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
