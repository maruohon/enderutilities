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
        if (this.getStackInSlot(slot).isEmpty() == false || stack.isEmpty())
        {
            return stack;
        }

        if (stack.getCount() > 1)
        {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);

            newStack = super.insertItem(slot, newStack, simulate);

            if (newStack.isEmpty() == false)
            {
                return stack;
            }

            newStack = stack.copy();
            newStack.shrink(1);

            return newStack;
        }
        else
        {
            return super.insertItem(slot, stack, simulate);
        }
    }
}
