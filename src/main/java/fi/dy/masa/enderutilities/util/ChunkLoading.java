package fi.dy.masa.enderutilities.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import fi.dy.masa.enderutilities.item.IChunkLoadingItem;

public class ChunkLoading implements LoadingCallback
{
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		if (world.isRemote == true)
		{
			return;
		}

		for (int i = 0; i < tickets.size(); ++i)
		{
			System.out.println("looping: " + i);
			Ticket ticket = tickets.get(i);

			// Release tickets that are not used for persistent chunk loading
			NBTTagCompound nbt = ticket.getModData();
			if (ticket.isPlayerTicket() == true && nbt.getBoolean("PersistentTicket") == false)
			{
				System.out.println("player, not persistent");
				if (nbt.hasKey("PlayerUUIDMost") == true && nbt.hasKey("PlayerUUIDLeast") == true)
				{
					System.out.println("has UUID");
					UUID uuid = new UUID(nbt.getLong("PlayerUUIDMost"), nbt.getLong("PlayerUUIDLeast"));
					EntityPlayer player = EntityUtils.findPlayerFromUUID(uuid);

					if (player == null || player.getCurrentEquippedItem() == null ||
						player.getCurrentEquippedItem().getItem() instanceof IChunkLoadingItem == false ||
						player.getCurrentEquippedItem().getTagCompound() == null ||
						player.getCurrentEquippedItem().getTagCompound().getBoolean("IsActive") == false)
					{						
						System.out.println("releasing (1): " + i);
						ForgeChunkManager.releaseTicket(ticket);
					}
				}
				else
				{
					System.out.println("releasing (2): " + i);
					ForgeChunkManager.releaseTicket(ticket);
				}
			}
		}
	}

}
