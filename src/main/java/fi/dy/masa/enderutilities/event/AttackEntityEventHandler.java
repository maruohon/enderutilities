package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class AttackEntityEventHandler
{
	@SubscribeEvent
	public void onEntityAttackEvent(AttackEntityEvent event)
	{
		if (event.isCancelable() == false)
		{
			return;
		}
		ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

		if (stack != null)
		{
			if (stack.getItem() == EnderUtilitiesItems.enderArrow)
			{
				if (event.target instanceof EntityLivingBase)
				{
					TeleportEntity.teleportEntityRandomly((EntityLivingBase)event.target, 10.0d);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
}
