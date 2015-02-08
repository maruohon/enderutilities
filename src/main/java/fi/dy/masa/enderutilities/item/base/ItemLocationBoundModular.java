package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemLocationBoundModular extends ItemLocationBound implements IModular, IKeyBound
{
    @Override
    public boolean onItemUse(ItemStack toolStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true || player == null || player.isSneaking() == false || toolStack == null || toolStack.getItem() == null)
        {
            return false;
        }

        ItemStack moduleStack = this.getSelectedModuleStack(toolStack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null && moduleStack.getItem() != null)
        {
            boolean adjustPosHit = true;

            // Don't adjust the target position for uses that are targeting the block, not the in-world location
            if (moduleStack.getItem() == EnderUtilitiesItems.linkCrystal && moduleStack.getItemDamage() != 0)
            {
                adjustPosHit = false;
            }

            NBTTagCompound nbt = moduleStack.getTagCompound();
            nbt = NBTHelperTarget.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, hitX, hitY, hitZ, adjustPosHit);
            nbt = NBTHelperPlayer.writePlayerTagToNBT(nbt, player);
            moduleStack.setTagCompound(nbt);
            this.setSelectedModuleStack(toolStack, ModuleType.TYPE_LINKCRYSTAL, moduleStack);

            return true;
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null)
        {
            String itemName = StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
            NBTTagCompound nbt = moduleStack.getTagCompound();

            // If the currently selected module has been renamed, show that name
            if (nbt != null && nbt.hasKey("display", Constants.NBT.TAG_COMPOUND) == true)
            {
                NBTTagCompound tagDisplay = nbt.getCompoundTag("display");
                if (tagDisplay.hasKey("Name", Constants.NBT.TAG_STRING) == true)
                {
                    String dNamePre = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
                    String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

                    if (itemName.length() >= 14)
                    {
                        return EUStringUtils.getInitialsWithDots(itemName) + " " + dNamePre + tagDisplay.getString("Name") + rst;
                    }

                    return itemName + " " + dNamePre + tagDisplay.getString("Name") + rst;
                }
            }

            /*NBTHelperTarget target = new NBTHelperTarget();
            if (target.readTargetTagFromNBT(nbt) != null)
            {
                String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true);

                if (itemName.length() >= 14)
                {
                    return EUStringUtils.getInitialsWithDots(itemName) + " (" + dimName + ")";
                }

                return itemName + " (" + dimName + ")";
            }*/
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.use.toolworkstation"));
            return;
        }

        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        NBTHelperTarget target = new NBTHelperTarget();

        String numPre = EnumChatFormatting.BLUE.toString();
        String dNamePre = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        // Link Crystals installed
        if (linkCrystalStack != null)
        {
            // Valid target set in the currently selected Link Crystal
            if (target.readTargetTagFromNBT(linkCrystalStack.getTagCompound()) != null)
            {
                super.addInformationSelective(linkCrystalStack, player, list, advancedTooltips, verbose);
            }
            else
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
            }
        }
        else
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.nolinkcrystals"));
        }

        if (verbose == true && linkCrystalStack != null)
        {
            int num = UtilItemModular.getModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
            int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
            String dName = (linkCrystalStack.hasDisplayName() ? dNamePre + linkCrystalStack.getDisplayName() + rst + " " : "");
            list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal.short") + String.format(" %s(%s%d%s / %s%d%s)", dName, numPre, sel, rst, numPre, num, rst));
        }

        // Ender Capacitor charge, if one has been installed
        if (verbose == true)
        {
            ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
            if (capacitorStack != null && capacitorStack.getItem() instanceof ItemEnderCapacitor)
            {
                ((ItemEnderCapacitor)capacitorStack.getItem()).addInformation(capacitorStack, player, list, advancedTooltips);
            }
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE && ReferenceKeys.keypressContainsShift(key) == true)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressContainsControl(key));
        }
    }

    /* Returns the number of installed modules of the given type. */
    @Override
    public int getModuleCount(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getModuleCount(stack, moduleType);
    }

    /* Returns the maximum number of modules that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 4;
    }

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        return 0;
    }

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        ModuleType moduleType = ((IModule) moduleStack.getItem()).getModuleType(moduleStack);

        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            // Only allow the in-world/location type Link Crystals by default
            if (((IModule) moduleStack.getItem()).getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION)
            {
                return 3;
            }
        }

        return 0;
    }

    /* Returns the (max, if multiple) tier of the installed module. */
    @Override
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(stack, moduleType);
    }

    /* Returns the tier of the selected module of the given type. */
    public int getSelectedModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(stack, moduleType);
    }

    /* Returns the ItemStack of the (selected, if multiple) given module type. */
    @Override
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(stack, moduleType);
    }

    /* Sets the selected modules' ItemStack of the given module type to the one provided. */
    @Override
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);
    }

    /* Change the selected module to the next one, if any. */
    @Override
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse)
    {
        return UtilItemModular.changeSelectedModule(stack, moduleType, reverse);
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
