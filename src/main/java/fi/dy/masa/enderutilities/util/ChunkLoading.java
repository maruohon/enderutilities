package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.OrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.PlayerOrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;

public class ChunkLoading implements LoadingCallback, OrderedLoadingCallback, PlayerOrderedLoadingCallback
{
    private static ChunkLoading instance;
    private Map<String, DimChunkCoordTimeout> timeOuts;
    private SetMultimap<String, Ticket> playerTickets;
    private SetMultimap<World, Ticket> modTickets;

    public ChunkLoading()
    {
        instance = this;
        this.clear();
    }

    public static ChunkLoading getInstance()
    {
        return instance;
    }

    public void clear()
    {
        this.timeOuts = new HashMap<String, DimChunkCoordTimeout>();
        this.playerTickets = HashMultimap.create();
        this.modTickets = HashMultimap.create();
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world)
    {
        for (Ticket ticket : tickets)
        {
            System.out.println("void ticketsLoaded(): looping tickets");
            if (ticket != null)
            {
                for (ChunkCoordIntPair chunk : ticket.getChunkList())
                {
                    System.out.println("void ticketsLoaded(): forcing chunk: " + chunk + " in dimension: " + ticket.world.provider.dimensionId);
                    ForgeChunkManager.forceChunk(ticket, chunk);
                }
            }
        }
    }

