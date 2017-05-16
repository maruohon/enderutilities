package fi.dy.masa.enderutilities.inventory;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemStackHandlerBasic implements IItemHandlerModifiable, INBTSerializable<NBTTagCompound>, IItemHandlerSelective, IItemHandlerSize
{
    protected final NonNullList<ItemStack> items;
    private final boolean allowCustomStackSizes;
    private final int inventorySize;
    private int stackLimit;
    private String tagName;

    public ItemStackHandlerBasic(int invSize)
    {
        this(invSize, 64, false, "Items");
    }

    public ItemStackHandlerBasic(int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName)
    {
        this.inventorySize = invSize;
        this.tagName = tagName;
        this.allowCustomStackSizes = allowCustomStackSizes;
        this.items = NonNullList.withSize(invSize, ItemStack.EMPTY);
        this.setStackLimit(stackLimit);
    }

    @Override
    public int getSlots()
    {
        return this.inventorySize;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return this.stackLimit;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        return this.items.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        this.items.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        this.onContentsChanged(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty() || this.isItemValidForSlot(slot, stack) == false)
        {
            return stack;
        }

        ItemStack existingStack = this.items.get(slot);
        int existingStackSize = existingStack.getCount();
        boolean hasStack = existingStack.isEmpty() == false;
        int max = this.getItemStackLimit(slot, stack);

        if (this.allowCustomStackSizes == false)
        {
            max = Math.min(max, stack.getMaxStackSize());
        }

        // Existing items in the target slot
        if (hasStack)
        {
            // If the slot is already full, or the to-be-inserted item is different
            if (existingStackSize >= max ||
                stack.getItem() != existingStack.getItem() ||
                stack.getMetadata() != existingStack.getMetadata() ||
                ItemStack.areItemStackTagsEqual(stack, existingStack) == false)
            {
                return stack;
            }
        }

        int amount = Math.min(max - existingStackSize, stack.getCount());

        if (amount <= 0)
        {
            return stack;
        }

        if (simulate == false)
        {
            if (hasStack)
            {
                existingStack.grow(amount);
            }
            else
            {
                ItemStack newStack = stack.copy();
                newStack.setCount(amount);
                this.items.set(slot, newStack);
            }

            this.onContentsChanged(slot);
        }

        if (amount < stack.getCount())
        {
            ItemStack stackRemaining = stack.copy();
            stackRemaining.shrink(amount);

            return stackRemaining;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.canExtractFromSlot(slot) == false)
        {
            return ItemStack.EMPTY;
        }

        ItemStack existingStack = this.items.get(slot);

        if (existingStack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        amount = Math.min(amount, Math.min(existingStack.getCount(), existingStack.getMaxStackSize()));

        ItemStack stack;

        if (simulate)
        {
            stack = existingStack.copy();
            stack.setCount(amount);

            return stack;
        }
        else
        {
            if (amount == existingStack.getCount())
            {
                stack = existingStack;
                this.items.set(slot, ItemStack.EMPTY);
            }
            else
            {
                stack = existingStack.splitStack(amount);

                if (existingStack.getCount() <= 0)
                {
                    this.items.set(slot, ItemStack.EMPTY);
                }
            }

            this.onContentsChanged(slot);
        }

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
        return this.stackLimit;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        if (this.allowCustomStackSizes)
        {
            return this.getInventoryStackLimit();
        }

        return Math.min(stack.getMaxStackSize(), this.getSlotLimit(slot));
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
    public void setItemStorageTagName(@Nonnull String tagName)
    {
        this.tagName = tagName;
    }

    public String getItemStorageTagName()
    {
        return this.tagName;
    }
}
