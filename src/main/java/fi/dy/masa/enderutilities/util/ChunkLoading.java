package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import fi.dy.masa.enderutilities.item.IChunkLoadingItem;

public class ChunkLoading implements LoadingCallback
{
	private static ChunkLoading instance;
	private HashMap<String, DimChunkCoordTimeout> timeOuts;

	public ChunkLoading()
	{
		super();
		instance = this;
		this.timeOuts = new HashMap<String, DimChunkCoordTimeout>();
	}

	public static ChunkLoading getInstance()
	{
		return instance;
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		if (world.isRemote == true)
		{
			return;
		}

		for (int i = 0; i < tickets.size(); ++i)
		{
			//System.out.println("ticketsLoaded(): looping: " + i);
			Ticket ticket = tickets.get(i);

			NBTTagCompound nbt = ticket.getModData();
			if (ticket.isPlayerTicket() == true)
			{
				if(nbt.getBoolean("PersistentTicket") == true)
				{
				}
				// Release tickets that are not used for persistent chunk loading and are not currently in use
				else
				{
					//System.out.println("player, not persistent");
					if (nbt.hasKey("PlayerUUIDMost") == true && nbt.hasKey("PlayerUUIDLeast") == true)
					{
						//System.out.println("has UUID");
						UUID uuid = new UUID(nbt.getLong("PlayerUUIDMost"), nbt.getLong("PlayerUUIDLeast"));
						EntityPlayer player = EntityUtils.findPlayerFromUUID(uuid);

						if (player == null || player.getCurrentEquippedItem() == null ||
							player.getCurrentEquippedItem().getItem() instanceof IChunkLoadingItem == false ||
							player.getCurrentEquippedItem().getTagCompound() == null ||
							player.getCurrentEquippedItem().getTagCompound().hasKey("ChunkLoadingRequired") == false)
						{
							//System.out.println("ticketsLoaded(): releasing (1): " + i);
							ForgeChunkManager.releaseTicket(ticket);
						}
					}
					else
					{
						//System.out.println("ticketsLoaded(): releasing (no UUID): " + i);
						ForgeChunkManager.releaseTicket(ticket);
					}
				}
			}
		}
	}

	public static String dimChunkPairToString(int dim, ChunkCoordIntPair cc)
	{
		return dim + "-" + cc.chunkXPos + "-" + cc.chunkZPos;
	}

	public static String dimChunkPairToString(int dim, int x, int z)
	{
		return dim + "-" + x + "-" + z;
	}

	public class DimChunkCoordTimeout
	{
		public int dimension;
		public World world;
		public ChunkCoordIntPair chunkCoords;
		public int timeout;
		public int timeoutFresh;
		public Ticket ticket;

		public DimChunkCoordTimeout(Ticket ticket, World world, int dimension, ChunkCoordIntPair cc, int timeout)
		{
			this.ticket = ticket;
			this.world = world;
			this.dimension = dimension;
			this.chunkCoords = cc;
			this.timeout = timeout;
		}

		public void setTimeout(int timeout)
		{
			this.timeout = timeout;
			this.timeoutFresh = timeout;
		}

		public void refreshTimeout()
		{
			this.timeout = this.timeoutFresh;
		}

		public int tick()
		{
			if (this.timeout > 0)
			{
				--this.timeout;
			}

			return this.timeout;
		}

		public String toString()
		{
			return this.dimension + "-" + this.chunkCoords.chunkXPos + "-" + this.chunkCoords.chunkZPos;
		}

		public boolean equals(DimChunkCoordTimeout d)
		{
			return this.dimension == d.dimension && this.chunkCoords.equals(d.chunkCoords);
		}

		public boolean equals(int dim, ChunkCoordIntPair cc)
		{
			return this.dimension == dim && this.chunkCoords.equals(cc);
		}
	}

	public void addChunkTimeout(Ticket ticket, World world, int dimension, int chunkX, int chunkZ, int timeout)
	{
		if (world == null)
		{
			return;
		}

		String s = dimChunkPairToString(dimension, chunkX, chunkZ);

		if (this.timeOuts.containsKey(s) == true)
		{
			this.timeOuts.get(s).setTimeout(timeout);
		}
		else
		{
			this.timeOuts.put(s, new DimChunkCoordTimeout(ticket, world, dimension, new ChunkCoordIntPair(chunkX, chunkZ), timeout));
		}
	}

	public boolean refreshChunkTimeout(int dimension, int chunkX, int chunkZ)
	{
		String s = dimChunkPairToString(dimension, chunkX, chunkZ);

		if (this.timeOuts.containsKey(s) == true)
		{
			this.timeOuts.get(s).refreshTimeout();
			return true;
		}

		return false;
	}

	public void tickChunkTimeouts()
	{
		DimChunkCoordTimeout dcct;
		List<String> toRemove = new ArrayList<String>();

		for (Map.Entry<String, DimChunkCoordTimeout> entry : this.timeOuts.entrySet())
		{
			dcct = entry.getValue();
			//System.out.printf("tickChunkTimeouts(): loop, timeout: %d\n",  dcct.timeout);

			if (dcct.tick() == 0)
			{
				//System.out.printf("tickChunkTimeouts(): unforcing, dim: %d, %s\n", dcct.dimension, dcct.chunkCoords.toString());

				ForgeChunkManager.unforceChunk(dcct.ticket, dcct.chunkCoords);

				if (dcct.ticket.getChunkList().size() == 0)
				{
					//System.out.println("tickChunkTimeouts(): releasing ticket");
					ForgeChunkManager.releaseTicket(dcct.ticket); // FIXME does this cause problems? we should also remove the ticket from the player
				}

				toRemove.add(entry.getKey());
			}
		}

		for (int i = 0; i < toRemove.size(); ++i)
		{
			this.timeOuts.remove(toRemove.get(i));
		}
	}
}
