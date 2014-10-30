package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
	public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
	{
		super(te, inventory);
	}

	@Override
	protected void addSlots()
	{
		// Item slot
		this.addSlotToContainer(new SlotItemModular(this.te, 0, 8, 19));

		// Module slots
		int x = 80, y = 19;
		for (int i = 0; i < 10; x += 18)
		{
			// TODO add item types for slots (last parameter)
			// We should check how many cores ae allowed, add that number of those slots, same for capacitors and link crystals
			// Then add the rest of the slots as generic
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 1, x, y, -1));
			if (++i == 5)
			{
				y += 18;
				x -= 5 * 18;
			}
		}

		// Module storage inventory slots
		x = 8; y = 66;
		for (int i = 0; i < 9; x += 18, ++i)
		{
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 11, x, y, -1));
		}
	}

	@Override
	protected int getPlayerInventoryVerticalOffset()
	{
		return 94;
	}
/*
	private void writeModulesToItem(int toolSlotNum, int slotStart, int numModuleSlots)
	{
		if (toolSlotNum >= this.inventorySlots.size() || this.getSlot(toolSlotNum).getHasStack() == false)
		{
			return;
		}

		if (this.getSlot(toolSlotNum).getStack().getItem() instanceof IModular)
		{
			NBTTagList nbtTagList = new NBTTagList();

			// Write all the modules into a TAG_List
			int numSlots = this.inventorySlots.size();
			for (int slotNum = slotStart; slotNum < numSlots && slotNum < (slotStart + numModuleSlots); ++slotNum)
			{
				if (this.getSlot(slotNum).getHasStack() == true)
				{
					NBTTagCompound nbtTagCompound = new NBTTagCompound();
					nbtTagCompound.setByte("Slot", (byte)slotNum);
					this.getSlot(slotNum).getStack().writeToNBT(nbtTagCompound);
					nbtTagList.appendTag(nbtTagCompound);
				}
				this.getSlot(slotNum).putStack(null);
			}

			// Write the module list to the tool
			NBTTagCompound nbt = this.getSlot(toolSlotNum).getStack().getTagCompound();
			if (nbt == null) { nbt = new NBTTagCompound(); }

			System.out.println("writing; list tag count: " + nbtTagList.tagCount());
			nbt.setTag("Items", nbtTagList);
			this.getSlot(toolSlotNum).getStack().setTagCompound(nbt);
		}
	}

	private void readModulesFromItem(int toolSlotNum, int slotStart, int numModuleSlots)
	{
		if (toolSlotNum >= this.inventorySlots.size() || this.getSlot(toolSlotNum).getHasStack() == false)
		{
			return;
		}

		if (this.getSlot(toolSlotNum).getStack().getItem() instanceof IModular)
		{
			NBTTagCompound nbt = this.getSlot(toolSlotNum).getStack().getTagCompound();
			if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
			{
				return;
			}
			NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

			// First clear the module slots, just in case
			int numSlots = this.inventorySlots.size();
			for (int i = slotStart; i < numSlots && i < (slotStart + numModuleSlots); ++i)
			{
				this.getSlot(i).putStack(null);
			}

			// Read all the module ItemStacks from the tool, and write them to the workstation's module ItemStacks
			int listNumStacks = nbtTagList.tagCount();
			for (int i = 0; i < listNumStacks; ++i)
			{
				NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
				byte slotNum = nbtTagCompound.getByte("Slot");

				if (slotNum < numSlots && slotNum >= slotStart && slotNum < (slotStart + numModuleSlots))
				{
					this.getSlot(slotNum).putStack(ItemStack.loadItemStackFromNBT(nbtTagCompound));
				}
			}
		}
	}

	@Override
	public ItemStack slotClick(int slotNum, int p2, int p3, EntityPlayer player)
	{
		// Taking out the tool from the tool slot, we want to write the modules to the tool's NBT data, and then clear the module slots
		if (slotNum == 0 && player != null && player.inventory != null &&
			(player.inventory.getItemStack() == null || player.inventory.getItemStack().getItem() instanceof IModular))
		{
			this.writeModulesToItem(0, 1, 10);
		}

		System.out.println("slotClick(), remote?: " + this.te.getWorldObj().isRemote);
		return super.slotClick(slotNum, p2, p3, player);
	}
*/
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
	{
		return super.transferStackInSlot(player, slotNum);
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
	{
		return super.mergeItemStack(stack, slotStart, slotRange, reverse);
	}
}
