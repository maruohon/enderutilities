package fi.dy.masa.enderutilities.item.base;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public abstract class ItemModular extends ItemEnderUtilities implements IModular, IKeyBound
{
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
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getInstalledModuleCount(containerStack, moduleType);
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
