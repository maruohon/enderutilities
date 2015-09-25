package fi.dy.masa.enderutilities.inventory;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public abstract class ContainerLargeStacks extends ContainerEnderUtilities
{
    protected boolean isDragging;
    protected boolean draggingRightClick;
    protected final Set<Integer> draggedSlots = new HashSet<Integer>();
    protected int selectedSlot = -1;

    public ContainerLargeStacks(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        super(inventoryPlayer, inventory);
    }

    public int getSelectedSlot()
    {
        return this.selectedSlot;
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Player inventory
        if (slot.inventory != this.inventory)
        {
            return Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
        }

        // Our inventory
        return slot.getSlotStackLimit();
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        //String side = this.inventoryPlayer.player.worldObj.isRemote ? "client" : "server";
        //EnderUtilities.logger.info(String.format("slotClick(): side: %s slotNum: %d, button: %d type: %d", side, slotNum, button, type));

        // slotNum: real button: 0 type: 0 - regular left click - on button down with empty cursor, on button up with stack in cursor
        // slotNum: real button: 1 type: 0 - regular right click - on button down with empty cursor, on button up with stack in cursor

        // slotNum: real button: 0 type: 1 - shift + left click - on button down with empty cursor, on button up with stack in cursor
        // slotNum: real button: 1 type: 1 - shift + right click - on button down with empty cursor, on button up with stack in cursor

        // slotNum: -999 button: 0 type: 0 - left click outside of inventory screen with stack on cursor - on button up
        // slotNum: -999 button: 1 type: 0 - right click outside of inventory screen with stack on cursor - on button up

        // slotNum: real button: 2 type: 3 - middle click on any slot - on button down with empty cursor, on button up with stack in cursor

        // slotNum: -999 button: 1 type: 4 - (shift +) right click outside the inventory with an empty cursor - on button down

        // slotNum: real button: 0 type: 4 - pressing Q over a slot which has items - on button down
        // shift + Q does real,0,0, -999,0,0, real,0,0 ie. simulates picking up the stack, clicking outside of inventory, and clicking again on the slot (why?)

        // slotNum: real button: 0..8 type: 2 - hotbar number key over slot - on button down, only with empty cursor

        // slotNum: real button: 0 type: 6 - left double-click with an empty cursor on a slot with items

        // slotNum: -999 button: 0 type: 5 - left click drag with stack in cursor start - after drag ends, as the first call
        // slotNum: real button: 1 type: 5 - left click drag with stack in cursor - for each slot dragged over - after drag ends, in sequence
        // slotNUm: -999 button: 2 type: 5 - left click drag with stack in cursor end - after drag ends, as the last call

        // slotNum: -999 button: 4 type: 5 - right click drag with stack in cursor start - after drag ends, as the first call
        // slotNum: real button: 5 type: 5 - right click drag with stack in cursor - for each slot dragged over - after drag ends, in sequence
        // slotNUm: -999 button: 6 type: 5 - right click drag with stack in cursor end - after drag ends, as the last call

        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;
        ItemStack stackCursor = this.inventoryPlayer.getItemStack();
        ItemStack stackSlot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum).getStack() : null;

        if (this.isDragging == true)
        {
            // End of dragging
            if (type == 5 && (button == 2 || button == 6))
            {
                if (stackCursor != null)
                {
                    int totalNum = 0;
                    int numSlots = this.draggedSlots.size();
                    int itemsPerSlot = this.draggingRightClick == true ? 1 : stackCursor.stackSize / numSlots;
                    for (int i : this.draggedSlots)
                    {
                        Slot slotTmp = this.getSlot(i);
                        int slotMax = slotTmp.getSlotStackLimit();
                        ItemStack stackTmp = slotTmp.getStack();
                        int num = Math.min(itemsPerSlot, slotMax);

                        // The target slot is not in our large inventory, also check the max stack size of the item
                        if (slotTmp.inventory != this.inventory)
                        {
                            num = Math.min(num, stackCursor.getMaxStackSize());
                        }

                        // Target slot already has items, check how many more can fit to the slot
                        if (stackTmp != null)
                        {
                            num = Math.min(num, slotMax - stackTmp.stackSize);
                            stackTmp.stackSize += num;
                        }
                        else
                        {
                            stackTmp = stackCursor.copy();
                            stackTmp.stackSize = num;
                        }

                        totalNum += num;
                        this.putStackInSlot(i, stackTmp);
                    }

                    stackCursor.stackSize -= totalNum;
                    this.inventoryPlayer.setItemStack(stackCursor.stackSize > 0 ? stackCursor : null);
                }

                this.isDragging = false;
            }
            // This gets called for each slot that was dragged over
            else if (type == 5 && (button == 1 || button == 5))
            {
                this.draggedSlots.add(slotNum);
            }
        }
        // Starting a left or right click drag
        else if (type == 5 && (button == 0 || button == 4))
        {
            this.isDragging = true;
            this.draggingRightClick = (button == 4);
            this.draggedSlots.clear();
        }
        // Regular left click
        else if (button == 0 && type == 0)
        {
            // Left click outside of inventory screen - drop the stack from the cursor
            if (slotNum == -999)
            {
                if (stackCursor != null)
                {
                    int max = stackCursor.getMaxStackSize();
                    while (stackCursor.stackSize > max)
                    {
                        ItemStack stackDrop = stackCursor.copy();
                        stackDrop.stackSize = max;
                        player.dropPlayerItemWithRandomChoice(stackDrop, true);
                    }

                    player.dropPlayerItemWithRandomChoice(stackCursor, true);
                    this.inventoryPlayer.setItemStack(null);
                }
            }
            // Left click on a slot
            else
            {
                // Trying to put items to the slot
                if (stackCursor != null)
                {
                    // Matching items in cursor and the clicked on slot
                    if (InventoryUtils.areItemStacksEqual(stackCursor, stackSlot) == true)
                    {
                        int num = Math.min(slot.getSlotStackLimit() - stackSlot.stackSize, stackCursor.stackSize);
                        if (num > 0)
                        {
                            stackSlot.stackSize += num;
                            slot.putStack(stackSlot);
                            stackCursor.stackSize -= num;
                            this.inventoryPlayer.setItemStack(stackCursor.stackSize > 0 ? stackCursor : null);
                        }
                    }
                    // Different items in cursor and the clicked on slot
                    else if (slot.isItemValid(stackCursor) == true)
                    {
                        // TODO Do we want to allow picking up stacks of any size to the cursor here?
                        // The stack size of the stack in the slot is small enough to fit into one regular stack, or is null
                        // (to be put to the cursor), and the stack in the cursor fits to the stack size limit of the slot
                        if ((stackSlot == null || stackSlot.stackSize <= stackSlot.getMaxStackSize()) &&
                             stackCursor.stackSize <= slot.getSlotStackLimit())
                        {
                            slot.putStack(stackCursor);
                            this.inventoryPlayer.setItemStack(stackSlot);
                        }
                    }
                }
                // Trying to take items from the slot
                else if (stackSlot != null && slot.canTakeStack(this.inventoryPlayer.player) == true)
                {
                    int num = Math.min(stackSlot.stackSize, stackSlot.getMaxStackSize());
                    // Can't take all the items from the slot
                    if (num < stackSlot.stackSize)
                    {
                        stackCursor = stackSlot.copy();
                        stackCursor.stackSize = num;
                        this.inventoryPlayer.setItemStack(stackCursor);
                        stackSlot.stackSize -= num;
                        slot.putStack(stackSlot);
                    }
                    // Taking all the items from the slot
                    else
                    {
                        this.inventoryPlayer.setItemStack(stackSlot);
                        slot.putStack(null);
                    }
                }
            }
        }
        // Regular right click
        else if (button == 1 && type == 0)
        {
            // Right click outside of inventory screen - drop one item from the cursor
            if (slotNum == -999)
            {
                if (stackCursor != null)
                {
                    ItemStack stackDrop = stackCursor.copy();
                    stackDrop.stackSize = 1;
                    stackCursor.stackSize -= 1;
                    player.dropPlayerItemWithRandomChoice(stackDrop, true);
                    this.inventoryPlayer.setItemStack(stackCursor.stackSize > 0 ? stackCursor : null);
                }
            }
            // Right click on a slot
            else
            {
                // Items in the cursor, trying to put one item to the slot
                if (stackCursor != null)
                {
                    // Empty target slot, put one item into it
                    if (stackSlot == null)
                    {
                        if (slot.isItemValid(stackCursor) == true)
                        {
                            stackSlot = stackCursor.copy();
                            stackSlot.stackSize = 1;
                            stackCursor.stackSize -= 1;
                            slot.putStack(stackSlot);
                            this.inventoryPlayer.setItemStack(stackCursor);
                        }
                    }
                    // Matching items in cursor and the clicked on slot
                    else if (InventoryUtils.areItemStacksEqual(stackCursor, stackSlot) == true)
                    {
                        int num = Math.min(slot.getSlotStackLimit() - stackSlot.stackSize, 1);
                        if (num > 0)
                        {
                            stackSlot.stackSize += num;
                            stackCursor.stackSize -= num;
                            slot.putStack(stackSlot);
                            this.inventoryPlayer.setItemStack(stackCursor.stackSize > 0 ? stackCursor : null);
                        }
                    }
                    // Different items in cursor and the clicked on slot, try to swap the stacks
                    else if (slot.isItemValid(stackCursor) == true)
                    {
                        // TODO Do we want to allow picking up stacks of any size to the cursor here?
                        // The stack size of the stack in the slot is small enough to fit into one regular stack, or is null
                        // (to be put to the cursor), and the stack in the cursor fits to the stack size limit of the slot
                        if ((stackSlot == null || stackSlot.stackSize <= stackSlot.getMaxStackSize()) &&
                             stackCursor.stackSize <= slot.getSlotStackLimit())
                        {
                            slot.putStack(stackCursor);
                            this.inventoryPlayer.setItemStack(stackSlot);
                        }
                    }
                }
                // Empty cursor, trying to take items from the slot (split the stack)
                else if (stackSlot != null && slot.canTakeStack(this.inventoryPlayer.player) == true)
                {
                    int num = Math.min((int)Math.ceil((double)stackSlot.stackSize / 2.0d), (int)Math.ceil((double)stackSlot.getMaxStackSize() / 2.0d));
                    // Can't take all the items from the slot
                    if (num < stackSlot.stackSize)
                    {
                        stackCursor = stackSlot.copy();
                        stackCursor.stackSize = num;
                        stackSlot.stackSize -= num;
                        this.inventoryPlayer.setItemStack(stackCursor);
                        slot.putStack(stackSlot);
                    }
                    // Taking all the items from the slot
                    else
                    {
                        this.inventoryPlayer.setItemStack(stackSlot);
                        slot.putStack(null);
                    }
                }
            }
        }
        // Shift left click or shift right click, they both do the same
        else if (type == 1 && (button == 0 || button == 1))
        {
            // Only transfer a maximum of one regular stack
            if (stackSlot != null && stackSlot.stackSize > stackSlot.getMaxStackSize())
            {
                int max = stackSlot.getMaxStackSize();
                int sizeOrig = stackSlot.stackSize;
                ItemStack stackTmp = stackSlot.copy();
                stackSlot.stackSize = max;

                this.transferStackInSlot(player, slotNum);

                ItemStack stackSlotNew = this.getSlot(slotNum).getStack();
                if (stackSlotNew != null)
                {
                    stackSlotNew.stackSize += sizeOrig - max;
                    this.putStackInSlot(slotNum, stackSlotNew);
                }
                else
                {
                    stackTmp.stackSize -= max;
                    this.putStackInSlot(slotNum, stackTmp);
                }
            }
            else
            {
                this.transferStackInSlot(player, slotNum);
            }
        }
        // Pressing Q while hovering over a stack
        else if (button == 0 && type == 4)
        {
            if (stackSlot != null && slot.canTakeStack(this.inventoryPlayer.player) == true)
            {
                ItemStack stackDrop = stackSlot.copy();
                stackDrop.stackSize = 1;
                stackSlot.stackSize -= 1;
                slot.putStack(stackSlot.stackSize > 0 ? stackSlot : null);
                player.dropPlayerItemWithRandomChoice(stackDrop, true);
            }
        }
        // Pressing a hotbar hotkey over a slot
        else if (type == 2 && button >= 0 && button <= 8)
        {
            ItemStack stackHotbar = this.inventoryPlayer.getStackInSlot(button);
            // The stack in the slot is null or the stack size is small enough to fit into one regular stack,
            // and the stack in the hotbar is null or the stack is small enough to fit to the slot
            if ((stackSlot == null || stackSlot.stackSize <= stackSlot.getMaxStackSize()) &&
                 (stackHotbar == null || stackHotbar.stackSize <= slot.getSlotStackLimit()) &&
                 slot.canTakeStack(this.inventoryPlayer.player) == true &&
                 slot.isItemValid(stackHotbar) == true)
            {
                slot.putStack(stackHotbar);
                this.inventoryPlayer.setInventorySlotContents(button, stackSlot);
            }
            // Hotbar slot is empty, but the whole stack doesn't fit into it
            else if (stackHotbar == null && stackSlot != null)
            {
                int num = Math.min(stackSlot.getMaxStackSize(), this.inventoryPlayer.getInventoryStackLimit());
                num = Math.min(num, stackSlot.stackSize);
                stackHotbar = stackSlot.copy();
                stackHotbar.stackSize = num;
                stackSlot.stackSize -= num;
                slot.putStack(stackSlot.stackSize > 0 ? stackSlot : null);
                this.inventoryPlayer.setInventorySlotContents(button, stackHotbar);
            }
            // Matching items in both slots, fill the hotbar stack if it has still space, otherwise take the stack from it
            else if (stackHotbar != null && stackSlot != null && InventoryUtils.areItemStacksEqual(stackHotbar, stackSlot) == true)
            {
                int num = Math.min(stackHotbar.getMaxStackSize() - stackHotbar.stackSize, stackSlot.stackSize);
                if (num > 0)
                {
                    stackHotbar.stackSize += num;
                    stackSlot.stackSize -= num;
                    slot.putStack(stackSlot.stackSize > 0 ? stackSlot : null);
                    this.inventoryPlayer.setInventorySlotContents(button, stackHotbar);
                }
                else
                {
                    num = Math.min(slot.getSlotStackLimit() - stackSlot.stackSize, stackHotbar.stackSize);
                    if (num > 0)
                    {
                        stackHotbar.stackSize -= num;
                        stackSlot.stackSize += num;
                        slot.putStack(stackSlot);
                        this.inventoryPlayer.setInventorySlotContents(button, stackHotbar.stackSize > 0 ? stackHotbar : null);
                    }
                }
            }
        }
        // Left double-click with an empty cursor over a slot with items
        else if (button == 0 && type == 6)
        {
            if (stackCursor != null)
            {
                ItemStack stackTmp = InventoryUtils.collectItemsFromInventory(slot.inventory, stackCursor, stackCursor.getMaxStackSize() - stackCursor.stackSize);
                if (stackTmp != null)
                {
                    stackCursor.stackSize += stackTmp.stackSize;
                    this.inventoryPlayer.setItemStack(stackCursor);
                }
            }
        }
        // Middle click on a slot - select the slot for swapping, or swap the contents with the selected slot
        else if (button == 2 && type == 3)
        {
            // Only allow swapping in the "this" inventory (that supports the large stacks)
            if (slotNum >= 0 && slotNum < this.inventorySlots.size() && this.getSlot(slotNum).isSlotInInventory(this.inventory, slotNum) == true)
            {
                if (this.selectedSlot != -1)
                {
                    // Don't swap with self
                    if (this.selectedSlot != slotNum)
                    {
                        ItemStack stackTmp = this.getSlot(slotNum).getStack();
                        this.getSlot(slotNum).putStack(this.getSlot(this.selectedSlot).getStack());
                        this.getSlot(this.selectedSlot).putStack(stackTmp);
                    }
                    this.selectedSlot = -1;
                }
                else
                {
                    this.selectedSlot = slotNum;
                }
            }

            // Middle click on a slot - if the cursor is empty, take the full stack out of the slot
            // FIXME: Stacks with stackSize > 100 reset to stackSize = 1 when picked up
            /*if (stackCursor == null)
            {
                if (this.inventoryPlayer.player.capabilities.isCreativeMode == true)
                {
                    stackCursor = stackSlot.copy();
                    stackCursor.stackSize = stackCursor.getMaxStackSize();
                    this.inventoryPlayer.setItemStack(stackCursor);
                }
                else if (stackSlot != null)
                {
                    this.inventoryPlayer.setItemStack(stackSlot);
                    slot.putStack(null);
                }
            }*/
        }

        return null;
    }
}
