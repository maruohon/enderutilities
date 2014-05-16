package fi.dy.masa.minecraft.mods.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EntityInteract
{
	@SubscribeEvent
	public void onEntityInteractEvent(EntityInteractEvent event)
	{
		if (event.target instanceof EntityLiving)
		{
			ItemStack stack = event.entityPlayer.inventory.getCurrentItem();

			//if (stack != null && stack.getItem() == GameRegistry.findItem(Reference.MOD_ID, Reference.NAME_ITEM_ENDER_LASSO))
			if (stack != null && stack.getItem() == EnderUtilitiesItems.enderLasso)
			{
				System.out.println("entity interact with lasso!"); // FIXME debug
				event.setCanceled(true);
			}
		}
	}
}
