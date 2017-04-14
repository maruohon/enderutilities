package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;

public class SlotItemHandlerBaubles extends SlotItemHandlerGeneric
{
    protected static BaublesItemValidatorBase validator = new BaublesItemValidatorBase();
    protected final ContainerEnderUtilities container;

    public SlotItemHandlerBaubles(ContainerEnderUtilities container, IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.container = container;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return 1;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        return validator.isItemValidForSlot(this, stack);
    }

    public ContainerEnderUtilities getContainer()
    {
        return this.container;
    }

    public static void setBaublesItemValidator(BaublesItemValidatorBase validatorIn)
    {
        validator = validatorIn;
    }

    public static class BaublesItemValidatorBase
    {
        public boolean isItemValidForSlot(SlotItemHandlerBaubles slot, ItemStack stack)
        {
            return false;
        }
    }
}
