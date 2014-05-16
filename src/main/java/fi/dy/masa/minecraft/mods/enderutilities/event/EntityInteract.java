package fi.dy.masa.minecraft.mods.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.util.TeleportEntity;

public class EntityInteract
{
	@SubscribeEvent
	public void onEntityInteractEvent(EntityInteractEvent event)
	{
		if (event.target instanceof EntityLiving && event.target.worldObj.isRemote == false)
		{
			ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

			if (stack != null)
			{
				if (stack.getItem() == EnderUtilitiesItems.enderLasso)
				{
					TeleportEntity.lassoTeleportEntity(stack, (EntityLiving)event.target, event.entity.dimension);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
}
