package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerEnderUtilitiesInventory
{
    private TileEntityEnderFurnace teef;
    public int burnTimeRemaining;
    public int burnTimeFresh;
    public int cookTime;
    public int cookTimeFresh;
    public int fuelProgress;
    public int smeltingProgress;
    public int outputBufferAmount;

    public ContainerEnderFurnace(TileEntityEnderFurnace te, InventoryPlayer inventory)
    {
        super(te, inventory);
        this.teef = te;
    }

    protected void addSlots()
    {
        this.addSlotToContainer(new SlotItemInput(this.te, 0, 34, 17));
        this.addSlotToContainer(new SlotItemInput(this.te, 1, 34, 53));
        this.addSlotToContainer(new SlotFurnace(this.inventoryPlayer.player, this.te, 2, 88, 35));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        ICrafting icrafting;
        for (int i = 0; i < this.crafters.size(); ++i)
        {
            icrafting = (ICrafting)this.crafters.get(i);

            // Scale all values down by 8 (max burn time atm is 150 * COOKTIME = 180 000)
            // We need to fit it in a short, where these get truncated to in non-local SMP
            if (this.teef.burnTimeRemaining != this.burnTimeRemaining
                || this.teef.burnTimeFresh != this.burnTimeFresh
                || this.teef.cookTime != this.cookTime
                || this.teef.cookTimeFresh != this.cookTimeFresh)
            {
                int b = 0, c = 0;
                if (this.teef.burnTimeFresh != 0)
                {
                    b = 100 * this.teef.burnTimeRemaining / this.teef.burnTimeFresh;
                }
                if (this.teef.cookTimeFresh != 0)
                {
                    c = 100 * this.teef.cookTime / this.teef.cookTimeFresh;
                }
                // smelting progress and fuel burning progress are both 0..100, we send the smelting progress in the upper byte of the short
                icrafting.sendProgressBarUpdate(this, 0, c << 8 | b);
            }

            if (this.teef.getOutputBufferAmount() != this.outputBufferAmount)
            {
                icrafting.sendProgressBarUpdate(this, 1, this.teef.getOutputBufferAmount());
            }

            this.burnTimeRemaining = this.teef.burnTimeRemaining;
            this.burnTimeFresh = this.teef.burnTimeFresh;
            this.cookTime = this.teef.cookTime;
            this.cookTimeFresh = this.teef.cookTimeFresh;
            this.outputBufferAmount = this.teef.getOutputBufferAmount();
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);

        int b = 0, c = 0;
        if (this.teef.burnTimeFresh != 0)
        {
            b = 100 * this.teef.burnTimeRemaining / this.teef.burnTimeFresh;
        }
        if (this.teef.cookTimeFresh != 0)
        {
            c = 100 * this.teef.cookTime / this.teef.cookTimeFresh;
        }
        icrafting.sendProgressBarUpdate(this, 0, c << 8 | b);
        icrafting.sendProgressBarUpdate(this, 1, this.teef.getOutputBufferAmount());
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int var, int val)
    {
        switch(var)
        {
            case 0:
                this.fuelProgress = val & 0x7F; // value is 0..100, mask it to he lowest 7 bits (0..127)
                this.smeltingProgress = (val >> 8) & 0x7F;
                break;
            case 1:
                this.outputBufferAmount = val;
                break;
            default:
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

            // Shift-click from the furnace into the player inventory
            if (slotNum < invSize)
            {
                // Try to merge the stack into the player inventory
                if(mergeItemStack(stackInSlot, invSize, inventorySlots.size(), false) == false)
                {
                    return null;
                }

                // Shift-click from the output slot
                if (slotNum == 2)
                {
                    slot.onSlotChange(stackInSlot, stack);
                }
            }
            // Shift-click from the player inventory into the furnace
            else
            {
                // Has a smelting recipe, try to put in in the input slot
                if (FurnaceRecipes.smelting().getSmeltingResult(stackInSlot) != null)
                {
                    if (this.mergeItemStack(stackInSlot, 0, 1, false) == false)
                    {
                        return null;
                    }
                }
                // Is fuel, try to put it in the fuel slot
                else if (TileEntityEnderFurnace.isItemFuel(stackInSlot) == true
                        || stackInSlot.getItem() == EnderUtilitiesItems.enderBucket)
                {
                    if (this.mergeItemStack(stackInSlot, 1, 2, false) == false)
                    {
                        return null;
                    }
                }
                // Not fuel or smeltable, transfer between player main inventory and hotbar
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
