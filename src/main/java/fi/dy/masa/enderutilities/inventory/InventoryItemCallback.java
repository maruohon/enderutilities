package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryItemCallback extends InventoryItem
{
    private final IModularInventoryHolder callback;

    public InventoryItemCallback(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player, IModularInventoryHolder callback)
    {
        super(containerStack, invSize, isRemote, player);
        this.callback = callback;
    }

    public InventoryItemCallback(ItemStack containerStack, int invSize, boolean allowCustomStackSizes, boolean isRemote, EntityPlayer player, IModularInventoryHolder callback)
    {
        super(containerStack, invSize, 64, allowCustomStackSizes, isRemote, player);
        this.callback = callback;
    }

    @Override
    public ItemStack getContainerItemStack()
    {
        if (this.callback != null)
        {
            return this.callback.getContainerStack();
        }

        return super.getContainerItemStack();
    }
}
