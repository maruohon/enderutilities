package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryItemModularModules extends InventoryItemModules
{
    protected InventoryItemModular inventoryItemModular;

    public InventoryItemModularModules (InventoryItemModular invModular, ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        super(containerStack, invSize, isRemote, player);
        this.inventoryItemModular = invModular;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.inventoryItemModular.readFromContainerItemStack();
    }
}
