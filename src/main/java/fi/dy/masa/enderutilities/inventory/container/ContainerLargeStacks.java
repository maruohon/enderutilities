package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;

public class ContainerLargeStacks extends ContainerCustomSlotClick
{
    public ContainerLargeStacks(EntityPlayer player, IItemHandler inventory)
    {
        super(player, inventory);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our inventory
        if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() == this.inventory)
        {
            return slot.getItemStackLimit(stack);
        }

        // Player inventory
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public void addListener(ICrafting iCrafting)
    {
        if (this.listeners.contains(iCrafting))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            this.listeners.add(iCrafting);

            if (iCrafting instanceof EntityPlayerMP)
            {
                this.syncAllSlots((EntityPlayerMP)iCrafting);
                ((EntityPlayerMP)iCrafting).connection.sendPacket(new SPacketSetSlot(-1, -1, ((EntityPlayerMP)iCrafting).inventory.getItemStack()));
            }

            this.detectAndSendChanges();
        }
    }

    protected void syncAllSlots(EntityPlayerMP player)
    {
        for (int slot = 0; slot < this.inventorySlots.size(); slot++)
        {
            ItemStack stack = this.inventorySlots.get(slot).getStack();
            PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, slot, stack), player);
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        for (int slot = 0; slot < this.inventorySlots.size(); slot++)
        {
            ItemStack currentStack = this.inventorySlots.get(slot).getStack();
            ItemStack prevStack = this.inventoryItemStacks.get(slot);

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = ItemStack.copyItemStack(currentStack);
                this.inventoryItemStacks.set(slot, prevStack);

                for (int j = 0; j < this.listeners.size(); ++j)
                {
                    ICrafting ic = (ICrafting)this.listeners.get(j);
                    if (ic instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player = (EntityPlayerMP)ic;
                        PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, slot, prevStack), player);
                    }
                }
            }
        }
    }
}
