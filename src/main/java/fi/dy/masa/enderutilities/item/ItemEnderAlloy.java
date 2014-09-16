package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemEnderAlloy extends ItemEU
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderAlloy()
	{
		super();
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_ALLOY);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		// Damage 0: Ender Alloy (Basic)
		// Damage 1: Ender Alloy (Enhanced)
		// Damage 2: Ender Alloy (Advanced)
		if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
		{
			return super.getUnlocalizedName() + "." + stack.getItemDamage();
		}

		return super.getUnlocalizedName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		if (EUConfigs.disableItemEnderAlloy.getBoolean(false) == false)
		{
			for (int i = 0; i < 3; i++)
			{
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage)
	{
		if (damage >= 0 && damage <= 2)
		{
			return this.iconArray[damage];
		}

		return this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".0");
		this.iconArray = new IIcon[3];

		for (int i = 0; i < 3; ++i)
		{
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + i);
		}
	}
}
