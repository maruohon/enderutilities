package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.IScrollableInventory;

public class SlotItemHandlerScrollable extends SlotItemHandlerGeneric
{
    protected final IScrollableInventory scrollable;

    public SlotItemHandlerScrollable(IItemHandler itemHandler, int index, int xPosition, int yPosition, IScrollableInventory scrollable)
    {
        super(itemHandler, index, xPosition, yPosition);

        this.scrollable = scrollable;
    }

    @Override
    public int getSlotIndex()
    {
        int index = super.getSlotIndex() + this.scrollable.getSlotOffset();
        return MathHelper.clamp(index, 0, this.getItemHandler().getSlots() - 1);
    }
}
