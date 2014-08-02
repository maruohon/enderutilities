package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
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
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.entity.ExtendedPlayer;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.reference.key.ReferenceKeys;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.ItemNBTHelperTarget;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBag extends ItemEU implements IChunkLoadingItem, IKeyBound
{
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
			player.displayGUIChest(player.getInventoryEnderChest());
			return stack;
		}

		// If the bag is not set to public and the player trying to access the bag is not the owner
		if (nbt.getByte("Mode") != (byte)1 &&
			(nbt.getLong("OwnerUUIDMost") != player.getUniqueID().getMostSignificantBits() ||
			nbt.getLong("OwnerUUIDLeast") != player.getUniqueID().getLeastSignificantBits()))
		{
			return stack;
		}

		// Instance of IInventory; Get the target information
		ItemNBTHelperTarget target = new ItemNBTHelperTarget();
		if (target.readFromNBT(nbt) == false) { return stack; }

		World tgtWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
		if (tgtWorld == null) { return stack; }
		IChunkProvider chunkProvider = tgtWorld.getChunkProvider();
		if (chunkProvider == null) { return stack; }

		int chunkX = target.posX >> 4;
		int chunkZ = target.posZ >> 4;

		ExtendedPlayer ep = ExtendedPlayer.get(player);
		if (ep == null)
		{
			ExtendedPlayer.register(player);
			ep = ExtendedPlayer.get(player);
		}

		// Force load the chunk to be sure that it won't unload while we are accessing it
		Ticket ticket = ep.getTemporaryTicket(tgtWorld);
		if (ticket == null)
		{
			ticket = ForgeChunkManager.requestPlayerTicket(EnderUtilities.instance, player.getCommandSenderName(), tgtWorld, Type.NORMAL);
			if (ticket == null) { return stack; }

			ticket.getModData().setBoolean("TemporaryTicket", true);
			ticket.getModData().setLong("PlayerUUIDMost", player.getUniqueID().getMostSignificantBits());
			ticket.getModData().setLong("PlayerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
			ep.setTemporaryTicket(tgtWorld, ticket);
		}
		ChunkCoordIntPair ccip = new ChunkCoordIntPair(chunkX, chunkZ);
		ForgeChunkManager.forceChunk(ticket, ccip);
		// 60 second delay before unloading
		ChunkLoading.getInstance().addChunkTimeout(tgtWorld, target.dimension, ccip, 60 * 20);

		// Load the chunk if necessary
		if (chunkProvider.chunkExists(chunkX, chunkZ) == false)
		{
			chunkProvider.loadChunk(chunkX, chunkZ);
		}

		// Only open the GUI if the chunk is now loaded
		if (chunkProvider.chunkExists(chunkX, chunkZ) == true)
		{
			Block block = tgtWorld.getBlock(target.posX, target.posY, target.posZ);
			if (block == null)
			{
				return stack;
			}

			// The target block has changed since binding the bag, remove the bind (not for Ender Chests)
			if (Block.blockRegistry.getNameForObject(block).equals(nbt.getString("BlockName")) == false)
			{
				nbt.removeTag("BlockName");
				nbt.removeTag("Slots");
				ItemNBTHelperTarget.writeTargetToItem(stack, null);
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
			block.onBlockActivated(tgtWorld, target.posX, target.posY, target.posZ, player, target.blockFace, 0.5f, 0.5f, 0.5f);
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

		// If the player trying to set/modify the bag is not the owner
		if (nbt.hasKey("OwnerUUIDMost") == true && nbt.hasKey("OwnerUUIDLeast") == true &&
			(nbt.getLong("OwnerUUIDMost") != player.getUniqueID().getMostSignificantBits() ||
			nbt.getLong("OwnerUUIDLeast") != player.getUniqueID().getLeastSignificantBits()))
		{
			return false;
		}

		Block block = world.getBlock(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);
		if (block == null || block == Blocks.air || te == null)
		{
			return false;
		}

		if (te instanceof TileEntityEnderChest || te instanceof IInventory)
		{
			nbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
			nbt.setString("Owner", player.getCommandSenderName());
			nbt.setLong("OwnerUUIDMost", player.getUniqueID().getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
			nbt = ItemNBTHelperTarget.writeToNBT(nbt, x, y, z, player.dimension, side, false);

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
			return true;
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
		ItemNBTHelperTarget target = new ItemNBTHelperTarget();
		if (target.readFromNBT(nbt) == false)
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
}
