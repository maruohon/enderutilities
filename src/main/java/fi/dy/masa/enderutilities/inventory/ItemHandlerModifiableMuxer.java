package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ItemHandlerModifiableMuxer extends ItemHandlerMuxer implements IItemHandlerModifiable
{
    protected final IItemHandlerModifiableProvider providerModifiable;

    public ItemHandlerModifiableMuxer(IItemHandlerModifiableProvider provider)
    {
        super(provider);

        this.providerModifiable = provider;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        this.providerModifiable.getInventoryModifiable().setStackInSlot(slot, stack);
    }
}
