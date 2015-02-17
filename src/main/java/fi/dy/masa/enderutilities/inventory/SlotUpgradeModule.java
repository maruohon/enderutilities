package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class SlotUpgradeModule extends Slot
{
    protected ModuleType moduleType;

    public SlotUpgradeModule(IInventory inventory, int slot, int posX, int posY, ModuleType moduleType)
    {
        super(inventory, slot, posX, posY);
        this.moduleType = moduleType;
    }

    public ModuleType getModuleType()
    {
        return this.moduleType;
    }

    public void setModuleType(ModuleType type)
    {
        this.moduleType = type;
    }

    /**
     * Checks if the given ItemStack is valid for this slot.
     * For it to be valid, there needs to be an IModular item in the tool slot,
     * and the input stack's item needs to be an IModule, the ModuleType of it
     * has to match the ModuleType of this slot, or this slot must accept any type
     * of module, and the tool must support this type of module.
     */
    @Override
    public boolean isItemValid(ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return false;
        }

        ItemStack toolStack = this.inventory.getStackInSlot(0);
        if (toolStack == null || (toolStack.getItem() instanceof IModular) == false)
        {
            return false;
        }

        IModular imodular = (IModular) toolStack.getItem();
        // If the slot number is larger than the max amount of modules allowed for the tool
        if (this.slotNumber > imodular.getMaxModules(toolStack))
        {
            return false;
        }

        ModuleType type = ((IModule) moduleStack.getItem()).getModuleType(moduleStack);
        if (type.equals(ModuleType.TYPE_INVALID) == false &&
            (this.moduleType.equals(type) == true || this.moduleType.equals(ModuleType.TYPE_ANY) == true))
        {
            // FIXME This doesn't actually check how many modules of this subtype there are already installed...
            if (imodular.getMaxModules(toolStack, moduleStack) > 0)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }
}
