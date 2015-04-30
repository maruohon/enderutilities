package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityEnergyBridge extends TileEntityEnderUtilities
{
    public boolean isActive;

    public TileEntityEnergyBridge()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.isActive = nbt.getBoolean("Active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("Active", this.isActive);
    }

    public void setState(boolean active)
    {
        this.isActive = active;
    }
}
