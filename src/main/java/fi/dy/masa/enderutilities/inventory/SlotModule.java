package fi.dy.masa.enderutilities.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class SlotModule extends SlotGeneric
{
    protected ModuleType moduleType;
    protected int moduleTierMin;
    protected int moduleTierMax;
    protected int stackLimit;

    public SlotModule(IInventory inventory, int slot, int posX, int posY, ModuleType moduleType)
    {
        super(inventory, slot, posX, posY);
        this.moduleType = moduleType;
        this.stackLimit = 1;
    }

    public ModuleType getModuleType()
    {
        return this.moduleType;
    }

    public SlotModule setModuleType(ModuleType type)
    {
        this.moduleType = type;
        return this;
    }

    public SlotModule setMinAndMaxModuleTier(int min, int max)
    {
        this.moduleTierMin = min;
        this.moduleTierMax = max;
        return this;
    }

    public SlotModule setSlotStackLimit(int limit)
    {
        this.stackLimit = limit;
        return this;
    }

    @Override
    public int getSlotStackLimit()
    {
        return this.stackLimit;
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

        IModule module = (IModule)stack.getItem();
        ModuleType type = module.getModuleType(stack);

        if (type.equals(ModuleType.TYPE_INVALID) == false && super.isItemValid(stack) == true)
        {
            if (this.moduleType.equals(ModuleType.TYPE_ANY))
            {
                return true;
            }

            // Matching basic module type, check for the sub-type/tier
            if (this.moduleType.equals(type))
            {
                return module.getModuleTier(stack) >= this.moduleTierMin && module.getModuleTier(stack) <= this.moduleTierMax;
            }
        }

        return false;
    }

    /* TODO: Enable this in 1.8; in 1.7.10, there is a Forge bug that causes
     * these background icons to render incorrectly if there is an item with the glint effect
     * before the slot with the background icon.
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex()
    {
        return EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(this.moduleType);
    }
    */
}
