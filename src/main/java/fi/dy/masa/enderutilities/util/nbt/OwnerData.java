package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;
import net.minecraft.entity.Entity;
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

    public static boolean nbtHasOwnerTag(NBTTagCompound nbt)
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

    public static boolean itemHasOwnerTag(ItemStack stack)
    {
        return (stack != null && nbtHasOwnerTag(stack.getTagCompound()));
    }

    public static boolean selectedModuleHasOwnerTag(ItemStack toolStack, ModuleType moduleType)
    {
        return itemHasOwnerTag(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public NBTTagCompound readFromNBT(NBTTagCompound nbt)
    {
        if (nbtHasOwnerTag(nbt) == false)
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
        return writeOwnerTagToNBT(nbt, this.ownerUUIDMost, this.ownerUUIDLeast, this.ownerName, this.isPublic);
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

    public static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, long uuidMost, long uuidLeast, String name, boolean isPublic)
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

    public static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, Entity entity, boolean isPublic)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        if (entity == null)
        {
            nbt.removeTag("Player");
            return nbt;
        }

        return writeOwnerTagToNBT(nbt, entity.getUniqueID().getMostSignificantBits(), entity.getUniqueID().getLeastSignificantBits(), entity.getName(), isPublic);
    }

    public static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, EntityPlayer player)
    {
        return writeOwnerTagToNBT(nbt, player, true);
    }

    public static void writeOwnerTagToItem(ItemStack stack, Entity entity, boolean isPublic)
    {
        if (stack != null)
        {
            stack.setTagCompound(writeOwnerTagToNBT(stack.getTagCompound(), entity, isPublic));
        }
    }

    public static boolean writeOwnerTagToSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity, boolean isPublic)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            writeOwnerTagToItem(moduleStack, entity, isPublic);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public static OwnerData getOwnerDataFromNBT(NBTTagCompound nbt)
    {
        OwnerData owner = new OwnerData();
        if (owner.readFromNBT(nbt) != null)
        {
            return owner;
        }

        return null;
    }

    public static OwnerData getOwnerDataFromItem(ItemStack stack)
    {
        if (stack != null)
        {
            return getOwnerDataFromNBT(stack.getTagCompound());
        }

        return null;
    }

    public static OwnerData getOwnerDataFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        return getOwnerDataFromItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public boolean isOwner(Entity entity)
    {
        if (entity == null)
        {
            return false;
        }

        // FIXME verify that this would work: if (this.playerUUID != null && this.playerUUID.equals(player.getUniqueID()) == true)
        if (this.ownerUUIDMost == entity.getUniqueID().getMostSignificantBits() &&
            this.ownerUUIDLeast == entity.getUniqueID().getLeastSignificantBits())
        {
            return true;
        }

        return false;
    }

    public boolean canAccess(Entity entity)
    {
        return (this.isPublic || this.isOwner(entity));
    }

    public static boolean isOwnerOfItem(ItemStack stack, Entity entity)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            OwnerData owner = new OwnerData();
            if (owner.readFromNBT(stack.getTagCompound()) != null)
            {
                return owner.isOwner(entity);
            }
        }

        return false;
    }

    public static boolean isOwnerOfSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        return isOwnerOfItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), entity);
    }

    /**
     * Check if the given player is allowed to access this item.
     * Returns true if there is no player information stored yet, or if the privacy mode is set to Public, or if the given player is the owner.
     */
    public static boolean canAccessItem(ItemStack stack, Entity entity)
    {
        if (stack == null)
        {
            return false;
        }

        if (stack.getTagCompound() == null)
        {
            return true;
        }

        OwnerData owner = getOwnerDataFromItem(stack);
        return (owner == null || owner.isPublic == true || owner.isOwner(entity) == true);
    }

    public static boolean canAccessSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        return canAccessItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), entity);
    }
}
