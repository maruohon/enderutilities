package fi.dy.masa.enderutilities.inventory;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public abstract class ContainerLargeStacks extends ContainerEnderUtilities
{
    protected boolean isDragging;
    protected boolean draggingRightClick;
    protected final Set<Integer> draggedSlots = new HashSet<Integer>();

    public ContainerLargeStacks(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        super(inventoryPlayer, inventory);
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
        String side = this.inventoryPlayer.player.worldObj.isRemote ? "client" : "server";
        EnderUtilities.logger.info(String.format("slotClick(): side: %s slotNum: %d, button: %d type: %d", side, slotNum, button, type));
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
            int max = stackSlot.getMaxStackSize();
            // Only transfer a maximum of one regular stack
            if (stackSlot != null && stackSlot.stackSize > max)
            {
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
        // Middle click on a slot - if the cursor is empty, take the full stack out of the slot
        else if (button == 2 && type == 3)
        {
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

        /*
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = player.inventory;
        int i1;
        ItemStack itemstack3;

        if (type == 5)
        {
            int l = this.field_94536_g;
            this.field_94536_g = func_94532_c(button);

            if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g)
            {
                this.func_94533_d();
            }
            else if (inventoryplayer.getItemStack() == null)
            {
                this.func_94533_d();
            }
            else if (this.field_94536_g == 0)
            {
                this.field_94535_f = func_94529_b(button);

                if (func_94528_d(this.field_94535_f))
                {
                    this.field_94536_g = 1;
                    this.field_94537_h.clear();
                }
                else
                {
                    this.func_94533_d();
                }
            }
            else if (this.field_94536_g == 1)
            {
                Slot slot = (Slot)this.inventorySlots.get(slotNum);

                if (slot != null && func_94527_a(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.field_94537_h.size() && this.canDragIntoSlot(slot))
                {
                    this.field_94537_h.add(slot);
                }
            }
            else if (this.field_94536_g == 2)
            {
                if (!this.field_94537_h.isEmpty())
                {
                    itemstack3 = inventoryplayer.getItemStack().copy();
                    i1 = inventoryplayer.getItemStack().stackSize;
                    Iterator iterator = this.field_94537_h.iterator();

                    while (iterator.hasNext())
                    {
                        Slot slot1 = (Slot)iterator.next();

                        if (slot1 != null && func_94527_a(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.field_94537_h.size() && this.canDragIntoSlot(slot1))
                        {
                            ItemStack itemstack1 = itemstack3.copy();
                            int j1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                            func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);

                            if (itemstack1.stackSize > itemstack1.getMaxStackSize())
                            {
                                itemstack1.stackSize = itemstack1.getMaxStackSize();
                            }

                            if (itemstack1.stackSize > slot1.getSlotStackLimit())
                            {
                                itemstack1.stackSize = slot1.getSlotStackLimit();
                            }

                            i1 -= itemstack1.stackSize - j1;
                            slot1.putStack(itemstack1);
                        }
                    }

                    itemstack3.stackSize = i1;

                    if (itemstack3.stackSize <= 0)
                    {
                        itemstack3 = null;
                    }

                    inventoryplayer.setItemStack(itemstack3);
                }

                this.func_94533_d();
            }
            else
            {
                this.func_94533_d();
            }
        }
        else if (this.field_94536_g != 0)
        {
            this.func_94533_d();
        }
        else
        {
            Slot slot2;
            int l1;
            ItemStack itemstack5;

            if ((type == 0 || type == 1) && (button == 0 || button == 1))
            {
                if (slotNum == -999)
                {
                    if (inventoryplayer.getItemStack() != null && slotNum == -999)
                    {
                        if (button == 0)
                        {
                            player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), true);
                            inventoryplayer.setItemStack((ItemStack)null);
                        }

                        if (button == 1)
                        {
                            player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), true);

                            if (inventoryplayer.getItemStack().stackSize == 0)
                            {
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                }
                else if (type == 1)
                {
                    if (slotNum < 0)
                    {
                        return null;
                    }

                    slot2 = (Slot)this.inventorySlots.get(slotNum);

                    if (slot2 != null && slot2.canTakeStack(player))
                    {
                        itemstack3 = this.transferStackInSlot(player, slotNum);

                        if (itemstack3 != null)
                        {
                            Item item = itemstack3.getItem();
                            itemstack = itemstack3.copy();

                            if (slot2.getStack() != null && slot2.getStack().getItem() == item)
                            {
                                this.retrySlotClick(slotNum, button, true, player);
                            }
                        }
                    }
                }
                else
                {
                    if (slotNum < 0)
                    {
                        return null;
                    }

                    slot2 = (Slot)this.inventorySlots.get(slotNum);

                    if (slot2 != null)
                    {
                        itemstack3 = slot2.getStack();
                        ItemStack itemstack4 = inventoryplayer.getItemStack();

                        if (itemstack3 != null)
                        {
                            itemstack = itemstack3.copy();
                        }

                        if (itemstack3 == null)
                        {
                            if (itemstack4 != null && slot2.isItemValid(itemstack4))
                            {
                                l1 = button == 0 ? itemstack4.stackSize : 1;

                                if (l1 > slot2.getSlotStackLimit())
                                {
                                    l1 = slot2.getSlotStackLimit();
                                }

                                if (itemstack4.stackSize >= l1)
                                {
                                    slot2.putStack(itemstack4.splitStack(l1));
                                }

                                if (itemstack4.stackSize == 0)
                                {
                                    inventoryplayer.setItemStack((ItemStack)null);
                                }
                            }
                        }
                        else if (slot2.canTakeStack(player))
                        {
                            if (itemstack4 == null)
                            {
                                l1 = button == 0 ? itemstack3.stackSize : (itemstack3.stackSize + 1) / 2;
                                itemstack5 = slot2.decrStackSize(l1);
                                inventoryplayer.setItemStack(itemstack5);

                                if (itemstack3.stackSize == 0)
                                {
                                    slot2.putStack((ItemStack)null);
                                }

                                slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
                            }
                            else if (slot2.isItemValid(itemstack4))
                            {
                                if (itemstack3.getItem() == itemstack4.getItem() && itemstack3.getItemDamage() == itemstack4.getItemDamage() && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                                {
                                    l1 = button == 0 ? itemstack4.stackSize : 1;

                                    if (l1 > slot2.getSlotStackLimit() - itemstack3.stackSize)
                                    {
                                        l1 = slot2.getSlotStackLimit() - itemstack3.stackSize;
                                    }

                                    if (l1 > itemstack4.getMaxStackSize() - itemstack3.stackSize)
                                    {
                                        l1 = itemstack4.getMaxStackSize() - itemstack3.stackSize;
                                    }

                                    itemstack4.splitStack(l1);

                                    if (itemstack4.stackSize == 0)
                                    {
                                        inventoryplayer.setItemStack((ItemStack)null);
                                    }

                                    itemstack3.stackSize += l1;
                                }
                                else if (itemstack4.stackSize <= slot2.getSlotStackLimit())
                                {
                                    slot2.putStack(itemstack4);
                                    inventoryplayer.setItemStack(itemstack3);
                                }
                            }
                            else if (itemstack3.getItem() == itemstack4.getItem() && itemstack4.getMaxStackSize() > 1 && (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == itemstack4.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, itemstack4))
                            {
                                l1 = itemstack3.stackSize;

                                if (l1 > 0 && l1 + itemstack4.stackSize <= itemstack4.getMaxStackSize())
                                {
                                    itemstack4.stackSize += l1;
                                    itemstack3 = slot2.decrStackSize(l1);

                                    if (itemstack3.stackSize == 0)
                                    {
                                        slot2.putStack((ItemStack)null);
                                    }

                                    slot2.onPickupFromSlot(player, inventoryplayer.getItemStack());
                                }
                            }
                        }

                        slot2.onSlotChanged();
                    }
                }
            }
            else if (type == 2 && button >= 0 && button < 9)
            {
                slot2 = (Slot)this.inventorySlots.get(slotNum);

                if (slot2.canTakeStack(player))
                {
                    itemstack3 = inventoryplayer.getStackInSlot(button);
                    boolean flag = itemstack3 == null || slot2.inventory == inventoryplayer && slot2.isItemValid(itemstack3);
                    l1 = -1;

                    if (!flag)
                    {
                        l1 = inventoryplayer.getFirstEmptyStack();
                        flag |= l1 > -1;
                    }

                    if (slot2.getHasStack() && flag)
                    {
                        itemstack5 = slot2.getStack();
                        inventoryplayer.setInventorySlotContents(button, itemstack5.copy());

                        if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(itemstack3)) && itemstack3 != null)
                        {
                            if (l1 > -1)
                            {
                                inventoryplayer.addItemStackToInventory(itemstack3);
                                slot2.decrStackSize(itemstack5.stackSize);
                                slot2.putStack((ItemStack)null);
                                slot2.onPickupFromSlot(player, itemstack5);
                            }
                        }
                        else
                        {
                            slot2.decrStackSize(itemstack5.stackSize);
                            slot2.putStack(itemstack3);
                            slot2.onPickupFromSlot(player, itemstack5);
                        }
                    }
                    else if (!slot2.getHasStack() && itemstack3 != null && slot2.isItemValid(itemstack3))
                    {
                        inventoryplayer.setInventorySlotContents(button, (ItemStack)null);
                        slot2.putStack(itemstack3);
                    }
                }
            }
            else if (type == 3 && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && slotNum >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(slotNum);

                if (slot2 != null && slot2.getHasStack())
                {
                    itemstack3 = slot2.getStack().copy();
                    itemstack3.stackSize = itemstack3.getMaxStackSize();
                    inventoryplayer.setItemStack(itemstack3);
                }
            }
            else if (type == 4 && inventoryplayer.getItemStack() == null && slotNum >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(slotNum);

                if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player))
                {
                    itemstack3 = slot2.decrStackSize(button == 0 ? 1 : slot2.getStack().stackSize);
                    slot2.onPickupFromSlot(player, itemstack3);
                    player.dropPlayerItemWithRandomChoice(itemstack3, true);
                }
            }
            else if (type == 6 && slotNum >= 0)
            {
                slot2 = (Slot)this.inventorySlots.get(slotNum);
                itemstack3 = inventoryplayer.getItemStack();

                if (itemstack3 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player)))
                {
                    i1 = button == 0 ? 0 : this.inventorySlots.size() - 1;
                    l1 = button == 0 ? 1 : -1;

                    for (int i2 = 0; i2 < 2; ++i2)
                    {
                        for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.stackSize < itemstack3.getMaxStackSize(); j2 += l1)
                        {
                            Slot slot3 = (Slot)this.inventorySlots.get(j2);

                            if (slot3.getHasStack() && func_94527_a(slot3, itemstack3, true) && slot3.canTakeStack(player) && this.func_94530_a(itemstack3, slot3) && (i2 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize()))
                            {
                                int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.stackSize, slot3.getStack().stackSize);
                                ItemStack itemstack2 = slot3.decrStackSize(k1);
                                itemstack3.stackSize += k1;

                                if (itemstack2.stackSize <= 0)
                                {
                                    slot3.putStack((ItemStack)null);
                                }

                                slot3.onPickupFromSlot(player, itemstack2);
                            }
                        }
                    }
                }

                this.detectAndSendChanges();
            }
        }

        return itemstack;*/
    }
}
