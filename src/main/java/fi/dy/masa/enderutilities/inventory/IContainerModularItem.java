package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

public interface IContainerModularItem
{
    /**
     * Returns the ItemStack holding the modular item, or null if one isn't present.
     */
    public ItemStack getModularItem();
}
