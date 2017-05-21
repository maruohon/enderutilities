package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public interface IModular
{
    /**
     * Returns the number of installed modules of the given type.
     * @param containerStack
     * @param moduleType
     * @return number of installed modules of the given type
     */
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType);

    /**
     * Returns the maximum number of modules that can be installed on this item.
     * @param containerStack
     * @return maximum number of modules that can be installed on this item
     */
    public int getMaxModules(ItemStack containerStack);

    /**
     * Returns the maximum number of modules of the given type that can be installed on this item.
     * @param containerStack
     * @param moduleType
     * @return maximum number of modules of the given type
     */
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType);

    /**
     * Returns the maximum number of the given module type that can be installed on this item.
     * This is for exact module checking (module tier or sub-type), instead of the generic module type.
     * @param containerStack
     * @param moduleStack
     * @return maximum number of the given module type
     */
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack);

    /**
     * Returns the (maximum, if multiple) tier of the installed module of the given type.
     * @param containerStack
     * @param moduleType
     * @return (maximum, if multiple) tier of the installed module of the given type
     */
    public int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType);

    /**
     * Returns the tier of the selected module of the given type.
     * @param containerStack
     * @param type
     * @return the tier of the selected module of the given type
     */
    public int getSelectedModuleTier(ItemStack containerStack, ModuleType type);

    /**
     * Returns the ItemStack of the (selected, if multiple) given module type.
     * @param containerStack
     * @param moduleType
     * @return the ItemStack of the selected module of the given module type, or null
     */
    public ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType);

    /**
     * Sets the currently selected module's ItemStack of the given module type to the one provided.
     * @param containerStack
     * @param moduleType
     * @param moduleStack
     * @return
     */
    public boolean setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack moduleStack);

    /**
     * Change the currently selected module to the next one, if any.
     * @param containerStack
     * @param moduleType
     * @param reverse
     * @return
     */
    public boolean changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse);

    /**
     * Callback for when the modules in a modular item are changed in a Tool Workstation
     * @param containerStack
     */
    public void onModulesChanged(ItemStack containerStack);
}
