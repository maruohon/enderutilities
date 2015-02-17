package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModule
{
    /**
     * Returns the ModuleType of this module.
     * @param stack
     * @return
     */
    public ModuleType getModuleType(ItemStack stack);

    /**
     * Returns the module tier. Valid tiers are in the range of 0..n.
     * Invalid items should return -1.
     * @param stack
     * @return
     */
    public int getModuleTier(ItemStack stack);
}
