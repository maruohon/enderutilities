package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderPearlReusable extends ItemEU
{
	public ItemEnderPearlReusable()
	{
		this.setMaxStackSize(4);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_PEARL_REUSABLE);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (world.isRemote == true)
		{
			return stack;
		}

		--stack.stackSize;
		world.spawnEntityInWorld(new EntityEnderPearlReusable(world, player));
		world.playSoundAtEntity(player, "random.bow", 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f));

		return stack;
	}
}

