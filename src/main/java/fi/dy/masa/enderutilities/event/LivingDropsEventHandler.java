package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;

public class LivingDropsEventHandler
{
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event)
    {
        DamageSource source = event.getSource();

        if (source != null && source.damageType != null && source.damageType.equals("player") && source.getImmediateSource() instanceof EntityPlayer)
        {
            ItemStack stack = ((EntityPlayer) source.getImmediateSource()).getHeldItemMainhand();

            if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.ENDER_SWORD)
            {
                ((ItemEnderSword) stack.getItem()).handleLivingDropsEvent(stack, event);
            }
        }
    }
}
