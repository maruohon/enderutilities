package fi.dy.masa.enderutilities.inventory;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ItemStackHandlerTileEntity extends ItemStackHandlerBasic
{
    protected final TileEntityEnderUtilitiesInventory te;

    public ItemStackHandlerTileEntity(int invSize, TileEntityEnderUtilitiesInventory te)
    {
        super(invSize);
        this.te = te;
    }

    public ItemStackHandlerTileEntity(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName, TileEntityEnderUtilitiesInventory te)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.te = te;
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        this.te.inventoryChanged(slot);
        this.te.markDirty();
    }
}
