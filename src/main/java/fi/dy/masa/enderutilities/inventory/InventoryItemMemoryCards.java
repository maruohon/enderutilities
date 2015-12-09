package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;

public class InventoryItemMemoryCards extends InventoryItemModules
{
    public InventoryItemMemoryCards(InventoryItemModular modularInventory, ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        super(modularInventory, containerStack, invSize, isRemote, player);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (this.inventoryItemModular.getContainerItemStack() == null)
        {
            return false;
        }

        if (stack == null)
        {
            return true;
        }

        if (stack.getItem() instanceof IModule && ((IModule)stack.getItem()).getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD))
        {
            IModule module = (IModule)stack.getItem();
            return module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B && module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
        }

        return false;
    }
}
