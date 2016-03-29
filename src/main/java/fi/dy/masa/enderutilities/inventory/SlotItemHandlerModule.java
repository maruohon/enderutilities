package fi.dy.masa.enderutilities.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

public class SlotItemHandlerModule extends SlotItemHandlerGeneric
{
    protected ModuleType moduleType;
    protected int moduleTierMin;
    protected int moduleTierMax;

    public SlotItemHandlerModule(IItemHandler inventory, int slot, int posX, int posY, ModuleType moduleType)
    {
        super(inventory, slot, posX, posY);
        this.moduleType = moduleType;
        this.moduleTierMin = -1;
        this.moduleTierMax = -1;
    }

    public ModuleType getModuleType()
    {
        return this.moduleType;
    }

    public SlotItemHandlerModule setModuleType(ModuleType type)
    {
        this.moduleType = type;
        return this;
    }

    public SlotItemHandlerModule setMinAndMaxModuleTier(int min, int max)
    {
        this.moduleTierMin = min;
        this.moduleTierMax = max;
        return this;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        //System.out.println("SlotItemHandlerModule.getItemStackLimit(stack)");
        return this.getSlotStackLimit();
    }

    @Override
    public int getSlotStackLimit()
    {
        //System.out.println("SlotItemHandlerModule.getSlotStackLimit()");
        return 1;
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
                if (this.moduleTierMin == -1 && this.moduleTierMax == -1)
                {
                    return true;
                }

                return module.getModuleTier(stack) >= this.moduleTierMin && module.getModuleTier(stack) <= this.moduleTierMax;
            }
        }

        return false;
    }

    /**
     * Get the texture u coordinate on the widgets sheet for this type of module.
     * If the type is ANY, then -1 is returned
     * @return
     */
    public int getBackgroundIconU()
    {
        if (this.moduleType.equals(ModuleType.TYPE_INVALID) == true)
        {
            return 102;
        }

        return this.moduleType.equals(ModuleType.TYPE_ANY) ? -1 : 240;
    }

    /**
     * Get the texture u coordinate on the widgets sheet for this type of module.
     * If the type is ANY, then -1 is returned
     * @return
     */
    public int getBackgroundIconV()
    {
        if (this.moduleType.equals(ModuleType.TYPE_INVALID) == true)
        {
            return 0;
        }

        return this.moduleType.equals(ModuleType.TYPE_ANY) ? -1 : this.moduleType.getIndex() * 16;
    }
}
