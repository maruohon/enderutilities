package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryStackArray implements IInventory
{
    protected ItemStack[] itemStacks;
    protected int invSize;
    protected int invStackLimit;
    boolean ignoreMaxStackSize;
    protected IInventory inventoryCallback;

    public InventoryStackArray(ItemStack[] stacks)
    {
        this(stacks, 64, 27, false);
    }

    public InventoryStackArray(ItemStack[] stacks, int invStackLimit, int invSize, boolean ignoreMaxStackSize)
    {
        this.itemStacks = stacks;
        this.invStackLimit = invStackLimit;
        this.invSize = invSize;
        this.ignoreMaxStackSize = ignoreMaxStackSize;
    }

    public void setInventoryCallback(IInventory inv)
    {
        this.inventoryCallback = inv;
    }

    public void setStackArray(ItemStack[] stacks)
    {
        this.itemStacks = stacks;
    }

    public void setInvSize(int invSize)
    {
        this.invSize = invSize;
    }

    public void setInvStackLimit(int limit)
    {
        this.invStackLimit = limit;
    }

    public void setIgnoreMaxStackSize(boolean ignore)
    {
        this.ignoreMaxStackSize = ignore;
    }

    public boolean getIgnoreMaxStackSize()
    {
        return this.ignoreMaxStackSize;
    }

    @Override
    public int getSizeInventory()
    {
        return this.invSize;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

        return this.itemStacks[slotNum];
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

        if (this.itemStacks[slotNum] != null)
        {
            ItemStack stack;

            if (this.itemStacks[slotNum].stackSize >= maxAmount)
            {
                stack = this.itemStacks[slotNum].splitStack(maxAmount);

                if (this.itemStacks[slotNum].stackSize <= 0)
                {
                    this.itemStacks[slotNum] = null;
                }
            }
            else
            {
                stack = this.itemStacks[slotNum];
                this.itemStacks[slotNum] = null;
            }

            this.markDirty();

            return stack;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return null;
        }

        ItemStack stack = this.itemStacks[slotNum];
        this.itemStacks[slotNum] = null;

        this.markDirty();

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack stack)
    {
        if (slotNum >= this.itemStacks.length)
        {
            return;
        }

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.itemStacks[slotNum] = stack;

        this.markDirty();
    }

    @Override
    public String getInventoryName()
    {
        return "";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.invStackLimit;
    }

    @Override
    public void markDirty()
    {
        if (this.inventoryCallback != null)
        {
            this.inventoryCallback.markDirty();
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
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
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        return true;
    }
}
