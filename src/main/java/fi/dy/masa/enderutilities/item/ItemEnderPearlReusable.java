package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderPearlReusable extends Item
{
	public ItemEnderPearlReusable()
	{
		this.setMaxStackSize(4);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_PEARL_REUSABLE);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		--stack.stackSize;
		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (world.isRemote == false)
		{
			world.spawnEntityInWorld(new EntityEnderPearlReusable(world, player));
		}

		return stack;
	}
}

