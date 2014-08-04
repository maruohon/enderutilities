package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.reference.key.ReferenceKeys;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.ItemNBTHelper;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBag extends ItemEU implements IChunkLoadingItem, IKeyBound
{
	@SideOnly(Side.CLIENT)
	private IIcon iconArray[];

	public ItemEnderBag()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BAG);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (world.isRemote == true || nbt == null)
		{
			return stack;
		}

		// Ender Chest
		if (nbt.hasKey("Type") == true && nbt.getByte("Type") == (byte)1)
		{
			nbt.setBoolean("IsOpen", true);
			player.displayGUIChest(player.getInventoryEnderChest());
			return stack;
		}

		ItemNBTHelper itemData = new ItemNBTHelper();
		// If the bag is not set to public and the player trying to access the bag is not the owner
		if (nbt.getByte("Mode") != (byte)1 && itemData.readPlayerTagFromNBT(nbt) != null &&
			(itemData.playerUUIDMost != player.getUniqueID().getMostSignificantBits() ||
			itemData.playerUUIDLeast != player.getUniqueID().getLeastSignificantBits()))
		{
			return stack;
		}

		// Instance of IInventory (= not Ender Chest); Get the target information
		if (itemData.readTargetTagFromNBT(nbt) == null)
		{
			return stack;
		}

		// Only open the GUI if the chunk loading succeeds. 60 second unload delay.
		if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, itemData.dimension, itemData.posX >> 4, itemData.posZ >> 4, 60 * 20) == true)
		{
			World tgtWorld = MinecraftServer.getServer().worldServerForDimension(itemData.dimension);
			if (tgtWorld == null)
			{
				return stack;
			}
			Block block = tgtWorld.getBlock(itemData.posX, itemData.posY, itemData.posZ);
			if (block == null)
			{
				return stack;
			}

			// The target block has changed since binding the bag, remove the bind (not for Ender Chests)
			if (Block.blockRegistry.getNameForObject(block).equals(nbt.getString("BlockName")) == false)
			{
				nbt.removeTag("BlockName");
				nbt.removeTag("Slots");
				nbt = ItemNBTHelper.removeTargetTagFromNBT(nbt);
				nbt.removeTag("ChunkLoadingRequired");
				nbt.setBoolean("IsOpen", false);
				stack.setTagCompound(nbt);
				player.addChatMessage(new ChatComponentTranslation("chat.message.enderbag.blockchanged"));
				return stack;
			}

			nbt.setBoolean("ChunkLoadingRequired", true);
			nbt.setBoolean("IsOpen", true);
			stack.setTagCompound(nbt);

			// Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
			block.onBlockActivated(tgtWorld, itemData.posX, itemData.posY, itemData.posZ, player, itemData.blockFace, 0.5f, 0.5f, 0.5f);
		}

		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (player.isSneaking() == false)
		{
			return false;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
			nbt.setByte("Mode", (byte)0);
		}

		ItemNBTHelper itemData = new ItemNBTHelper();
		// If the player trying to set/modify the bag is not the owner
		if (itemData.readPlayerTagFromNBT(nbt) != null &&
			(itemData.playerUUIDMost != player.getUniqueID().getMostSignificantBits() ||
			itemData.playerUUIDLeast != player.getUniqueID().getLeastSignificantBits()))
		{
			return true;
		}

		Block block = world.getBlock(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);
		if (block == null || block == Blocks.air || te == null)
		{
			return true;
		}

		if (te instanceof TileEntityEnderChest || te instanceof IInventory)
		{
			nbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
			nbt.setString("Owner", player.getCommandSenderName());
			nbt.setLong("OwnerUUIDMost", player.getUniqueID().getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
			nbt = ItemNBTHelper.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, false);

			if (te instanceof IInventory)
			{
				nbt.setInteger("Slots", ((IInventory)te).getSizeInventory());
				nbt.setByte("Type", (byte)0);
			}
			else
			{
				nbt.setInteger("Slots", player.getInventoryEnderChest().getSizeInventory());
				nbt.setByte("Type", (byte)1);
			}

			stack.setTagCompound(nbt);
		}

		return true;
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

		String locName	= Block.getBlockFromName(nbt.getString("BlockName")).getLocalizedName();
		int numSlots	= nbt.getInteger("Slots");
		String owner	= nbt.getString("Owner");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		list.add(StatCollector.translateToLocal("gui.tooltip.type") + ": " + coordPre + locName + rst);
		list.add(StatCollector.translateToLocal("gui.tooltip.slots") + ": " + coordPre + numSlots + rst);

		// Only show the location info if the bag is not bound to an ender chest, and if the player is the owner
		if (nbt.getByte("Type") == (byte)0 &&
			nbt.getLong("OwnerUUIDMost") == player.getUniqueID().getMostSignificantBits() &&
			nbt.getLong("OwnerUUIDLeast") == player.getUniqueID().getLeastSignificantBits())
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + target.dimension + " " + dimPre + TooltipHelper.getLocalizedDimensionName(target.dimension) + rst);
			list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
		}

		// Only show private vs. public when bound to regular inventories, not Ender Chest
		if (nbt.getByte("Type") == (byte)0)
		{
			String mode = (nbt.getByte("Mode") == (byte)1 ? StatCollector.translateToLocal("gui.tooltip.public") : StatCollector.translateToLocal("gui.tooltip.private"));
			list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + mode);
			list.add(StatCollector.translateToLocal("gui.tooltip.owner") + ": " + owner);
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
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
			nbt.setByte("Mode", val);
			stack.setTagCompound(nbt);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent)
	{
		// Ugly workaround to get the bag closing tag update to sync to the client
		// For some reason it won't sync if set directly in the PlayerOpenContainerEvent
		if (stack != null && stack.getTagCompound() != null)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("IsOpenDummy") == true)
			{
				nbt.removeTag("IsOpenDummy");
				nbt.setBoolean("IsOpen", false);
			}
		}
	}

	/**
	 * Called when a player drops the item into the world,
	 * returning false from this will prevent the item from
	 * being removed from the players inventory and spawning
	 * in the world
	 *
	 * @param player The player that dropped the item
	 * @param item The item stack, before the item is removed.
	 */
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
	{
		if (item != null && item.getTagCompound() != null && item.getTagCompound().getBoolean("IsOpen") == true)
		{
			return false;
		}

		return true;
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
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
		this.iconArray = new IIcon[4];

		this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
		this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".regular.open");
		this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".enderchest.closed");
		this.iconArray[3] = iconRegister.registerIcon(this.getIconString() + ".enderchest.open");
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

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null)
		{
			// Linked to Ender Chest
			if (nbt.getByte("Type") == (byte)1)
			{
				index += 2;
			}

			// Bag currently open
			if (nbt.hasKey("IsOpen") == true && nbt.getBoolean("IsOpen") == true)
			{
				index += 1;
			}
		}

		return this.iconArray[(index < this.iconArray.length ? index : 0)];
	}
}
