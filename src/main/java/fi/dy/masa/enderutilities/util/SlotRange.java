package fi.dy.masa.enderutilities.util;

import net.minecraftforge.items.IItemHandler;

public class SlotRange
{
    public final int first;
    /** The last slot in the range */
    public final int lastInc;
    /** The end of the slot range, exclusive (meaning one larger than the last slot number) */
    public final int lastExc;

    public SlotRange(IItemHandler inv)
    {
        this(0, inv.getSlots());
    }

    public SlotRange(int start, int numSlots)
    {
        this.first = start;
        this.lastInc = start + numSlots - 1;
        this.lastExc = start + numSlots;
    }

    public boolean contains(int slot)
    {
        return slot >= this.first && slot <= this.lastInc;
    }

    @Override
    public String toString()
    {
        return String.format("SlotRange: {first: %d, lastInc: %d, lastExc: %d}", this.first, this.lastInc, this.lastExc);
    }
}
