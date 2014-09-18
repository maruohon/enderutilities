package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEU;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemEnderPart extends ItemEU
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderPart()
	{
		super();
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDERPART);
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
			return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERALLOY + "." + stack.getItemDamage();
		}

		// Damage 10: Ender Core (Basic)
		// Damage 11: Ender Core (Enhanced)
		// Damage 12: Ender Core (Advanced)
		if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
		{
			return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERCORE + "." + (stack.getItemDamage() - 10);
		}

		// Damage 20: Ender Stick
		if (stack.getItemDamage() == 20)
		{
			return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERSTICK;
		}

		return super.getUnlocalizedName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		if (EUConfigs.disableItemCraftingPart.getBoolean(false) == false)
		{
			// Ender Alloys
			for (int i = 0; i <= 2; i++)
			{
				list.add(new ItemStack(this, 1, i));
			}

			// Ender Cores
			for (int i = 10; i <= 12; i++)
			{
				list.add(new ItemStack(this, 1, i));
			}

			list.add(new ItemStack(this, 1, 20)); // Ender Stick
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage)
	{
		// Ender Alloy
		if (damage >= 0 && damage <= 2)
		{
			return this.iconArray[damage];
		}
		// Ender Core
		if (damage >= 10 && damage <= 12)
		{
			return this.iconArray[damage - 7];
		}

		// Ender Stick
		if (damage == 20)
		{
			return this.iconArray[6];
		}

		return this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERALLOY + ".0");
		this.iconArray = new IIcon[7];

		int i = 0, j;

		for (j = 0; j < 3; ++i, ++j)
		{
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERALLOY + "." + j);
		}

		for (j = 0; j < 3; ++i, ++j)
		{
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERCORE + "." + j);
		}

		this.iconArray[6] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDERPART_ENDERSTICK);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		list.add(StatCollector.translateToLocal("gui.tooltip.craftingingredient"));
	}
}
