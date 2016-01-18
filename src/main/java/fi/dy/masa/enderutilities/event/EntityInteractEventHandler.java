package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.player.EntityInteractEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemLivingManipulator;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityInteractEventHandler
{
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteractEvent event)
    {
        ItemStack stack = event.entityPlayer.getCurrentEquippedItem();

        if (stack == null || (stack.getItem() instanceof ItemEnderUtilities) == false)
        {
            return;
        }

        Item item = stack.getItem();

        if (item == EnderUtilitiesItems.livingManipulator)
        {
            if (event.target instanceof EntityLivingBase)
            {
                ((ItemLivingManipulator)item).handleInteraction(stack, event.entityPlayer, (EntityLivingBase)event.target);
                event.setCanceled(true);
            }
        }
        else if (item == EnderUtilitiesItems.mobHarness)
        {
            if (event.target instanceof EntityLivingBase)
            {
                if (event.entityPlayer.worldObj.isRemote == false)
                {
                    ((ItemMobHarness)stack.getItem()).handleInteraction(stack, event.entityPlayer, event.target);
                }
                event.setCanceled(true);
            }
        }
        else if (item == EnderUtilitiesItems.enderLasso && event.target instanceof EntityLivingBase)
        {
            if (Configs.enderLassoAllowPlayers.getBoolean(false) == true || EntityUtils.doesEntityStackHavePlayers(event.target) == false)
            {
                if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, event.entityPlayer) == true &&
                    UtilItemModular.useEnderCharge(stack, ItemEnderLasso.ENDER_CHARGE_COST, true) == true)
                {
                    if (event.target instanceof EntityLiving && UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                    {
                        EntityUtils.applyMobPersistence((EntityLiving)event.target);
                    }

                    if (event.entityPlayer.worldObj.isRemote == true || TeleportEntity.teleportEntityUsingModularItem(event.target, stack) != null)
                    {
                        event.setCanceled(true);
                    }
                }
            }
        }
        else if (event.entityPlayer.dimension == 1 && event.target instanceof EntityEnderCrystal && event.entityPlayer.worldObj.isRemote == false)
        {
            if (item instanceof IChargeable)
            {
                IChargeable chargeable = (IChargeable)item;
                chargeable.addCharge(stack, chargeable.getCapacity(stack) >> 2, true);
            }
            else if (item instanceof IModule && item == EnderUtilitiesItems.enderPart && ((IModule)item).getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_INACTIVE))
            {
                ((ItemEnderPart)item).activateEnderCore(stack);
            }
        }
    }
}
