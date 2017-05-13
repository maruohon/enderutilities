package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemStackHandlerBasic implements IItemHandlerModifiable, INBTSerializable<NBTTagCompound>, IItemHandlerSelective, IItemHandlerSize
{
    protected final ItemStack[] items;
    private final boolean allowCustomStackSizes;
    private int stackLimit;
    private String tagName;

    public ItemStackHandlerBasic(int invSize)
    {
        this(invSize, 64, false, "Items");
    }

    public ItemStackHandlerBasic(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName)
    {
        this.tagName = tagName;
        this.allowCustomStackSizes = allowCustomStackSizes;
        this.items = new ItemStack[invSize];
        this.setStackLimit(stackLimit);
    }

    @Override
    public int getSlots()
    {
        return this.items.length;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return this.stackLimit;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.items[slot];
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.items[slot] = stack;
        this.onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (stack == null || this.isItemValidForSlot(slot, stack) == false)
        {
            return stack;
        }

        int existingStackSize = this.items[slot] != null ? this.items[slot].stackSize : 0;
        int max = this.getItemStackLimit(slot, stack);

        if (this.allowCustomStackSizes == false)
        {
            max = Math.min(max, stack.getMaxStackSize());
        }

        // Existing items in the target slot
        if (this.items[slot] != null)
        {
            // The slot is already full, bail out now
            if (this.items[slot].stackSize >= max)
            {
                return stack;
            }

            // Check that the item-to-be-inserted is identical to the existing items
            if (stack.getItem() != this.items[slot].getItem() ||
                stack.getMetadata() != this.items[slot].getMetadata() ||
                ItemStack.areItemStackTagsEqual(stack, this.items[slot]) == false)
            {
                return stack;
            }
        }

        int amount = Math.min(max - existingStackSize, stack.stackSize);

        if (amount <= 0)
        {
            return stack;
        }

        if (simulate == false)
        {
            if (this.items[slot] != null)
            {
                this.items[slot].stackSize += amount;
            }
            else
            {
                this.items[slot] = stack.copy();
                this.items[slot].stackSize = amount;
            }

            this.onContentsChanged(slot);
        }

        if (amount < stack.stackSize)
        {
            ItemStack stackRemaining = stack.copy();
            stackRemaining.stackSize -= amount;

            return stackRemaining;
        }

        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (slot < 0 || slot >= this.items.length || this.items[slot] == null || this.canExtractFromSlot(slot) == false)
        {
            return null;
        }

        amount = Math.min(amount, this.items[slot].stackSize);
        amount = Math.min(amount, this.items[slot].getMaxStackSize());

        ItemStack stack;

        if (simulate)
        {
            stack = this.items[slot].copy();
            stack.stackSize = amount;

            return stack;
        }
        else
        {
            if (amount == this.items[slot].stackSize)
            {
                stack = this.items[slot];
                this.items[slot] = null;
            }
            else
            {
                stack = this.items[slot].splitStack(amount);

                if (this.items[slot].stackSize <= 0)
                {
                    this.items[slot] = null;
                }
            }
        }

        this.onContentsChanged(slot);

        return stack;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return NBTUtils.writeItemsToTag(new NBTTagCompound(), this.items, this.tagName, true);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTUtils.readStoredItemsFromTag(nbt, this.items, this.tagName);
    }

    @Override
    public int getInventoryStackLimit()
    {
        //System.out.println("ItemStackHandlerBasic.getInventoryStackLimit()");
        return this.stackLimit;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        //System.out.println("ItemStackHandlerBasic.getItemStackLimit(stack)");
        if (this.allowCustomStackSizes || (stack != null && this.getInventoryStackLimit() < stack.getMaxStackSize()))
        {
            return this.getInventoryStackLimit();
        }

        return stack.getMaxStackSize();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        return true;
    }

    public void setStackLimit(int stackLimit)
    {
        this.stackLimit = stackLimit;
    }

    public void onContentsChanged(int slot)
    {
    }

    /**
     * Sets the NBTTagList tag name that stores the items of this inventory in the container ItemStack
     * @param tagName
     */
    public void setItemStorageTagName(String tagName)
    {
        if (tagName != null)
        {
            this.tagName = tagName;
        }
    }

    public String getItemStorageTagName()
    {
        return this.tagName;
    }
}
