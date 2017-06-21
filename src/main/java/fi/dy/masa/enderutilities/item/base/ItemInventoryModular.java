package fi.dy.masa.enderutilities.item.base;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public abstract class ItemInventoryModular extends ItemModular
{
    public ItemInventoryModular(String name)
    {
        super(name);
    }

    public int getSizeModuleInventory(ItemStack containerStack)
    {
        return this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD_ITEMS);
    }

    public abstract int getSizeInventory(ItemStack containerStack);
}
