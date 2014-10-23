package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class TileEntityEnderUtilities extends TileEntity
{
	protected String tileEntityName;
	public byte rotation;
	protected String ownerName;
	protected UUID ownerUUID;

	public TileEntityEnderUtilities(String name)
	{
		this.rotation = 0;
		this.ownerName = null;
		this.ownerUUID = null;
		this.tileEntityName = name;
	}

	public String getTEName()
	{
		return this.tileEntityName;
	}

	public void setRotation(byte rot)
	{
		this.rotation = rot;
	}

	public byte getRotation()
	{
		return this.rotation;
	}

	public void setOwner(EntityPlayer player)
	{
		if (player != null)
		{
			this.ownerName = player.getCommandSenderName();
			this.ownerUUID = player.getUniqueID();
		}
		else
		{
			this.ownerName = null;
			this.ownerUUID = null;
		}
	}

	public String getOwnerName()
	{
		return this.ownerName;
	}

	public UUID getOwnerUUID()
	{
		return this.ownerUUID;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.rotation = nbt.getByte("Rotation");

		if (nbt.hasKey("OwnerName", Constants.NBT.TAG_STRING) == true)
		{
			this.ownerName = nbt.getString("OwnerName");
		}

		if (nbt.hasKey("OwnerUUIDMost", Constants.NBT.TAG_LONG) == true && nbt.hasKey("OwnerUUIDLeast", Constants.NBT.TAG_LONG) == true)
		{
			this.ownerUUID = new UUID(nbt.getLong("OwnerUUIDMost"), nbt.getLong("OwnerUUIDLeast"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setByte("Rotation", this.rotation);

		if (this.ownerName != null)
		{
			nbt.setString("OwnerName", this.ownerName);
		}

		if (this.ownerUUID != null)
		{
			nbt.setLong("OwnerUUIDMost", this.ownerUUID.getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", this.ownerUUID.getLeastSignificantBits());
		}
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(x=" + xCoord + ", y=" + yCoord + ", z=" + zCoord + ")@" + System.identityHashCode(this);
	}
}
