package fi.dy.masa.enderutilities.item;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemEnderLasso extends ItemLocationBoundModular
{
    public static final int ENDER_CHARGE_COST = 2000;

    public ItemEnderLasso()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_LASSO);
    }

    /* Returns the maximum number of modules that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 5;
    }

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        if (moduleType.equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 1;
        }

        return 0;
    }

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        ModuleType moduleType = ((IModule) moduleStack.getItem()).getModuleType(moduleStack);
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            // Only allow the in-world/location type Link Crystals by default
            if (((IModule) moduleStack.getItem()).getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION)
            {
                return 3;
            }
        }
        else
        {
            return this.getMaxModules(toolStack, moduleType);
        }

        return 0;
    }
}
