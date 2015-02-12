package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderInfuser;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityEnderInfuser extends TileEntityEnderUtilitiesSided
{
    protected static final int[] SLOTS_SIDES = new int[] {0, 1, 2};

    public TileEntityEnderInfuser()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        this.itemStacks = new ItemStack[3];
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
    }

    @Override
    public void updateEntity()
    {
    }

    /* Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. */
    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        // Only allow Ender Pearls and Eyes of Ender to the melting slot
        if (slotNum == 0)
        {
            return (stack != null && (stack.getItem() == Items.ender_pearl || stack.getItem() == Items.ender_eye));
        }

        // Only accept chargeable items to the item input slot
        if (slotNum == 1)
        {
            return (stack != null && stack.getItem() instanceof IChargeable);
        }

        return false;
    }

    /* Returns an array containing the indices of the slots that can be accessed by automation on the given side of this block. */
    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        // Allow access to all slots from all sides
        return SLOTS_SIDES;
    }

    /* Returns true if automation can insert the given item in the given slot from the given side. Args: slot, itemstack, side */
    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side)
    {
        return this.isItemValidForSlot(slot, stack);
    }

    // Returns true if automation can extract the given item in the given slot from the given side. Args: slot, itemstack, side
    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {
        // Only allow pulling out items from the output slot
        return slot == 2;
    }

    @Override
    public ContainerEnderInfuser getContainer(InventoryPlayer inventory)
    {
        return new ContainerEnderInfuser(this, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiEnderInfuser(this.getContainer(inventoryPlayer), this);
    }

    @Override
    public void performGuiAction(int element, short action)
    {
    }
}
