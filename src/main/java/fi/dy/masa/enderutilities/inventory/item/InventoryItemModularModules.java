package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.item.ItemStack;

public class InventoryItemModularModules extends InventoryItemModules
{
    protected InventoryItemModular inventoryItemModular;

    public InventoryItemModularModules (InventoryItemModular invModular, ItemStack containerStack, int invSize, boolean isRemote)
    {
        super(containerStack, invSize, isRemote);
        this.inventoryItemModular = invModular;
    }

    @Override
    public ItemStack getContainerItemStack()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    @Override
    public void onContentsChanged(int slot)
    {
        if (this.isRemote == false)
        {
            //System.out.println("InventoryItemModularModules#markDirty() - " + (this.isRemote ? "client" : "server"));
            super.onContentsChanged(slot);
            this.inventoryItemModular.readFromContainerItemStack();
        }
    }
}
