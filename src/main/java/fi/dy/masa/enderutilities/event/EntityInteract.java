package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class EntityInteract
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
			if (event.target instanceof EntityPlayerMP)
			{
				return;
			}

			if (event.target instanceof EntityLiving && event.entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)event.entity;
				TeleportEntity.lassoTeleportEntity(stack, (EntityLiving)event.target, player, player.dimension);
				event.setCanceled(true);
			}
		}
	}
}
