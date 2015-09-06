package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;

public class ContainerEnderInfuser extends ContainerTileEntityInventory
{
    private TileEntityEnderInfuser teef;
    public int amountStored;
    public int meltingProgress; // 0..100, 100 being 100% done; input item consumed and stored amount increased @ 100
    public int chargeProgress; // 0..100, 100 being 100% done; used for the filling animation only
    public int ciCapacity; // chargeableItemCapacity
    public int ciStarting; // chargeableItemStartingCharge
    public int ciCurrent; // chargeableItemCurrentCharge
    public int ciCurrentLast;

    public ContainerEnderInfuser(TileEntityEnderInfuser te, InventoryPlayer inventory)
    {
        super(te, inventory);
        this.teef = te;
    }

    protected void addSlots()
    {
        this.addSlotToContainer(new SlotItemInput(this.te, 0, 44, 24));
        this.addSlotToContainer(new SlotItemInput(this.te, 1, 134, 8));
        this.addSlotToContainer(new SlotOutput(this.te, 2, 134, 66));
    }

    @Override
    protected int getPlayerInventoryVerticalOffset()
    {
        return 94;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.teef.chargeableItemCurrentCharge != this.ciCurrent)
        {
            this.ciCurrent = this.teef.chargeableItemCurrentCharge;
            this.ciCapacity = this.teef.chargeableItemCapacity;
            this.ciStarting = this.teef.chargeableItemStartingCharge;
            this.updateChargingProgress();
        }

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            // The values need to fit into a short, where these get truncated to in non-local SMP

            if (this.teef.amountStored != this.amountStored)
            {
                icrafting.sendProgressBarUpdate(this, 0, this.teef.amountStored);
            }

            if (this.teef.meltingProgress != this.meltingProgress)
            {
                icrafting.sendProgressBarUpdate(this, 1, this.teef.meltingProgress);
            }

            if (this.ciCurrentLast != this.ciCurrent)
            {
                icrafting.sendProgressBarUpdate(this, 2, this.chargeProgress);
            }
        }

        this.ciCurrentLast = this.ciCurrent;
        this.amountStored = this.teef.amountStored;
        this.meltingProgress = this.teef.meltingProgress;
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);

        this.updateChargingProgress();
        icrafting.sendProgressBarUpdate(this, 0, this.amountStored);
        icrafting.sendProgressBarUpdate(this, 1, this.meltingProgress);
        icrafting.sendProgressBarUpdate(this, 2, this.chargeProgress);
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int var, int val)
    {
        switch(var)
        {
            case 0:
                this.teef.amountStored = val;
                break;
            case 1:
                this.teef.meltingProgress = val;
                break;
            case 2:
                this.chargeProgress = val;
                break;
            default:
        }
    }

    private void updateChargingProgress()
    {
        if (this.ciCapacity != this.ciStarting)
        {
            this.chargeProgress = (this.ciCurrent - this.ciStarting) * 100 / (this.ciCapacity - this.ciStarting);
        }
        else
        {
            this.chargeProgress = 0;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
    {
        ItemStack stack = null;
        Slot slot = (Slot) inventorySlots.get(slotNum);
        int invSize = this.te.getSizeInventory();

        // Slot clicked on has items
        if(slot != null && slot.getHasStack() == true)
        {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            Item item = stackInSlot.getItem();

            // Shift-click from the machine into the player inventory
            if (slotNum < invSize)
            {
                // Try to merge the stack into the player inventory
                if(mergeItemStack(stackInSlot, invSize, inventorySlots.size(), false) == false)
                {
                    return null;
                }

                // Shift-click from the output slot
                /*if (slotNum == 2)
                {
                    slot.onSlotChange(stackInSlot, stack);
                }*/
            }
            // Shift-click from the player inventory into the furnace
            else
            {
                // Meltable energy sources to the melting slot
                if (item == Items.ender_pearl || item == Items.ender_eye)
                {
                    if (this.mergeItemStack(stackInSlot, 0, 1, false) == false)
                    {
                        return null;
                    }
                }
                // Chargeable items to the input item slot
                else if (item instanceof IChargeable || item instanceof IModular)
                {
                    if (this.mergeItemStack(stackInSlot, 1, 2, false) == false)
                    {
                        return null;
                    }
                }
                // Not a valid item, only transfer it inside the player inventory
                // From main inventory into hotbar
                else if (slotNum >= invSize && slotNum < (27 + invSize))
                {
                    if (this.mergeItemStack(stackInSlot, (27 + invSize), (36 + invSize), false) == false)
                    {
                        return null;
                    }
                }
                // From hotbar into main inventory
                else if (slotNum >= (27 + invSize) && slotNum < (36 + invSize))
                {
                    if (this.mergeItemStack(stackInSlot, invSize, (27 + invSize), false) == false)
                    {
                        return null;
                    }
                }
            }

            // All items moved, empty the slot
            if(stackInSlot.stackSize == 0)
            {
                slot.putStack(null);
            }
            // Update the slot
            else
            {
                slot.onSlotChanged();
            }

            // No items were moved
            if(stackInSlot.stackSize == stack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }
}
