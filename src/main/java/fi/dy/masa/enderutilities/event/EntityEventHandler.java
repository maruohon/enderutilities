package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.capabilities.EnderUtilitiesCapabilities;
import fi.dy.masa.enderutilities.capabilities.EnderUtilitiesCapabilities.PortalCooldownCapabilityProvider;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemLivingManipulator;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.ItemPortalScaler;
import fi.dy.masa.enderutilities.item.ItemSyringe;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.WorldUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityEventHandler
{
    private static boolean preventItemSpawningInWorld;

    public static void setPreventItemSpawning(boolean preventItemSpawning)
    {
        preventItemSpawningInWorld = preventItemSpawning;
    }

    @SubscribeEvent
    public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event)
    {
        event.addCapability(EnderUtilitiesCapabilities.PORTAL_COOLDOWN_CAP_NAME, new PortalCooldownCapabilityProvider());
    }

    @SubscribeEvent
    public void onEntityInteractEvent(PlayerInteractEvent.EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());

        if (stack.isEmpty())
        {
            return;
        }

        Item item = stack.getItem();
        boolean isRemote = player.getEntityWorld().isRemote;

        // This needs to be in the event instead of itemInteractionForEntity() if we want it to also work in creative mode...
        // (Otherwise in creative mode the NBT will get wiped after the use when the item is restored)
        if (item == EnderUtilitiesItems.LIVING_MANIPULATOR)
        {
            if (event.getTarget() instanceof EntityLivingBase)
            {
                if (isRemote == false)
                {
                    ((ItemLivingManipulator)item).handleInteraction(stack, player, (EntityLivingBase)event.getTarget());
                }

                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
        else if (item == EnderUtilitiesItems.MOB_HARNESS && event.getTarget() instanceof EntityLivingBase)
        {
            ((ItemMobHarness) item).handleInteraction(stack, player, event.getTarget());
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
        else if (item == EnderUtilitiesItems.ENDER_LASSO && event.getTarget() instanceof EntityLivingBase)
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

                    if (isRemote || TeleportEntity.teleportEntityUsingModularItem(event.getTarget(), stack, true, true) != null)
                    {
                        event.setCanceled(true);
                        event.setCancellationResult(EnumActionResult.SUCCESS);
                    }
                }
            }
        }
        else if (WorldUtils.isEndDimension(player.getEntityWorld()) && event.getTarget() instanceof EntityEnderCrystal && isRemote == false)
        {
            if (item instanceof IChargeable)
            {
                IChargeable chargeable = (IChargeable)item;
                chargeable.addCharge(stack, chargeable.getCapacity(stack) >> 2, true);
            }
            else if (item == EnderUtilitiesItems.ENDER_PART && ((IModule) item).getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE))
            {
                ItemEnderPart part = (ItemEnderPart) item;
                int tier = part.getModuleTier(stack);

                if (tier >= ItemEnderPart.ENDER_CORE_TYPE_INACTIVE_BASIC && tier <= ItemEnderPart.ENDER_CORE_TYPE_INACTIVE_ADVANCED)
                {
                    part.activateEnderCore(stack);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTravelToDimensionEvent(EntityTravelToDimensionEvent event)
    {
        // Prevent recursion from this same event being fired from the teleport method
        if (TeleportEntity.isTeleportInProgress())
        {
            return;
        }

        Entity entity = event.getEntity();

        if (entity instanceof EntityPlayer)
        {
            // If the player is holding a Portal Scaler, then try to use that and cancel the regular
            // teleport if the Portal Scaler teleportation succeeds
            ItemStack stack = EntityUtils.getHeldItemOfType((EntityPlayer) entity, EnderUtilitiesItems.PORTAL_SCALER);

            if (stack.isEmpty() == false && EntityUtils.isEntityCollidingWithBlockSpace(entity.getEntityWorld(), entity, Blocks.PORTAL))
            {
                World worldSource = entity.getEntityWorld();
                World worldDestination = entity.getServer().getWorld(event.getDimension());
                boolean validSource = WorldUtils.isNetherDimension(worldSource) || WorldUtils.isOverworldDimension(worldSource);
                boolean validDestination = worldDestination != null && (WorldUtils.isNetherDimension(worldDestination) || WorldUtils.isOverworldDimension(worldDestination));

                // Check that the player is traveling between the overworld and the nether
                if (validSource && validDestination)
                {
                    if (((ItemPortalScaler) stack.getItem()).usePortalWithPortalScaler(stack, entity.getEntityWorld(), (EntityPlayer) entity))
                    {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttackEntity(LivingAttackEvent event)
    {
        // When a "passified" entity is attacked by another entity, remove the "passified" tag,
        // and restore the target AI tasks by re-adding all the AI tasks from a fresh clone
        if ((event.getEntity() instanceof EntityLiving) && event.getEntity().getEntityWorld().isRemote == false &&
            event.getSource() instanceof EntityDamageSource && event.getEntity().getTags().contains(ItemSyringe.TAG_PASSIFIED))
        {
            ItemSyringe.removePassifiedState((EntityLiving) event.getEntity());
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getWorld().isRemote == false)
        {
            if ((event.getEntity() instanceof EntityLiving) && event.getEntity().getTags().contains(ItemSyringe.TAG_PASSIFIED))
            {
                ItemSyringe.passifyEntity((EntityLiving) event.getEntity());
            }
            else if (preventItemSpawningInWorld && (event.getEntity() instanceof EntityItem))
            {
                event.setCanceled(true);
            }
        }
    }

    /*
    @SubscribeEvent
    public void onCollisionBoxesEvent(GetCollisionBoxesEvent event)
    {
        if (Configs.replaceEntityItemCollisionBoxHandling &&
            ((event.getEntity() instanceof EntityItem) || (event.getEntity() instanceof EntityXPOrb)))
        {
            event.getCollisionBoxesList().clear();
            event.getCollisionBoxesList().addAll(
                    PositionUtils.getSurroundingCollisionBoxesForEntityItem(event.getWorld(), event.getAabb(), event.getEntity()));
        }
    }
    */
}
