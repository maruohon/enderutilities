package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityInteractEventHandler
{
	@SubscribeEvent
	public void onEntityInteractEvent(EntityInteractEvent event)
	{
		ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

		if (stack == null)
		{
			return;
		}

		if(stack.getItem() == EnderUtilitiesItems.enderLasso)
		{
			if (event.target instanceof EntityLivingBase && event.entity instanceof EntityPlayer)
			{
				TeleportEntity.teleportEntityUsingItem(event.target, stack);
				event.setCanceled(true);
			}
		}
	}
}
