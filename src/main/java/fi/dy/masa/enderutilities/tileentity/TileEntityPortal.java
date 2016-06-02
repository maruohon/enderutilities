package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortal extends TileEntityEnderUtilities
{
    private TargetData destination;
    private OwnerData owner;
    private int color;

    public TileEntityPortal()
    {
        super(ReferenceNames.NAME_TILE_PORTAL);

        this.color = 0xA010E0;
    }

    public int getColor()
    {
        return this.color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public TargetData getDestination()
    {
        return this.destination;
    }

    public OwnerData getOwner()
    {
        return this.owner;
    }

    public void setDestination(TargetData destination)
    {
        this.destination = destination;
    }

    public void setOwner(OwnerData owner)
    {
        this.owner = owner;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.color = nbt.getInteger("Color");
        this.destination = TargetData.readTargetFromNBT(nbt);
        this.owner = OwnerData.getOwnerDataFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger("Color", this.color);

        if (this.destination != null)
        {
            this.destination.writeToNBT(nbt);
        }

        if (this.owner != null)
        {
            this.owner.writeToNBT(nbt);
        }
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setInteger("c", this.color);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        if (nbt.hasKey("c"))
        {
            this.color = nbt.getInteger("c");
        }

        super.onDataPacket(net, packet);
    }
}
