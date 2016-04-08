package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.Constants;

import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class OwnerData
{
    private long ownerUUIDMost;
    private long ownerUUIDLeast;
    private String ownerName;
    private UUID ownerUUID;
    private boolean isPublic;

    public OwnerData()
    {
        this.ownerUUIDMost = 0;
        this.ownerUUIDLeast = 0;
        this.ownerName = "";
        this.isPublic = false;
    }

    public boolean getIsPublic()
    {
        return this.isPublic;
    }

    public void setIsPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    public String getOwnerName()
    {
        return this.ownerName;
    }

    public UUID getOwnerUUID()
    {
        return this.ownerUUID;
    }

    public static boolean nbtHasPlayerTag(NBTTagCompound nbt)
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

    public static boolean itemHasPlayerTag(ItemStack stack)
    {
        return (stack != null && nbtHasPlayerTag(stack.getTagCompound()));
    }

    public static boolean selectedModuleHasPlayerTag(ItemStack toolStack, ModuleType moduleType)
    {
        return itemHasPlayerTag(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public NBTTagCompound readFromNBT(NBTTagCompound nbt)
    {
        if (nbtHasPlayerTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Player");
        this.ownerUUIDMost = tag.getLong("UUIDM");
        this.ownerUUIDLeast = tag.getLong("UUIDL");
        this.ownerName = tag.getString("Name");
        this.isPublic = tag.getBoolean("Public");
        this.ownerUUID = new UUID(this.ownerUUIDMost, this.ownerUUIDLeast);

        return tag;
    }

    public String getPlayerName()
    {
        if (this.ownerName == null)
        {
            this.ownerName = "";
        }

        // FIXME we should get the player name from the UUID
        return this.ownerName;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writePlayerTagToNBT(nbt, this.ownerUUIDMost, this.ownerUUIDLeast, this.ownerName, this.isPublic);
    }

    public boolean writeToItem(ItemStack stack)
    {
        if (stack != null)
        {
            stack.setTagCompound(this.writeToNBT(stack.getTagCompound()));
            return true;
        }

        return false;
    }

    public boolean writeToSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            if (this.writeToItem(moduleStack) == false)
            {
                return false;
            }

            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, long uuidMost, long uuidLeast, String name, boolean isPublic)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("UUIDM", uuidMost);
        tag.setLong("UUIDL", uuidLeast);
        tag.setString("Name", name);
        tag.setBoolean("Public", isPublic);
        nbt.setTag("Player", tag);

        return nbt;
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

        return writePlayerTagToNBT(nbt, player.getUniqueID().getMostSignificantBits(), player.getUniqueID().getLeastSignificantBits(), player.getName(), isPublic);
    }

    public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, EntityPlayer player)
    {
        return writePlayerTagToNBT(nbt, player, true);
    }

    public static void writePlayerTagToItem(ItemStack stack, EntityPlayer player, boolean isPublic)
    {
        if (stack != null)
        {
            stack.setTagCompound(writePlayerTagToNBT(stack.getTagCompound(), player, isPublic));
        }
    }

    public static boolean writePlayerTagToSelectedModule(ItemStack toolStack, ModuleType moduleType, EntityPlayer player, boolean isPublic)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            writePlayerTagToItem(moduleStack, player, isPublic);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public static OwnerData getPlayerDataFromNBT(NBTTagCompound nbt)
    {
        OwnerData player = new OwnerData();
        if (player.readFromNBT(nbt) != null)
        {
            return player;
        }

        return null;
    }

    public static OwnerData getPlayerDataFromItem(ItemStack stack)
    {
        if (stack != null)
        {
            return getPlayerDataFromNBT(stack.getTagCompound());
        }

        return null;
    }

    public static OwnerData getPlayerDataFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        return getPlayerDataFromItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public boolean isOwner(EntityPlayer player)
    {
        if (player == null)
        {
            return false;
        }

        // FIXME verify that this would work: if (this.playerUUID != null && this.playerUUID.equals(player.getUniqueID()) == true)
        if (this.ownerUUIDMost == player.getUniqueID().getMostSignificantBits() &&
            this.ownerUUIDLeast == player.getUniqueID().getLeastSignificantBits())
        {
            return true;
        }

        return false;
    }

    public boolean canAccess(EntityPlayer player)
    {
        return (this.isPublic || this.isOwner(player));
    }

    public static boolean isOwnerOfItem(ItemStack stack, EntityPlayer player)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            OwnerData playerData = new OwnerData();
            if (playerData.readFromNBT(stack.getTagCompound()) != null)
            {
                return playerData.isOwner(player);
            }
        }

        return false;
    }

    public static boolean isOwnerOfSelectedModule(ItemStack toolStack, ModuleType moduleType, EntityPlayer player)
    {
        return isOwnerOfItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), player);
    }

    /**
     * Check if the given player is allowed to access this item.
     * Returns true if there is no player information stored yet, or if the privacy mode is set to Public, or if the given player is the owner.
     */
    public static boolean canAccessItem(ItemStack stack, EntityPlayer player)
    {
        if (stack == null)
        {
            return false;
        }

        if (stack.getTagCompound() == null)
        {
            return true;
        }

        OwnerData playerData = getPlayerDataFromItem(stack);
        return (playerData == null || playerData.isPublic == true || playerData.isOwner(player) == true);
    }

    public static boolean canAccessSelectedModule(ItemStack toolStack, ModuleType moduleType, EntityPlayer player)
    {
        return canAccessItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), player);
    }
}
