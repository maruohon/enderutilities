package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemLivingManipulator;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.ItemPortalScaler;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEventHandler
{
    @SubscribeEvent
    public void onEntityInteractEvent(PlayerInteractEvent.EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());

        if (stack == null)
        {
            return;
        }

        Item item = stack.getItem();
        boolean isRemote = player.getEntityWorld().isRemote;

        // This needs to be in the event instead of itemInteractionForEntity() if we want it to also work in creative mode...
        // (Otherwise in creative mode the NBT will get wiped after the use when the item is restored)
        if (item == EnderUtilitiesItems.livingManipulator)
        {
            if (event.getTarget() instanceof EntityLivingBase)
            {
                if (isRemote == false)
                {
                    ((ItemLivingManipulator)item).handleInteraction(stack, player, (EntityLivingBase)event.getTarget());
                }
                event.setCanceled(true);
            }
        }
        else if (item == EnderUtilitiesItems.mobHarness && event.getTarget() instanceof EntityLivingBase)
        {
            ((ItemMobHarness) item).handleInteraction(stack, player, event.getTarget());
            event.setCanceled(true);
        }
        else if (item == EnderUtilitiesItems.enderLasso && event.getTarget() instanceof EntityLivingBase)
        {
            if (Configs.enderLassoAllowPlayers || EntityUtils.doesEntityStackHavePlayers(event.getTarget()) == false)
            {
                if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) &&
                    UtilItemModular.useEnderCharge(stack, ItemEnderLasso.ENDER_CHARGE_COST, false))
                {
                    if (event.getTarget() instanceof EntityLiving && UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                    {
                        EntityUtils.applyMobPersistence((EntityLiving)event.getTarget());
                    }

                    if (isRemote || TeleportEntity.teleportEntityUsingModularItem(event.getTarget(), stack) != null)
                    {
                        event.setCanceled(true);
                    }
                }
            }
        }
        else if (player.getEntityWorld().provider.getDimension() == 1 && event.getTarget() instanceof EntityEnderCrystal && isRemote == false)
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
        Entity entity = event.getEntity();
        int dim = event.getDimension();
        int entityDim = entity.getEntityWorld().provider.getDimension();

        // Check that the entity is traveling between the overworld and the nether, and that it is a player
        if ((dim == 0 || dim == -1) && (entityDim == 0 || entityDim == -1 ) && (entity instanceof EntityPlayer))
        {
            // If the player is holding a Portal Scaler, then try to use that and cancel the regular
            // teleport if the Portal Scaler teleportation succeeds
            ItemStack stack = EntityUtils.getHeldItemOfType((EntityPlayer)entity, EnderUtilitiesItems.portalScaler);

            if (stack != null && EntityUtils.isEntityCollidingWithBlockSpace(entity.getEntityWorld(), entity, Blocks.PORTAL))
            {
                if (((ItemPortalScaler)stack.getItem()).usePortalWithPortalScaler(stack, entity.getEntityWorld(), (EntityPlayer)entity))
                {
                    event.setCanceled(true);
                }
            }
        }
    }
}
