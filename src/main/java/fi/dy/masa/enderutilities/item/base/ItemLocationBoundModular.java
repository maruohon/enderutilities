package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular.ModuleType;

public abstract class ItemLocationBoundModular extends ItemLocationBound implements IModular, IKeyBound
{
    @Override
    public boolean onItemUse(ItemStack toolStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (player == null || player.isSneaking() == false || toolStack == null || toolStack.getItem() == null)
        {
            return false;
        }

        ItemStack moduleStack = this.getSelectedModuleStack(toolStack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null && moduleStack.getItem() != null)
        {
            boolean adjustPosHit = true;

            // Don't adjust the target position for uses that are targeting the block, not the in-world location
            if (moduleStack.getItem() == EnderUtilitiesItems.linkCrystal && moduleStack.getItemDamage() != 0)
            {
                adjustPosHit = false;
            }

            moduleStack.setTagCompound(NBTHelperTarget.writeTargetTagToNBT(moduleStack.getTagCompound(), x, y, z, player.dimension, side, hitX, hitY, hitZ, adjustPosHit));
            this.setSelectedModuleStack(toolStack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);

            return true;
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack toolStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(toolStack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null && moduleStack.getItem() != null)
        {
            NBTTagCompound nbt = moduleStack.getTagCompound();
            // If the currently selected module has been renamed, show that name
            if (nbt != null && nbt.hasKey("display", Constants.NBT.TAG_COMPOUND) == true)
            {
                NBTTagCompound tagDisplay = nbt.getCompoundTag("display");
                if (tagDisplay.hasKey("Name", Constants.NBT.TAG_STRING) == true)
                {
                    return tagDisplay.getString("Name");
                }
            }

            NBTHelperTarget target = new NBTHelperTarget();
            if (target.readTargetTagFromNBT(nbt) != null)
            {
                String dimName = TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true);
                return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(toolStack) + ".name")).trim() + " (" + dimName + ")";
            }
        }

        return super.getItemStackDisplayName(toolStack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        /*if (EnderUtilities.proxy.isShiftKeyDown() == false)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.holdshift"));
            return;
        }*/

        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null || moduleStack.getItem() == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.nolinkcrystals"));
            return;
        }

        String dimPre = "" + EnumChatFormatting.DARK_GREEN;
        String numPre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

        NBTHelperTarget target = new NBTHelperTarget();
        if (target.readTargetTagFromNBT(moduleStack.getTagCompound()) == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
        }
        else
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + numPre + target.dimension + rst
                    + " " + dimPre + TooltipHelper.getDimensionName(target.dimension, target.dimensionName, false) + rst);
            list.add(String.format("x: %s%.2f%s y: %s%.2f%s z: %s%.2f%s", numPre, target.dPosX, rst, numPre, target.dPosY, rst, numPre, target.dPosZ, rst));
            // For debug:
            //list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
        }

        int num = UtilItemModular.getModuleCount(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (num > 0)
        {
            int sel = UtilItemModular.getClampedModuleSelection(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL) + 1;
            list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %s%d / %d%s", numPre, sel, num, rst));
            //list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %d / %d", sel, max));
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE && player != null && player.isSneaking() == true)
        {
            this.changeSelectedModule(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressContainsControl(key));
        }
    }

    /* Returns the number of installed modules of the given type. */
    @Override
    public int getModuleCount(ItemStack stack, UtilItemModular.ModuleType moduleType)
    {
        return UtilItemModular.getModuleCount(stack, moduleType);
    }

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack, UtilItemModular.ModuleType moduleType)
    {
        if (moduleType.equals(UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL))
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
        if (UtilItemModular.getModuleType(moduleStack).equals(UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (UtilItemModular.getModuleType(moduleStack).equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL))
        {
            // Only allow the in-world type Link Crystals by default
            if (moduleStack.getItemDamage() == 0)
            {
                return 3;
            }
        }

        return 0;
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

    /* Sets the selected modules' ItemStack of the given module type to the one provided. */
    @Override
    public ItemStack setSelectedModuleStack(ItemStack toolStack, UtilItemModular.ModuleType moduleType, ItemStack moduleStack)
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
