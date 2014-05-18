package fi.dy.masa.minecraft.mods.enderutilities.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import fi.dy.masa.minecraft.mods.enderutilities.EnderUtilities;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.reference.GuiReference;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderBag extends Item
{
	public EnderBag()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_BAG);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
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
			if (nbt.getByte("mode") == 1 || nbt.getString("owner").equals(player.getDisplayName()) == true)
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
				if (nbt == null || nbt.getByte("mode") == 1 || nbt.getString("owner").equals(player.getDisplayName()) == true)
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
						target.setString("locname", b.getLocalizedName()); // FIXME crappy check
					}

					nbt.setString("owner", player.getDisplayName()); // FIXME
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
		if (stack.getTagCompound() != null)
		{
			NBTTagCompound nbt = stack.getTagCompound();
			String owner	= nbt.getString("owner");
			int dim			= nbt.getCompoundTag("target").getInteger("dim");
			int x			= nbt.getCompoundTag("target").getInteger("posX");
			int y			= nbt.getCompoundTag("target").getInteger("posY");
			int z			= nbt.getCompoundTag("target").getInteger("posZ");
			short numSlots	= nbt.getCompoundTag("target").getShort("numslots");
			String locName	= nbt.getCompoundTag("target").getString("locname");

			String regPre = "" + EnumChatFormatting.OBFUSCATED;
			String dimPre = "" + EnumChatFormatting.OBFUSCATED;
			String coordPre = "" + EnumChatFormatting.OBFUSCATED;
			String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

			// Only show the bound location, if the bag is set to public, or if the player is the owner
			if (nbt.getByte("mode") == 1 || player.getDisplayName().equals(owner) == true) // FIXME
			{
				regPre = "" + EnumChatFormatting.GRAY;
				dimPre = "" + EnumChatFormatting.GREEN;
				coordPre = "" + EnumChatFormatting.BLUE;
			}

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
			list.add(String.format("Type: %s%s%s", coordPre, locName, rst));
			list.add(String.format("Slots: %s%d%s", coordPre, numSlots, rst));
			list.add("Owner: " + owner);
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
	}
}
