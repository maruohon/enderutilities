package fi.dy.masa.enderutilities.item.base;

import javax.annotation.Nonnull;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface IAnvilRepairable
{
    /**
     * Repair the tool by the provided amount of uses.
     * If amount is -1, then the tool will be fully repaired.
     * @param stack
     * @param amount
     * @return true if the repair operation was successful and repaired at least some uses
     */
    public boolean repairItem(@Nonnull ItemStack stack, int amount);

    /**
     * Check whether the given item <b>material</b> can be used to repair this item.
     * @param stackTool
     * @param stackMaterial
     * @return true if <b>stackMaterial</b> is a valid repair item for <b>stackTool</b>
     */
    public boolean isRepairItem(@Nonnull ItemStack stackTool, @Nonnull ItemStack stackMaterial);

    /**
     * Check if the given enchantment is valid to apply to this item
     * @param stackTool
     * @param stackBook
     * @return
     */
    public boolean canApplyEnchantment(@Nonnull ItemStack stackTool, Enchantment enchantment);
}
