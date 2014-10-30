package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NBTHelperItemModular
{
	// Note, the value of these modules is also used when rendering the slot backgrounds from the GUI texture!
	public static final int TYPE_ENDERCORE = 0;
	public static final int TYPE_ENDERCAPACITOR = 1;
	public static final int TYPE_LINKCRYSTAL = 2;
	public static final int TYPE_MOBPERSISTANCE = 3;

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

	/* Returns the type of module the input stack contains. -1 if it's not a valid upgrade module. */
	public static int getModuleType(ItemStack moduleStack)
	{
		// Active Ender Core
		if (moduleStack.getItem() == EnderUtilitiesItems.enderPart && moduleStack.getItemDamage() >= 15 && moduleStack.getItemDamage() <= 17)
		{
			return TYPE_ENDERCORE;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.enderCapacitor)
		{
			return TYPE_ENDERCAPACITOR;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.linkCrystal)
		{
			return TYPE_LINKCRYSTAL;
		}

		return -1;
	}
}
