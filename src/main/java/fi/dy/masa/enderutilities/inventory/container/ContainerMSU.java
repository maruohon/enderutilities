package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ContainerMSU extends ContainerLargeStacksTile
{
    protected TileEntityMSU temsu;

    public ContainerMSU(EntityPlayer player, TileEntityMSU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.temsu = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int tier = MathHelper.clamp(this.temsu.getStorageTier(), 0, 1);

        int posX = tier == 1 ? 8 : 80;
        int posY = 23;
        int slots = tier == 1 ? 9 : 1;

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, slots);
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
                    // When middle clicked with an identical single item in cursor, cycle the stack size in x10 increments
                    if (stackCursor.stackSize == 1)
                    {
                        long newSize = stackSlot.stackSize * 10L;

                        if (newSize >= 0 && newSize <= Configs.msuMaxItems)
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
                        if (stackSlot.stackSize < Configs.msuMaxItems)
                        {
                            stackSlot = stackSlot.copy();
                            stackSlot.stackSize = Configs.msuMaxItems;
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
