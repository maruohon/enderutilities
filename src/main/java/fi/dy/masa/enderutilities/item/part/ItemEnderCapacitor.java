package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.ItemEU;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperEnderCharge;

public class ItemEnderCapacitor extends ItemEU implements IChargeable
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderCapacitor()
	{
		super();
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_CAPACITOR);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		// Damage 0: Ender Capacitor (Basic)
		// Damage 1: Ender Capacitor (Enhanced)
		// Damage 2: Ender Capacitor (Advanced)
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
		for (int i = 0; i <= 2; i++)
		{
			list.add(new ItemStack(this, 1, i));
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
		NBTHelperEnderCharge charge = new NBTHelperEnderCharge();
		if (charge.readChargeTagFromNBT(stack.getTagCompound()) == null)
		{
			return 0;
		}

		return charge.enderChargeAmount;
	}

	public int addCharge(ItemStack stack, int amount, boolean simulate)
	{
		int charge = this.getCharge(stack);
		int capacity = this.getCapacity(stack);

		if ((capacity - charge) < amount)
		{
			amount = (capacity - charge);
		}

		if (simulate == false)
		{
			NBTTagCompound nbt = NBTHelperEnderCharge.writeChargeTagToNBT(stack.getTagCompound(), capacity, charge + amount);
			stack.setTagCompound(nbt);
		}

		return amount;
	}

	public int useCharge(ItemStack stack, int amount, boolean simulate)
	{
		int charge = this.getCharge(stack);

		if (charge < amount)
		{
			return 0;
		}

		if (simulate == false)
		{
			NBTTagCompound nbt = NBTHelperEnderCharge.writeChargeTagToNBT(stack.getTagCompound(), this.getCapacity(stack), charge - amount);
			stack.setTagCompound(nbt);
		}

		return amount;
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

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		int charge = 0;
		int capacity = this.getCapacity(stack);
		NBTHelperEnderCharge chargeData = new NBTHelperEnderCharge();

		if (chargeData.readChargeTagFromNBT(stack.getTagCompound()) != null)
		{
			charge = chargeData.enderChargeAmount;
		}

		list.add(StatCollector.translateToLocal("gui.tooltip.charge") + ": " + charge + " / " + capacity);
	}
}
