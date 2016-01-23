package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

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

    @Override
    public int getInventoryStackLimit()
    {
        ItemStack stack = this.getContainerItemStack();
        if (stack != null && stack.getItem() == EnderUtilitiesItems.enderPart)
        {
            int tier = ((IModule) stack.getItem()).getModuleTier(stack);
            if (tier >= 6 && tier <= 12)
            {
                return (int)Math.pow(2, tier);
            }
        }

        return super.getInventoryStackLimit();
    }
}
