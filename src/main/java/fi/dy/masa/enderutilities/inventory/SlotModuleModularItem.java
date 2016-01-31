package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class SlotModuleModularItem extends SlotGeneric
{
    protected IContainerModularItem container;
    protected ModuleType moduleType = ModuleType.TYPE_ANY;

    public SlotModuleModularItem(IInventory inventory, int slot, int posX, int posY, ModuleType moduleType, IContainerModularItem container)
    {
        super(inventory, slot, posX, posY);
        this.moduleType = moduleType;
        this.container = container;
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
     * For it to be valid, the input stack's item needs to be an IModule, the ModuleType of it
     * has to match the ModuleType of this slot, or this slot must accept any type of module.
     */
    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack == null)
        {
            return true;
        }

        if ((stack.getItem() instanceof IModule) == false)
        {
            return false;
        }

        ModuleType type = ((IModule)stack.getItem()).getModuleType(stack);
        ItemStack modularStack = this.container.getModularItem();

        if (type.equals(ModuleType.TYPE_INVALID) == false && modularStack != null && super.isItemValid(stack) == true)
        {
            if (this.moduleType.equals(ModuleType.TYPE_ANY))
            {
                return true;
            }

            // Matching basic module type, check for the sub-type based on the host modular item
            if (this.moduleType.equals(type) && modularStack.getItem() instanceof IModular)
            {
                return ((IModular)modularStack.getItem()).getMaxModules(modularStack, stack) > 0;
            }
        }

        return false;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    /* TODO: Enable this in 1.8; in 1.7.10, there is a Forge bug that causes
     * these background icons to render incorrectly if there is an item with the glint effect
     * before the slot with the background icon.
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex()
    {
        return EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
    }
    */
}
