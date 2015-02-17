package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModular
{
    /**
     * Returns the number of installed modules of the given type.
     * @param stack
     * @param moduleType
     * @return
     */
    public int getModuleCount(ItemStack stack, ModuleType moduleType);

    /**
     * Returns the maximum number of modules that can be installed on this item.
     * @param stack
     * @return
     */
    public int getMaxModules(ItemStack stack);

    /**
     * Returns the maximum number of modules of the given type that can be installed on this item.
     * @param stack
     * @param moduleType
     * @return
     */
    public int getMaxModules(ItemStack stack, ModuleType moduleType);

    /**
     * Returns the maximum number of the given module type that can be installed on this item.
     * This is for exact module checking (module tier or sub-type), instead of the generic module type.
     * @param toolStack
     * @param moduleStack
     * @return
     */
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack);

    /**
     * Returns the (maximum, if multiple) tier of the installed module of the given type.
     * @param stack
     * @param moduleType
     * @return
     */
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType);

    /**
     * Returns the tier of the selected module of the given type.
     * @param stack
     * @param type
     * @return
     */
    public int getSelectedModuleTier(ItemStack stack, ModuleType type);

    /**
     * Returns the ItemStack of the (selected, if multiple) given module type.
     * @param stack
     * @param moduleType
     * @return
     */
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType);

    /**
     * Sets the currently selected module's ItemStack of the given module type to the one provided.
     * @param toolStack
     * @param moduleType
     * @param moduleStack
     * @return
     */
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack);

    /**
     * Change the currently selected module to the next one, if any.
     * @param stack
     * @param moduleType
     * @param reverse
     * @return
     */
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse);

    /**
     * Returns a list of all the installed modules.
     * @param stack
     * @return
     */
    public List<NBTTagCompound> getAllModules(ItemStack stack);

    /**
     * Sets the modules to the ones provided in the list.
     * @param stack
     * @param modules
     * @return
     */
    public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules);

    /**
     * Sets the module indicated by the position in 'index' to the one provided in moduleStack.
     * @param stack
     * @param index
     * @param moduleStack
     * @return
     */
    public ItemStack setModule(ItemStack stack, int index, NBTTagCompound moduleStack);
}
