package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemEnderPearlReusable extends ItemEU
{
	@SideOnly(Side.CLIENT)
	private IIcon eliteIcon;

	public ItemEnderPearlReusable()
	{
		this.setMaxStackSize(4);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_PEARL_REUSABLE);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		// damage 1: Elite Ender Pearl
		if (stack.getItemDamage() == 1)
		{
			return super.getUnlocalizedName() + ".elite";
		}

		return super.getUnlocalizedName();
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

		EntityEnderPearlReusable pearl;

		// "Elite version" of the pearl, makes the thrower fly with it. Idea by xisumavoid in episode Hermitcraft III 303 :)
		if (stack.getItemDamage() == 1)
		{
			pearl = new EntityEnderPearlReusable(world, player, true);
			pearl.setLetMeFly(true);

			// Dismount the previous pearl if we are already riding one
			if (EntityUtils.getBottomEntity(player) instanceof EntityEnderPearlReusable
				&& EntityUtils.getBottomEntity(player).riddenByEntity != null)
			{
				EntityUtils.getBottomEntity(player).riddenByEntity.mountEntity(null);
			}

			EntityUtils.getBottomEntity(player).mountEntity(pearl);
		}
		else
		{
			pearl = new EntityEnderPearlReusable(world, player);
		}

		--stack.stackSize;
		world.spawnEntityInWorld(pearl);
		world.playSoundAtEntity(player, "random.bow", 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f));

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		if (EUConfigs.disableItemEnderPearl.getBoolean(false) == false)
		{
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 1));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString());
		this.eliteIcon = iconRegister.registerIcon(this.getIconString() + ".elite");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage)
	{
		if (damage == 1)
		{
			return this.eliteIcon;
		}

		return this.itemIcon;
	}
}
