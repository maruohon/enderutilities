package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityEnderUtilities extends TileEntity
{
    protected String tileEntityName;
    protected int rotation;
    protected String ownerName;
    protected UUID ownerUUID;
    protected boolean isPublic;

    public TileEntityEnderUtilities(String name)
    {
        this.rotation = 0;
        this.ownerName = null;
        this.ownerUUID = null;
        this.isPublic = false;
        this.tileEntityName = name;
    }

    public String getTEName()
    {
        return this.tileEntityName;
    }

    public void setRotation(int rot)
    {
        this.rotation = rot;
    }

    public int getRotation()
    {
        return this.rotation;
    }

    public void setOwner(EntityPlayer player)
    {
        if (player != null)
        {
            this.ownerName = player.getName();
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

    public void onLeftClickBlock(EntityPlayer player) { }

    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.rotation = nbt.getByte("Rotation");

        OwnerData playerData = OwnerData.getPlayerDataFromNBT(nbt);
        if (playerData != null)
        {
            this.ownerUUID = playerData.getOwnerUUID();
            this.ownerName = playerData.getOwnerName();
            this.isPublic = playerData.getIsPublic();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        System.out.printf("readFromNBT @ %s\n", this.getPos());
        super.readFromNBT(nbt);
        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        System.out.printf("writeToNBT @ %s\n", this.getPos());
        super.writeToNBT(nbt);

        nbt.setString("Version", Reference.MOD_VERSION);
        nbt.setByte("Rotation", (byte)this.rotation);

        if (this.ownerUUID != null && this.ownerName != null)
        {
            OwnerData.writePlayerTagToNBT(nbt, this.ownerUUID.getMostSignificantBits(), this.ownerUUID.getLeastSignificantBits(), this.ownerName, this.isPublic);
        }

        return nbt;
    }

    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        System.out.printf("getUpdatePacketTag @ %s\n", this.getPos());
        nbt.setByte("r", (byte)(this.getRotation() & 0x07));

        if (this.ownerName != null)
        {
            nbt.setString("o", this.ownerName);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        System.out.printf("getUpdateTag @ %s\n", this.getPos());
        return this.getUpdatePacketTag(new NBTTagCompound());
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        if (this.worldObj != null)
        {
            System.out.printf("getUpdatePacket @ %s\n", this.getPos());
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdateTag());
        }

        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        System.out.printf("onDataPacket @ %s - Data: %s\n", this.getPos(), nbt.toString());
        if (nbt.hasKey("r") == true)
        {
            this.setRotation((byte)(nbt.getByte("r") & 0x07));
        }
        if (nbt.hasKey("o", Constants.NBT.TAG_STRING) == true)
        {
            this.ownerName = nbt.getString("o");
        }

        IBlockState state = this.worldObj.getBlockState(this.getPos());
        this.worldObj.notifyBlockUpdate(this.getPos(), state, state, 3);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.getPos() + ")@" + System.identityHashCode(this);
    }
}
