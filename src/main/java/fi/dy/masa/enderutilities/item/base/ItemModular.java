package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperItemModular;

public abstract class ItemModular extends ItemEnderUtilities implements IModular
{
	/* Return whether the given module type has been installed. */
	@Override
	public boolean hasModule(ItemStack stack, int moduleType)
	{
		return NBTHelperItemModular.hasModule(stack, moduleType);
	}

	/* Returns the number of installed modules of the given type. */
	@Override
	public int getModuleCount(ItemStack stack, int moduleType)
	{
		return NBTHelperItemModular.getModuleCount(stack, moduleType);
	}

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	@Override
	public int getInstalledModulesMask(ItemStack stack)
	{
		return NBTHelperItemModular.getInstalledModulesMask(stack);
	}

	/* Returns the (max, if multiple) tier of the installed module. */
	@Override
	public int getModuleTier(ItemStack stack, int moduleType)
	{
		return NBTHelperItemModular.getModuleTier(stack, moduleType);
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	@Override
	public ItemStack getSelectedModuleStack(ItemStack stack, int moduleType)
	{
		return NBTHelperItemModular.getSelectedModuleStack(stack, moduleType);
	}

	/* Returns a list of all the installed modules. */
	@Override
	public List<NBTTagCompound> getAllModules(ItemStack stack)
	{
		return NBTHelperItemModular.getAllModules(stack);
	}

	/* Sets the modules to the ones provided in the list. */
	@Override
	public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
	{
		return NBTHelperItemModular.setAllModules(stack, modules);
	}

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	@Override
	public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
	{
		return NBTHelperItemModular.setModule(stack, index, nbt);
	}
}
