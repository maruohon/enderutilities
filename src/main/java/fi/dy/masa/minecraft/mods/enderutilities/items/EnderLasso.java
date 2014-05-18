package fi.dy.masa.minecraft.mods.enderutilities.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;
import fi.dy.masa.minecraft.mods.enderutilities.util.TeleportEntity;

public class EnderLasso extends Item
{
	public EnderLasso()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_LASSO);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return false;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		if (player.isSneaking() == true)
		{
			// Sneaking and targeting a block: store the location
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				String strSide = "top";

				// Adjust the target block position
				if (side == 0) { --y; strSide = "bottom"; }
				if (side == 1) { ++y; }
				if (side == 2) { --z; strSide = "east"; }
				if (side == 3) { ++z; strSide = "west"; }
				if (side == 4) { --x; strSide = "north"; }
				if (side == 5) { ++x; strSide = "south"; }

				nbt.setInteger("dim", player.dimension);
				nbt.setInteger("x", x);
				nbt.setInteger("y", y);
				nbt.setInteger("z", z);
				nbt.setString("side", strSide);
				stack.setTagCompound(nbt);
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			list.add("No target set");
			return;
		}

		String side	= nbt.getString("side");
		int dim		= nbt.getInteger("dim");
		int x		= nbt.getInteger("x");
		int y		= nbt.getInteger("y");
		int z		= nbt.getInteger("z");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

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
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}
}
