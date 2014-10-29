package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class ContainerToolWorkstation extends ContainerEnderUtilitiesInventory
{
	public ContainerToolWorkstation(TileEntityToolWorkstation te, InventoryPlayer inventory)
	{
		super(te, inventory);
	}

	protected void addSlots()
	{
		// Item slot
		this.addSlotToContainer(new SlotItemModular(this.te, 0, 30, 24));

		// Module slots
		for (int i = 0, x = 80, y = 20; i < 15; x += 18)
		{
			this.addSlotToContainer(new SlotUpgradeItem(this.te, i + 1, x, y));
			++i;
			if (i == 5)
			{
				y += 18;
				x -= 5 * 18;
			}
			else if (i == 10)
			{
				y += 23;
				x -= 5 * 18;
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
	{
		ItemStack stack = null;
		Slot slot = (Slot) inventorySlots.get(slotNum);
		int invSize = this.te.getSizeInventory();

		// Slot clicked on has items
		if(slot != null && slot.getHasStack() == true)
		{
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();

			// Clicked on a slot is in the "external" inventory
			if(slotNum < invSize)
			{
				// Try to merge the stack into the player inventory
				if(mergeItemStack(stackInSlot, invSize, inventorySlots.size(), true) == false)
				{
					return null;
				}
			}
			// Clicked on slot is in the player inventory, try to merge the stack to the external inventory
			else if(mergeItemStack(stackInSlot, 0, invSize, false) == false)
			{
				return null;
			}

			// All items moved, empty the slot
			if(stackInSlot.stackSize == 0)
			{
				slot.putStack(null);
			}
			// Update the slot
			else
			{
				slot.onSlotChanged();
			}

			// No items were moved
			if(stackInSlot.stackSize == stack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, stackInSlot);
		}

		return stack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
	{
		boolean successful = false;
		int slotIndex = slotStart;
		int maxStack = Math.min(stack.getMaxStackSize(), this.te.getInventoryStackLimit());

		if(reverse)
		{
			slotIndex = slotRange - 1;
		}

		Slot slot;
		ItemStack existingStack;

		if(stack.isStackable() == true)
		{
			while(stack.stackSize > 0 && (reverse == false && slotIndex < slotRange || reverse == true && slotIndex >= slotStart))
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();

				if(slot.isItemValid(stack) == true && existingStack != null && existingStack.getItem().equals(stack.getItem())
					&& (stack.getHasSubtypes() == false || stack.getItemDamage() == existingStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, existingStack))
				{
					int existingSize = existingStack.stackSize + stack.stackSize;

					if(existingSize <= maxStack)
					{
						stack.stackSize = 0;
						existingStack.stackSize = existingSize;
						slot.onSlotChanged();
						successful = true;
					}
					else if (existingStack.stackSize < maxStack)
					{
						stack.stackSize -= maxStack - existingStack.stackSize;
						existingStack.stackSize = maxStack;
						slot.onSlotChanged();
						successful = true;
					}
				}

				if(reverse == true)
				{
					--slotIndex;
				}
				else
				{
					++slotIndex;
				}
			}
		}

		if(stack.stackSize > 0)
		{
			if(reverse == true)
			{
				slotIndex = slotRange - 1;
			}
			else
			{
				slotIndex = slotStart;
			}

			while(reverse == false && slotIndex < slotRange || reverse == true && slotIndex >= slotStart)
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();

				if(slot.isItemValid(stack) == true && existingStack == null)
				{
					slot.putStack(stack.copy());
					slot.onSlotChanged();
					stack.stackSize = 0;
					successful = true;
					break;
				}

				if(reverse == true)
				{
					--slotIndex;
				}
				else
				{
					++slotIndex;
				}
			}
		}

		return successful;
	}
}
