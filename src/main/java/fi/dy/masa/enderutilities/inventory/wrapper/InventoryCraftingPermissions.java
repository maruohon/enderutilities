package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSyncable;

public class InventoryCraftingPermissions extends InventoryCraftingEnderUtilities implements IItemHandlerSyncable
{
    private final ItemHandlerWrapperPermissions craftMatrix;

    public InventoryCraftingPermissions(int width, int height,
            ItemHandlerWrapperPermissions craftMatrix, ItemHandlerCraftResult resultInventory, EntityPlayer player)
    {
        super(width, height, craftMatrix, resultInventory, player);
        this.craftMatrix = craftMatrix;
    }

    public ItemHandlerWrapperPermissions getBaseInventory()
    {
        return this.craftMatrix;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return this.craftMatrix.isAccessibleByPlayer(player);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return this.craftMatrix.isItemValidForSlot(slot, stack);
    }

    @Override
    public void syncStackInSlot(int slot, ItemStack stack)
    {
        this.craftMatrix.syncStackInSlot(slot, stack);
    }
}
