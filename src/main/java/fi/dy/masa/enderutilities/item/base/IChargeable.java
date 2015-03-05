package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;

public interface IChargeable
{
    /**
     * Returns the total charge capacity of this item.
     */
    public int getCapacity(ItemStack stack);

    /**
     * Sets the total charge capacity of this item.
     */
    public void setCapacity(ItemStack stack, int capacity);

    /**
     * Returns the current amount of charge stored in this item.
     */
    public int getCharge(ItemStack stack);

    /**
     * Adds or simulates adding charge to this item.
     * @param stack The target ItemStack
     * @param amount The amount of charge to add
     * @param simulate True if we just want to simulate adding charge, and not actually do it
     * @return The amount of charge that was or would have been successfully added
     */
    public int addCharge(ItemStack stack, int amount, boolean simulate);

    /**
     * Uses or simulates using charge from this item.
     * @param stack The target ItemStack
     * @param amount The amount of charge to use
     * @param simulate True if we just want to simulate using charge, and not actually do it
     * @return The amount of charge that was or would have been successfully drained from the item
     */
    public int useCharge(ItemStack stack, int amount, boolean simulate);
}
