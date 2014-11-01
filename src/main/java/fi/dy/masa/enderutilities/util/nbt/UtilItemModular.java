package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;

public class UtilItemModular
{
	public enum ModuleType
	{
		// Note, the value of these modules is also used as the offset when rendering the slot backgrounds from the GUI texture!
		TYPE_ENDERCORE_ACTIVE	(0, 0x01),
		TYPE_ENDERCAPACITOR		(1, 0x02),
		TYPE_LINKCRYSTAL		(2, 0x04),
		TYPE_MOBPERSISTANCE		(3, 0x08),
		TYPE_ANY				(-1, 0x00),
		TYPE_INVALID			(-10, 0x00);

		private int index;
		private int bitmask;

		ModuleType(int index, int bitmask)
		{
			this.index = index;
			this.bitmask = bitmask;
		}

		public int getOrdinal()
		{
			return this.index;
		}

		public int getModuleBitmask()
		{
			return this.bitmask;
		}

		public boolean equals(ModuleType val)
		{
			return val.getOrdinal() == this.index;
		}

		public boolean equals(int val)
		{
			return val == this.index;
		}
	}

	/* Return whether the given module type has been installed. */
	public static boolean hasModule(ItemStack stack, ModuleType moduleType)
	{
		if (stack == null) { return false; }
		return false;
	}

	/* Returns the number of installed modules of the given type. */
	public static int getModuleCount(ItemStack stack, ModuleType moduleType)
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
	public static int getModuleTier(ItemStack stack, ModuleType moduleType)
	{
		if (stack == null) { return 0; }
		return 0;
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	public static ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
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

	/* Returns the type of module the input stack contains. */
	public static ModuleType getModuleType(ItemStack moduleStack)
	{
		// Active Ender Core
		if (moduleStack.getItem() == EnderUtilitiesItems.enderPart && moduleStack.getItemDamage() >= 15 && moduleStack.getItemDamage() <= 17)
		{
			return UtilItemModular.ModuleType.TYPE_ENDERCORE_ACTIVE;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.enderCapacitor)
		{
			return UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR;
		}

		if (moduleStack.getItem() == EnderUtilitiesItems.linkCrystal)
		{
			return UtilItemModular.ModuleType.TYPE_LINKCRYSTAL;
		}

		return UtilItemModular.ModuleType.TYPE_INVALID;
	}
}
