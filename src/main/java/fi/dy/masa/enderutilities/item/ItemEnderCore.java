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

public class ItemEnderCore extends ItemEU implements IChargeable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderCore()
	{
		super();
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_CORE);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		// Damage 0: Ender Core (Basic)
		// Damage 1: Ender Core (Enhanced)
		// Damage 2: Ender Core (Advanced)
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
		if (EUConfigs.disableItemEnderCore.getBoolean(false) == false)
		{
			for (int i = 0; i < 3; i++)
			{
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	public int getCapacity(ItemStack stack)
	{
		if (stack.getItemDamage() == 1) { return 1024; } // Enhanced
		if (stack.getItemDamage() == 2) { return 4096; } // Advanced
		return 256; // Basic
	}

	public int getCharge(ItemStack stack)
	{
		// TODO
		return 0;
	}

	public int addCharge(ItemStack stack, int amount, boolean simulate)
	{
		// TODO
		return 0;
	}

	public int useCharge(ItemStack stack, int amount, boolean simulate)
	{
		// TODO
		return 0;
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
