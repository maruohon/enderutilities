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
    private boolean targetIsPortal;

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

    public TargetData getDestination()
    {
        return this.destination;
    }

    public OwnerData getOwner()
    {
        return this.owner;
    }

    public boolean targetIsPortal()
    {
        return this.targetIsPortal;
    }

    public void setPortalData(PortalData data)
    {
        this.destination = data.getDestination();
        this.owner = data.getOwner();
        this.color = data.getColor();
        this.targetIsPortal = data.targetIsPortal();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.color = nbt.getInteger("Color");
        this.destination = TargetData.readTargetFromNBT(nbt);
        this.owner = OwnerData.getOwnerDataFromNBT(nbt);
        this.targetIsPortal = nbt.getBoolean("Paired");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger("Color", this.color);

        if (this.targetIsPortal)
        {
            nbt.setBoolean("Paired", this.targetIsPortal);
        }

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

    public static class PortalData
    {
        private final TargetData destination;
        private final OwnerData owner;
        private final int color;
        private final boolean targetIsPortal;

        public PortalData(TargetData destination, OwnerData owner, int color, boolean targetIsPortal)
        {
            this.destination = destination;
            this.owner = owner;
            this.color = color;
            this.targetIsPortal = targetIsPortal;
        }

        public TargetData getDestination()
        {
            return destination;
        }

        public OwnerData getOwner()
        {
            return owner;
        }

        public int getColor()
        {
            return color;
        }

        public boolean targetIsPortal()
        {
            return targetIsPortal;
        }
    }
}
