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
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemLinkCrystal extends ItemEU
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemLinkCrystal()
	{
		super();
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDERPART_LINKCRYSTAL);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		// Damage 0: Link Crystal (In-World)
		// Damage 1: Link Crystal (Inventory)
		// Damage 2: Link Crystal (Portal)
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
		if (EUConfigs.disableItemLinkCrystal.getBoolean(false) == false)
		{
			for (int i = 0; i <= 2; i++)
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
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".inventory");
		this.iconArray = new IIcon[3];

		this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".world");
		this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".inventory");
		this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".portal");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		list.add(StatCollector.translateToLocal("gui.tooltip.notlinked"));
	}
}
