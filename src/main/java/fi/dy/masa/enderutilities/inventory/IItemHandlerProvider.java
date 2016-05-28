package fi.dy.masa.enderutilities.inventory;

import net.minecraftforge.items.IItemHandler;

public interface IItemHandlerProvider
{
    /**
     * Returns the currently active inventory.
     * Used for the inventory muxer.
     * @return the currently active inventory to be used
     */
    IItemHandler getInventory();
}
