package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;

public class InventoryItemCallback extends InventoryItem
{
    private final IModularInventoryHolder callback;

    public InventoryItemCallback(ItemStack containerStack, int invSize, boolean allowCustomStackSizes,
            boolean isRemote, IModularInventoryHolder callback)
    {
        this(containerStack, invSize, 64, allowCustomStackSizes, isRemote, callback, "Items");
    }

    public InventoryItemCallback(ItemStack containerStack, int invSize, int maxStackSize, boolean allowCustomStackSizes,
            boolean isRemote, IModularInventoryHolder callback, String tagName)
    {
        super(containerStack, invSize, maxStackSize, allowCustomStackSizes, isRemote, tagName);
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
            ((TileEntity) this.callback).markDirty();
        }
    }
}
