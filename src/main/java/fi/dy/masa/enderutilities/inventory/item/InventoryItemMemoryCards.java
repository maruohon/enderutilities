package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;

public class InventoryItemMemoryCards extends InventoryItemModularModules
{
    public InventoryItemMemoryCards(InventoryItemModular invModular, ItemStack containerStack, int invSize, boolean isRemote)
    {
        super(invModular, containerStack, invSize, isRemote);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItemMemoryCards#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        if (super.isItemValidForSlot(slotNum, stack) == false || stack.isEmpty())
        {
            return false;
        }

        if (stack.getItem() instanceof IModule && ((IModule)stack.getItem()).getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD_ITEMS))
        {
            IModule module = (IModule)stack.getItem();
            return module.getModuleTier(stack) >= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B &&
                   module.getModuleTier(stack) <= ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_12B;
        }

        return false;
    }
}
