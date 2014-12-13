package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.HashMap;
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
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;

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

            if (ticket != null && ticket.isPlayerTicket() == true)
            {
                //System.out.println("ticketsLoaded(): player ticket");
                NBTTagCompound nbt = ticket.getModData();

                // Release tickets that are not used for persistent chunk loading and are not currently in use
                if (nbt == null || nbt.hasKey("PersistentTicket") == false || nbt.getBoolean("PersistentTicket") == false)
                {
                    //System.out.println("ticketsLoaded(): player ticket, not persistent");
                    Set<ChunkCoordIntPair> chunks = ticket.getChunkList();
                    //System.out.println("ticketsLoaded(): getChunkList().size(): " + chunks.size());

                    for (ChunkCoordIntPair chunk : chunks)
                    {
                        //System.out.println("ticketsLoaded(): chunk set loop, unForceChunk()");
                        ForgeChunkManager.unforceChunk(ticket, chunk);

                        if (ticket != null && ticket.world != null && ticket.world.provider != null)
                        {
                            //System.out.println("ticketsLoaded(): chunk set loop, this.timeOuts.remove");
                            this.timeOuts.remove(dimChunkCoordsToString(ticket.world.provider.dimensionId, chunk.chunkXPos, chunk.chunkZPos));
                        }
                    }

                    //System.out.println("ticketsLoaded(): ForgeChunkManager.releaseTicket(): " + i);
                    ForgeChunkManager.releaseTicket(ticket);

                    //System.out.println("ticketsLoaded(): removePlayerTicket(): " + i);
                    this.removePlayerTicket(ticket);
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
        NBTTagCompound nbt = ticket.getModData();
        NBTHelperPlayer.writePlayerTagToNBT(nbt, player);

        if (isTemporary == true)
        {
            nbt.setBoolean("TemporaryPlayerTicket", true);
        }

        this.addPlayerTicket(player, dimension, ticket);

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

    public void removePlayerTicket(Ticket ticket)
    {
        if (ticket == null || ticket.world == null || ticket.world.provider == null)
        {
            return;
        }

        UUID uuid = this.getPlayerUUIDFromTicket(ticket);
        if (uuid != null)
        {
            this.removePlayerTicket(uuid.toString(), ticket.world.provider.dimensionId);
        }
    }

    public void removePlayerTicket(String uuidStr, int dimension)
    {
        this.playerTickets.remove(uuidStr + "-" + dimension);
    }

    public static String dimChunkCoordsToString(int dim, ChunkCoordIntPair cc)
    {
        return dim + "_" + cc.chunkXPos + "_" + cc.chunkZPos;
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
        Ticket ticket = this.requestPlayerTicket(player, dimension, unloadDelay != 0);

        return this.loadChunkForcedWithPlayerTicket(ticket, dimension, chunkX, chunkZ, unloadDelay);
    }

    public boolean loadChunkForcedWithPlayerTicket(Ticket ticket, int dimension, int chunkX, int chunkZ, int unloadDelay)
    {
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
        String s = dimChunkCoordsToString(dimension, chunkX, chunkZ);

        DimChunkCoordTimeout dcct = this.timeOuts.get(s);
        if (dcct != null)
        {
            //System.out.println("addChunkTimeout(): re-setting");
            dcct.setTimeout(timeout);
        }
        else
        {
            //System.out.println("addChunkTimeout(): adding");
            this.timeOuts.put(s, new DimChunkCoordTimeout(ticket, dimension, new ChunkCoordIntPair(chunkX, chunkZ), timeout));
        }
    }

    public boolean refreshChunkTimeout(int dimension, int chunkX, int chunkZ)
    {
        String s = dimChunkCoordsToString(dimension, chunkX, chunkZ);

        DimChunkCoordTimeout dcct = this.timeOuts.get(s);
        if (dcct != null)
        {
            dcct.refreshTimeout();
            return true;
        }

        return false;
    }

    public void tickPlayerLoadedChunkTimeouts()
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
