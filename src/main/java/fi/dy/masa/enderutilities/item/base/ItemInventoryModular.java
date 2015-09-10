package fi.dy.masa.enderutilities.item.base;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ItemInventoryModular extends ItemModular
{
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
    }

    public int getSizeInventory(ItemStack containerStack)
    {
        return 0;
    }

    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return 64;
    }
}
