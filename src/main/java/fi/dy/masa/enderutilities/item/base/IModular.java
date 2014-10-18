package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IModular
{
	public static final int MODULE_ENDERCORE = 0x01;
	public static final int MODULE_ENDECAPACITOR = 0x02;
	public static final int MODULE_LINKCRYSTAL = 0x04;
	public static final int MODULE_PERSISTANCE = 0x08;

	/* Return whether the given module type has been installed. */
	public boolean hasModule(ItemStack stack, int moduleType);

	/* Returns the number of installed modules of the given type. */
	public int getModuleCount(ItemStack stack, int moduleType);

	/* Returns the maximum number of modules that can be installed on this item. */
	public int getMaxModules(ItemStack stack);

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	public int getInstalledModulesMask(ItemStack stack);

	/* Returns the (max, if multiple) tier of the installed module. */
	public int getModuleTier(ItemStack stack, int moduleType);

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	public ItemStack getSelectedModuleStack(ItemStack stack, int moduleType);

	/* Returns a list of all the installed modules. */
	public List<NBTTagCompound> getAllModules(ItemStack stack);

	/* Sets the modules to the ones provided in the list. */
	public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules);

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt);
}
