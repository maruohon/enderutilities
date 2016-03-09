package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;

public class ContainerLargeStacks extends ContainerEnderUtilitiesCustomSlotClick
{
    public ContainerLargeStacks(EntityPlayer player, IItemHandler inventory)
    {
        super(player, inventory);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Our inventory
        if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).itemHandler == this.inventory)
        {
            return slot.getItemStackLimit(stack);
        }

        // Player inventory
        return super.getMaxStackSizeFromSlotAndStack(slot, stack);
    }

    @Override
    public void onCraftGuiOpened(ICrafting iCrafting)
    {
        if (this.crafters.contains(iCrafting))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            this.crafters.add(iCrafting);

            if (iCrafting instanceof EntityPlayerMP)
            {
                this.syncAllSlots((EntityPlayerMP)iCrafting);
                ((EntityPlayerMP)iCrafting).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, ((EntityPlayerMP)iCrafting).inventory.getItemStack()));
            }

            this.detectAndSendChanges();
        }
    }

    protected void syncAllSlots(EntityPlayerMP player)
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            ItemStack stack = this.inventorySlots.get(i).getStack();
            PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, i, stack), player);
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            ItemStack currentStack = this.inventorySlots.get(i).getStack();
            ItemStack prevStack = this.inventoryItemStacks.get(i);

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = currentStack != null ? currentStack.copy() : null;
                this.inventoryItemStacks.set(i, prevStack);

                for (int j = 0; j < this.crafters.size(); ++j)
                {
                    ICrafting ic = (ICrafting)this.crafters.get(j);
                    if (ic instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player = (EntityPlayerMP)ic;
                        PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, i, prevStack), player);
                    }
                }
            }
        }
    }
}
