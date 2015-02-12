package fi.dy.masa.enderutilities.inventory;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotInfuserMaterial extends Slot
{
    public SlotInfuserMaterial(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack != null && (stack.getItem() == Items.ender_pearl || stack.getItem() == Items.ender_eye);
    }
}
