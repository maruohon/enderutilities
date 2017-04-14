package fi.dy.masa.enderutilities.inventory.container.base;

public interface IScrollableInventory
{
    /**
     * @return the current inventory index offset that should be applied for each slot
     */
    public int getSlotOffset();
}
