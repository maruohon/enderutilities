package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModule
{
    public ModuleType getModuleType(ItemStack stack);

    /* Returns the module tier. Valid tiers are in the range 0..n.
     * Invalid items should return -1.
     */
    public int getModuleTier(ItemStack stack);
}
