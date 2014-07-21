package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.ItemNBTHelperTarget;
import fi.dy.masa.enderutilities.util.TooltipHelper;

public class ItemEnderBag extends ItemEU
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
		if (world.isRemote == true)
		{
			return stack;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			// The bag must be in public mode, or the player must be the owner
			if (nbt.getByte("Mode") == 1 || nbt.getString("Owner").equals(player.getCommandSenderName()) == true)
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
		if (te != null && te instanceof IInventory)
		{
			NBTTagCompound nbt = stack.getTagCompound();

			// The bag must be unbound, or in public mode, or the player must be the owner
			if (nbt == null || nbt.getByte("Mode") == 1 || nbt.getString("Owner").equals(player.getCommandSenderName()) == true)
			{
				if (nbt == null)
				{
					nbt = new NBTTagCompound();
				}

				nbt = ItemNBTHelperTarget.writeToNBT(nbt, x, y, z, player.dimension, side, false);

				Block block = world.getBlock(x, y, z);
				if (block != null)
				{
					nbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
				}

				nbt.setShort("Slots", (short)((IInventory) te).getSizeInventory());
				nbt.setByte("Mode", (byte)0); // 0 = private, 1 = public, 2 = friends (N/A)
				nbt.setString("Owner", player.getCommandSenderName());
				stack.setTagCompound(nbt);
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

		String locName	= StatCollector.translateToLocal(nbt.getString("BlockName"));
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
