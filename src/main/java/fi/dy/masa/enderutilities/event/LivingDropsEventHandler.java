package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingDropsEventHandler
{
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event)
    {
        DamageSource source = event.getSource();

        if (source != null && source.damageType != null && source.damageType.equals("player") && source.getSourceOfDamage() instanceof EntityPlayer)
        {
            ItemStack stack = ((EntityPlayer)source.getSourceOfDamage()).getHeldItemMainhand();
            if (stack != null && stack.getItem() == EnderUtilitiesItems.enderSword)
            {
                ((ItemEnderSword) stack.getItem()).handleLivingDropsEvent(stack, event);
            }
        }
    }
}
