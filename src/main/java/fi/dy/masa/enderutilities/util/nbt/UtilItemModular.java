package fi.dy.masa.enderutilities.util.nbt;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.ChunkLoading;

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
     * Returns the number of installed modules in containerStack of the type moduleType.
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
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
     * Returns the (maximum, if multiple) tier of the installed module in containerStack of the type moduleType.
     * Valid tiers are in the range of 0..n. Invalid tier/module returns -1.
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        int tier = -1;
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
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
     * Returns the tier of the currently selected module of type moduleType in containerStack,
     * or -1 for invalid or missing modules.
     * @param containerStack
     * @param moduleType
     * @return 0..n for valid modules, -1 for invalid or missing modules
     */
    public static int getSelectedModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        ItemStack moduleStack = getSelectedModuleStack(containerStack, moduleType);
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return -1;
        }

        return ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
    }

    /**
     * Returns the tier of the currently selected module (if any) of type moduleType in containerStack
     * using absolute module indexing, or -1 for invalid or missing modules.
     * @param containerStack
     * @param moduleType
     * @return 0..n for valid modules, -1 for invalid or missing modules
     */
    public static int getSelectedModuleTierAbs(ItemStack containerStack, ModuleType moduleType)
    {
        int slotNum = getStoredModuleSelection(containerStack, moduleType);
        slotNum += getFirstIndexOfModuleType(containerStack, moduleType);
        ItemStack moduleStack = getModuleStackBySlotNumber(containerStack, slotNum, moduleType);
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return -1;
        }

        return ((IModule) moduleStack.getItem()).getModuleTier(moduleStack);
    }

    /**
     * Returns the index (0..num-1) of the currently selected module of type moduleType in containerStack.
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static int getClampedModuleSelection(ItemStack containerStack, ModuleType moduleType)
    {
        int selected = getStoredModuleSelection(containerStack, moduleType);
        int num = getInstalledModuleCount(containerStack, moduleType);
        if (selected >= num)
        {
            // If the selected module number is larger than the current number of installed modules of that type, then select the last one
            selected = (num > 0 ? num - 1 : 0);
        }

        return selected;
    }

    /**
     * Return the stored module selection position/index of the given module type.
     * It is not clamped to the range of installed modules, but it is clamped to the range of
     * the maximum number of modules of that type.
     * @param containerStack
     * @param moduleType
     * @return the stored selection index (clamped to 0 .. (max - 1), but not clamped to installed modules range)
     */
    public static int getStoredModuleSelection(ItemStack containerStack, ModuleType moduleType)
    {
        if (containerStack == null || containerStack.getTagCompound() == null || (containerStack.getItem() instanceof IModular) == false)
        {
            return 0;
        }

        int selected = containerStack.getTagCompound().getByte("Selected_" + moduleType.getName());
        int max = ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType);

        return selected < max ? selected : max - 1;
    }

    /**
     * Returns the ItemStack of the installed module of type <b>moduleType</b> in the given <b>slotNum</b>.
     * If installed module in the given <b>slotNum</b> is not of type <b>moduleType</b> (and moduleType is not ModuleType.TYPE_ANY)
     * then null is returned.
     */
    public static ItemStack getModuleStackBySlotNumber(ItemStack containerStack, int slotNum, ModuleType moduleType)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
        if (nbtTagList == null)
        {
            return null;
        }

        int listNumStacks = nbtTagList.tagCount();
        for (int i = 0; i < listNumStacks; ++i)
        {
            NBTTagCompound moduleTag = nbtTagList.getCompoundTagAt(i);
            if (moduleTag.getByte("Slot") == slotNum)
            {
                ItemStack moduleStack = ItemStack.loadItemStackFromNBT(moduleTag);
                if (moduleType.equals(ModuleType.TYPE_ANY) || moduleTypeEquals(moduleStack, moduleType) == true)
                {
                    return moduleStack;
                }
                else
                {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Returns the ItemStack of the (currently selected, if multiple) installed module of type moduleType.
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
        if (nbtTagList == null)
        {
            return null;
        }

        int listNumStacks = nbtTagList.tagCount();
        int selected = getClampedModuleSelection(containerStack, moduleType);

        // Get the selected-th TAG_Compound of the given module type
        for (int i = 0, count = -1; i < listNumStacks && count < selected; ++i)
        {
            ItemStack moduleStack = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
            if (moduleTypeEquals(moduleStack, moduleType) == true)
            {
                if (++count >= selected)
                {
                    return moduleStack;
                }
            }
        }

        return null;
    }

    /**
     * Returns the ItemStack of the (currently selected, if any) installed module of type moduleType,
     * using absolute (non empty slot skipping) module indexing.
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static ItemStack getSelectedModuleStackAbs(ItemStack containerStack, ModuleType moduleType)
    {
        int slotNum = getStoredModuleSelection(containerStack, moduleType);
        slotNum += getFirstIndexOfModuleType(containerStack, moduleType);
        return getModuleStackBySlotNumber(containerStack, slotNum, moduleType);
    }

    /**
     * Sets the currently selected module's ItemStack of type moduleStack to the one provided in newModuleStack.
     * @param containerStack
     * @param moduleType
     * @param newModuleStack
     * @return
     */
    public static boolean setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack newModuleStack)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, true);
        int listNumStacks = nbtTagList.tagCount();
        int selected = getClampedModuleSelection(containerStack, moduleType);

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
                    nbtTagList.set(i, newModuleStack.writeToNBT(moduleTag));
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean setSelectedModuleStackAbs(ItemStack containerStack, ModuleType moduleType, ItemStack newModuleStack)
    {
        int slotNum = getStoredModuleSelection(containerStack, moduleType);
        slotNum += getFirstIndexOfModuleType(containerStack, moduleType);
        return setModuleStackBySlotNumber(containerStack, slotNum, newModuleStack);
    }

    /**
     * Sets the module ItemStack in the given slot number <b>slotNum</b> to the one provided in moduleStack.
     * @param containerStack
     * @param moduleType
     * @param newModuleStack
     * @return
     */
    public static boolean setModuleStackBySlotNumber(ItemStack containerStack, int slotNum, ItemStack moduleStack)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, true);
        int listNumStacks = nbtTagList.tagCount();

        // Replace the module ItemStack with slot number slotNum, or add it if it doesn't exist
        for (int i = 0; i < listNumStacks; ++i)
        {
            NBTTagCompound moduleTag = nbtTagList.getCompoundTagAt(i);
            if (moduleTag.hasKey("Slot", Constants.NBT.TAG_BYTE) == true && moduleTag.getByte("Slot") == slotNum)
            {
                // Write the new module ItemStack to the compound tag of the old one, so that we
                // preserve the Slot tag and any other non-ItemStack tags of the old one.
                nbtTagList.set(i, moduleStack.writeToNBT(moduleTag));
                return true;
            }
        }

        // No ItemStack found with slot number slotNum, appending a new tag
        NBTTagCompound moduleTag = new NBTTagCompound();
        moduleTag.setByte("Slot", (byte)slotNum);
        moduleStack.writeToNBT(moduleTag);
        nbtTagList.appendTag(moduleTag);

        return false;
    }

    /**
     * Returns a list of all the installed modules. UNIMPLEMENTED ATM
     * @param containerStack
     * @return
     */
    public static List<NBTTagCompound> getAllModules(ItemStack containerStack)
    {
        if (containerStack == null)
        {
            return null;
        }

        // TODO

        return null;
    }

    /**
     * Sets the modules to the ones provided in the list. <b>UNIMPLEMENTED ATM</b>
     * @param containerStack
     * @param modules
     * @return
     */
    public static boolean setAllModules(ItemStack containerStack, List<NBTTagCompound> modules)
    {
        if (containerStack == null)
        {
            return false;
        }

        // TODO

        return false;
    }

    /**
     * Sets the module indicated by the position to the one provided in the compound tag.
     * <b>UNIMPLEMENTED ATM</b>
     * @param containerStack
     * @param index
     * @param nbt
     * @return
     */
    public static boolean setModule(ItemStack containerStack, int index, NBTTagCompound nbt)
    {
        if (containerStack == null)
        {
            return false;
        }

        // TODO

        return false;
    }

    /**
     * Change the currently selected module of type moduleType to the next one, if any.
     * @param containerStack
     * @param moduleType
     * @param reverse True if we want to change to the previous module instead of the next module
     * @return
     */
    public static boolean changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        int moduleCount = getInstalledModuleCount(containerStack, moduleType);
        NBTTagCompound nbt = containerStack.getTagCompound();
        if (moduleCount == 0 || nbt == null)
        {
            return false;
        }

        int selected = getClampedModuleSelection(containerStack, moduleType);

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

        return true;
    }

    /**
     * Change the currently selected module of type <b>moduleType</b> to the next one (if any),
     * on a modular item that uses absolute module selection index (ie. not empty slot skipping).
     * @param containerStack
     * @param moduleType
     * @param reverse True if we want to change to the previous module instead of the next module
     * @return
     */
    public static boolean changeSelectedModuleAbs(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        if ((containerStack.getItem() instanceof IModular) == false ||
           ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType) <= 0)
        {
            return false;
        }

        int maxOfType = ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType);
        int current = getStoredModuleSelection(containerStack, moduleType);

        if (reverse == true)
        {
            if (--current < 0)
            {
                current = maxOfType - 1;
            }
        }
        else
        {
            if (++current >= maxOfType)
            {
                current = 0;
            }
        }

        setModuleSelection(containerStack, moduleType, current);
        return true;
    }

    /**
     * Sets the module selection index of the module type <b>moduleType</b> to the one given in <b>index</b>.
     * The value is clamped to be between 0..(max - 1) if the item is of type IModular.
     * @param containerStack
     * @param moduleType
     * @param index
     */
    public static void setModuleSelection(ItemStack containerStack, ModuleType moduleType, int index)
    {
        NBTTagCompound nbt = NBTUtils.getCompoundTag(containerStack, null, true);

        if (containerStack.getItem() instanceof IModular)
        {
            index = Math.min(index, ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType) - 1);
        }

        if (index < 0)
        {
            index = 0;
        }

        nbt.setByte("Selected_" + moduleType.getName(), (byte)index);
    }

    /**
     * Returns the total number of stored items in the containerStack.
     * @param containerStack
     * @return
     */
    public static int getTotalNumberOfStoredItems(ItemStack containerStack)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
        if (nbtTagList == null)
        {
            return 0;
        }

        int count = 0;
        int num = nbtTagList.tagCount();
        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);

            if (tag.hasKey("ActualCount", Constants.NBT.TAG_INT))
            {
                count += tag.getInteger("ActualCount");
            }
            else
            {
                count += tag.getByte("Count");

                /*ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                if (stack != null)
                {
                    count += stack.stackSize;
                }*/
            }
        }

        return count;
    }

    /**
     * Reads the display names of all the stored items in <b>containerStack</b>.
     */
    public static void readItemNamesFromContainerItem(ItemStack containerStack, List<String> listNames)
    {
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);
        if (nbtTagList == null)
        {
            return;
        }

        int num = nbtTagList.tagCount();
        for (int i = 0; i < num; ++i)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

            if (stack != null)
            {
                listNames.add(stack.getDisplayName());
            }
        }
    }

    /**
     * Adds a formatted list of ItemStack sizes and the display names of the ItemStacks
     * to the <b>listLines</b> list. Returns the total number of items stored.
     * @param containerStack
     * @param listLines
     * @return total number of items stored
     */
    @SideOnly(Side.CLIENT)
    public static int getFormattedItemListFromContainerItem(ItemStack containerStack, List<String> listLines, int maxLines)
    {
        int itemCount = 0;
        int overflow = 0;
        String preWhite = EnumChatFormatting.WHITE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        NBTTagList nbtTagList = NBTUtils.getStoredItemsList(containerStack, false);

        if (nbtTagList != null && nbtTagList.tagCount() > 0)
        {
            int num = nbtTagList.tagCount();
            for (int i = 0; i < num; ++i)
            {
                NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
                if (tag != null)
                {
                    ItemStack tmpStack = ItemStack.loadItemStackFromNBT(tag);

                    if (tmpStack != null)
                    {
                        int stackSize = tmpStack.stackSize;

                        if (tag.hasKey("ActualCount", Constants.NBT.TAG_INT) == true)
                        {
                            stackSize = tag.getInteger("ActualCount");
                        }
                        itemCount += stackSize;

                        if (i < maxLines)
                        {
                            listLines.add(String.format("  %s%4d%s %s", preWhite, stackSize, rst, tmpStack.getDisplayName()));
                        }
                        else
                        {
                            overflow++;
                        }
                    }
                }
            }
        }

        if (overflow > 0)
        {
            String str1 = StatCollector.translateToLocal("enderutilities.tooltip.item.and");
            String str2 = StatCollector.translateToLocal("enderutilities.tooltip.item.morestacksnotlisted");
            listLines.add(String.format("     ... %s %s%d%s %s", str1, preWhite, overflow, rst, str2));
        }

        return itemCount;
    }

    /**
     * Returns the position of the ItemStack in the NBTTagList that is stored in the slot slotNum
     * in whatever container handles it.
     * @param tagList
     * @param slotNum
     * @return the position of the stack in slot slotNum in the list, or -1 if it doesn't exist in the list
     */
    public static int getListPositionOfStackInSlot(NBTTagList nbtTagList, int slotNum)
    {
        int size = nbtTagList.tagCount();
        for (int i = 0; i < size; i++)
        {
            NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
            if (tag.hasKey("Slot", Constants.NBT.TAG_BYTE) == true && tag.getByte("Slot") == slotNum)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * If the given modular item has an Ender Capacitor module installed,
     * then the given amount of charge (or however much can be added) is added to the capacitor.
     * In case of any errors, no charge will be added.
     * @param containerStack
     * @param amount
     * @param doCharge True if we want to actually add charge, false if we want to just simulate it
     * @return The amount of charge that was successfully added to the installed capacitor module
     */
    public static int addEnderCharge(ItemStack containerStack, int amount, boolean doCharge)
    {
        if ((containerStack.getItem() instanceof IModular) == false)
        {
            return 0;
        }

        ItemStack moduleStack = getSelectedModuleStack(containerStack, ModuleType.TYPE_ENDERCAPACITOR);
        if (moduleStack == null || (moduleStack.getItem() instanceof IChargeable) == false)
        {
            return 0;
        }

        IChargeable cap = (IChargeable) moduleStack.getItem();
        if (cap.addCharge(moduleStack, amount, false) == 0)
        {
            return 0;
        }

        int added = 0;
        if (doCharge == true)
        {
            added = cap.addCharge(moduleStack, amount, true);
            setSelectedModuleStack(containerStack, ModuleType.TYPE_ENDERCAPACITOR, moduleStack);
        }

        return added;
    }

    /**
     * Returns the amount of Ender Charge currently stored in the selected Ender Capacitor
     * @param containerStack
     * @return the amount of Ender Charge stored in the selected Capacitor
     */
    public static int getAvailableEnderCharge(ItemStack containerStack)
    {
        if ((containerStack.getItem() instanceof IModular) == false)
        {
            return 0;
        }

        ItemStack moduleStack = getSelectedModuleStack(containerStack, ModuleType.TYPE_ENDERCAPACITOR);
        if (moduleStack == null || (moduleStack.getItem() instanceof IChargeable) == false)
        {
            return 0;
        }

        return ((IChargeable) moduleStack.getItem()).getCharge(moduleStack);
    }

    /**
     * If the given modular item has an Ender Capacitor module installed, and the capacitor has sufficient charge,
     * then the given amount of charge will be drained from it, and true is returned.
     * In case of any errors, no charge will be drained and false is returned.
     * @param containerStack
     * @param amount
     * @param simulate true to only simulate, false to actually use charge
     * @return false if the requested amount of charge could not be drained
     */
    public static boolean useEnderCharge(ItemStack containerStack, int amount, boolean simulate)
    {
        if (Configs.valueUseEnderCharge == false)
        {
            return true;
        }

        if ((containerStack.getItem() instanceof IModular) == false)
        {
            return false;
        }

        ItemStack moduleStack = getSelectedModuleStack(containerStack, ModuleType.TYPE_ENDERCAPACITOR);
        if (moduleStack == null || (moduleStack.getItem() instanceof ItemEnderCapacitor) == false)
        {
            return false;
        }

        ItemEnderCapacitor cap = (ItemEnderCapacitor) moduleStack.getItem();
        if (cap.useCharge(moduleStack, amount, false) < amount)
        {
            return false;
        }

        if (simulate == false)
        {
            cap.useCharge(moduleStack, amount, true);
            setSelectedModuleStack(containerStack, ModuleType.TYPE_ENDERCAPACITOR, moduleStack);
        }

        return true;
    }

    /**
     * Stores the player's current position as the Target tag to the currently selected Link Crystal in the modular item in containerStack.
     * @param containerStack The ItemStack containing the modular item
     * @param player The player that we get the position from.
     * @param storeRotation true if we also want to store the player's yaw and pitch rotations
     */
    public static void setTarget(ItemStack containerStack, EntityPlayer player, boolean storeRotation)
    {
        int x = (int)player.posX;
        int y = (int)player.posY;
        int z = (int)player.posZ;
        double hitX = player.posX - x;
        double hitY = player.posY - y;
        double hitZ = player.posZ - z;
        // Don't adjust the target position for uses that are targeting the block, not the in-world location
        boolean adjustPosHit = getSelectedModuleTier(containerStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION;

        setTarget(containerStack, player, player.getPosition(), EnumFacing.UP, hitX, hitY, hitZ, adjustPosHit, storeRotation);
    }

    /**
     * Store a new target tag to the currently selected Link Crystal in the modular item in containerStack.
     * @param containerStack
     * @param player
     * @param x
     * @param y
     * @param z
     * @param side
     * @param hitX
     * @param hitY
     * @param hitZ
     * @param doHitOffset true if we want to calculate the actual position (including the hitX/Y/Z offsets) instead of only
     * using the integer position. This is normally true for location type Link Crystals, and false for block type Link Crystals.
     * @param storeRotation true if we also want to store the player's yaw and pitch rotations
     */
    public static void setTarget(ItemStack containerStack, EntityPlayer player, BlockPos pos, EnumFacing side,
            double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeRotation)
    {
        if (NBTHelperPlayer.canAccessSelectedModule(containerStack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return;
        }

        NBTHelperTarget.writeTargetTagToSelectedModule(containerStack, ModuleType.TYPE_LINKCRYSTAL, pos, player.dimension, side,
                hitX, hitY, hitZ, doHitOffset, player.rotationYaw, player.rotationPitch, storeRotation);

        if (NBTHelperPlayer.selectedModuleHasPlayerTag(containerStack, ModuleType.TYPE_LINKCRYSTAL) == false)
        {
            NBTHelperPlayer.writePlayerTagToSelectedModule(containerStack, ModuleType.TYPE_LINKCRYSTAL, player, true);
        }
    }

    /**
     * Toggle the Public/Private mode on the selected module of the given type.
     * If the module doesn't have the Player tag yet, it will be created and set to Private.
     * @param containerStack
     * @param player
     * @param moduleType
     */
    public static void changePrivacyModeOnSelectedModule(ItemStack containerStack, EntityPlayer player, ModuleType moduleType)
    {
        if (NBTHelperPlayer.selectedModuleHasPlayerTag(containerStack, moduleType) == false)
        {
            NBTHelperPlayer.writePlayerTagToSelectedModule(containerStack, moduleType, player, false);
        }
        else
        {
            NBTHelperPlayer data = NBTHelperPlayer.getPlayerDataFromSelectedModule(containerStack, moduleType);
            if (data != null && data.isOwner(player) == true)
            {
                data.isPublic = ! data.isPublic;
                data.writeToSelectedModule(containerStack, moduleType);
            }
        }
    }

    /**
     * Toggle the Public/Private mode on the selected module of the given type
     * on a modular item that uses absolute module selection index (ie. not empty slot skipping).
     * If the module doesn't have the Player tag yet, it will be created and set to Private.
     * @param containerStack
     * @param player
     * @param moduleType
     */
    public static void changePrivacyModeOnSelectedModuleAbs(ItemStack containerStack, EntityPlayer player, ModuleType moduleType)
    {
        int slotNum = getStoredModuleSelection(containerStack, moduleType);
        ItemStack moduleStack = getModuleStackBySlotNumber(containerStack, slotNum, moduleType);
        if (moduleStack == null)
        {
            return;
        }

        if (NBTHelperPlayer.itemHasPlayerTag(moduleStack) == false)
        {
            NBTHelperPlayer.writePlayerTagToItem(moduleStack, player, false);
            setModuleStackBySlotNumber(containerStack, slotNum, moduleStack);
        }
        else
        {
            NBTHelperPlayer data = NBTHelperPlayer.getPlayerDataFromItem(moduleStack);
            if (data != null && data.isOwner(player) == true)
            {
                data.isPublic = ! data.isPublic;
                data.writeToItem(moduleStack);
                setModuleStackBySlotNumber(containerStack, slotNum, moduleStack);
            }
        }
    }

    /**
     * Returns the first index for the modules of the given type inside the modular item's module inventory.
     * This assumes they are stored/assigned in the enum's order.
     * Note: Thus this depends on the Tool Workstation's Container implementation
     * @param containerStack
     * @param moduleType
     * @return
     */
    public static int getFirstIndexOfModuleType(ItemStack containerStack, ModuleType moduleType)
    {
        if (containerStack == null || (containerStack.getItem() instanceof IModular) == false)
        {
            return 0;
        }

        IModular item = (IModular)containerStack.getItem();
        int start = 0;
        for (ModuleType type : ModuleType.values())
        {
            if (type.equals(moduleType) == true)
            {
                break;
            }

            start += item.getMaxModules(containerStack, type);
        }

        return start;
    }

    /**
     * Returns the inventory that the selected Link Crystal in the given modular item is currently bound to,
     * or null in case of errors.
     */
    public static IItemHandler getBoundInventory(ItemStack modularStack, EntityPlayer player, int chunkLoadDuration)
    {
        if (modularStack == null || (modularStack.getItem() instanceof IModular) == false)
        {
            return null;
        }

        IModular iModular = (IModular)modularStack.getItem();
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(modularStack, ModuleType.TYPE_LINKCRYSTAL);

        if (iModular.getSelectedModuleTier(modularStack, ModuleType.TYPE_LINKCRYSTAL) != ItemLinkCrystal.TYPE_BLOCK || target == null)
        {
            return null;
        }

        // Bound to a vanilla Ender Chest
        if ("minecraft:ender_chest".equals(target.blockName) == true)
        {
            return new InvWrapper(player.getInventoryEnderChest());
        }

        // For cross-dimensional item teleport we require the third tier of active Ender Core
        if (NBTHelperPlayer.canAccessSelectedModule(modularStack, ModuleType.TYPE_LINKCRYSTAL, player) == false
            || (target.dimension != player.dimension &&
                iModular.getMaxModuleTier(modularStack, ModuleType.TYPE_ENDERCORE) != ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED))
        {
            return null;
        }

        World targetWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
        if (targetWorld == null)
        {
            return null;
        }

        if (chunkLoadDuration > 0)
        {
            // Chunk load the target
            ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension,
                    target.pos.getX() >> 4, target.pos.getZ() >> 4, chunkLoadDuration);
        }

        TileEntity te = targetWorld.getTileEntity(target.pos);
        // Block has changed since binding, or does not have IItemHandler capability
        if (te == null || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing) == false || target.isTargetBlockUnchanged() == false)
        {
            // Remove the bind
            NBTHelperTarget.removeTargetTagFromSelectedModule(modularStack, ModuleType.TYPE_LINKCRYSTAL);
            player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.bound.block.changed"));
            return null;
        }

        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing);
    }
}
