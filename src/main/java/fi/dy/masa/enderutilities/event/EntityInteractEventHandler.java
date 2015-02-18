package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.ItemEnderLasso;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityInteractEventHandler
{
    @SubscribeEvent
    public void onEntityInteractEvent(EntityInteractEvent event)
    {
        ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

        if (stack == null || (stack.getItem() instanceof ItemEnderUtilities) == false)
        {
            return;
        }

        Item item = stack.getItem();

        if (item == EnderUtilitiesItems.enderLasso && event.entity.worldObj.isRemote == false)
        {
            if (event.target instanceof EntityLivingBase && event.entity instanceof EntityPlayer &&
                (Configs.enderLassoAllowPlayers.getBoolean(false) == true || EntityUtils.doesEntityStackHavePlayers(event.target) == false))
            {
                if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, (EntityPlayer)event.entity) == false
                    || UtilItemModular.useEnderCharge(stack, (EntityPlayer)event.entity, ItemEnderLasso.ENDER_CHARGE_COST, true) == false)
                {
                    return;
                }

                Entity entity = TeleportEntity.teleportEntityUsingModularItem(event.target, stack);
                if (entity != null)
                {
                    if (entity instanceof EntityLiving && UtilItemModular.getModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                    {
                        EntityUtils.applyMobPersistence((EntityLiving)entity);
                    }

                    event.setCanceled(true);
                }
            }
            return;
        }

        if (item == EnderUtilitiesItems.mobHarness)
        {
            if (event.target instanceof EntityLivingBase && event.entity instanceof EntityPlayer)
            {
                ((ItemMobHarness)stack.getItem()).handleInteraction(stack, (EntityPlayer)event.entity, event.target);
                event.setCanceled(true);
            }
            return;
        }

        if (item instanceof IModule && item == EnderUtilitiesItems.enderPart && ((IModule)item).getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_INACTIVE))
        {
            if (event.entity.worldObj.isRemote == false && event.target instanceof EntityEnderCrystal && event.entity.dimension == 1)
            {
                ((ItemEnderPart)item).activateEnderCore(stack);
            }
            return;
        }

        if (item instanceof IChargeable)
        {
            if (event.entity.worldObj.isRemote == false && event.target instanceof EntityEnderCrystal && event.entity.dimension == 1)
            {
                IChargeable chargeable = (IChargeable)item;
                chargeable.addCharge(stack, chargeable.getCapacity(stack) >> 2, true);
            }
            return;
        }
    }
}
