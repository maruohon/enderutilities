package fi.dy.masa.enderutilities.inventory;

import java.util.BitSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemStackHandlerLockable extends ItemStackHandlerTileEntity
{
    private final NonNullList<ItemStack> templateStacks;
    private final BitSet locked;

    public ItemStackHandlerLockable(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes,
            String tagName, TileEntityEnderUtilitiesInventory te)
    {
        super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);

        this.templateStacks = NonNullList.withSize(invSize, ItemStack.EMPTY);
        this.locked = new BitSet(invSize);
    }

    public boolean isSlotLocked(int slot)
    {
        return this.locked.get(slot);
    }

    /**
     * Toggles the locked state, and returns the new value.
     * @param slot
     * @return
     */
    public boolean toggleSlotLocked(int slot)
    {
        boolean isLocked = this.locked.get(slot) == false;
        this.setSlotLocked(slot, isLocked);
        return isLocked;
    }

    public void setSlotLocked(int slot, boolean isLocked)
    {
        if (isLocked)
        {
            this.locked.set(slot);
        }
        else
        {
            this.locked.clear(slot);
        }

        this.onContentsChanged(slot);
    }

    @Nullable
    public ItemStack getTemplateStackInSlot(int slot)
    {
        return this.templateStacks.get(slot);
    }

    /**
     * Sets the template stack to <b>a copy of</b> the stack given
     * @param slot
     * @param stack
     */
    public void setTemplateStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        if (stack.isEmpty())
        {
            this.templateStacks.set(slot, ItemStack.EMPTY);
        }
        else
        {
            stack = stack.copy();
            stack.setCount(1);
            this.templateStacks.set(slot, stack);
        }

        this.onContentsChanged(slot);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return this.locked.get(slot) == false || InventoryUtils.areItemStacksEqual(this.templateStacks.get(slot), stack);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = super.serializeNBT();
        nbt = NBTUtils.writeItemsToTag(nbt, this.templateStacks, "TemplateItems", false);
        nbt.setByteArray("LockedSlots", this.locked.toByteArray());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
        NBTUtils.readStoredItemsFromTag(nbt, this.templateStacks, "TemplateItems");
        this.locked.clear();
        this.locked.or(BitSet.valueOf(nbt.getByteArray("LockedSlots")));
    }
}
