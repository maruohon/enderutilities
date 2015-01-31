package fi.dy.masa.enderutilities.entity;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.IExtendedEntityProperties;
import fi.dy.masa.enderutilities.reference.Reference;

public class ExtendedPlayer implements IExtendedEntityProperties
{
    public final static String PROPERTY_NAME = Reference.MOD_ID;
    //private final EntityPlayer player;
    private HashMap<World, Ticket> chunkloadTicketsTemporary;

    public ExtendedPlayer(EntityPlayer player)
    {
        //this.player = player;
        this.chunkloadTicketsTemporary = new HashMap<World, Ticket>();
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
    }

    @Override
    public void init(Entity entity, World world)
    {
    }

    public static final void register(EntityPlayer player)
    {
        player.registerExtendedProperties(ExtendedPlayer.PROPERTY_NAME, new ExtendedPlayer(player));
    }

    public static final ExtendedPlayer get(EntityPlayer player)
    {
        return (ExtendedPlayer)player.getExtendedProperties(PROPERTY_NAME);
    }

    public Ticket setTemporaryTicket(World world, Ticket ticket)
    {
        this.chunkloadTicketsTemporary.put(world, ticket);
        return ticket;
    }

    public void removeTemporaryTicket(World world)
    {
        this.chunkloadTicketsTemporary.remove(world);
    }

    public Ticket getTemporaryTicket(World world)
    {
        return this.chunkloadTicketsTemporary.get(world);
    }

    public HashMap<World, Ticket> getTemporaryTickets()
    {
        return this.chunkloadTicketsTemporary;
    }
}
