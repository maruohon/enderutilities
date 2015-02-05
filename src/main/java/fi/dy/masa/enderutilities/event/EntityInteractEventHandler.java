package fi.dy.masa.enderutilities.event;

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
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;
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
                if (UtilItemModular.useEnderCharge(stack, (EntityPlayer)event.entity, ItemEnderLasso.ENDER_CHARGE_COST, true) == false)
                {
                    return;
                }

                if (TeleportEntity.teleportEntityUsingModularItem(event.target, stack) != null)
                {
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

        if (item == EnderUtilitiesItems.enderPart)
        {
            if (event.entity.worldObj.isRemote == false && event.target instanceof EntityEnderCrystal)
            {
                int dmg = stack.getItemDamage();

                // Inactive Ender Core: Change the stack to an active Ender Core
                if (dmg >= 10 && dmg <= 12)
                {
                    stack.setItemDamage(dmg + 5);
                }
            }
            return;
        }

        if (item instanceof IChargeable)
        {
            if (event.entity.worldObj.isRemote == false && event.target instanceof EntityEnderCrystal)
            {
                IChargeable chargeable = (IChargeable)item;
                chargeable.addCharge(stack, chargeable.getCapacity(stack) >> 2, true);
            }
            return;
        }
    }
}
