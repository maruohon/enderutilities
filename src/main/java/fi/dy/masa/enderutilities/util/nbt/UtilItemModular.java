package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.setup.Configs;

public class UtilItemModular
{
    /**
     * Checks if the given moduleStack is an IModule and the ModuleType of it
     * is the same as moduleType.
     * @param moduleStack
     * @param moduleType
     * @return
     */
    public static boolean moduleTypeEquals(ItemStack moduleStack, ModuleType moduleType)
    {
        if (moduleStack != null && moduleStack.getItem() instanceof IModule
            && ((IModule)moduleStack.getItem()).getModuleType(moduleStack).equals(moduleType) == true)
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the NBTTagList containing all the modules in toolStack, or null in case it fails.
     * @param toolStack
     * @return
     */
    public static NBTTagList getInstalledModules(ItemStack toolStack)
    {
        if (toolStack == null || (toolStack.getItem() instanceof IModular) == false)
        {
            return null;
        }

        NBTTagCompound nbt = toolStack.getTagCompound();
        if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
        {
            return null;
        }

        return nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
    }

    /**
     * Returns the number of installed modules in toolStack of the type moduleType.
     * @param toolStack
     * @param moduleType
     * @return
     */
    public static int getModuleCount(ItemStack toolStack, ModuleType moduleType)
    {
        NBTTagList nbtTagList = getInstalledModules(toolStack);
        if (nbtTagList == null)
        {
            return 0;
        }

        int count = 0;
        int listNumStacks = nbtTagList.tagCount();

        // Read all the module ItemStacks from the tool
        for (int i = 0; i < listNumStacks; ++i)
        {
            ItemStack moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
            if (moduleTypeEquals(moduleStack, moduleType) == true)
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the (maximum, if multiple) tier of the installed module in toolStack of the type moduleType.
     * Valid tiers are in the range of 0..n. Invalid tier/module returns -1.
     * @param toolStack
     * @param moduleType
     * @return
     */
    public static int getMaxModuleTier(ItemStack toolStack, ModuleType moduleType)
    {
        int tier = -1;
        NBTTagList nbtTagList = getInstalledModules(toolStack);
        if (nbtTagList == null)
        {
            return tier;
        }

        int listNumStacks = nbtTagList.tagCount();
        // Read all the module ItemStacks from the tool
        for (int i = 0; i < listNumStacks; ++i)
        {
            ItemStack moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
            if (moduleTypeEquals(moduleStack, moduleType) == true)
            {
                int t = ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
                if (t > tier)
                {
                    tier = t;
                }
            }
        }

        return tier;
    }

    /**
     * Returns the tier of the currently selected module of type moduleType in toolStack,
     * or -1 for invalid or missing modules.
     * @param toolStack
     * @param moduleType
     * @return 0..n for valid modules, -1 for invalid or missing modules
     */
    public static int getSelectedModuleTier(ItemStack toolStack, ModuleType moduleType)
    {
        ItemStack moduleStack = getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return -1;
        }

        return ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
    }

    /**
     * Returns the index (0..num-1) of the currently selected module of type moduleType in toolStack.
     * @param toolStack
     * @param moduleType
     * @return
     */
    public static int getClampedModuleSelection(ItemStack toolStack, ModuleType moduleType)
    {
        if (toolStack == null || toolStack.getTagCompound() == null)
        {
            return 0;
        }

        int selected = toolStack.getTagCompound().getByte("Selected_" + moduleType.getName());
        int num = UtilItemModular.getModuleCount(toolStack, moduleType);
        if (selected >= num)
        {
            // If the selected module number is larger than the current number of installed modules of that type, then select the last one
            selected = (num > 0 ? num - 1 : 0);
        }

        return selected;
    }

    /**
     * Returns the TAG_Compound containing the (currently selected, if multiple) installed module of type moduleType.
     * The tag contains the Slot and the ItemStack data.
     * @param toolStack
     * @param moduleType
     * @return
     */
    public static NBTTagCompound getSelectedModuleTagCompound(ItemStack toolStack, ModuleType moduleType)
    {
        NBTTagList nbtTagList = getInstalledModules(toolStack);
        if (nbtTagList == null)
        {
            return null;
        }

        int listNumStacks = nbtTagList.tagCount();
        int selected = UtilItemModular.getClampedModuleSelection(toolStack, moduleType);

        // Get the selected-th TAG_Compound of the given module type
        for (int i = 0, count = -1; i < listNumStacks && count < selected; ++i)
        {
            NBTTagCompound moduleTag = nbtTagList.getCompoundTagAt(i);
            ItemStack moduleStack = ItemStack.loadItemStackFromNBT(moduleTag);
            if (moduleTypeEquals(moduleStack, moduleType) == true)
            {
                if (++count >= selected)
                {
                    return moduleTag;
                }
            }
        }

        return null;
    }

    /**
     * Returns the ItemStack of the (currently selected, if multiple) installed module of type moduleType.
     * @param toolStack
     * @param moduleType
     * @return
     */
    public static ItemStack getSelectedModuleStack(ItemStack toolStack, ModuleType moduleType)
    {
        NBTTagCompound tag = UtilItemModular.getSelectedModuleTagCompound(toolStack, moduleType);
        if (tag != null)
        {
            return ItemStack.loadItemStackFromNBT(tag);
        }

        return null;
    }

    /**
     * Sets the currently selected module's ItemStack of type moduleStack to the one provided in newModuleStack.
     * @param toolStack
     * @param moduleType
     * @param newModuleStack
     * @return
     */
    public static ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack newModuleStack)
    {
        NBTTagList nbtTagList = getInstalledModules(toolStack);
        if (nbtTagList == null)
        {
            return null;
        }

        NBTTagCompound nbt = toolStack.getTagCompound();
        if (nbt == null) // Redundant check at this point, but whatever
        {
            return null;
        }

        int listNumStacks = nbtTagList.tagCount();
        int selected = getClampedModuleSelection(toolStack, moduleType);

        // Replace the module ItemStack of the selected-th TAG_Compound of the given module type
        for (int i = 0, count = -1; i < listNumStacks && count < selected; ++i)
        {
            NBTTagCompound moduleTag = nbtTagList.getCompoundTagAt(i);
            if (moduleTypeEquals(ItemStack.loadItemStackFromNBT(moduleTag), moduleType) == true)
            {
                if (++count >= selected)
                {
                    // Write the new module ItemStack to the compound tag of the old one, so that we
                    // preserve the Slot tag and any other non-ItemStack tags of the old one.
                    nbtTagList.func_150304_a(i, newModuleStack.writeToNBT(moduleTag));
                    return toolStack;
                }
            }
        }

        return toolStack;
    }

    /**
     * Returns a list of all the installed modules. UNIMPLEMENTED ATM
     * @param stack
     * @return
     */
    public static List<NBTTagCompound> getAllModules(ItemStack stack)
    {
        if (stack == null)
        {
            return null;
        }

        // TODO

        return null;
    }

    /**
     * Sets the modules to the ones provided in the list. UNIMPLEMENTED ATM
     * @param stack
     * @param modules
     * @return
     */
    public static ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
    {
        if (stack == null)
        {
            return null;
        }

        // TODO

        return stack;
    }

    /**
     * Sets the module indicated by the position to the one provided in the compound tag.
     * UNIMPLEMENTED ATM
     * @param stack
     * @param index
     * @param nbt
     * @return
     */
    public static ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
    {
        if (stack == null)
        {
            return null;
        }

        // TODO

        return stack;
    }

    /**
     * Change the currently selected module of type moduleType to the next one, if any.
     * @param toolStack
     * @param moduleType
     * @param reverse True if we want to change to the previous module instead of the next module
     * @return
     */
    public static ItemStack changeSelectedModule(ItemStack toolStack, ModuleType moduleType, boolean reverse)
    {
        int moduleCount = UtilItemModular.getModuleCount(toolStack, moduleType);
        NBTTagCompound nbt = toolStack.getTagCompound();
        if (moduleCount == 0 || nbt == null)
        {
            return toolStack;
        }

        int selected = getClampedModuleSelection(toolStack, moduleType);

        if (reverse == true)
        {
            if (--selected < 0)
            {
                selected = moduleCount - 1;
            }
        }
        else
        {
            if (++selected >= moduleCount)
            {
                selected = 0;
            }
        }

        nbt.setByte("Selected_" + moduleType.getName(), (byte)selected);

        return toolStack;
    }

    /**
     * If the given tool has an Ender Capacitor module installed, and the capacitor has sufficient charge,
     * then the given amount of charge will be drained from it, and true is returned.
     * In case of any errors, no charge will be drained and false is returned.
     * @param stack
     * @param amount
     * @param doUse True to actually drain, false to simulate
     * @return false if the requested amount of charge could not be drained
     */
    public static boolean useEnderCharge(ItemStack stack, int amount, boolean doUse)
    {
        if (Configs.valueUseEnderCharge == false)
        {
            return true;
        }

        if ((stack.getItem() instanceof IModular) == false)
        {
            return false;
        }

        ItemStack moduleStack = getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
        if (moduleStack == null || (moduleStack.getItem() instanceof ItemEnderCapacitor) == false)
        {
            return false;
        }

        ItemEnderCapacitor cap = (ItemEnderCapacitor) moduleStack.getItem();
        if (cap.useCharge(moduleStack, amount, false) < amount)
        {
            return false;
        }

        if (doUse == true)
        {
            cap.useCharge(moduleStack, amount, true);
            setSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR, moduleStack);
        }

        return true;
    }

    /**
     * If the player is in creative mode, then no charge will be required or drained, and true will be returned.
     * Normally:
     * If the given tool has an Ender Capacitor module installed, and the capacitor has sufficient charge,
     * then the given amount of charge will be drained from it, and true is returned.
     * In case of any errors, no charge will be drained and false is returned.
     * @param stack
     * @param player
     * @param amount
     * @param doUse
     * @return
     */
    public static boolean useEnderCharge(ItemStack stack, EntityPlayer player, int amount, boolean doUse)
    {
        if (Configs.valueUseEnderCharge == false || (player != null && player.capabilities.isCreativeMode == true))
        {
            return true;
        }

        return useEnderCharge(stack, amount, doUse);
    }
}
