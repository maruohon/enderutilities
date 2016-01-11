package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;

public class ContainerLargeStacks extends ContainerEnderUtilitiesCustomSlotClick
{
    public ContainerLargeStacks(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        super(inventoryPlayer, inventory);
    }

    @Override
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        // Player inventory
        if (slot.inventory != this.inventory)
        {
            return super.getMaxStackSizeFromSlotAndStack(slot, stack);
        }

        // Our inventory
        return slot.getSlotStackLimit();
    }

    @Override
    public void addCraftingToCrafters(ICrafting iCrafting)
    {
        if (this.crafters.contains(iCrafting))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            this.crafters.add(iCrafting);
            //iCrafting.sendContainerAndContentsToPlayer(this, this.getInventory());

            if (iCrafting instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP)iCrafting).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, ((EntityPlayerMP)iCrafting).inventory.getItemStack()));
            }

            this.detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            ItemStack currentStack = ((Slot)this.inventorySlots.get(i)).getStack();
            ItemStack prevStack = (ItemStack)this.inventoryItemStacks.get(i);

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
