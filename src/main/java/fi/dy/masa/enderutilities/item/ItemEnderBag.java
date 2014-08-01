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
import fi.dy.masa.enderutilities.util.ItemNBTHelperTarget;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBag extends ItemEU implements IChunkLoadingItem
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
		// Do nothing on the client side
		if (world.isRemote == true || stack.getTagCompound() == null)
		{
			return stack;
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			return stack;
		}

		// Access the inventory
		ItemNBTHelperTarget target = new ItemNBTHelperTarget();
		if (target.readFromNBT(nbt) == false)
		{
			return stack;
		}

		World tgtWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
		if (tgtWorld == null)
		{
			return stack;
		}

		IChunkProvider chunkProvider = tgtWorld.getChunkProvider();
		if (chunkProvider == null)
		{
			return stack;
		}

		int chunkX = target.posX >> 4;
		int chunkZ = target.posZ >> 4;

		// Force load the chunk to be sure that it won't unload while we are accessing it
		ExtendedPlayer ep = ExtendedPlayer.get(player);
		if (ep == null)
		{
			ExtendedPlayer.register(player);
			ep = ExtendedPlayer.get(player);
		}

		Ticket ticket = ep.getTicket();
		if (ticket == null)
		{
			ticket = ForgeChunkManager.requestPlayerTicket(EnderUtilities.instance, player.getCommandSenderName(), tgtWorld, Type.NORMAL);
			ticket.getModData().setBoolean("TemporaryTicket", true);
			ticket.getModData().setLong("PlayerUUIDMost", player.getUniqueID().getMostSignificantBits());
			ticket.getModData().setLong("PlayerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
			ep.setTicket(ticket);
		}
		ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(chunkX, chunkZ));

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

			nbt.setBoolean("IsActive", true);
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

		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof IInventory)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
			{
				nbt = new NBTTagCompound();
			}

			Block block = world.getBlock(x, y, z);
			if (block != null && block != Blocks.air)
			{
				nbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
				nbt.setShort("Slots", (short)((IInventory) te).getSizeInventory());
				nbt.setString("Owner", player.getCommandSenderName());
				nbt = ItemNBTHelperTarget.writeToNBT(nbt, x, y, z, player.dimension, side, false);
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
		ItemNBTHelperTarget target = new ItemNBTHelperTarget();
		if (target.readFromNBT(nbt) == false)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));
			return;
		}

		String locName	= Block.getBlockFromName(nbt.getString("BlockName")).getLocalizedName();
		short numSlots	= nbt.getShort("Slots");
		String owner	= nbt.getString("Owner");

		String dimPre = "" + EnumChatFormatting.OBFUSCATED;
		String coordPre = "" + EnumChatFormatting.OBFUSCATED;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		// Only show the bound location, if the bag is set to public, or if the player is the owner
		if (nbt.getByte("Mode") == 1 || player.getCommandSenderName().equals(owner) == true) // FIXME
		{
			dimPre = "" + EnumChatFormatting.GREEN;
			coordPre = "" + EnumChatFormatting.BLUE;
		}

		list.add(StatCollector.translateToLocal("gui.tooltip.type") + ": " + coordPre + locName + rst);
		list.add(StatCollector.translateToLocal("gui.tooltip.slots") + ": " + coordPre + numSlots + rst);
		list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + target.dimension + " " + dimPre + TooltipHelper.getLocalizedDimensionName(target.dimension) + rst);
		list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, target.posX, rst, coordPre, target.posY, rst, coordPre, target.posZ, rst));
		list.add(StatCollector.translateToLocal("gui.tooltip.owner") + ": " + owner);
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
	}
}
