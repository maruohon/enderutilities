package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSyncable;

public class InvWrapperSyncable extends InvWrapper implements IItemHandlerSyncable
{
    public InvWrapperSyncable(IInventory inv)
    {
        super(inv);
    }

    @Override
    public void syncStackInSlot(int slot, ItemStack stack)
    {
        if (this.getInv() instanceof IItemHandlerSyncable)
        {
            ((IItemHandlerSyncable) this.getInv()).syncStackInSlot(slot, stack);
        }
        else
        {
            this.setStackInSlot(slot, stack);
        }
    }
}
