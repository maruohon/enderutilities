package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.base.ISlotOffset;

public class SlotItemHandlerOffset extends SlotItemHandlerGeneric
{
    protected final ISlotOffset callback;

    public SlotItemHandlerOffset(IItemHandler itemHandler, int index, int xPosition, int yPosition, ISlotOffset scrollable)
    {
        super(itemHandler, index, xPosition, yPosition);

        this.callback = scrollable;
    }

    @Override
    public int getSlotIndex()
    {
        int index = super.getSlotIndex() + this.callback.getSlotOffset();
        return MathHelper.clamp(index, 0, this.getItemHandler().getSlots() - 1);
    }
}
