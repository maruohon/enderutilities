package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderUtilitiesInventory;

public class TileEntityEU extends TileEntity implements ISidedInventory
{
	protected String customInventoryName;
	protected ItemStack[] itemStacks;
	protected String name;

	public TileEntityEU(String name)
	{
		this.name = name;
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

	/* Returns the name of the inventory */
	@Override
	public String getInventoryName()
	{
		return this.hasCustomInventoryName() ? this.customInventoryName : "";
	}

	/* Returns if the inventory is named */
	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customInventoryName != null && this.customInventoryName.length() > 0;
	}

	public void setInventoryName(String name)
	{
		this.customInventoryName = name;
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
			return false; // : player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
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

	@SideOnly(Side.CLIENT)
	public String getGuiBackground()
	{
		return this.name.toLowerCase() + ".png";
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(x=" + xCoord + ", y=" + yCoord + ", z=" + zCoord + ")@" + System.identityHashCode(this);
	}
}
