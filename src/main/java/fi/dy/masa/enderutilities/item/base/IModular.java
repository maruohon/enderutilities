package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModular
{
    /* Returns the number of installed modules of the given type. */
    public int getModuleCount(ItemStack stack, ModuleType moduleType);

    /* Returns the maximum number of modules that can be installed on this item. */
    public int getMaxModules(ItemStack stack);

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    public int getMaxModules(ItemStack stack, ModuleType moduleType);

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack);

    /* Returns the (max, if multiple) tier of the installed module. */
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType);

    /* Returns the tier of the selected module of the given type. */
    public int getSelectedModuleTier(ItemStack stack, ModuleType type);

    /* Returns the ItemStack of the (selected, if multiple) given module type. */
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType);

    /* Sets the selected modules' ItemStack of the given module type to the one provided. */
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack);

    /* Change the selected module to the next one, if any. */
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse);

    /* Returns a list of all the installed modules. */
    public List<NBTTagCompound> getAllModules(ItemStack stack);

    /* Sets the modules to the ones provided in the list. */
    public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules);

    /* Sets the module indicated by the position to the one provided in the compound tag. */
    public ItemStack setModule(ItemStack stack, int index, NBTTagCompound moduleStack);
}
