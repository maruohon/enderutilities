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
					// FIXME debug: trying to figure out the chunks-loaded-check
					for (int i = -1; i <= 1; i++)
					{
						WorldServer worldServerDst = DimensionManager.getWorld(i);
						System.out.printf("Loaded chunk count for dim %d: %d\n", i, worldServerDst.getChunkProvider().getLoadedChunkCount());
						System.out.println("isDimensionRegistered(): " + DimensionManager.isDimensionRegistered(i));

						for (ChunkCoordIntPair coord : worldServerDst.getPersistentChunks().keySet())
						{
							System.out.printf("Persistent: x: %d, z: %d\n", coord.chunkXPos, coord.chunkZPos);
						}
					}
					TeleportEntity.teleportEntity(stack, (EntityLiving)event.target, event.entity.dimension);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
}
