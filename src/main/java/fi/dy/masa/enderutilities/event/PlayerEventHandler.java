package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.entity.ExtendedPlayer;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.IChunkLoadingItem;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.ItemNBTHelperTarget;

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
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)event.entity) == null)
		{
			ExtendedPlayer.register((EntityPlayer)event.entity);
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerOpenContainerEvent event)
	{
		if (event != null && event.entityPlayer != null && event.entityPlayer.worldObj != null && event.entityPlayer.worldObj.isRemote == false)
		{
			EntityPlayer player = event.entityPlayer;
			ExtendedPlayer ep = ExtendedPlayer.get(player);
			ChunkLoading.getInstance().tickChunkTimeouts(ep.getTemporaryTickets());

			if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() != null)
			{
				ItemStack stack = player.getCurrentEquippedItem();

				if (stack.getItem() == EnderUtilitiesItems.enderBag && player.openContainer != player.inventoryContainer)
				{
					// Allow access from anywhere with the Ender Bag (bypassing the distance checks)
					event.setResult(Result.ALLOW);
				}

				if (stack.getItem() instanceof IChunkLoadingItem)
				{
					NBTTagCompound nbt = stack.getTagCompound();
					if (nbt != null)
					{
						// Ender Bag: Player has just closed the remote container
						if (stack.getItem() == EnderUtilitiesItems.enderBag &&
							player.openContainer != this.containerLast && player.openContainer == player.inventoryContainer)
						{
							//if (nbt.hasKey("ChunkLoadingRequired") == true && nbt.getBoolean("ChunkLoadingRequired") == true)
							nbt.removeTag("ChunkLoadingRequired");
							nbt.setBoolean("IsOpen", false);
							stack.setTagCompound(nbt);
						}

						// If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
						if (nbt.hasKey("ChunkLoadingRequired") == true && nbt.getBoolean("ChunkLoadingRequired") == true)
						{
							ItemNBTHelperTarget target = new ItemNBTHelperTarget();
							if (target.readFromNBT(nbt) == true)
							{
								//System.out.println("refreshing chunk");
								World tgtWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
								// 60 second delay before unloading
								ChunkLoading.getInstance().addChunkTimeout(tgtWorld, target.dimension, new ChunkCoordIntPair(target.posX >> 4, target.posZ >> 4), 60 * 20);
							}
						}
					}
				}
			}

			this.containerLast = player.openContainer;
		}
	}
}
