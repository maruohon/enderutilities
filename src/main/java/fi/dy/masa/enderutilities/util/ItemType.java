package fi.dy.masa.enderutilities.util;

import net.minecraft.item.ItemStack;

/**
 * Wrapper class for ItemStack, which implements equals()
 * for the item, damage and NBT, but not stackSize.
 */
public class ItemType
{
    private final ItemStack stack;

    public ItemType(ItemStack stack)
    {
        this.stack = stack.copy();
    }

    public ItemStack getStack()
    {
        return this.stack;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        //result = prime * result + ((stack == null) ? 0 : stack.hashCode());
        result = prime * result + this.stack.getItemDamage();
        result = prime * result + this.stack.getItem().hashCode();
        result = prime * result + (this.stack.getTagCompound() != null ? this.stack.getTagCompound().hashCode() : 0);
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

        if (this.stack == null || other.stack == null)
        {
            if (this.stack != other.stack)
                return false;
        }
        else
        {
            if (this.stack.getItemDamage() != other.stack.getItemDamage())
            {
                return false;
            }

            if (this.stack.getItem() != other.stack.getItem())
            {
                return false;
            }

            return ItemStack.areItemStackTagsEqual(this.stack, other.stack);
        }

        return true;
    }
}
