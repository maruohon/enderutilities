package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemLocationBoundModular extends ItemLocationBound implements IModular, IKeyBound
{
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (player != null && player.isSneaking() == true)
        {
            if (world.isRemote == false)
            {
                boolean adjustPosHit = UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION;
                this.setTarget(stack, player, x, y, z, side, hitX, hitY, hitZ, adjustPosHit, false);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, entity, slot, isCurrent);

        if (world.isRemote == false && EnergyBridgeTracker.dimensionHasEnergyBridge(world.provider.dimensionId) == true &&
            ((entity instanceof EntityPlayer) == false || ((EntityPlayer)entity).isUsingItem() == false || ((EntityPlayer)entity).getCurrentEquippedItem() != stack) &&
            (world.provider.dimensionId == 1 || EnergyBridgeTracker.dimensionHasEnergyBridge(1) == true))
        {
            UtilItemModular.addEnderCharge(stack, ItemEnderCapacitor.CHARGE_RATE_FROM_ENERGY_BRIDGE, true);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null && moduleStack.getItem() instanceof ILocationBound)
        {
            String itemName = StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
            String preGreen = EnumChatFormatting.GREEN.toString();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                String preGreenIta = preGreen + EnumChatFormatting.ITALIC.toString();
                if (itemName.length() >= 14)
                {
                    return EUStringUtils.getInitialsWithDots(itemName) + " " + preGreenIta + moduleStack.getDisplayName() + rst;
                }

                return itemName + " " + preGreenIta + moduleStack.getDisplayName() + rst;
            }

            // Link Crystal not named and has a valid target
            NBTHelperTarget target = ((ILocationBound)moduleStack.getItem()).getTarget(moduleStack);
            if (target != null && moduleStack.getItem() instanceof IModule)
            {
                if (itemName.length() >= 14)
                {
                    itemName = EUStringUtils.getInitialsWithDots(itemName);
                }

                // Display the target block name if it's a Block type Link Crystal
                if (((IModule)moduleStack.getItem()).getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
                {
                    Block block = Block.getBlockFromName(target.blockName);
                    ItemStack targetStack = new ItemStack(block, 1, block.damageDropped(target.blockMeta & 0xF));
                    if (targetStack != null && targetStack.getItem() != null)
                    {
                        return itemName + " " + preGreen + targetStack.getDisplayName() + rst;
                    }
                }
                // Location type Link Crystal
                else if (((IModule)moduleStack.getItem()).getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION)
                {
                    return itemName + " " + preGreen + TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true) + rst;
                }
            }
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

        String preBlue = EnumChatFormatting.BLUE.toString();
        String preWhiteIta = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        // Link Crystals installed
        if (linkCrystalStack != null)
        {
            // Valid target set in the currently selected Link Crystal
            if (NBTHelperTarget.itemHasTargetTag(linkCrystalStack) == true)
            {
                super.addInformationSelective(linkCrystalStack, player, list, advancedTooltips, verbose);
            }
            else
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.notargetset"));
            }

            if (verbose == true)
            {
                int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
                int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
                String dName = (linkCrystalStack.hasDisplayName() ? preWhiteIta + linkCrystalStack.getDisplayName() + rst + " " : "");
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.selectedlinkcrystal.short") + String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
            }
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nolinkcrystals"));
        }

        if (verbose == true)
        {
            // Item supports Jailer modules, show if one is installed
            if (this.getMaxModules(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                String s;
                if (this.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                {
                    s = StatCollector.translateToLocal("enderutilities.tooltip.item.jailer") + ": " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    s = StatCollector.translateToLocal("enderutilities.tooltip.item.jailer") + ": " + EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst;
                }

                list.add(s);
            }

            // Ender Capacitor charge, if one has been installed
            ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
            if (capacitorStack != null && capacitorStack.getItem() instanceof ItemEnderCapacitor)
            {
                ((ItemEnderCapacitor)capacitorStack.getItem()).addInformation(capacitorStack, player, list, advancedTooltips);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        super.addTooltips(stack, list, verbose);
        addTooltips("enderutilities.tooltips.itemlocationboundmodular", list, verbose);
    }

    @Override
    public void setTarget(ItemStack stack, EntityPlayer player, boolean storeRotation)
    {
        UtilItemModular.setTarget(stack, player, storeRotation);
    }

    @Override
    public void setTarget(ItemStack toolStack, EntityPlayer player, int x, int y, int z, int side, double hitX, double hitY, double hitZ, boolean doHitOffset, boolean storeRotation)
    {
        UtilItemModular.setTarget(toolStack, player, x, y, z, side, hitX, hitY, hitZ, doHitOffset, storeRotation);
    }

    @Override
    public void changePrivacyMode(ItemStack stack, EntityPlayer player)
    {
        NBTHelperPlayer data = NBTHelperPlayer.getPlayerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (data != null && data.isOwner(player) == true)
        {
            data.isPublic = ! data.isPublic;
            data.writeToSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Ctrl + (Shift + ) Toggle mode
        if (ReferenceKeys.keypressContainsControl(key) == true && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getInstalledModuleCount(containerStack, moduleType);
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
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

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the in-world/location type Link Crystals by default
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION)
        {
            return this.getMaxModules(containerStack, moduleType);
        }

        return 0;
    }

    @Override
    public int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(containerStack, moduleType);
    }

    @Override
    public int getSelectedModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(containerStack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(containerStack, moduleType);
    }

    @Override
    public ItemStack setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(containerStack, moduleType, moduleStack);
    }

    @Override
    public ItemStack changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        return UtilItemModular.changeSelectedModule(containerStack, moduleType, reverse);
    }

    @Override
    public List<NBTTagCompound> getAllModules(ItemStack containerStack)
    {
        return UtilItemModular.getAllModules(containerStack);
    }

    @Override
    public ItemStack setAllModules(ItemStack containerStack, List<NBTTagCompound> modules)
    {
        return UtilItemModular.setAllModules(containerStack, modules);
    }

    @Override
    public ItemStack setModule(ItemStack containerStack, int index, NBTTagCompound nbt)
    {
        return UtilItemModular.setModule(containerStack, index, nbt);
    }
}
