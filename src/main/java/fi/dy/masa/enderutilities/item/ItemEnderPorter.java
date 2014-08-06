package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.client.effects.Sounds;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.ItemNBTHelper;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderPorter extends ItemEU
{
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemEnderPorter()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_PORTER);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
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
				nbt = ItemNBTHelper.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, true);
				stack.setTagCompound(nbt);

				return true;
			}
		}

		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		// This needs to also happen on the client, otherwise the in-use will derp up

		// Don't activate when sneaking and looking at a block, aka. binding to a new location
		if (player.isSneaking() == true)
		{
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				return stack;
			}
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && ItemNBTHelper.hasTargetTag(nbt) == true)
		{
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
			if (player.worldObj.isRemote == false)
			{
				Sounds.playSound(world, player.posX, player.posY, player.posZ, "portal.travel", 0.2f, 1.2f);
			}
			//player.playSound("portal.travel", 0.2f, 1.2f);
		}

		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int inUseCount)
	{
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		ItemNBTHelper target = new ItemNBTHelper();
		if (nbt != null && target.readTargetTagFromNBT(nbt) != null && TeleportEntity.teleportEntityUsingItem(player, stack, false, false) != null)
		{
			if (player.capabilities.isCreativeMode == false && --stack.stackSize <= 0)
			{
				player.destroyCurrentEquippedItem();
			}
		}

		return stack;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 80;
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
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".stage.1");
		this.iconArray = new IIcon[7];

		for (int i = 0; i < this.iconArray.length; ++i)
		{
			this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + ".stage." + (i + 1));
		}
	}

	/**
	 * used to cycle through icons based on their used duration, i.e. for the bow
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getItemIconForUseDuration(int index)
	{
		if (index < 0 || index >= this.iconArray.length)
		{
			index = 0;
		}

		return this.iconArray[index];
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

		if (stack != null && useRemaining > 0)
		{
			int inUse = stack.getMaxItemUseDuration() - useRemaining;
			index += (this.iconArray.length * inUse / stack.getMaxItemUseDuration());
		}

		return this.iconArray[(index < this.iconArray.length ? index : 0)];
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
		ItemNBTHelper target = new ItemNBTHelper();
		if (target.readTargetTagFromNBT(nbt) == null)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
			return;
		}

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + target.dimension + " " + dimPre + TooltipHelper.getLocalizedDimensionName(target.dimension) + rst);
		list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
	}
}
