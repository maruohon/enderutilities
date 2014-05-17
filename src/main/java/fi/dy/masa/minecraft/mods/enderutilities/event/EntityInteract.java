package fi.dy.masa.minecraft.mods.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.util.TeleportEntity;

public class EntityInteract
{
	@SubscribeEvent
	public void onEntityInteractEvent(EntityInteractEvent event)
	{
		if (event.target.worldObj.isRemote == true)
		{
			return;
		}

		ItemStack stack = event.entityPlayer.inventory.getCurrentItem();
		if (stack != null && stack.getItem() == EnderUtilitiesItems.enderLasso)
		{
			if (event.target instanceof EntityPlayerMP)
			{
				return;
			}

			if (event.target instanceof EntityLiving )
			{
				TeleportEntity.lassoTeleportEntity(stack, (EntityLiving)event.target, event.entity.dimension);
				event.setCanceled(true);
				return;
			}

			return;
		}
	}
}
