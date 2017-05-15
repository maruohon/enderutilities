package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class SlotModuleModularItem extends SlotItemHandlerModule
{
    protected IContainerItem container;

    public SlotModuleModularItem(IItemHandler inventory, int slot, int posX, int posY, ModuleType moduleType, IContainerItem container)
    {
        super(inventory, slot, posX, posY, moduleType);
        this.container = container;
    }

    /**
     * Checks if the given ItemStack is valid for this slot.
     * For it to be valid, the input stack's item needs to be an IModule, the ModuleType of it
     * has to match the ModuleType of this slot, or this slot must accept any type of module.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        if ((stack.getItem() instanceof IModule) == false)
        {
            return false;
        }

        ModuleType type = ((IModule) stack.getItem()).getModuleType(stack);
        ItemStack modularStack = this.container.getContainerItem();

        if (type.equals(ModuleType.TYPE_INVALID) == false && modularStack.isEmpty() == false && super.isItemValid(stack))
        {
            // Matching basic module type, check for the sub-type based on the host modular item
            if ((this.moduleType.equals(ModuleType.TYPE_ANY) || this.moduleType.equals(type)) && modularStack.getItem() instanceof IModular)
            {
                return ((IModular) modularStack.getItem()).getMaxModules(modularStack, stack) > 0;
            }
        }

        return false;
    }
}
