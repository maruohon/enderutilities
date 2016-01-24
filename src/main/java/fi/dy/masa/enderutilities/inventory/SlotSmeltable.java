package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class SlotSmeltable extends SlotGeneric
{
    public SlotSmeltable(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || (FurnaceRecipes.smelting().getSmeltingResult(stack) != null && super.isItemValid(stack));
    }
}
