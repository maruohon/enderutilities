package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBag extends Item
{
	public ItemEnderBag()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BAG);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return stack;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			// The bag must be in public mode, or the player must be the owner
			if (nbt.getByte("mode") == 1 || nbt.getString("owner").equals(player.getCommandSenderName()) == true)
			{
				// Unbind the bag when sneak + right clicking on air TODO is there any point in this?
				if (player.isSneaking() == true)
				{
					MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
					if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)
					{
						stack.setTagCompound(null);
					}
				}
				// Access the inventory
				else
				{
					//player.openGui(EnderUtilities.instance, GuiReference.GUI_ID_ENDER_BAG,
					//		player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
				}
			}
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

		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null)
		{
			if (te instanceof IInventory)
			{
				int numSlots = ((IInventory) te).getSizeInventory();
				int dim = player.dimension; // FIXME is this the right way of getting the dimension?

				NBTTagCompound nbt = stack.getTagCompound();

				// The bag must be unbound, or in public mode, or the player must be the owner
				if (nbt == null || nbt.getByte("mode") == 1 || nbt.getString("owner").equals(player.getCommandSenderName()) == true)
				{
					nbt = new NBTTagCompound();
					NBTTagCompound target = new NBTTagCompound();
					target.setInteger("dim", dim);
					target.setInteger("posX", x);
					target.setInteger("posY", y);
					target.setInteger("posZ", z);
					target.setShort("numslots", (short)numSlots);

					Block b = te.getBlockType();
					if (b != null)
					{
						target.setString("unlocname", b.getUnlocalizedName()); // FIXME crappy check
					}

					nbt.setString("owner", player.getCommandSenderName());
					nbt.setByte("mode", (byte)0); // 0 = private, 1 = public, 2 = friends (N/A)
					nbt.setTag("target", target);
					stack.setTagCompound(nbt);
				}
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
/*
		if (EnderUtilities.proxy.isShiftKeyDown() == false)
		{
			list.add("<" + StatCollector.translateToLocal("gui.tooltip.holdshift") + ">");
			return;
		}
*/

		if (stack.getTagCompound() != null)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			NBTTagCompound target = nbt.getCompoundTag("target");
			String owner	= nbt.getString("owner");
			int dim			= target.getInteger("dim");
			int x			= target.getInteger("posX");
			int y			= target.getInteger("posY");
			int z			= target.getInteger("posZ");
			short numSlots	= target.getShort("numslots");
			String locName	= StatCollector.translateToLocal(target.getString("unlocname"));

			String dimPre = "" + EnumChatFormatting.OBFUSCATED;
			String coordPre = "" + EnumChatFormatting.OBFUSCATED;
			String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

			// Only show the bound location, if the bag is set to public, or if the player is the owner
			if (nbt.getByte("mode") == 1 || player.getCommandSenderName().equals(owner) == true) // FIXME
			{
				dimPre = "" + EnumChatFormatting.GREEN;
				coordPre = "" + EnumChatFormatting.BLUE;
			}

			list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + coordPre + dim + " " + dimPre + TooltipHelper.getLocalizedDimensionName(dim) + rst);
			list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, x, rst, coordPre, y, rst, coordPre, z, rst));
			list.add(StatCollector.translateToLocal("gui.tooltip.type") + ": " + coordPre + locName + rst);
			list.add(StatCollector.translateToLocal("gui.tooltip.slots") + String.format(": %s%d%s", coordPre, numSlots, rst));
			list.add(StatCollector.translateToLocal("gui.tooltip.owner") + ": " + owner);
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
	}
}
