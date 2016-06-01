package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityEnderUtilities extends TileEntity
{
    protected String tileEntityName;
    protected EnumFacing facing;
    protected String ownerName;
    protected UUID ownerUUID;
    protected boolean isPublic;

    public TileEntityEnderUtilities(String name)
    {
        this.facing = BlockEnderUtilities.DEFAULT_FACING;
        this.ownerName = null;
        this.ownerUUID = null;
        this.isPublic = false;
        this.tileEntityName = name;
    }

    public String getTEName()
    {
        return this.tileEntityName;
    }

    public void setFacing(EnumFacing facing)
    {
        this.facing = facing;
    }

    public EnumFacing getFacing()
    {
        return this.facing;
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
        if (nbt.hasKey("Rotation", Constants.NBT.TAG_BYTE))
        {
            EnumFacing facing = EnumFacing.getFront(nbt.getByte("Rotation"));

            // If the TileEntity has been rotated already from the default facing,
            // then that probably means that it is being rotated while placing
            // blocks from a structure template.
            // In that case we want to adjust the current facing by the same rotation
            // that the saved facing in NBT differs from the default facing.
            if (this.facing != BlockEnderUtilities.DEFAULT_FACING)
            {
                Rotation rotation = PositionUtils.getRotation(BlockEnderUtilities.DEFAULT_FACING, facing);
                this.facing = rotation.rotate(this.facing);
            }
            else
            {
                this.facing = facing;
            }
        }

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
        super.readFromNBT(nbt);
        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setString("Version", Reference.MOD_VERSION);
        nbt.setByte("Rotation", (byte)this.facing.getIndex());

        if (this.ownerUUID != null && this.ownerName != null)
        {
            OwnerData.writePlayerTagToNBT(nbt, this.ownerUUID.getMostSignificantBits(), this.ownerUUID.getLeastSignificantBits(), this.ownerName, this.isPublic);
        }
    }

    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt.setByte("r", (byte)(this.facing.getIndex() & 0x07));

        if (this.ownerName != null)
        {
            nbt.setString("o", this.ownerName);
        }

        return nbt;
    }

    @Override
    public Packet<INetHandlerPlayClient> getDescriptionPacket()
    {
        if (this.worldObj != null)
        {
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getDescriptionPacketTag(new NBTTagCompound()));
        }

        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        if (nbt.hasKey("r") == true)
        {
            this.setFacing(EnumFacing.getFront((byte)(nbt.getByte("r") & 0x07)));
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
