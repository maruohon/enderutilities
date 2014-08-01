package fi.dy.masa.enderutilities.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ExtendedPlayer implements IExtendedEntityProperties
{
	public final static String PROPERTY_NAME = "ExtendedPlayer";
	//private final EntityPlayer player;
	private Ticket ticket;

	public ExtendedPlayer(EntityPlayer player)
	{
		//this.player = player;
		this.ticket = null;
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

	public Ticket setTicket(Ticket ticket)
	{
		this.ticket = ticket;

		return this.ticket;
	}

	public Ticket getTicket()
	{
		return this.ticket;
	}
}
