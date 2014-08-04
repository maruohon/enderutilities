package fi.dy.masa.enderutilities.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemNBTHelper
{
	public int posX;
	public int posY;
	public int posZ;
	public int dimension;
	/* Face of the target block */
	public int blockFace;

	public long playerUUIDMost;
	public long playerUUIDLeast;
	public String playerName;

	public ItemNBTHelper()
	{
		this.posX = 0;
		this.posY = 0;
		this.posZ = 0;
		this.dimension = 0;
		this.blockFace = -1;
		this.playerName = "";
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

		NBTTagCompound tagTarget = nbt.getCompoundTag("Target");
		this.posX = tagTarget.getInteger("posX");
		this.posY = tagTarget.getInteger("posY");
		this.posZ = tagTarget.getInteger("posZ");
		this.dimension = tagTarget.getInteger("Dim");
		this.blockFace = tagTarget.getInteger("BlockFace");

		return tagTarget;
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

		NBTTagCompound tagTarget = new NBTTagCompound();
		tagTarget.setInteger("posX", x);
		tagTarget.setInteger("posY", y);
		tagTarget.setInteger("posZ", z);
		tagTarget.setInteger("Dim", dim);
		tagTarget.setInteger("BlockFace", face);

		nbt.setTag("Target", tagTarget);

		return nbt;
	}

	public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
	{
		if (nbt != null)
		{
			nbt.removeTag("Target");
		}

		return nbt;
	}

	public static boolean hasPlayerTag(NBTTagCompound nbt)
	{
		if (nbt == null || nbt.hasKey("Player", Constants.NBT.TAG_COMPOUND) == false)
		{
			return false;
		}

		NBTTagCompound tag = nbt.getCompoundTag("Player");
		if (tag != null &&
			tag.hasKey("PlayerUUIDMost", Constants.NBT.TAG_LONG) == true &&
			tag.hasKey("PlayerUUIDLeast", Constants.NBT.TAG_LONG) == true &&
			tag.hasKey("PlayerName", Constants.NBT.TAG_STRING) == true)
		{
			return true;
		}

		return false;
	}

	public NBTTagCompound readPlayerTagFromNBT(NBTTagCompound nbt)
	{
		if (hasPlayerTag(nbt) == false)
		{
			return null;
		}

		NBTTagCompound tagPlayer = nbt.getCompoundTag("Player");
		this.playerUUIDMost = tagPlayer.getLong("PlayerUUIDMost");
		this.playerUUIDLeast = tagPlayer.getLong("PlayerUUIDLeast");
		this.playerName = tagPlayer.getString("PlayerName");

		return tagPlayer;
	}

	public static NBTTagCompound writePlayerTagToNBT(NBTTagCompound nbt, EntityPlayer player)
	{
		if (nbt == null)
		{
			return nbt;
		}

		if (player == null)
		{
			nbt.removeTag("Player");
			return nbt;
		}

		NBTTagCompound tagPlayer = new NBTTagCompound();
		tagPlayer.setLong("PlayerUUIDMost", player.getUniqueID().getMostSignificantBits());
		tagPlayer.setLong("PlayerUUIDLeast", player.getUniqueID().getLeastSignificantBits());
		tagPlayer.setString("PlayerName", player.getCommandSenderName());

		nbt.setTag("Player", tagPlayer);

		return nbt;
	}

	public static ItemStack writeTagToItem(ItemStack stack, String name, NBTTagCompound tag)
	{
		if (stack == null || name == null)
		{
			return null;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			if (tag == null)
			{
				return stack;
			}

			nbt = new NBTTagCompound();
		}

		if (tag == null)
		{
			if (nbt.hasKey(name, 10) == true)
			{
				nbt.removeTag(name);
			}
		}
		else
		{
			nbt.setTag(name, tag);
		}

		stack.setTagCompound(nbt);

		return stack;
	}

	public static ItemStack writeNBTToItem(ItemStack stack, NBTTagCompound nbt)
	{
		if (stack != null)
		{
			stack.setTagCompound(nbt);
		}

		return stack;
	}
}