    @Override
    public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount)
    {
        Set<Ticket> persistentTickets = new HashSet<Ticket>();

        int i = 0;
        for (Ticket ticket : tickets)
        {
            if (ticket == null)
            {
                continue;
            }

            if (ticket.world != null && ticket.world.provider != null)
            {
                System.out.println("List<Ticket> ticketsLoaded(): looping: " + i + " world: " + world + " dim: " + ticket.world.provider.dimensionId);
            }
            else
            {
                System.out.println("List<Ticket> ticketsLoaded(): looping: " + i + " world: " + world);
            }

            NBTTagCompound nbt = ticket.getModData();

            // Only claim tickets that are used for persistent chunk loading
            if (nbt != null && nbt.getBoolean("Persistent") == true)
            {
                persistentTickets.add(ticket);
            }

            ++i;
        }

        LinkedList<Ticket> claimedTickets = new LinkedList<Ticket>();
        claimedTickets.addAll(persistentTickets);

        return claimedTickets;
    }

    @Override
    public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world)
    {
        Multimap<String, Ticket> persistentPlayerTickets = HashMultimap.create();
        LinkedListMultimap<String, Ticket> claimedTickets = LinkedListMultimap.create();

        int i = 0;
        for (String player : tickets.keys())
        {
            System.out.println("playerTicketsLoaded(): looping outer start: " + i);
            for (Ticket ticket : tickets.get(player))
            {
                if (ticket == null)
                {
                    continue;
                }

                if (ticket.world != null && ticket.world.provider != null)
                {
                    System.out.println("playerTicketsLoaded(): looping: " + i + " world: " + world + " dim: " + ticket.world.provider.dimensionId);
                }
                else
                {
                    System.out.println("playerTicketsLoaded(): looping: " + i + " world: " + world);
                }

                NBTTagCompound nbt = ticket.getModData();

                // Only claim tickets that are used for persistent chunk loading
                if (nbt != null && nbt.getBoolean("Persistent") == true)
                {
                    System.out.println("playerTicketsLoaded(): found persistent ticket");
                    persistentPlayerTickets.put(player, ticket);
                }

                ++i;
            }
        }

        claimedTickets.putAll(persistentPlayerTickets);

        return claimedTickets;
    }

    public Ticket requestPlayerTicket(EntityPlayer player, int dimension, boolean isTemporary)
    {
        for (Ticket ticket : this.playerTickets.get(player.getUniqueID().toString() + "-" + dimension))
        {
            // If this ticket can still load more chunks
            if (ticket != null && ticket.getChunkList().size() < ticket.getChunkListDepth())
            {
                System.out.println("requestPlayerTicket(): found an existing ticket with capacity; used: " + ticket.getChunkList().size() + " / " + ticket.getChunkListDepth());
                return ticket;
            }
            else
            {
                System.out.println("requestPlayerTicket(): Found an existing ticket without capacity");
            }
        }

        World world = MinecraftServer.getServer().worldServerForDimension(dimension);
        if (world == null)
        {
            EnderUtilities.logger.warn("requestPlayerTicket(): Couldn't get world for dimension (" + dimension + ")");
            return null;
        }

        Ticket ticket = ForgeChunkManager.requestPlayerTicket(EnderUtilities.instance, player.getCommandSenderName(), world, ForgeChunkManager.Type.NORMAL);
        if (ticket == null)
        {
            EnderUtilities.logger.warn("requestPlayerTicket(): Couldn't get a chunk loading ticket for player '" + player.getCommandSenderName() + "'");
            return null;
        }

        System.out.println("requestPlayerTicket() succeeded");
        NBTTagCompound nbt = ticket.getModData();
        NBTHelperPlayer.writePlayerTagToNBT(nbt, player);

        if (isTemporary == false)
        {
            nbt.setBoolean("Persistent", true);
        }

        this.playerTickets.get(player.getUniqueID().toString() + "-" + dimension).add(ticket);

        return ticket;
    }

    public Ticket requestModTicket(int dimension, boolean isTemporary)
    {
        MinecraftServer srv = MinecraftServer.getServer();
        if (srv == null)
        {
            return null;
        }

        World world = srv.worldServerForDimension(dimension);
        if (world == null)
        {
            EnderUtilities.logger.warn("requestModTicket(): Couldn't get world for dimension (" + dimension + ")");
            return null;
        }

        for (Ticket ticket : this.modTickets.get(srv.worldServerForDimension(dimension)))
        {
            // If this ticket can still load more chunks
            if (ticket != null && ticket.getChunkList().size() < ticket.getChunkListDepth())
            {
                System.out.println("requestModTicket(): found an existing ticket with capacity; used: " + ticket.getChunkList().size() + " / " + ticket.getChunkListDepth());
                return ticket;
            }
            else
            {
                System.out.println("requestModTicket(): Found an existing ticket without capacity");
            }
        }

        Ticket ticket = ForgeChunkManager.requestTicket(EnderUtilities.instance, world, ForgeChunkManager.Type.NORMAL);
        if (ticket == null)
        {
            EnderUtilities.logger.warn("requestModTicket(): Couldn't get a mod chunk loading ticket");
            return null;
        }

        System.out.println("requestModTicket() succeeded");
        NBTTagCompound nbt = ticket.getModData();

        if (isTemporary == false)
        {
            System.out.println("requestModTicket() setting persistent flag");
            nbt.setBoolean("Persistent", true);
        }

        this.modTickets.get(world).add(ticket);

        return ticket;
    }

    public UUID getPlayerUUIDFromTicket(Ticket ticket)
    {
        if (ticket == null)
        {
            return null;
        }

        NBTTagCompound nbt = ticket.getModData();
        NBTHelperPlayer playerData = new NBTHelperPlayer();
        if (playerData.readPlayerTagFromNBT(nbt) == null)
        {
            return null;
        }

        return playerData.playerUUID;
    }

    public void removeTicket(Ticket ticket)
    {
        if (ticket == null || ticket.world == null)
        {
            return;
        }

        if (ticket.isPlayerTicket() == true)
        {
            UUID uuid = this.getPlayerUUIDFromTicket(ticket);
            if (ticket.world.provider != null && uuid != null)
            {
                this.playerTickets.get(uuid.toString() + "-" + ticket.world.provider.dimensionId).remove(ticket);
            }
        }
        else
        {
            this.modTickets.get(ticket.world).remove(ticket);
        }
    }

    public static String dimChunkCoordsToString(int dim, int x, int z)
    {
        return dim + "_" + x + "_" + z;
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
        if (MinecraftServer.getServer() == null)
        {
            return false;
        }

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
        // If the target chunk is already being loaded by something, we don't need another ticket/forced load for it
        // We just refresh the timeout, if the previous one is shorter that the new request
        if (this.hasChunkTimeout(dimension, chunkX, chunkZ) == true)
        {
            if (this.refreshChunkTimeout(dimension, chunkX, chunkZ, unloadDelay, true) == true)
            {
                return true;
            }
        }

        Ticket ticket = this.requestPlayerTicket(player, dimension, unloadDelay != 0);

        return this.loadChunkForcedWithTicket(ticket, dimension, chunkX, chunkZ, unloadDelay);
    }

    public boolean loadChunkForcedWithModTicket(int dimension, int chunkX, int chunkZ, int unloadDelay)
    {
        // If the target chunk is already being loaded by something, we don't need another ticket/forced load for it
        // We just refresh the timeout, if the previous one is shorter that the new request
        if (this.hasChunkTimeout(dimension, chunkX, chunkZ) == true)
        {
            if (this.refreshChunkTimeout(dimension, chunkX, chunkZ, unloadDelay, true) == true)
            {
                return true;
            }
        }

        Ticket ticket = this.requestModTicket(dimension, unloadDelay != 0);

        return this.loadChunkForcedWithTicket(ticket, dimension, chunkX, chunkZ, unloadDelay);
    }

    public boolean loadChunkForcedWithTicket(Ticket ticket, int dimension, int chunkX, int chunkZ, int unloadDelay)
    {
        if (ticket == null)
        {
            EnderUtilities.logger.warn("loadChunkForcedWithTicket(): ticket == null");
            return false;
        }

        ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(chunkX, chunkZ));
        if (unloadDelay > 0)
        {
            System.out.println("loadChunkForcedWithTicket() adding timeout: " + unloadDelay);
            this.addChunkTimeout(ticket, dimension, chunkX, chunkZ, unloadDelay);
        }

        return this.loadChunkWithoutForce(dimension, chunkX, chunkZ);
    }

    public boolean hasChunkTimeout(int dimension, int chunkX, int chunkZ)
    {
        if (this.timeOuts.get(dimChunkCoordsToString(dimension, chunkX, chunkZ)) != null)
        {
            return true;
        }

        return false;
    }

    public void addChunkTimeout(Ticket ticket, int dimension, int chunkX, int chunkZ, int timeout)
    {
        String s = dimChunkCoordsToString(dimension, chunkX, chunkZ);

        DimChunkCoordTimeout dcct = this.timeOuts.get(s);
        if (dcct != null)
        {
            System.out.println("addChunkTimeout(): re-setting");
            dcct.setTimeout(timeout);
        }
        else if (ticket != null)
        {
            System.out.println("addChunkTimeout(): adding");
            this.timeOuts.put(s, new DimChunkCoordTimeout(ticket, dimension, new ChunkCoordIntPair(chunkX, chunkZ), timeout));
        }
    }

    public boolean refreshChunkTimeout(int dimension, int chunkX, int chunkZ)
    {
        return this.refreshChunkTimeout(dimension, chunkX, chunkZ, -1, true);
    }

    public boolean refreshChunkTimeout(int dimension, int chunkX, int chunkZ, int timeout)
    {
       return this.refreshChunkTimeout(dimension, chunkX, chunkZ, timeout, true);
    }

    public boolean refreshChunkTimeout(int dimension, int chunkX, int chunkZ, int timeout, boolean increaseOnly)
    {
        String s = dimChunkCoordsToString(dimension, chunkX, chunkZ);
        DimChunkCoordTimeout dcct = this.timeOuts.get(s);

        if (dcct != null)
        {
            if (timeout >= 0 && (increaseOnly == false || timeout > dcct.timeoutFresh))
            {
                System.out.println("refreshChunkTimeout(): re-setting");
                dcct.setTimeout(timeout);
            }
            else
            {
                System.out.println("refreshChunkTimeout(): refreshing...");
                dcct.refreshTimeout();
            }

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

            // If this chunk doesn't have a valid ticket anymore, just remove the entry
            if (dcct != null && this.playerTickets.containsValue(dcct.ticket) == false)
            {
                //System.out.println("tickChunkTimeouts(): invalid ticket, removing timeout entry");
                toRemove.add(entry.getKey());
                continue;
            }

            if (dcct.tick() == 0)
            {
                //System.out.printf("tickChunkTimeouts(): unforcing, dim: %d, %s\n", dcct.dimension, dcct.chunkCoords.toString());
                ForgeChunkManager.unforceChunk(dcct.ticket, dcct.chunkCoords);

                if (dcct.ticket != null && dcct.ticket.getChunkList().size() == 0)
                {
                    if (dcct.ticket.isPlayerTicket())
                    {
                        System.out.println("tickChunkTimeouts(): releasing player ticket");
                    }
                    else
                    {
                        System.out.println("tickChunkTimeouts(): releasing mod ticket");
                    }

                    this.removeTicket(dcct.ticket);
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
