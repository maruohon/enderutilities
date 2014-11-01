package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemModular extends ItemEnderUtilities implements IModular
{
	/* Return whether the given module type has been installed. */
	@Override
	public boolean hasModule(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.hasModule(stack, moduleType);
	}

	/* Returns the number of installed modules of the given type. */
	@Override
	public int getModuleCount(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getModuleCount(stack, moduleType);
	}

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	@Override
	public int getInstalledModulesMask(ItemStack stack)
	{
		return UtilItemModular.getInstalledModulesMask(stack);
	}

	/* Returns the (max, if multiple) tier of the installed module. */
	@Override
	public int getModuleTier(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getModuleTier(stack, moduleType);
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	@Override
	public ItemStack getSelectedModuleStack(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getSelectedModuleStack(stack, moduleType);
	}

	/* Returns a list of all the installed modules. */
	@Override
	public List<NBTTagCompound> getAllModules(ItemStack stack)
	{
		return UtilItemModular.getAllModules(stack);
	}

	/* Sets the modules to the ones provided in the list. */
	@Override
	public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
	{
		return UtilItemModular.setAllModules(stack, modules);
	}

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	@Override
	public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
	{
		return UtilItemModular.setModule(stack, index, nbt);
	}
}
