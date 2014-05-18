package fi.dy.masa.minecraft.mods.enderutilities.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderBow extends Item
{
	public static final String[] bowPullIconNameArray = new String[] {"pulling.0", "pulling.1", "pulling.2"};
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public EnderBow()
	{
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(384);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_BOW);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	/**
	 * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
	 */
    @Override
	public void onPlayerStoppedUsing(ItemStack bowStack, World world, EntityPlayer player, int itemInUseCount)
	{
		int j = this.getMaxItemUseDuration(bowStack) - itemInUseCount;

		ArrowLooseEvent event = new ArrowLooseEvent(player, bowStack, j);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return;
		}
//		j = event.charge;
//		boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bowStack) > 0;

		// flag ||
		if (player.inventory.hasItem(EnderUtilitiesItems.enderArrow))
		{
			float f = (float)j / 20.0F;
			f = (f * f + f * 2.0F) / 3.0F;

			if ((double)f < 0.1D)
			{
				return;
			}

			if (f > 1.0F)
			{
				f = 1.0F;
			}

			EntityEnderArrow entityenderarrow = new EntityEnderArrow(world, player, f * 2.0F);
			//int slot = player.inventory.func_146029_c(EnderUtilitiesItems.enderArrow);
			//ItemStack stack = player.inventory.getStackInSlot(slot);
			NBTTagCompound nbt = bowStack.getTagCompound();
			int x = (int)player.posX;
			int y = (int)player.posY;
			int z = (int)player.posZ;
			int dim = player.dimension;
			if (nbt != null)
			{
				x = nbt.getInteger("x");
				y = nbt.getInteger("y");
				z = nbt.getInteger("z");
				dim = nbt.getInteger("dim");
			}
			entityenderarrow.setTpTarget(x, y, z, dim);
/*
			if (f == 1.0F)
			{
				entityarrow.setIsCritical(true);
			}
			int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bowStack);
			if (k > 0)
			{
					entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
			}
			int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bowStack);
			if (l > 0)
			{
				entityarrow.setKnockbackStrength(l);
			}
			if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bowStack) > 0)
			{
				entityarrow.setFire(100);
			}
			bowStack.damageItem(1, player);
			world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
			if (flag)
			{
				entityarrow.canBePickedUp = 2;
			}
			else
			{
				//player.inventory.consumeInventoryItem(EnderUtilitiesItems.enderArrow);
			}
*/
			player.inventory.consumeInventoryItem(EnderUtilitiesItems.enderArrow);
			bowStack.damageItem(1, player);
			world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
			if (world.isRemote == false)
			{
				world.spawnEntityInWorld(entityenderarrow);
			}
		}
	}

	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		return par1ItemStack;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 72000;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

    /**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack stack, World par2World, EntityPlayer player)
	{
		ArrowNockEvent event = new ArrowNockEvent(player, stack);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return event.result;
		}

		// Don't shoot when sneaking and looking at a block, aka. binding the bow to a new location
		if (Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
				player.isSneaking() == true)
		{
			return stack;
		}

		if (player.inventory.hasItem(EnderUtilitiesItems.enderArrow))
		{
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		}

		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return false;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		if (player.isSneaking() == true)
		{
			// Sneaking and targeting a block: store the location
			if (Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				String strSide = "top";

				// Adjust the target block position
				if (side == 0) { --y; strSide = "bottom"; }
				if (side == 1) { ++y; }
				if (side == 2) { --z; strSide = "east"; }
				if (side == 3) { ++z; strSide = "west"; }
				if (side == 4) { --x; strSide = "north"; }
				if (side == 5) { ++x; strSide = "south"; }

				nbt.setInteger("dim", player.dimension);
				nbt.setInteger("x", x);
				nbt.setInteger("y", y);
				nbt.setInteger("z", z);
				nbt.setString("side", strSide);
				stack.setTagCompound(nbt);
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			list.add("No target set");
			return;
		}

		String side	= nbt.getString("side");
		int dim		= nbt.getInteger("dim");
		int x		= nbt.getInteger("x");
		int y		= nbt.getInteger("y");
		int z		= nbt.getInteger("z");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		if (dim >= -1 && dim <= 1)
		{
			String dimStr = (dim == -1 ? "Nether" : (dim == 0 ? "Overworld" : "The End"));
			list.add(String.format("Dimension: %s%s%s", dimPre, dimStr, rst));
		}
		else
		{
			list.add(String.format("Dimension: %s%d%s", dimPre, dim, rst));
		}

		list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, x, rst, coordPre, y, rst, coordPre, z, rst));
	}

	/**
	 * Return the enchantability factor of the item, most of the time is based on material.
	 */
	public int getItemEnchantability()
	{
		return 0;
	}

    @SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".standby");
		this.iconArray = new IIcon[bowPullIconNameArray.length];

		for (int i = 0; i < this.iconArray.length; ++i)
		{
			System.out.printf("registered icon %d %s\n", i, this.getIconString() + "." + bowPullIconNameArray[i]);
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + bowPullIconNameArray[i]);
		}
	}

	/**
	 * used to cycle through icons based on their used duration, i.e. for the bow
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getItemIconForUseDuration(int par1)
	{
//		System.out.printf("geticon: %d\n", par1);
		return this.iconArray[par1];
	}

	/**
	 * Player, Render pass, and item usage sensitive version of getIconIndex.
	 *
	 * @param stack The item stack to get the icon for. (Usually this, and usingItem will be the same if usingItem is not null)
	 * @param renderPass The pass to get the icon for, 0 is default.
	 * @param player The player holding the item
	 * @param usingItem The item the player is actively using. Can be null if not using anything.
	 * @param useRemaining The ticks remaining for the active item.
	 * @return The icon index
	 */
	@Override
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		if (player.getItemInUse() != null)
		{
			int inUse = stack.getMaxItemUseDuration() - useRemaining;
			if (inUse >= 18) { return this.getItemIconForUseDuration(2); }
			if (inUse >= 13) { return this.getItemIconForUseDuration(1); }
			if (inUse > 0) { return this.getItemIconForUseDuration(0); }
		}
		return this.itemIcon;
	}
}
