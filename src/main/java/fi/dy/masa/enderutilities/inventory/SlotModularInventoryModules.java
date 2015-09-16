package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotModularInventoryModules extends Slot
{
    protected InventoryItemModularModules inventoryModules;

    public SlotModularInventoryModules(InventoryItemModularModules inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
        this.inventoryModules = inventory;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    /**
     * Checks if the given ItemStack is valid for this slot.
     * Does the check by calling isItemValidForSlot() on the IInventory.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return this.inventoryModules.isItemValidForSlot(this.slotNumber, stack);
    }

    /* TODO: Enable this in 1.8; in 1.7.10, there is a Forge bug that causes
     * these background icons to render incorrectly if there is an item with the glint effect
     * before the slot with the background icon.
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex()
    {
        return EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
    }
    */
}
