package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortal extends TileEntityEnderUtilities
{
    private TargetData destination;
    private EnumDyeColor color;

    public TileEntityPortal()
    {
        super(ReferenceNames.NAME_TILE_PORTAL);

        this.color = EnumDyeColor.GREEN;
    }

    public EnumDyeColor getColor()
    {
        return this.color;
    }

    public void setColor(EnumDyeColor color)
    {
        this.color = color;
    }

    public TargetData getDestination()
    {
        return this.destination;
    }

    public void setDestination(TargetData destination)
    {
        this.destination = destination;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.color = EnumDyeColor.byMetadata(nbt.getByte("Color"));
        this.destination = TargetData.readTargetFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Color", (byte)this.color.getMetadata());

        if (this.destination != null)
        {
            this.destination.writeToNBT(nbt);
        }
    }
}
