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

public abstract class ItemModular extends ItemEnderUtilities implements IModular
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
    public int getModuleCount(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getModuleCount(stack, moduleType);
    }

    @Override
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(stack, moduleType);
    }

    @Override
    public int getSelectedModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(stack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(stack, moduleType);
    }

    @Override
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);
    }

    @Override
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse)
    {
        return UtilItemModular.changeSelectedModule(stack, moduleType, reverse);
    }

    @Override
    public List<NBTTagCompound> getAllModules(ItemStack stack)
    {
        return UtilItemModular.getAllModules(stack);
    }

    @Override
    public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
    {
        return UtilItemModular.setAllModules(stack, modules);
    }

    @Override
    public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
    {
        return UtilItemModular.setModule(stack, index, nbt);
    }
}
