package fi.dy.masa.enderutilities.network.message;

import net.minecraft.item.ItemStack;

public interface ISyncableTile
{
    /**
     * Syncs an array of integer values, and an array of ItemStacks. The array lengths may be zero, but the arrays themselves will not be null.
     * <b>NOTE:</b> Neither of the arrays may be longer than 15 entries!!
     * @param value
     */
    public void syncTile(int[] values, ItemStack stacks[]);
}
