package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemLivingManipulator;
import fi.dy.masa.enderutilities.item.ItemPortalScaler;
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

public class EntityEventHandler
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

        // This needs to be in the event instead of itemInteractionForEntity() if we want it to also work in creative mode...
        // (Otherwise in creative mode the NBT will get wiped after the use when the item is restored)
        if (item == EnderUtilitiesItems.livingManipulator)
        {
            if (event.target instanceof EntityLivingBase)
            {
                ((ItemLivingManipulator)item).handleInteraction(stack, event.entityPlayer, (EntityLivingBase)event.target);
                event.setCanceled(true);
            }
        }
        else if (item == EnderUtilitiesItems.enderLasso && event.target instanceof EntityLivingBase)
        {
            if (Configs.enderLassoAllowPlayers == true || EntityUtils.doesEntityStackHavePlayers(event.target) == false)
            {
                if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, event.entityPlayer) == true &&
                    UtilItemModular.useEnderCharge(stack, ItemEnderLasso.ENDER_CHARGE_COST, false) == true)
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
            else if (item instanceof IModule && item == EnderUtilitiesItems.enderPart && ((IModule)item).getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE))
            {
                int tier = ((IModule)item).getModuleTier(stack);
                if (tier >= ItemEnderPart.ENDER_CORE_TYPE_INACTIVE_BASIC && tier <= ItemEnderPart.ENDER_CORE_TYPE_INACTIVE_ADVANCED)
                {
                    ((ItemEnderPart)item).activateEnderCore(stack);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTravelToDimensionEvent(EntityTravelToDimensionEvent event)
    {
        // Check that the entity is traveling between the overworld and the nether, and that it is a player
        if ((event.dimension != 0 && event.dimension != -1) ||
            (event.entity.dimension != 0 && event.entity.dimension != -1 ) ||
            (event.entity instanceof EntityPlayer) == false)
        {
            return;
        }

        // If the player is holding a Portal Scaler, then try to use that and cancel the regular
        // teleport if the Portal Scaler teleportation succeeds
        ItemStack stack = ((EntityPlayer)event.entity).getCurrentEquippedItem();
        if (stack != null && stack.getItem() == EnderUtilitiesItems.portalScaler &&
            EntityUtils.isEntityCollidingWithBlockSpace(event.entity.worldObj, event.entity, Blocks.portal))
        {
            if (((ItemPortalScaler)stack.getItem()).usePortalWithPortalScaler(stack, event.entity.worldObj, (EntityPlayer)event.entity) == true)
            {
                event.setCanceled(true);
            }
        }
    }
}
