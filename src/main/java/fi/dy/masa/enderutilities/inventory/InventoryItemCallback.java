package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class InventoryItemCallback extends InventoryItem
{
    IModularInventoryCallback callback;

    public InventoryItemCallback(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player, IModularInventoryCallback callback)
    {
        super(containerStack, invSize, isRemote, player);
        this.callback = callback;
    }

    public void setCallback(IModularInventoryCallback callback)
    {
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
