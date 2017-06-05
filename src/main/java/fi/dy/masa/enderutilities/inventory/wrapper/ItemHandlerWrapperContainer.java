package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSelective;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSize;

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
public class ItemHandlerWrapperContainer implements IItemHandlerModifiable, IItemHandlerSelective, IItemHandlerSize
{
    protected final IItemHandlerModifiable baseHandlerModifiable;
    protected final IItemHandler wrapperHandler;
    private final boolean useWrapperForExtract;

    public ItemHandlerWrapperContainer(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler)
    {
        this(baseHandler, wrapperHandler, false);
    }

    public ItemHandlerWrapperContainer(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler, boolean useWrapperForExtract)
    {
        this.baseHandlerModifiable = baseHandler;
        this.wrapperHandler = wrapperHandler;
        this.useWrapperForExtract = useWrapperForExtract;
    }

    @Override
    public int getSlots()
    {
        return this.wrapperHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.wrapperHandler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.baseHandlerModifiable.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.wrapperHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (this.useWrapperForExtract)
        {
            return this.wrapperHandler.extractItem(slot, amount, simulate);
        }
        else
        {
            return this.baseHandlerModifiable.extractItem(slot, amount, simulate);
        }
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
        if (this.useWrapperForExtract && this.wrapperHandler instanceof IItemHandlerSelective)
        {
            return ((IItemHandlerSelective) this.wrapperHandler).canExtractFromSlot(slot);
        }

        return true;
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.wrapperHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.wrapperHandler).getInventoryStackLimit();
        }

        return 64;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        if (this.wrapperHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.wrapperHandler).getItemStackLimit(stack);
        }

        return this.getInventoryStackLimit();
    }
}
