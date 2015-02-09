package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class NBTHelperPlayer
{
    public long playerUUIDMost;
    public long playerUUIDLeast;
    public String playerName;
    public boolean isPublic;
    public UUID playerUUID;

    public NBTHelperPlayer()
    {
        this.playerUUIDMost = 0;
        this.playerUUIDLeast = 0;
        this.playerName = "";
        this.isPublic = false;
    }

    public static NBTHelperPlayer getPlayerData(NBTTagCompound nbt)
    {
        NBTHelperPlayer player = new NBTHelperPlayer();
        if (player.readFromNBT(nbt) != null)
        {
            return player;
        }

        return null;
    }

    public static NBTHelperPlayer getPlayerData(ItemStack stack)
    {
        if (stack != null)
        {
            return getPlayerData(stack.getTagCompound());
        }

        return null;
    }

    public static NBTHelperPlayer getPlayerDataFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        return getPlayerData(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public static boolean hasPlayerTag(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("Player", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Player");
        if (tag != null &&
            tag.hasKey("UUIDM", Constants.NBT.TAG_LONG) == true &&
            tag.hasKey("UUIDL", Constants.NBT.TAG_LONG) == true &&
            tag.hasKey("Name", Constants.NBT.TAG_STRING) == true)
        {
            return true;
        }

        return false;
    }

    public NBTTagCompound readFromNBT(NBTTagCompound nbt)
    {
        if (hasPlayerTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Player");
        this.playerUUIDMost = tag.getLong("UUIDM");
        this.playerUUIDLeast = tag.getLong("UUIDL");
        this.playerName = tag.getString("Name");
        this.isPublic = tag.getBoolean("Public");
        this.playerUUID = new UUID(this.playerUUIDMost, this.playerUUIDLeast);

        return tag;
    }

    public static NBTTagCompound writeToNBT(NBTTagCompound nbt, long most, long least, String name, boolean isPublic)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("UUIDM", most);
        tag.setLong("UUIDL", least);
        tag.setString("Name", name);
        tag.setBoolean("Public", isPublic);
        nbt.setTag("Player", tag);

        return nbt;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeToNBT(nbt, this.playerUUIDMost, this.playerUUIDLeast, this.playerName, this.isPublic);
    }

    public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, EntityPlayer player, boolean isPublic)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        if (player == null)
        {
            nbt.removeTag("Player");
            return nbt;
        }

        return writeToNBT(nbt, player.getUniqueID().getMostSignificantBits(), player.getUniqueID().getLeastSignificantBits(), player.getCommandSenderName(), isPublic);
    }

    public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, EntityPlayer player)
    {
        return writePlayerTagToNBT(nbt, player, true);
    }

    public boolean isOwner(EntityPlayer player)
    {
        if (player == null)
        {
            return false;
        }

        // FIXME verify that this would work: if (this.playerUUID != null && this.playerUUID.equals(player.getUniqueID()) == true)
        if (this.playerUUIDMost == player.getUniqueID().getMostSignificantBits() &&
            this.playerUUIDLeast == player.getUniqueID().getLeastSignificantBits())
        {
            return true;
        }

        return false;
    }

    public boolean canAccess(EntityPlayer player)
    {
        return (this.isPublic || this.isOwner(player));
    }

    public String getPlayerName()
    {
        if (this.playerName == null)
        {
            this.playerName = "";
        }

        // FIXME we should get the player name from the UUID
        return this.playerName;
    }
}
