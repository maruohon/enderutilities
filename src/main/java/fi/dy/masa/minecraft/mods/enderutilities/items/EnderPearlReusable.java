package fi.dy.masa.minecraft.mods.enderutilities.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderPearlReusable extends Item
{
	public EnderPearlReusable()
	{
		this.setMaxStackSize(4);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_PEARL_REUSABLE);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (world.isRemote == true)
		{
			return stack;
		}

		--stack.stackSize;
		world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if (world.isRemote == false)
		{
			world.spawnEntityInWorld(new EntityEnderPearlReusable(world, player));
		}

		return stack;
	}
}

