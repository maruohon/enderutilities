package fi.dy.masa.enderutilities.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemNBTHelperTarget
{
	public int posX;
	public int posY;
	public int posZ;
	public int dimension;
	/* Face of block targeted */
	public int blockFace;

	public ItemNBTHelperTarget()
	{
		this.posX = 0;
		this.posY = 0;
		this.posZ = 0;
		this.dimension = 0;
		this.blockFace = -1;
	}

	public static NBTTagCompound getTargetTag(NBTTagCompound nbt)
	{
		if (nbt == null || nbt.hasKey("Target", 10) == false)
		{
			//System.out.println("getTargetTag(): nbt == null || no key Target"); // FIXME debug
			return null;
		}

		NBTTagCompound tagTarget = nbt.getCompoundTag("Target");

		if (tagTarget.hasKey("posX", 99) == false ||
			tagTarget.hasKey("posY", 99) == false ||
			tagTarget.hasKey("posZ", 99) == false ||
			tagTarget.hasKey("Dim", 99) == false)
		{
			//System.out.println("getTargetTag(): missing coord or dim"); // FIXME debug
			return null;
		}

		return tagTarget;
	}

	public boolean readFromNBT(NBTTagCompound nbt)
	{
		NBTTagCompound tagTarget = getTargetTag(nbt);

		if (tagTarget == null)
		{
			return false;
		}

		this.posX = tagTarget.getInteger("posX");
		this.posY = tagTarget.getInteger("posY");
		this.posZ = tagTarget.getInteger("posZ");
		this.dimension = tagTarget.getInteger("Dim");
		this.blockFace = tagTarget.getInteger("BlockFace");

		return true;
	}

	public static NBTTagCompound writeToNBT(NBTTagCompound nbt, int x, int y, int z, int dim, int side, boolean offset)
	{
		NBTTagCompound nbtTarget = new NBTTagCompound();

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		if (offset == true && side >= 0)
		{
			ForgeDirection dir = ForgeDirection.getOrientation(side);
			x += dir.offsetX;
			y += dir.offsetY;
			z += dir.offsetZ;
		}

		nbtTarget.setInteger("posX", x);
		nbtTarget.setInteger("posY", y);
		nbtTarget.setInteger("posZ", z);
		nbtTarget.setInteger("Dim", dim);
		nbtTarget.setInteger("BlockFace", side);

		nbt.setTag("Target", nbtTarget);

		return nbt;
	}

	public static ItemStack writeTargetToItem(ItemStack stack, NBTTagCompound nbtTarget)
	{
		if (stack == null)
		{
			return null;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			if (nbtTarget == null)
			{
				return stack;
			}

			nbt = new NBTTagCompound();
		}

		if (nbtTarget == null)
		{
			if (nbt.hasKey("Target", 10) == true)
			{
				nbt.removeTag("Target");
			}
		}
		else
		{
			nbt.setTag("Target", nbtTarget);
		}

		stack.setTagCompound(nbt);

		return stack;
	}

	public static ItemStack writeTargetToItem(ItemStack stack, int x, int y, int z, int dim, int side, boolean offset)
	{
		if (stack == null)
		{
			return null;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		nbt = writeToNBT(nbt, x, y, z, dim, side, offset);
		stack.setTagCompound(nbt);

		return stack;
	}
}
