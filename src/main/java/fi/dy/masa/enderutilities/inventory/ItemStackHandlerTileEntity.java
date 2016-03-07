package fi.dy.masa.enderutilities.inventory;

import net.minecraft.tileentity.TileEntity;

public class ItemStackHandlerTileEntity extends ItemStackHandlerBasic
{
    protected final TileEntity te;

    public ItemStackHandlerTileEntity(int invSize, TileEntity te)
    {
        super(invSize);
        this.te = te;
    }

    public ItemStackHandlerTileEntity(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName, TileEntity te)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.te = te;
    }

    @Override
    protected void onContentsChanged()
    {
        super.onContentsChanged();

        this.te.markDirty();
    }
}
