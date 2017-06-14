package fi.dy.masa.enderutilities.inventory.wrapper;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class InventoryCraftingWrapperPermissions extends InventoryCrafting
{
    protected final Container container;
    protected final ItemHandlerWrapperPermissions craftMatrix;

    public InventoryCraftingWrapperPermissions(Container container, int width, int height, ItemHandlerWrapperPermissions craftMatrix)
    {
        super(container, width, height);
        this.container = container;
        this.craftMatrix = craftMatrix;
    }

    public ItemHandlerWrapperPermissions getBaseInventory()
    {
        return this.craftMatrix;
    }

    @Override
    public int getSizeInventory()
    {
        return this.craftMatrix.getSlots();
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int slot)
    {
        return slot >= this.getSizeInventory() ? ItemStack.EMPTY : this.craftMatrix.getStackInSlot(slot);
    }

    @Override
    @Nullable
    public ItemStack removeStackFromSlot(int slot)
    {
        return this.craftMatrix.extractItem(slot, Integer.MAX_VALUE, false);
    }

    @Override
    @Nullable
    public ItemStack decrStackSize(int slot, int amount)
    {
        ItemStack stack = this.craftMatrix.extractItem(slot, amount, false);

        if (stack.isEmpty() == false)
        {
            this.markDirty();
        }

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.craftMatrix.setStackInSlot(slot, stack);
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.craftMatrix.getInventoryStackLimit();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return this.craftMatrix.isAccessibleByPlayer(player);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return this.craftMatrix.isItemValidForSlot(slot, stack);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();

        this.container.onCraftMatrixChanged(this);
    }

    @Override
    public void clear()
    {
        for (int slot = 0; slot < this.craftMatrix.getSlots(); slot++)
        {
            this.craftMatrix.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
}
