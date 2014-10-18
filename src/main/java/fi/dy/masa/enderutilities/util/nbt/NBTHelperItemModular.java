package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NBTHelperItemModular
{
	/* Return whether the given module type has been installed. */
	public static boolean hasModule(ItemStack stack, int moduleType)
	{
		if (stack == null) { return false; }
		return false;
	}

	/* Returns the number of installed modules of the given type. */
	public static int getModuleCount(ItemStack stack, int moduleType)
	{
		if (stack == null) { return 0; }
		return 0;
	}

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	public static int getInstalledModulesMask(ItemStack stack)
	{
		if (stack == null) { return 0; }
		return 0;
	}

	/* Returns the (max, if multiple) tier of the installed module. */
	public static int getModuleTier(ItemStack stack, int moduleType)
	{
		if (stack == null) { return 0; }
		return 0;
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	public static ItemStack getSelectedModuleStack(ItemStack stack, int moduleType)
	{
		if (stack == null) { return null; }
		return null;
	}

	/* Returns a list of all the installed modules. */
	public static List<NBTTagCompound> getAllModules(ItemStack stack)
	{
		if (stack == null) { return null; }
		return null;
	}

	/* Sets the modules to the ones provided in the list. */
	public static ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
	{
		if (stack == null) { return null; }
		return stack;
	}

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	public static ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
	{
		if (stack == null) { return null; }
		return stack;
	}
}
