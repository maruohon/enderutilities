package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.reference.key.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.ItemNBTHelper;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBow extends ItemEU implements IKeyBound
{
	public static final byte BOW_MODE_TP_TARGET = 0;
	public static final byte BOW_MODE_TP_SELF = 1;

	public static final String[] bowPullIconNameArray = new String[] {"standby", "pulling.0", "pulling.1", "pulling.2",
							"mode2.standby", "mode2.pulling.0", "mode2.pulling.1", "mode2.pulling.2"};
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderBow()
	{
		super();
		this.setMaxStackSize(1);
		this.setMaxDamage(384);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BOW);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	/**
	 * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack bowStack, World world, EntityPlayer player, int itemInUseCount)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return;
		}

		int j = this.getMaxItemUseDuration(bowStack) - itemInUseCount;

		ArrowLooseEvent event = new ArrowLooseEvent(player, bowStack, j);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return;
		}
		j = event.charge;

		if (player.capabilities.isCreativeMode == true || player.inventory.hasItem(EnderUtilitiesItems.enderArrow))
		{
			int x = (int)player.posX;
			int y = (int)player.posY;
			int z = (int)player.posZ;
			int dim = player.dimension;
			byte mode = BOW_MODE_TP_TARGET;

			NBTTagCompound nbt = bowStack.getTagCompound();
			if (nbt != null && nbt.hasKey("Mode") == true)
			{
				mode = nbt.getByte("Mode");
			}

			if (mode == BOW_MODE_TP_SELF)
			{
				if (EUConfigs.enderBowAllowSelfTP.getBoolean(true) == false)
				{
					return;
				}
			}

			if (mode == BOW_MODE_TP_TARGET)
			{
				ItemNBTHelper target = new ItemNBTHelper();
				// If we want to TP the target, we must have a valid target set
				if (target.readTargetTagFromNBT(nbt) == null)
				{
					return;
				}

				x = target.posX;
				y = target.posY;
				z = target.posZ;
				dim = target.dimension;
			}

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
			entityenderarrow.setTpTarget(x, y, z, dim);
			entityenderarrow.setTpMode(mode);

			if (f == 1.0F)
			{
				entityenderarrow.setIsCritical(true);
			}

			if (player.capabilities.isCreativeMode == false)
			{
				player.inventory.consumeInventoryItem(EnderUtilitiesItems.enderArrow);
				bowStack.damageItem(1, player);
			}

			world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
			world.spawnEntityInWorld(entityenderarrow);
		}
	}

	@Override
	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		return par1ItemStack;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 72000;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		// This needs to also happen on the client, otherwise the bow won't be set to in use
		ArrowNockEvent event = new ArrowNockEvent(player, stack);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			return event.result;
		}

		// Don't shoot when sneaking and looking at a block, aka. binding the bow to a new location
		if (player.isSneaking() == true)
		{
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				return stack;
			}
		}

		if (player.capabilities.isCreativeMode == true || player.inventory.hasItem(EnderUtilitiesItems.enderArrow))
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null || nbt.hasKey("Mode", Constants.NBT.TAG_BYTE) == false)
			{
				return stack;
			}

			// If the bow is in 'TP target' mode, it has to have a valid target set
			if (nbt.getByte("Mode") == BOW_MODE_TP_TARGET && ItemNBTHelper.hasTargetTag(nbt) == false)
			{
				return stack;
			}

			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		}

		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (stack == null)
		{
			return false;
		}

		if (player.isSneaking() == true)
		{
			// Sneaking and targeting a block: store the location
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt == null || nbt.hasKey("Mode", Constants.NBT.TAG_BYTE) == false)
				{
					nbt = new NBTTagCompound();
					nbt.setByte("Mode", BOW_MODE_TP_TARGET);
				}

				nbt = ItemNBTHelper.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, true);
				stack.setTagCompound(nbt);

				return true;
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
/*
		if (EnderUtilities.proxy.isShiftKeyDown() == false)
		{
			list.add("<" + StatCollector.translateToLocal("gui.tooltip.holdshift") + ">");
			return;
		}
*/

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
			return;
		}

		byte mode = BOW_MODE_TP_TARGET;
		if (nbt.hasKey("Mode", Constants.NBT.TAG_BYTE))
		{
			mode = nbt.getByte("Mode");
		}

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		// TP self to impact point
		if (mode == BOW_MODE_TP_SELF)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + EnumChatFormatting.RED + StatCollector.translateToLocal("gui.tooltip.tpself") + rst);
			return;
		}

		// TP the target entity
		ItemNBTHelper target = new ItemNBTHelper();
		if (target.readTargetTagFromNBT(nbt) == null)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
			return;
		}

		list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + coordPre + StatCollector.translateToLocal("gui.tooltip.tptarget") + rst);
		list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + target.dimension + " " + dimPre + TooltipHelper.getLocalizedDimensionName(target.dimension) + rst);
		list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
	}

	/**
	 * Return the enchantability factor of the item, most of the time is based on material.
	 */
	@Override
	public int getItemEnchantability()
	{
		return 0;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		if (stack1 != null && stack1.getItem() == EnderUtilitiesItems.enderBow)
		{
			if (stack2 != null && stack2.getItem() == Items.diamond)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderPasses(int metadata)
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".standby");
		this.iconArray = new IIcon[bowPullIconNameArray.length];

		for (int i = 0; i < this.iconArray.length; ++i)
		{
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + bowPullIconNameArray[i]);
		}
	}

	/**
	 * used to cycle through icons based on their used duration, i.e. for the bow
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getItemIconForUseDuration(int par1)
	{
		return this.iconArray[par1];
	}

	/**
	 * Return the correct icon for rendering based on the supplied ItemStack and render pass.
	 *
	 * Defers to {@link #getIconFromDamageForRenderPass(int, int)}
	 * @param stack to render for
	 * @param pass the multi-render pass
	 * @return the icon
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass)
	{
		return this.getIcon(stack, renderPass, null, null, 0);
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
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		int index = 0;
		byte mode = 0;

		if (stack.getTagCompound() != null)
		{
			mode = stack.getTagCompound().getByte("Mode");
			if (mode > 1 || mode < 0) { mode = 0; }
			index = mode * 4;
		}

		if (player != null && player.getItemInUse() != null)
		{
			int inUse = 0;
			if (stack != null)
			{
				inUse = stack.getMaxItemUseDuration() - useRemaining;
			}
			if (inUse >= 18) { index += 3; }
			else if (inUse >= 13) { index += 2; }
			else if (inUse > 0) { index += 1; }
		}

		return this.getItemIconForUseDuration(index);
	}

	@Override
	public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
	{
		if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
		{
			byte val = 0;
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt != null)
			{
				val = nbt.getByte("Mode");
			}
			else
			{
				nbt = new NBTTagCompound();
			}
			if (++val > 1)
			{
				val = 0;
			}
			if (EUConfigs.enderBowAllowSelfTP.getBoolean(true) == false)
			{
				val = BOW_MODE_TP_TARGET;
			}
			nbt.setByte("Mode", val);
			stack.setTagCompound(nbt);
		}
	}
}
