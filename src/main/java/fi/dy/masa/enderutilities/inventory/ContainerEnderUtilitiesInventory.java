/* Parts of the code taken from MineFactoryReloaded, credits powercrystals, skyboy and others */
package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class ContainerEnderUtilitiesInventory extends Container
{
	protected TileEntityEnderUtilitiesInventory te;
	protected InventoryPlayer inventoryPlayer;

	public ContainerEnderUtilitiesInventory(TileEntityEnderUtilitiesInventory te, InventoryPlayer inventory)
	{
		this.te = te;
		this.inventoryPlayer = inventory;

		if (te.getSizeInventory() > 0)
		{
			this.addSlots();
		}

		this.bindPlayerInventory(inventory);
	}

	protected void addSlots()
	{
	}

	protected int getPlayerInventoryVerticalOffset()
	{
		return 84;
	}

	protected int getPlayerInventoryHorizontalOffset()
	{
		return 8;
	}

	protected void bindPlayerInventory(InventoryPlayer inventory)
	{
		int yOff = getPlayerInventoryVerticalOffset();
		int xOff = getPlayerInventoryHorizontalOffset();

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventory, j + i * 9 + 9, xOff + j * 18, yOff + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventory, i, xOff + i * 18, yOff + 58));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return te.isInvalid() == false && te.isUseableByPlayer(player);
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
