package fi.dy.masa.minecraft.mods.enderutilities.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.items.EnderLasso;

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
				((EnderLasso)EnderUtilitiesItems.enderLasso).teleportEntity(stack, (EntityLiving)event.target, event.entity.dimension);
				event.setCanceled(true);
			}
		}
	}
}
