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
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderBag extends Item
{
	public EnderBag()
	{
		super();
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_BAG);
		this.setTextureName(Reference.MOD_ID + ":" + this.getUnlocalizedName()); // FIXME?
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (stack.stackTagCompound != null)
		{
			// The bag must be in public mode, or the player must be the owner
			if (stack.stackTagCompound.getByte("mode") == 1
				|| stack.stackTagCompound.getString("owner").equals(player.getDisplayName()) == true)
			{
				// Unbind the bag when sneak + right clicking on air
				if (player.isSneaking() == true && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)
				{
					stack.stackTagCompound = null;
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
				//System.out.printf("Block at %d, %d, %d (dim: %d) has an inventory of %d slots\n", x, y, z, dim, numSlots); // FIXME debug
				//System.out.println("te: " + te.toString()); // FIXME debug

				// The bag must be in public mode, or the player must be the owner
				if (stack.stackTagCompound == null
					|| stack.stackTagCompound.getByte("mode") == 1
					|| stack.stackTagCompound.getString("owner").equals(player.getDisplayName()) == true)
				{
					stack.stackTagCompound = new NBTTagCompound();
					NBTTagCompound target = new NBTTagCompound();
					target.setInteger("dim", dim);
					target.setInteger("posX", x);
					target.setInteger("posY", y);
					target.setInteger("posZ", z);
					target.setShort("numslots", (short)numSlots);
					Block b = te.getBlockType();
					if (b != null)
					{
						String name = b.getUnlocalizedName();
						target.setString("unlocname", name); // FIXME crappy check
						name = b.getLocalizedName();
						target.setString("locname", name); // FIXME crappy check
					}

					stack.stackTagCompound.setString("owner", player.getDisplayName()); // FIXME
					stack.stackTagCompound.setByte("mode", (byte)0); // 0 = private, 1 = public, 2 = friends (N/A)
					stack.stackTagCompound.setTag("target", target);
				}
			}
			//System.out.println("Is Tile Entity");
		}
		else
		{
			//System.out.println("Not a Tile Entity");
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (stack.stackTagCompound != null)
		{
			String owner = stack.stackTagCompound.getString("owner");
			int dim = stack.stackTagCompound.getCompoundTag("target").getInteger("dim");
			int x = stack.stackTagCompound.getCompoundTag("target").getInteger("posX");
			int y = stack.stackTagCompound.getCompoundTag("target").getInteger("posY");
			int z = stack.stackTagCompound.getCompoundTag("target").getInteger("posZ");
			short numSlots = stack.stackTagCompound.getCompoundTag("target").getShort("numslots");
			String locName = stack.stackTagCompound.getCompoundTag("target").getString("locname");
			list.add("owner: " + owner);

			String dimPre = "" + EnumChatFormatting.BLUE;
			String cPre = "" + EnumChatFormatting.BLUE;
			String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

			// Don't show the bound location to others, only the owner sees that
			if (stack.stackTagCompound.getByte("mode") != 1 && player.getDisplayName().equals(owner) == false) // FIXME
			{
				dimPre = "" + EnumChatFormatting.OBFUSCATED;
				cPre = "" + EnumChatFormatting.OBFUSCATED;
			}


			list.add(String.format("dim: %s%d%s x: %s%d%s, y: %s%d%s, z: %s%d%s", dimPre, dim, rst, cPre, x, rst, cPre, y, rst, cPre, z, rst));
			list.add("type: " + locName);
			list.add(String.format("slots: %s%d%s", dimPre, numSlots, rst));
		}
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
	}
}
