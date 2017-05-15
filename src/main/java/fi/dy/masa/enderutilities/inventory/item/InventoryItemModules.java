package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModule;

public class InventoryItemModules extends InventoryItem
{
    public InventoryItemModules(ItemStack containerStack, int invSize, boolean isRemote)
    {
        super(containerStack, invSize, 1, false, isRemote);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItemModules#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        if (super.isItemValidForSlot(slotNum, stack) == false)
        {
            return false;
        }

        return stack.isEmpty() == false && stack.getItem() instanceof IModule;
    }
}
