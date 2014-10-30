package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesSided
{
	private static final int[] SLOTS = new int[0];

	public TileEntityToolWorkstation()
	{
		super(ReferenceBlocksItems.NAME_TILEENTITY_TOOL_WORKSTATION);
		this.itemStacks = new ItemStack[20];
	}

	private void writeModulesToItem(int toolSlotNum, int slotStart, int numModuleSlots)
	{
		if (toolSlotNum >= this.itemStacks.length)
		{
			return;
		}

		if (this.itemStacks[toolSlotNum] != null && this.itemStacks[toolSlotNum].getItem() instanceof IModular)
		{
			NBTTagList nbtTagList = new NBTTagList();

			// Write all the modules into a TAG_List
			int invSlots = this.getSizeInventory();
			for (int slotNum = slotStart; slotNum < invSlots && slotNum < (slotStart + numModuleSlots); ++slotNum)
			{
				if (this.itemStacks[slotNum] != null)
				{
					NBTTagCompound nbtTagCompound = new NBTTagCompound();
					nbtTagCompound.setByte("Slot", (byte)slotNum);
					this.itemStacks[slotNum].writeToNBT(nbtTagCompound);
					nbtTagList.appendTag(nbtTagCompound);
				}
				this.itemStacks[slotNum] = null;
			}

			// Write the module list to the tool
			NBTTagCompound nbt = this.itemStacks[toolSlotNum].getTagCompound();
			if (nbt == null) { nbt = new NBTTagCompound(); }

			System.out.println("writing; list tag count: " + nbtTagList.tagCount() + " remote?: " + this.worldObj.isRemote);
			nbt.setTag("Items", nbtTagList);
			this.itemStacks[toolSlotNum].setTagCompound(nbt);
		}
	}

	private void readModulesFromItem(int toolSlotNum, int slotStart, int numModuleSlots)
	{
		if (toolSlotNum >= this.itemStacks.length)
		{
			return;
		}

		if (this.itemStacks[toolSlotNum] != null && this.itemStacks[toolSlotNum].getItem() instanceof IModular)
		{
			NBTTagCompound nbt = this.itemStacks[toolSlotNum].getTagCompound();
			if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
			{
				return;
			}
			NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

			// First clear the module slots, just in case
			for (int i = slotStart; i < this.getSizeInventory() && i < (slotStart + numModuleSlots); ++i)
			{
				this.itemStacks[i] = null;
			}

			// Read all the module ItemStacks from the tool, and write them to the workstation's module ItemStacks
			int listNumStacks = nbtTagList.tagCount();
			System.out.println("reading modules, count: " + listNumStacks + " remote?: " + this.worldObj.isRemote);
			for (int i = 0; i < listNumStacks; ++i)
			{
				NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
				byte slotNum = nbtTagCompound.getByte("Slot");

				if (slotNum < this.itemStacks.length && slotNum >= slotStart && slotNum < (slotStart + numModuleSlots))
				{
					this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
				}
			}
		}
	}

	@Override
	public void setInventorySlotContents(int slotNum, ItemStack itemStack)
	{
		//if (this.worldObj.isRemote == false)
			//System.out.println("set start; remote?: " + this.worldObj.isRemote);

		// Changing the item in the tool slot, write the current modules to the tool first
		if (slotNum == 0 && this.itemStacks[0] != null)
		{
			//if (this.worldObj.isRemote == false)
				//System.out.println("TO item; remote?:" + this.worldObj.isRemote);
			this.writeModulesToItem(0, 1, 10);
		}

		super.setInventorySlotContents(slotNum, itemStack);

		// Changing the item in the tool slot, read the modules from the new tool
		if (slotNum == 0 && this.itemStacks[0] != null)
		{
			//if (this.worldObj.isRemote == false)
				//System.out.println("FROM item; remote?:" + this.worldObj.isRemote);
			this.readModulesFromItem(0, 1, 10);
		}
	}

	@Override
	public ItemStack getStackInSlot(int slotNum)
	{
		//System.out.println("getStackInSlot(); remote?: " + this.worldObj.isRemote);
		return super.getStackInSlot(slotNum);
	}

	@Override
	public ItemStack decrStackSize(int slotNum, int maxAmount)
	{
		//System.out.println("decrStackSize(); remote?: " + this.worldObj.isRemote);
		if (this.itemStacks[slotNum] != null)
		{
			ItemStack itemstack;

			if (this.itemStacks[slotNum].stackSize <= maxAmount)
			{
				itemstack = this.itemStacks[slotNum];
				this.setInventorySlotContents(slotNum, null);

				return itemstack;
			}
			else
			{
				ItemStack newStack = this.itemStacks[slotNum].copy();
				itemstack = newStack.splitStack(maxAmount);
				this.setInventorySlotContents(slotNum, newStack);

				if (this.itemStacks[slotNum].stackSize == 0)
				{
					this.setInventorySlotContents(slotNum, null);
				}

				return itemstack;
			}
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotNum)
	{
		//System.out.println("getStackInSlotOnClosing(); remote?: " + this.worldObj.isRemote);
		if (this.itemStacks[slotNum] != null)
		{
			ItemStack itemstack = this.itemStacks[slotNum];
			this.setInventorySlotContents(slotNum, null);
			return itemstack;
		}

		return null;
	}
/*
	@Override
	public void setInventorySlotContents(int slotNum, ItemStack itemStack)
	{
		//if (this.worldObj.isRemote == false)
			System.out.println("setInventorySlotContents(" + slotNum + ", " + (itemStack != null ? itemStack.toString() : null) + "); remote?: " + this.worldObj.isRemote);

		super.setInventorySlotContents(slotNum, itemStack);
	}
*/
	@Override
	public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
	{
		System.out.println("isItemValidForSlot(); remote?: " + this.worldObj.isRemote);
		return super.isItemValidForSlot(slotNum, itemStack);
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int slotNum, ItemStack itemStack, int side)
	{
		return false;
	}

	@Override
	public ContainerToolWorkstation getContainer(InventoryPlayer inventory)
	{
		return new ContainerToolWorkstation(this, inventory);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
	}
}
