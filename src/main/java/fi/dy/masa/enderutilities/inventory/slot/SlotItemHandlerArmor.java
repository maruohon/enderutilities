package fi.dy.masa.enderutilities.inventory.slot;

import javax.annotation.Nullable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;

public class SlotItemHandlerArmor extends SlotItemHandlerGeneric
{
    protected final ContainerEnderUtilities container;
    protected final int armorSlotIndex;

    public SlotItemHandlerArmor(ContainerEnderUtilities container, IItemHandler itemHandler, int armorSlotIndex, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.container = container;
        this.armorSlotIndex = armorSlotIndex;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return 1;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        EntityEquipmentSlot slot = ContainerHandyBag.EQUIPMENT_SLOT_TYPES[this.armorSlotIndex];
        return stack.getItem().isValidArmor(stack, slot, this.container.player);
    }

    @Override
    @Nullable
    public String getSlotTexture()
    {
        return ItemArmor.EMPTY_SLOT_NAMES[ContainerHandyBag.EQUIPMENT_SLOT_TYPES[this.armorSlotIndex].getIndex()];
    }
}
