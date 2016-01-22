package fi.dy.masa.enderutilities.util;

public class SlotRange
{
    public final int first;
    /** The last slot in the range */
    public final int lastInc;
    /** The end of the slot range, exclusive (meaning one larger than the last slot number) */
    public final int lastExc;

    public SlotRange(int start, int numSlots)
    {
        this.first = start;
        this.lastInc = start + numSlots - 1;
        this.lastExc = start + numSlots;
    }

    @Override
    public String toString()
    {
        return String.format("SlotRange: {first: %d, lastInc: %d, lastExc: %d}", this.first, this.lastInc, this.lastExc);
    }
}
