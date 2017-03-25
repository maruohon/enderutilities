package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;

public class PlayerArmorInvWrapperLimited extends PlayerArmorInvWrapper
{
    public PlayerArmorInvWrapperLimited(InventoryPlayer inv)
    {
        super(inv);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (this.getStackInSlot(slot) != null || stack == null)
        {
            return stack;
        }

        if (stack.stackSize > 1)
        {
            ItemStack newStack = stack.copy();
            newStack.stackSize = 1;

            newStack = super.insertItem(slot, newStack, simulate);

            if (newStack != null)
            {
                return stack;
            }

            newStack = stack.copy();
            newStack.stackSize--;

            return newStack;
        }
        else
        {
            return super.insertItem(slot, stack, simulate);
        }
    }
}
