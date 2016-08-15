package fi.dy.masa.enderutilities.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class InventoryCraftingWrapper extends InventoryCrafting
{
    protected final Container container;
    protected final ItemHandlerWrapperPermissions craftMatrix;

    public InventoryCraftingWrapper(Container container, int width, int height, ItemHandlerWrapperPermissions craftMatrix)
    {
        super(container, width, height);
        this.container = container;
        this.craftMatrix = craftMatrix;
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
        return slot >= this.getSizeInventory() ? null : this.craftMatrix.getStackInSlot(slot);
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
        ItemStack stack = this.craftMatrix.extractItem(slot, amount, false); //ItemStackHelper.getAndSplit(this.stackList, index, count);

        if (stack != null)
        {
            this.container.onCraftMatrixChanged(this);
        }

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nullable ItemStack stack)
    {
        this.craftMatrix.setStackInSlot(slot, stack);
        this.container.onCraftMatrixChanged(this);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.craftMatrix.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.craftMatrix.isAccessibleByPlayer(player);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return this.craftMatrix.isItemValidForSlot(slot, stack);
    }

    @Override
    public void clear()
    {
        for (int slot = 0; slot < this.craftMatrix.getSlots(); slot++)
        {
            this.craftMatrix.setStackInSlot(slot, null);
        }
    }
}
