package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerLargeStacks extends ContainerEnderUtilitiesCustomSlotClick
{
    public ContainerLargeStacks(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        super(inventoryPlayer, inventory);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Player inventory
        if (slot.inventory != this.inventory)
        {
            return super.getMaxStackSizeFromSlotAndStack(slot, stack);
        }

        // Our inventory
        return slot.getSlotStackLimit();
    }
}
