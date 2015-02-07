package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModule
{
    public ModuleType getModuleType(ItemStack stack);

    public int getModuleTier(ItemStack stack);
}
