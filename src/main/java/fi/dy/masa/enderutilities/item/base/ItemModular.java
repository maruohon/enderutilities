package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemModular extends ItemEnderUtilities implements IModular, IKeyBound
{
    public boolean useAbsoluteModuleIndexing(ItemStack stack)
    {
        return false;
    }

    @Override
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getInstalledModuleCount(containerStack, moduleType);
    }

    @Override
    public int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(containerStack, moduleType);
    }

    @Override
    public int getSelectedModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            UtilItemModular.getSelectedModuleTierAbs(containerStack, moduleType);
        }

        return UtilItemModular.getSelectedModuleTier(containerStack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            return UtilItemModular.getSelectedModuleStackAbs(containerStack, moduleType);
        }

        return UtilItemModular.getSelectedModuleStack(containerStack, moduleType);
    }

    @Override
    public boolean setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack moduleStack)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            return UtilItemModular.setSelectedModuleStackAbs(containerStack, moduleType, moduleStack);
        }

        return UtilItemModular.setSelectedModuleStack(containerStack, moduleType, moduleStack);
    }

    @Override
    public boolean changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        if (this.useAbsoluteModuleIndexing(containerStack))
        {
            return UtilItemModular.changeSelectedModuleAbs(containerStack, moduleType, reverse);
        }

        return UtilItemModular.changeSelectedModule(containerStack, moduleType, reverse);
    }
}
