package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.IChunkLoadingItem;

public class ChunkLoading implements LoadingCallback
{
	private static ChunkLoading instance;
	private HashMap<String, DimChunkCoordTimeout> timeOuts;
	private HashMap<String, Ticket> playerTickets;

	public ChunkLoading()
	{
		super();
		instance = this;
		this.timeOuts = new HashMap<String, DimChunkCoordTimeout>();
		this.playerTickets = new HashMap<String, Ticket>();
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

	public Ticket requestPlayerTicket(EntityPlayer player, int dimension, boolean isTemporary)
	{
		Ticket ticket = this.getPlayerTicket(player, dimension);
		if (ticket != null)
		{
			//System.out.println("requestPlayerTicket() found an existing ticket");
			return ticket;
		}

		World world = MinecraftServer.getServer().worldServerForDimension(dimension);
		if (world == null)
		{
			EnderUtilities.logger.warn("requestTemporaryPlayerTicket(): Couldn't get world for dimension (" + dimension + ")");
			return null;
		}

		ticket = ForgeChunkManager.requestPlayerTicket(EnderUtilities.instance, player.getCommandSenderName(), world, ForgeChunkManager.Type.NORMAL);
		if (ticket == null)
		{
			EnderUtilities.logger.warn("requestTemporaryPlayerTicket(): Couldn't get a chunk loading ticket for player '" + player.getCommandSenderName() + "'");
			return null;
		}
		//System.out.println("requestPlayerTicket() succeeded");
		ticket.getModData().setString("PlayerName", player.getCommandSenderName());
		ticket.getModData().setLong("PlayerUUIDMost", player.getUniqueID().getMostSignificantBits());
		ticket.getModData().setLong("PlayerUUIDLeast", player.getUniqueID().getLeastSignificantBits());

		if (isTemporary == true)
		{
			ticket.getModData().setBoolean("TemporaryPlayerTicket", true);
		}

		this.addPlayerTicket(player, dimension, ticket);

		return ticket;
	}

	public UUID getPlayerUUIDFromTicket(Ticket ticket)
	{
		NBTTagCompound nbt = ticket.getModData();
		if (nbt == null || nbt.hasKey("PlayerUUIDMost") == false || nbt.hasKey("PlayerUUIDLeast") == false)
		{
			return null;
		}

		return new UUID(nbt.getLong("PlayerUUIDMost"), nbt.getLong("PlayerUUIDLeast"));
	}

	public void addPlayerTicket(EntityPlayer player, int dimension, Ticket ticket)
	{
		this.addPlayerTicket(player.getUniqueID().toString(), dimension, ticket);
	}

	public void addPlayerTicket(String uuidStr, int dimension, Ticket ticket)
	{
		this.playerTickets.put(uuidStr + "-" + dimension, ticket);
	}

	public Ticket getPlayerTicket(EntityPlayer player, int dimension)
	{
		return this.getPlayerTicket(player.getUniqueID().toString(), dimension);
	}

	public Ticket getPlayerTicket(String uuidStr, int dimension)
	{
		return this.playerTickets.get(uuidStr + "-" + dimension);
	}

	public void removePlayerTicket(EntityPlayer player, int dimension)
	{
		this.removePlayerTicket(player.getUniqueID().toString(), dimension);
	}

	public void removePlayerTicket(String uuidStr, int dimension)
	{
		this.playerTickets.remove(uuidStr + "-" + dimension);
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
		public ChunkCoordIntPair chunkCoords;
		public int timeout;
		public int timeoutFresh;
		public Ticket ticket;

		public DimChunkCoordTimeout(Ticket ticket, int dimension, ChunkCoordIntPair cc, int timeout)
		{
			this.ticket = ticket;
			this.dimension = dimension;
			this.chunkCoords = cc;
			this.timeout = timeout;
			this.timeoutFresh = timeout;
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

	public boolean loadChunkWithoutForce(int dimension, int chunkX, int chunkZ)
	{
		return this.loadChunkWithoutForce(MinecraftServer.getServer().worldServerForDimension(dimension), chunkX, chunkZ);
	}

	public boolean loadChunkWithoutForce(World world, int chunkX, int chunkZ)
	{
		//System.out.println("loadChunkWithoutForce() start");
		if (world == null)
		{
			return false;
		}
		IChunkProvider chunkProvider = world.getChunkProvider();
		if (chunkProvider == null)
		{
			return false;
		}
		if (chunkProvider.chunkExists(chunkX, chunkZ) == false)
		{
			//System.out.println("loadChunkWithoutForce() loading chunk");
			chunkProvider.loadChunk(chunkX, chunkZ);
		}
		//System.out.println("loadChunkWithoutForce() end");
		return true;
	}

	public boolean loadChunkForcedWithPlayerTicket(EntityPlayer player, int dimension, int chunkX, int chunkZ, int unloadDelay)
	{
		Ticket ticket = this.requestPlayerTicket(player, dimension, unloadDelay != 0);
		if (ticket == null)
		{
			//System.out.println("loadChunkForcedWithPlayerTicket() ticket == null");
			return false;
		}
		ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(chunkX, chunkZ));
		if (unloadDelay > 0)
		{
			//System.out.println("loadChunkForcedWithPlayerTicket() adding timeout: " + unloadDelay);
			this.addChunkTimeout(ticket, dimension, chunkX, chunkZ, unloadDelay);
		}

		return this.loadChunkWithoutForce(dimension, chunkX, chunkZ);
	}

	public void addChunkTimeout(Ticket ticket, int dimension, int chunkX, int chunkZ, int timeout)
	{
		String s = dimChunkPairToString(dimension, chunkX, chunkZ);

		if (this.timeOuts.containsKey(s) == true)
		{
			//System.out.println("addChunkTimeout(): re-setting");
			this.timeOuts.get(s).setTimeout(timeout);
		}
		else
		{
			//System.out.println("addChunkTimeout(): adding");
			this.timeOuts.put(s, new DimChunkCoordTimeout(ticket, dimension, new ChunkCoordIntPair(chunkX, chunkZ), timeout));
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

		//int j = 0; // FIXME debug
		for (Map.Entry<String, DimChunkCoordTimeout> entry : this.timeOuts.entrySet())
		{
			dcct = entry.getValue();
			//System.out.printf("tickChunkTimeouts(): loop %d, timeout: %d\n", j++, dcct.timeout);

			if (dcct.tick() == 0)
			{
				//System.out.printf("tickChunkTimeouts(): unforcing, dim: %d, %s\n", dcct.dimension, dcct.chunkCoords.toString());
				ForgeChunkManager.unforceChunk(dcct.ticket, dcct.chunkCoords);

				if (dcct.ticket.getChunkList().size() == 0)
				{
					//System.out.println("tickChunkTimeouts(): releasing ticket");
					this.removePlayerTicket(this.getPlayerUUIDFromTicket(dcct.ticket).toString(), dcct.dimension);
					ForgeChunkManager.releaseTicket(dcct.ticket);
				}

				toRemove.add(entry.getKey());
			}
		}

		for (int i = 0; i < toRemove.size(); ++i)
		{
			//System.out.println("tickChunkTimeouts() remove loop: " + i);
			this.timeOuts.remove(toRemove.get(i));
		}
	}
}
