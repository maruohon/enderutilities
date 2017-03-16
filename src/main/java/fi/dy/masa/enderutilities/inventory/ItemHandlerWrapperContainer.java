package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Wraps the "base" IItemHandler (which has no slot-specific insert or extract restrictions),
 * and the "wrapper handler" which is the external-facing inventory with the slot-specific restrictions
 * in place. Uses the base handler for everything else except insertItem(), which is called via the wrapper
 * handler instead. The idea of this is to have the slot-specific checks in place when manually putting
 * items into Slots in a container, but still allow manually taking items from every slot,
 * whereas the wrapper (ie. the externally exposed inventory) might have restriction in place
 * on what can be extracted from what slots, for example furnace fuel and input item slots.
 * Also, another important factor is having the setStackInSlot() available for the Container to sync slots!
 * 
 * @author masa
 */
public class ItemHandlerWrapperContainer implements IItemHandlerModifiable, IItemHandlerSelective
{
    protected final IItemHandlerModifiable baseHandlerModifiable;
    protected final IItemHandler wrapperHandler;

    public ItemHandlerWrapperContainer(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler)
    {
        this.baseHandlerModifiable = baseHandler;
        this.wrapperHandler = wrapperHandler;
    }

    @Override
    public int getSlots()
    {
        return this.baseHandlerModifiable.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.baseHandlerModifiable.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.wrapperHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return this.baseHandlerModifiable.extractItem(slot, amount, simulate);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.baseHandlerModifiable.setStackInSlot(slot, stack);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        if (this.wrapperHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.wrapperHandler).isItemValidForSlot(slot, stack);
        }

        return true;
    }

    @Override
    public boolean canExtractFromSlot(int slot)
    {
        if (this.wrapperHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.wrapperHandler).canExtractFromSlot(slot);
        }

        return true;
    }
}
