package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class ItemEnderLasso extends ItemEUTeleport
{
	public ItemEnderLasso()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_LASSO);
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
				nbt = NBTHelperTarget.writeTargetTagToNBT(nbt, x, y, z, player.dimension, side, true);
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
		NBTHelperTarget target = new NBTHelperTarget();
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
