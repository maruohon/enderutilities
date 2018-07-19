package fi.dy.masa.enderutilities.util.nbt;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
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

    public OwnerData(Entity entity)
    {
        UUID uuid = entity.getUniqueID();
        this.ownerUUID = uuid;
        this.ownerUUIDMost = uuid != null ? uuid.getMostSignificantBits() : 0;
        this.ownerUUIDLeast = uuid != null ? uuid.getLeastSignificantBits() : 0;
        this.ownerName = entity.getName();
        this.isPublic = true;
    }

    public OwnerData(String name, boolean isPublic)
    {
        this.ownerName = name;
        this.isPublic = isPublic;
    }

    private OwnerData()
    {
        this.ownerName = "";
        this.isPublic = true;
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

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeOwnerTagToNBT(nbt, this.ownerUUIDMost, this.ownerUUIDLeast, this.ownerName, this.isPublic);
    }

    public static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, Entity entity)
    {
        return writeOwnerTagToNBT(nbt, entity, true);
    }

    /**
     * Adds OwnerData to the item, but only if it doesn't exist yet.
     */
    public static void addOwnerDataToItemOptional(ItemStack stack, Entity entity)
    {
        addOwnerDataToItemOptional(stack, entity, false);
    }

    /**
     * Adds OwnerData to the item, but only if it doesn't exist yet.
     */
    public static void addOwnerDataToItemOptional(ItemStack stack, Entity entity, boolean isPublic)
    {
        OwnerData data = getOwnerDataFromItem(stack);

        if (data == null)
        {
            writeOwnerTagToItem(stack, entity, isPublic);
        }
    }

    /**
     * Adds OwnerData to the selected module in the item, but only if it doesn't exist yet.
     */
    public static void addOwnerDataToSelectedModuleOptional(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        addOwnerDataToSelectedModuleOptional(toolStack, moduleType, entity, false);
    }

    /**
     * Adds OwnerData to the selected module in the item, but only if it doesn't exist yet.
     */
    public static void addOwnerDataToSelectedModuleOptional(ItemStack toolStack, ModuleType moduleType, Entity entity, boolean isPublic)
    {
        OwnerData data = getOwnerDataFromSelectedModule(toolStack, moduleType);

        if (data == null)
        {
            writeOwnerTagToSelectedModule(toolStack, moduleType, entity, isPublic);
        }
    }

    /**
     * Toggles the privacy mode on the item, if the entity is the owner.
     * If the item doesn't yet have OwnerData, then the data will be added, in Private mode.
     */
    public static void togglePrivacyModeOnItem(ItemStack stack, Entity entity)
    {
        OwnerData data = getOwnerDataFromItem(stack);

        if (data == null)
        {
            addOwnerDataToItemOptional(stack, entity);
        }
        else if (data.isOwner(entity))
        {
            data.isPublic = ! data.isPublic;
            data.writeToItem(stack);
        }
    }

    /**
     * Toggles the privacy mode on the selected module on the item, if the entity is the owner.
     * If the module doesn't yet have OwnerData, then the data will be added, in Private mode.
     */
    public static void togglePrivacyModeOnSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        OwnerData data = getOwnerDataFromSelectedModule(toolStack, moduleType);

        if (data == null)
        {
            addOwnerDataToSelectedModuleOptional(toolStack, moduleType, entity);
        }
        else if (data.isOwner(entity))
        {
            data.isPublic = ! data.isPublic;
            data.writeToSelectedModule(toolStack, moduleType);
        }
    }

    public static OwnerData getOwnerDataFromNBT(NBTTagCompound nbt)
    {
        return new OwnerData().readFromNBT(nbt);
    }

    public static OwnerData getOwnerDataFromItem(ItemStack stack)
    {
        if (stack.isEmpty() == false)
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

        return this.ownerUUID != null && this.ownerUUID.equals(entity.getUniqueID());
    }

    private boolean isOwner(UUID uuid)
    {
        if (uuid == null)
        {
            return false;
        }

        return this.ownerUUID != null && this.ownerUUID.equals(uuid);
    }

    public boolean canAccess(Entity entity)
    {
        return this.isPublic || this.isOwner(entity);
    }

    public boolean canAccess(UUID uuid)
    {
        return this.isPublic || this.isOwner(uuid);
    }

    /*
    public static boolean isOwnerOfItem(ItemStack stack, Entity entity)
    {
        if (stack.isEmpty() == false && stack.getTagCompound() != null)
        {
            OwnerData data = getOwnerDataFromItem(stack);
            return data != null && data.isOwner(entity);
        }

        return false;
    }

    public static boolean isOwnerOfSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        return isOwnerOfItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), entity);
    }
    */

    /**
     * Check if the given entity is allowed to access this item.
     * Returns true if there is no owner information stored yet, or if the privacy mode is set to Public, or if the given entity is the owner.
     */
    public static boolean canAccessItem(ItemStack stack, @Nullable Entity entity)
    {
        return canAccessItem(stack, entity != null ? entity.getUniqueID() : null);
    }

    /**
     * Check if the given UUID is allowed to access this item.
     * Returns true if there is no owner information stored yet, or if the privacy mode is set to Public, or if the given UUID is the owner.
     */
    public static boolean canAccessItem(ItemStack stack, @Nullable UUID uuid)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        if (stack.getTagCompound() == null)
        {
            return true;
        }

        OwnerData data = getOwnerDataFromItem(stack);
        return data == null || data.isPublic || data.isOwner(uuid);
    }

    public static boolean canAccessSelectedModule(ItemStack toolStack, ModuleType moduleType, @Nullable Entity entity)
    {
        return canAccessItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType), entity);
    }

    private static boolean nbtHasOwnerTag(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return false;
        }

        if (nbt.hasKey("Owner", Constants.NBT.TAG_COMPOUND) == false &&
            nbt.hasKey("Player", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.hasKey("Owner", Constants.NBT.TAG_COMPOUND) ?
                nbt.getCompoundTag("Owner") : nbt.getCompoundTag("Player");

        if (tag != null &&
            tag.hasKey("UUIDM", Constants.NBT.TAG_LONG) &&
            tag.hasKey("UUIDL", Constants.NBT.TAG_LONG) &&
            tag.hasKey("Name", Constants.NBT.TAG_STRING))
        {
            return true;
        }

        return false;
    }

    private OwnerData readFromNBT(NBTTagCompound nbt)
    {
        if (nbtHasOwnerTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.hasKey("Owner", Constants.NBT.TAG_COMPOUND) ? nbt.getCompoundTag("Owner") : nbt.getCompoundTag("Player");
        this.ownerUUIDMost = tag.getLong("UUIDM");
        this.ownerUUIDLeast = tag.getLong("UUIDL");
        this.ownerName = tag.getString("Name");
        this.isPublic = tag.getBoolean("Public");
        this.ownerUUID = new UUID(this.ownerUUIDMost, this.ownerUUIDLeast);

        return this;
    }

    private static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, long uuidMost, long uuidLeast, String name, boolean isPublic)
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
        nbt.setTag("Owner", tag);

        return nbt;
    }

    private static NBTTagCompound writeOwnerTagToNBT(NBTTagCompound nbt, Entity entity, boolean isPublic)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        if (entity == null)
        {
            nbt.removeTag("Owner");
            nbt.removeTag("Player"); // legacy
            return nbt;
        }

        return writeOwnerTagToNBT(nbt, entity.getUniqueID().getMostSignificantBits(), entity.getUniqueID().getLeastSignificantBits(), entity.getName(), isPublic);
    }

    private static void writeOwnerTagToItem(ItemStack stack, Entity entity, boolean isPublic)
    {
        stack.setTagCompound(writeOwnerTagToNBT(stack.getTagCompound(), entity, isPublic));
    }

    /**
     * Removes the OwnerData from this ItemStack, if the entity doing this is the current owner.
     * @param stack
     * @param entity
     * @return true if the OwnerData tag was successfully removed
     */
    public static boolean removeOwnerDataFromItem(ItemStack stack, Entity entity)
    {
        OwnerData ownerData = OwnerData.getOwnerDataFromItem(stack);

        if (ownerData != null && ownerData.isOwner(entity))
        {
            stack.getTagCompound().removeTag("Owner");

            if (stack.getTagCompound().isEmpty())
            {
                stack.setTagCompound(null);
            }

            return true;
        }

        return false;
    }

    /**
     * Removes the OwnerData from the currently selected module ItemStack, if the entity doing this is the current owner.
     * @param toolStack
     * @param moduleType
     * @param entity
     * @return true if the OwnerData tag was successfully removed
     */
    public static boolean removeOwnerDataFromSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);

        if (moduleStack.isEmpty() == false)
        {
            removeOwnerDataFromItem(moduleStack, entity);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    private static boolean writeOwnerTagToSelectedModule(ItemStack toolStack, ModuleType moduleType, Entity entity, boolean isPublic)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);

        if (moduleStack.isEmpty() == false)
        {
            writeOwnerTagToItem(moduleStack, entity, isPublic);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    private boolean writeToItem(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            stack.setTagCompound(this.writeToNBT(stack.getTagCompound()));
            return true;
        }

        return false;
    }

    private boolean writeToSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);

        if (moduleStack.isEmpty() == false)
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
}
