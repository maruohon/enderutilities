package fi.dy.masa.enderutilities.inventory.container.base;

import net.minecraftforge.items.IItemHandler;

public class MergeSlotRange extends SlotRange
{
    public final boolean existingOnly;

    public MergeSlotRange(IItemHandler inv)
    {
        this(0, inv.getSlots());
    }

    public MergeSlotRange (int start, int numSlots)
    {
        this(start, numSlots, false);
    }

    public MergeSlotRange (int start, int numSlots, boolean existingOnly)
    {
        super(start, numSlots);
        this.existingOnly = existingOnly;
    }
}
