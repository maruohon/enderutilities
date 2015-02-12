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
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;

public class ContainerEnderInfuser extends ContainerEnderUtilitiesInventory
{
    private TileEntityEnderInfuser teef;
    public int burnTimeRemaining;
    public int burnTimeFresh;
    public int cookTime;
    public int cookTimeFresh;

    public ContainerEnderInfuser(TileEntityEnderInfuser te, InventoryPlayer inventory)
    {
        super(te, inventory);
        this.teef = te;
    }

    protected void addSlots()
    {
        this.addSlotToContainer(new SlotInfuserMaterial(this.te, 0, 44, 24));
        this.addSlotToContainer(new SlotChargeable(this.te, 1, 134, 8));
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

        ICrafting icrafting;
        for (int i = 0; i < this.crafters.size(); ++i)
        {
            icrafting = (ICrafting)this.crafters.get(i);

            // Scale all values down by 8 (max burn time atm is 150 * COOKTIME = 180 000)
            // We need to fit it in a short, where these get truncated to in non-local SMP
            /*if (this.teef.burnTimeRemaining != this.burnTimeRemaining)
            {
                icrafting.sendProgressBarUpdate(this, 0, this.teef.burnTimeRemaining >> 3);
            }

            if (this.teef.burnTimeFresh != this.burnTimeFresh)
            {
                icrafting.sendProgressBarUpdate(this, 1, this.teef.burnTimeFresh >> 3);
            }

            if (this.teef.cookTime != this.cookTime)
            {
                icrafting.sendProgressBarUpdate(this, 2, this.teef.cookTime >> 3);
            }

            if (this.teef.cookTimeFresh != this.cookTimeFresh)
            {
                icrafting.sendProgressBarUpdate(this, 3, this.teef.cookTimeFresh >> 3);
            }

            this.burnTimeRemaining = this.teef.burnTimeRemaining;
            this.burnTimeFresh = this.teef.burnTimeFresh;
            this.cookTime = this.teef.cookTime;
            this.cookTimeFresh = this.teef.cookTimeFresh;*/
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);
        /*icrafting.sendProgressBarUpdate(this, 0, this.teef.burnTimeRemaining >> 3);
        icrafting.sendProgressBarUpdate(this, 1, this.teef.burnTimeFresh >> 3);
        icrafting.sendProgressBarUpdate(this, 2, this.teef.cookTime >> 3);
        icrafting.sendProgressBarUpdate(this, 3, this.teef.cookTimeFresh >> 3);*/
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int var, int val)
    {
        /*switch(var)
        {
            case 0:
                this.teef.burnTimeRemaining = val;
                break;
            case 1:
                this.teef.burnTimeFresh = val;
                break;
            case 2:
                this.teef.cookTime = val;
                break;
            case 3:
                this.teef.cookTimeFresh = val;
                break;
            default:
        }*/
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
                else if (item instanceof IChargeable)
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
