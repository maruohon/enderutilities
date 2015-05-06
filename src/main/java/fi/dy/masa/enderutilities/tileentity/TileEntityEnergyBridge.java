package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setBoolean("a", this.isActive);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();
        this.setState(nbt.getBoolean("a"));

        super.onDataPacket(net, packet);
    }

    public void setState(boolean state)
    {
        this.isActive = state;
    }
}
