package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class SlotUpgradeModuleStorage extends SlotUpgradeModule
{
    public SlotUpgradeModuleStorage(IInventory inventory, int slot, int posX, int posY)
    {
        super(inventory, slot, posX, posY, UtilItemModular.ModuleType.TYPE_ANY);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack != null && UtilItemModular.getModuleType(stack).equals(UtilItemModular.ModuleType.TYPE_INVALID) == false)
        {
            return true;
        }

        return false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }
}
