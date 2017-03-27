package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ContainerBarrel extends ContainerLargeStacks
{
    private final ItemStackHandlerBasic baseInv;
    private final IItemHandler upgradeInv;

    public ContainerBarrel(EntityPlayer player, TileEntityBarrel te)
    {
        super(player, te.getWrappedInventoryForContainer(player));

        this.baseInv = te.getBaseItemHandler();
        this.upgradeInv = te.getUpgradeInventory();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 93);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 1);

        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 80, 23));

        // Upgrade slots
        for (int slot = 0; slot < 3; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.upgradeInv, slot, 62 + slot * 18, 59));
        }
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        // Middle click on an MSU slot with items in cursor, fill/cycle/clear the slot
        if (player.capabilities.isCreativeMode && player.inventory.getItemStack() != null &&
            clickType == ClickType.CLONE && dragType == 2 && this.customInventorySlots.contains(slotNum))
        {
            SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

            if (slot != null)
            {
                ItemStack stackCursor = player.inventory.getItemStack();
                ItemStack stackSlot = slot.getStack();

                if (InventoryUtils.areItemStacksEqual(stackCursor, stackSlot))
                {
                    int max = this.baseInv.getItemStackLimit(stackCursor);

                    // When middle clicked with an identical single item in cursor, cycle the stack size in x10 increments
                    if (stackCursor.stackSize == 1)
                    {
                        long newSize = stackSlot.stackSize * 10L;

                        if (newSize >= 0 && newSize <= max)
                        {
                            stackSlot = stackSlot.copy();
                            stackSlot.stackSize = (int) newSize;
                            slot.putStack(stackSlot);
                        }
                        else
                        {
                            slot.putStack(null);
                        }
                    }
                    // When middle clicked with an identical item in cursor (when holding more than one),
                    // fill the stack to the maximum size in one go, or clear the slot.
                    else
                    {
                        if (stackSlot.stackSize < max)
                        {
                            stackSlot = stackSlot.copy();
                            stackSlot.stackSize = max;
                            slot.putStack(stackSlot);
                        }
                        else
                        {
                            slot.putStack(null);
                        }
                    }
                }
                // Different items in cursor, copy the stack from the cursor to the slot
                else
                {
                    slot.putStack(stackCursor.copy());
                }

                return null;
            }
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }
}
