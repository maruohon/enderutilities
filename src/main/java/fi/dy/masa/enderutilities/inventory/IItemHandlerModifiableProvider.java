package fi.dy.masa.enderutilities.inventory;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface IItemHandlerModifiableProvider extends IItemHandlerProvider
{
    /**
     * Returns the currently active inventory.
     * Used for the inventory muxer.
     * @return the currently active inventory to be used
     */
    IItemHandlerModifiable getInventoryModifiable();
}
