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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.setup.EURegistry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
	@SideOnly(Side.CLIENT)
	private IIcon iconArray[];

	public ItemEnderBag()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BAG);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
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
		if (nbt.hasKey("Type", Constants.NBT.TAG_BYTE) == true && nbt.getByte("Type") == (byte)1)
		{
			nbt.setBoolean("IsOpen", true);
			player.displayGUIChest(player.getInventoryEnderChest());
			return stack;
		}

		NBTHelperPlayer playerData = new NBTHelperPlayer();
		// If the bag is not set to public and the player trying to access the bag is not the owner
		if (nbt.getByte("Mode") != (byte)1 && playerData.readPlayerTagFromNBT(nbt) != null &&
			(playerData.playerUUIDMost != player.getUniqueID().getMostSignificantBits() ||
			playerData.playerUUIDLeast != player.getUniqueID().getLeastSignificantBits()))
		{
			return stack;
		}

		NBTHelperTarget targetData = new NBTHelperTarget();
		// Instance of IInventory (= not Ender Chest); Get the target information
		if (targetData.readTargetTagFromNBT(nbt) == null)
		{
			return stack;
		}

		// Target block is not whitelisted, so it is known to not work unless within the client's loaded region
		// FIXME: How should we properly check if the player is within range?
		if (this.isTargetBlockWhitelisted(nbt.getString("BlockName")) == false &&
			(targetData.dimension != player.dimension || player.getDistanceSq(targetData.posX, targetData.posY, targetData.posZ) >= 10000.0d))
		{
			return stack;
		}

		// Only open the GUI if the chunk loading succeeds. 60 second unload delay.
		if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 60 * 20) == true)
		{
			World tgtWorld = MinecraftServer.getServer().worldServerForDimension(targetData.dimension);
			if (tgtWorld == null)
			{
				return stack;
			}
			Block block = tgtWorld.getBlock(targetData.posX, targetData.posY, targetData.posZ);
			if (block == null)
			{
				return stack;
			}

			// The target block has changed since binding the bag, remove the bind (not for Ender Chests)
			if (Block.blockRegistry.getNameForObject(block).equals(nbt.getString("BlockName")) == false)
			{
				nbt.removeTag("BlockName");
				nbt.removeTag("Slots");
				nbt = NBTHelperTarget.removeTargetTagFromNBT(nbt);
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
			block.onBlockActivated(tgtWorld, targetData.posX, targetData.posY, targetData.posZ, player, targetData.blockFace, 0.5f, 0.5f, 0.5f);
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

		if (world.isRemote == true)
		{
			return true;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
			nbt.setByte("Mode", (byte)0);
		}

		NBTHelperPlayer playerData = new NBTHelperPlayer();
		// If the player trying to set/modify the bag is not the owner
		if (playerData.readPlayerTagFromNBT(nbt) != null &&
			(playerData.playerUUIDMost != player.getUniqueID().getMostSignificantBits() ||
			playerData.playerUUIDLeast != player.getUniqueID().getLeastSignificantBits()))
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
			// FIXME debug
			//System.out.println("Block.blockRegistry.getNameForObject(): " + Block.blockRegistry.getNameForObject(block));
			//System.out.println("block.getClass().getSimpleName(): " + block.getClass().getSimpleName());
			//System.out.println("block.getUnlocalizedName(): " + block.getUnlocalizedName());
			//System.out.println("block.getLocalizedName(): " + block.getLocalizedName());
			//System.out.println("te.getClass().getSimpleName(): " + te.getClass().getSimpleName());
			//System.out.println("--------------------------------------------------");

			if (this.isTargetBlockWhitelisted(Block.blockRegistry.getNameForObject(block)) == false)
			{
				//EnderUtilities.logger.info("Ender Bag: Block '" + Block.blockRegistry.getNameForObject(block) + "' is not whitelisted, or is blacklisted.");
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.message.enderbag.blocknotwhitelisted") + " '" + Block.blockRegistry.getNameForObject(block) + "'"));
				return true;
			}

			nbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
			nbt = NBTHelperTarget.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, hitX, hitY, hitZ, false);
			nbt = NBTHelperPlayer.writePlayerTagToNBT(nbt, player);

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

	/* Returns the maximum number of modules that can be installed on this item. */
	@Override
	public int getMaxModules(ItemStack stack)
	{
		return 4;
	}

	private boolean isTargetBlockWhitelisted(String name)
	{
		List<String> list;

		// Black list
		if (EUConfigs.enderBagListType.getString().equalsIgnoreCase("blacklist") == true)
		{
			list = EURegistry.getEnderbagBlacklist();
			if (list.contains(name) == true)
			{
				return false;
			}
			return true;
		}
		// White list
		else
		{
			list = EURegistry.getEnderbagWhitelist();
			if (list.contains(name) == true)
			{
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
		NBTHelperTarget targetData = new NBTHelperTarget();
		if (targetData.readTargetTagFromNBT(nbt) == null)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
			return;
		}

		String locName	= Block.getBlockFromName(nbt.getString("BlockName")).getLocalizedName();
		int numSlots	= nbt.getInteger("Slots");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		list.add(StatCollector.translateToLocal("gui.tooltip.type") + ": " + coordPre + locName + rst);
		list.add(StatCollector.translateToLocal("gui.tooltip.slots") + ": " + coordPre + numSlots + rst);

		NBTHelperPlayer playerData = new NBTHelperPlayer();
		// Only show the location info if the bag is not bound to an ender chest, and if the player is the owner
		if (nbt.getByte("Type") == (byte)0 && playerData.readPlayerTagFromNBT(nbt) != null &&
			playerData.playerUUIDMost == player.getUniqueID().getMostSignificantBits() &&
			playerData.playerUUIDLeast == player.getUniqueID().getLeastSignificantBits())
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + targetData.dimension + " " + dimPre + TooltipHelper.getLocalizedDimensionName(targetData.dimension) + rst);
			list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", coordPre, targetData.posX, rst, coordPre, targetData.posY, rst, coordPre, targetData.posZ, rst));
		}

		// Only show private vs. public when bound to regular inventories, not Ender Chest
		if (nbt.getByte("Type") == (byte)0)
		{
			String mode = (nbt.getByte("Mode") == (byte)1 ? StatCollector.translateToLocal("gui.tooltip.public") : StatCollector.translateToLocal("gui.tooltip.private"));
			list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + mode);
			list.add(StatCollector.translateToLocal("gui.tooltip.owner") + ": " + playerData.playerName);
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
				NBTHelperPlayer playerData = new NBTHelperPlayer();
				if (playerData.readPlayerTagFromNBT(nbt) != null &&
					(playerData.playerUUIDMost != player.getUniqueID().getMostSignificantBits() ||
					playerData.playerUUIDLeast != player.getUniqueID().getLeastSignificantBits()))
				{
					return;
				}
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
