package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class ItemHandlerWrapperPermissions implements IItemHandlerModifiable, IItemHandlerSelective, IItemHandlerSize, IItemHandlerSyncable
{
    private final InventoryItem baseHandler;
    private final UUID accessorUUID;

    public ItemHandlerWrapperPermissions(InventoryItem baseHandler, Entity entity)
    {
        this.baseHandler = baseHandler;
        this.accessorUUID = entity != null ? entity.getUniqueID() : null;
    }

    public boolean isCurrentlyAccessible()
    {
        //System.out.println("ItemHandlerWrapperPermissions#isCurrentlyAccessible() - " + (this.isRemote ? "client" : "server"));
        return this.isAccessibleBy(this.accessorUUID);
    }

    public boolean isAccessibleByPlayer(EntityPlayer player)
    {
        //System.out.println("ItemHandlerWrapperPermissions#isAccessibleByPlayer() - " + (this.isRemote ? "client" : "server"));
        return OwnerData.canAccessItem(this.baseHandler.getContainerItemStack(), player);
    }

    public boolean isAccessibleBy(UUID uuid)
    {
        //System.out.println("ItemHandlerWrapperPermissions#isAccessibleBy() - " + (this.isRemote ? "client" : "server"));
        return OwnerData.canAccessItem(this.baseHandler.getContainerItemStack(), uuid);
    }

    @Override
    public int getSlots()
    {
        return this.baseHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (this.isCurrentlyAccessible())
        {
            return this.baseHandler.getStackInSlot(slot);
        }

        return null;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (this.isCurrentlyAccessible())
        {
            return this.baseHandler.insertItem(slot, stack, simulate);
        }

        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.isCurrentlyAccessible())
        {
            return this.baseHandler.extractItem(slot, amount, simulate);
        }

        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        if (this.isCurrentlyAccessible())
        {
            //System.out.printf("setStackInSlot  - slot: %3d stack: %s\n", slot, stack);
            this.baseHandler.setStackInSlot(slot, stack);
        }
    }

    @Override
    public void syncStackInSlot(int slot, ItemStack stack)
    {
        //System.out.printf("syncStackInSlot - slot: %3d stack: %s\n", slot, stack);
        this.baseHandler.setStackInSlot(slot, stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.baseHandler.getInventoryStackLimit();
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return this.baseHandler.getItemStackLimit(stack);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        if (this.isCurrentlyAccessible())
        {
            return this.baseHandler.isItemValidForSlot(slot, stack);
        }

        return false;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        if (this.isCurrentlyAccessible())
        {
            return this.baseHandler.canExtractFromSlot(slot);
        }

        return false;
    }
}
