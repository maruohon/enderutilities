package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.InventoryCraftingWrapper;

public class SlotCraftingWrapper extends SlotItemHandlerGeneric
{
    private final InventoryCraftingWrapper inventory;

    public SlotCraftingWrapper(InventoryCraftingWrapper inventory, int index, int xPosition, int yPosition)
    {
        super(inventory.getBaseInventory(), index, xPosition, yPosition);

        this.inventory = inventory;
    }

    @Override
    public void putStack(ItemStack stack)
    {
        super.putStack(stack);
        this.inventory.markDirty();
    }

    @Override
    public void syncStack(ItemStack stack)
    {
        this.inventory.getBaseInventory().syncStackInSlot(this.getSlotIndex(), stack);
        this.inventory.markDirty();
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate)
    {
        stack = super.insertItem(stack, simulate);

        this.inventory.markDirty();

        return stack;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        ItemStack stack = super.decrStackSize(amount);

        this.inventory.markDirty();

        return stack;
    }
}
