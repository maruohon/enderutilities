package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class NBTHelperTarget
{
	public int posX;
	public int posY;
	public int posZ;
	public int dimension;
	/* Face of the target block */
	public int blockFace;

	public NBTHelperTarget()
	{
		this.posX = 0;
		this.posY = 0;
		this.posZ = 0;
		this.dimension = 0;
		this.blockFace = -1;
	}

	public static boolean hasTargetTag(NBTTagCompound nbt)
	{
		if (nbt == null || nbt.hasKey("Target", Constants.NBT.TAG_COMPOUND) == false)
		{
			return false;
		}

		NBTTagCompound tag = nbt.getCompoundTag("Target");
		if (tag != null &&
			tag.hasKey("posX", Constants.NBT.TAG_ANY_NUMERIC) == true &&
			tag.hasKey("posY", Constants.NBT.TAG_ANY_NUMERIC) == true &&
			tag.hasKey("posZ", Constants.NBT.TAG_ANY_NUMERIC) == true &&
			tag.hasKey("Dim", Constants.NBT.TAG_INT) == true)
		{
			return true;
		}

		return false;
	}

	public NBTTagCompound readTargetTagFromNBT(NBTTagCompound nbt)
	{
		if (hasTargetTag(nbt) == false)
		{
			return null;
		}

		NBTTagCompound tag = nbt.getCompoundTag("Target");
		this.posX = tag.getInteger("posX");
		this.posY = tag.getInteger("posY");
		this.posZ = tag.getInteger("posZ");
		this.dimension = tag.getInteger("Dim");
		this.blockFace = tag.getInteger("BlockFace");

		return tag;
	}

	public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, int x, int y, int z, int dim, int face, boolean calculateOffset)
	{
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		if (calculateOffset == true && face >= 0)
		{
			ForgeDirection dir = ForgeDirection.getOrientation(face);
			x += dir.offsetX;
			y += dir.offsetY;
			z += dir.offsetZ;
		}

		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("posX", x);
		tag.setInteger("posY", y);
		tag.setInteger("posZ", z);
		tag.setInteger("Dim", dim);
		tag.setInteger("BlockFace", face);

		nbt.setTag("Target", tag);

		return nbt;
	}

	public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
	{
		NBTHelper.writeTagToNBT(nbt, "Target", null);

		return nbt;
	}
}
