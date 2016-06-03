package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;

public class InventoryItemCallback extends InventoryItem
{
    private final IModularInventoryHolder callback;

    public InventoryItemCallback(ItemStack containerStack, int invSize, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, IModularInventoryHolder callback)
    {
        this(containerStack, invSize, 64, allowCustomStackSizes, isRemote, player, callback);
    }

    public InventoryItemCallback(ItemStack containerStack, int invSize, int maxStackSize, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, IModularInventoryHolder callback)
    {
        this(containerStack, invSize, maxStackSize, allowCustomStackSizes, isRemote, player, callback, "Items");
    }

    public InventoryItemCallback(ItemStack containerStack, int invSize, int maxStackSize, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, IModularInventoryHolder callback, String tagName)
    {
        super(containerStack, invSize, maxStackSize, allowCustomStackSizes, isRemote, player, tagName);
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
    public void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        if (this.callback instanceof TileEntity)
        {
            ((TileEntity)this.callback).markDirty();
        }
    }
}
