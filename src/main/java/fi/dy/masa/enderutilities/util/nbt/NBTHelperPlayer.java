package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

public class NBTHelperPlayer
{
    public long playerUUIDMost;
    public long playerUUIDLeast;
    public String playerName;

    public NBTHelperPlayer()
    {
        this.playerUUIDMost = 0;
        this.playerUUIDLeast = 0;
        this.playerName = "";
    }

    public static boolean hasPlayerTag(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("Player", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Player");
        if (tag != null &&
            tag.hasKey("PlayerUUIDMost", Constants.NBT.TAG_LONG) == true &&
            tag.hasKey("PlayerUUIDLeast", Constants.NBT.TAG_LONG) == true &&
            tag.hasKey("PlayerName", Constants.NBT.TAG_STRING) == true)
        {
            return true;
        }

        return false;
    }

    public NBTTagCompound readPlayerTagFromNBT(NBTTagCompound nbt)
    {
        if (hasPlayerTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Player");
        this.playerUUIDMost = tag.getLong("PlayerUUIDMost");
        this.playerUUIDLeast = tag.getLong("PlayerUUIDLeast");
        this.playerName = tag.getString("PlayerName");

        return tag;
    }

    public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, EntityPlayer player)
    {
        if (nbt == null)
        {
            return nbt;
        }

        if (player == null)
        {
            nbt.removeTag("Player");
            return nbt;
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("PlayerUUIDMost", player.getUniqueID().getMostSignificantBits());
        tag.setLong("PlayerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
        tag.setString("PlayerName", player.getCommandSenderName());

        nbt.setTag("Player", tag);

        return nbt;
    }
}
