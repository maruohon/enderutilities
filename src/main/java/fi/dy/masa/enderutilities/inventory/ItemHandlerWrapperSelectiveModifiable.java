package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerWrapperSelectiveModifiable extends ItemHandlerWrapperSelective implements IItemHandlerModifiable
{
    private final IItemHandlerModifiable baseHandlerModifiable;

    public ItemHandlerWrapperSelectiveModifiable(IItemHandlerModifiable baseHandler)
    {
        super(baseHandler);
        this.baseHandlerModifiable = baseHandler;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.baseHandlerModifiable.setStackInSlot(slot, stack);
    }
}
