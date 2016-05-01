package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModule;

public class InventoryItemModules extends InventoryItem
{
    public InventoryItemModules(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        super(containerStack, invSize, 1, false, isRemote, player);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItemModules#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        if (super.isItemValidForSlot(slotNum, stack) == false)
        {
            return false;
        }

        return stack == null || stack.getItem() instanceof IModule;
    }
}
