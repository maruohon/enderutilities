package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.nbt.NBTTagCompound;
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
        this.destination = null;
        this.owner = null;
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
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

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setInteger("c", this.color);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("c"))
        {
            this.color = tag.getInteger("c");
        }

        super.handleUpdateTag(tag);
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }
}
