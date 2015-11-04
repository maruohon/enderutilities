package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.base.IModule;

public class InventoryItemModules extends InventoryItem
{
    protected InventoryItemModular inventoryItemModular;

    public InventoryItemModules(InventoryItemModular modularInventory, ItemStack containerStack, int invSize, World world, EntityPlayer player)
    {
        super(containerStack, invSize, world, player);
        this.inventoryItemModular = modularInventory;
    }

    /*@Override
    public ItemStack getContainerItemStack()
    {
        return this.inventoryItemModular.getModularItemStack();
    }*/

    @Override
    public void writeToContainerItemStack()
    {
        super.writeToContainerItemStack();

        if (this.isRemote == false)
        {
            this.inventoryItemModular.readFromContainerItemStack();
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (this.inventoryItemModular.getContainerItemStack() == null)
        {
            return false;
        }

        return stack == null || stack.getItem() instanceof IModule;
    }
}
