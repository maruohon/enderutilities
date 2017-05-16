package fi.dy.masa.enderutilities.util;

import net.minecraft.item.ItemStack;

/**
 * Wrapper class for ItemStack, which implements equals()
 * for the item, damage and NBT, but not stackSize.
 */
public class ItemType
{
    private final ItemStack stack;
    private final boolean checkNBT;

    public ItemType(ItemStack stack)
    {
        this(stack, true);
    }

    public ItemType(ItemStack stack, boolean checkNBT)
    {
        this.stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.checkNBT = checkNBT;
    }

    public ItemStack getStack()
    {
        return this.stack;
    }

    public boolean checkNBT()
    {
        return this.checkNBT;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.stack.getMetadata();
        result = prime * result + this.stack.getItem().hashCode();

        if (this.checkNBT())
        {
            result = prime * result + (this.stack.getTagCompound() != null ? this.stack.getTagCompound().hashCode() : 0);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ItemType other = (ItemType) obj;

        if (this.stack.isEmpty() || other.stack.isEmpty())
        {
            if (this.stack.isEmpty() != other.stack.isEmpty())
            {
                return false;
            }
        }
        else
        {
            if (this.stack.getMetadata() != other.stack.getMetadata())
            {
                return false;
            }

            if (this.stack.getItem() != other.stack.getItem())
            {
                return false;
            }

            return this.checkNBT() == false || ItemStack.areItemStackTagsEqual(this.stack, other.stack);
        }

        return true;
    }

    @Override
    public String toString()
    {
        if (this.checkNBT())
        {
            return this.stack.getItem().getRegistryName() + "@" + this.stack.getMetadata() + this.stack.getTagCompound();
        }
        else
        {
            return this.stack.getItem().getRegistryName() + "@" + this.stack.getMetadata();
        }
    }
}
