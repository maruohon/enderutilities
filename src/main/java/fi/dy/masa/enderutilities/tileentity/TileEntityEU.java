package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderUtilitiesInventory;

public class TileEntityEU extends TileEntity implements ISidedInventory
{
	protected String customInventoryName;
	protected ItemStack[] itemStacks;
	protected String tileEntityName;
	public byte rotation;

	protected String ownerName;
	protected UUID ownerUUID;

	public TileEntityEU(String name)
	{
		this.tileEntityName = name;
	}

	public void setInventoryName(String name)
	{
		this.customInventoryName = name;
	}

	/* Returns if the inventory is named */
	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customInventoryName != null && this.customInventoryName.length() > 0;
	}

	/* Returns the name of the inventory */
	@Override
	public String getInventoryName()
	{
		return this.hasCustomInventoryName() ? this.customInventoryName : "";
	}

	public void setRotation(byte r)
	{
		this.rotation = r;
	}

	public byte getRotation()
	{
		return this.rotation;
	}

	public void setOwner(EntityPlayer player)
	{
		if (player != null)
		{
			this.ownerName = player.getCommandSenderName();
			this.ownerUUID = player.getUniqueID();
		}
		else
		{
			this.ownerName = null;
			this.ownerUUID = null;
		}
	}

	public String getOwnerName()
	{
		return this.ownerName;
	}

	public UUID getOwnerUUID()
	{
		return this.ownerUUID;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.rotation = nbt.getByte("Rotation");

		if (nbt.hasKey("OwnerName", Constants.NBT.TAG_STRING) == true)
		{
			this.ownerName = nbt.getString("OwnerName");
		}

		if (nbt.hasKey("OwnerUUIDMost", Constants.NBT.TAG_LONG) == true && nbt.hasKey("OwnerUUIDLeast", Constants.NBT.TAG_LONG) == true)
		{
			this.ownerUUID = new UUID(nbt.getLong("OwnerUUIDMost"), nbt.getLong("OwnerUUIDLeast"));
		}

		if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING) == true)
		{
			this.customInventoryName = nbt.getString("CustomName");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setByte("Rotation", this.rotation);

		if (this.ownerName != null)
		{
			nbt.setString("OwnerName", this.ownerName);
		}

		if (this.ownerUUID != null)
		{
			nbt.setLong("OwnerUUIDMost", this.ownerUUID.getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", this.ownerUUID.getLeastSignificantBits());
		}

		if (this.hasCustomInventoryName())
		{
			nbt.setString("CustomName", this.customInventoryName);
		}
	}

	@Override
	public int getSizeInventory()
	{
		if (this.itemStacks != null)
		{
			return this.itemStacks.length;
		}

		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slotNum)
	{
		return itemStacks[slotNum];
	}

	/* Removes from an inventory slot (slotNum) up to a specified number (maxAmount) of items and returns them in a new stack. */
	@Override
	public ItemStack decrStackSize(int slotNum, int maxAmount)
	{
		if (this.itemStacks[slotNum] != null)
		{
			ItemStack itemstack;

			if (this.itemStacks[slotNum].stackSize <= maxAmount)
			{
				itemstack = this.itemStacks[slotNum];
				this.itemStacks[slotNum] = null;

				return itemstack;
			}
			else
			{
				itemstack = this.itemStacks[slotNum].splitStack(maxAmount);

				if (this.itemStacks[slotNum].stackSize == 0)
				{
					this.itemStacks[slotNum] = null;
				}

				return itemstack;
			}
		}

		return null;
	}

	/* When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
	 * like when you close a workbench GUI.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slotNum)
	{
		if (this.itemStacks[slotNum] != null)
		{
			ItemStack itemstack = this.itemStacks[slotNum];
			this.itemStacks[slotNum] = null;
			return itemstack;
		}

		return null;
	}

	/* Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections). */
	@Override
	public void setInventorySlotContents(int slotNum, ItemStack itemStack)
	{
		this.itemStacks[slotNum] = itemStack;

		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit())
		{
			itemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	/* Returns the maximum stack size for a inventory slot. */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	/* Do not make give this method the name canInteractWith because it clashes with Container */
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this)
		{
			return false;
		}

		if (player.getDistanceSq((double)this.xCoord + 0.5d, (double)this.yCoord + 0.5d, (double)this.zCoord + 0.5d) >= 64.0d)
		{
			return false;
		}

		return true;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
	{
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return null;
	}

	@Override
	public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
	{
		return true;
	}

	@Override
	public boolean canExtractItem(int slotNum, ItemStack itemStack, int side)
	{
		return true;
	}

	public ContainerEnderUtilitiesInventory getContainer(InventoryPlayer inventory)
	{
		return null;
	}

	@SideOnly(Side.CLIENT)
	public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return null;
	}

	public String getTEName()
	{
		return this.tileEntityName;
	}

	public void performGuiAction(int element, short action)
	{
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(x=" + xCoord + ", y=" + yCoord + ", z=" + zCoord + ")@" + System.identityHashCode(this);
	}
}
