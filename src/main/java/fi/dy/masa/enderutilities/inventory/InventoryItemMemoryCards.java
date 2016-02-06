package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;

public class InventoryItemMemoryCards extends InventoryItemModularModules
{
    public InventoryItemMemoryCards(InventoryItemModular invModular, ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        super(invModular, containerStack, invSize, isRemote, player);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItemMemoryCards#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        if (super.isItemValidForSlot(slotNum, stack) == false)
        {
            return false;
        }

        if (stack == null)
        {
            return true;
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
