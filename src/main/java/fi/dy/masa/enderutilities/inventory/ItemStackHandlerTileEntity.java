package fi.dy.masa.enderutilities.inventory;

import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ItemStackHandlerTileEntity extends ItemStackHandlerBasic
{
    protected final TileEntityEnderUtilitiesInventory te;
    protected final int inventoryId;

    public ItemStackHandlerTileEntity(int invSize, TileEntityEnderUtilitiesInventory te)
    {
        this(0, invSize, te);
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, TileEntityEnderUtilitiesInventory te)
    {
        super(invSize);
        this.te = te;
        this.inventoryId = inventoryId;
    }

    public ItemStackHandlerTileEntity(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName, TileEntityEnderUtilitiesInventory te)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.te = te;
        this.inventoryId = inventoryId;
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        this.te.inventoryChanged(this.inventoryId, slot);
        this.te.markDirty();
    }
}
