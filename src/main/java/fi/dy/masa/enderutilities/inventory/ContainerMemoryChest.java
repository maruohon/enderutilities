package fi.dy.masa.enderutilities.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncCustomSlot;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerMemoryChest extends ContainerEnderUtilities implements ICustomSlotSync
{
    protected TileEntityMemoryChest tetc;
    protected List<ItemStack> templateStacksLast;
    protected long templateMask;

    public ContainerMemoryChest(EntityPlayer player, TileEntityMemoryChest te)
    {
        super(player, te);
        this.tetc = te;
        this.templateStacksLast = new ArrayList<ItemStack>();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 58);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 8;
        int posY = 26;

        int tier = this.tetc.getStorageTier();
        int rows = TileEntityMemoryChest.INV_SIZES[tier] / 9;

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new SlotRange(customInvStart, this.inventorySlots.size() - customInvStart);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        posY = ((Slot)this.inventorySlots.get(this.inventorySlots.size() - 1)).yDisplayPosition + 32;

        super.addPlayerInventorySlots(posX, posY);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return super.canInteractWith(player) && this.tetc.isInvalid() == false;
    }

    @Override
    public boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;
        if (slot == null || slot.getHasStack() == false)
        {
            return false;
        }

        // We want to merge to matching template slots first, so we only handle slots FROM the player inventory
        if (slot.inventory != this.player.inventory)
        {
            return super.transferStackFromSlot(player, slotNum);
        }

        ItemStack stackSlot = slot.getStack();
        int origSize = stackSlot.stackSize;

        for (int i = 0; i < this.tetc.getSizeInventory(); i++)
        {
            ItemStack stackTmp = this.tetc.getTemplateStack(i);

            if (stackTmp != null && InventoryUtils.areItemStacksEqual(stackTmp, stackSlot) == true)
            {
                this.mergeItemStack(stackSlot, i, i + 1, false);

                if (stackSlot.stackSize <= 0)
                {
                    slot.putStack(null);
                    slot.onPickupFromSlot(player, stackSlot);
                    return true;
                }
            }
        }

        if (stackSlot.stackSize != origSize)
        {
            slot.onPickupFromSlot(player, stackSlot);
        }

        return super.transferStackFromSlot(player, slotNum);
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        // Middle click
        if (button == 2 && type == 3 && slotNum >= 0 && slotNum < (this.tetc.getSizeInventory()))
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;
            if (invSlotNum == -1)
            {
                return null;
            }

            this.tetc.setTemplateStack(invSlotNum, this.tetc.getStackInSlot(invSlotNum));
            this.tetc.toggleTemplateMask(invSlotNum);

            return null;
        }

        ItemStack stack = super.slotClick(slotNum, button, type, player);

        this.detectAndSendChanges();

        return stack;
    }

    public TileEntityMemoryChest getTileEntity()
    {
        return this.tetc;
    }

    @Override
    protected Slot addSlotToContainer(Slot slot)
    {
        this.templateStacksLast.add(null);

        return super.addSlotToContainer(slot);
    }

    @Override
    public void putCustomStack(int typeId, int slotNum, ItemStack stack)
    {
        this.tetc.setTemplateStack(slotNum, stack);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.tetc.getWorld().isRemote == true)
        {
            return;
        }

        for (int i = 0; i < this.templateStacksLast.size(); i++)
        {
            ItemStack currentStack = this.tetc.getTemplateStack(i);
            ItemStack prevStack = this.templateStacksLast.get(i);

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = currentStack != null ? currentStack.copy() : null;
                this.templateStacksLast.set(i, prevStack);

                for (int j = 0; j < this.crafters.size(); j++)
                {
                    ICrafting icrafting = (ICrafting)this.crafters.get(j);
                    if (icrafting instanceof EntityPlayerMP)
                    {
                        PacketHandler.INSTANCE.sendTo(new MessageSyncCustomSlot(this.windowId, 0, i, prevStack), (EntityPlayerMP)icrafting);
                    }
                }
            }
        }

        long mask = this.tetc.getTemplateMask();

        for (int j = 0; j < this.crafters.size(); ++j)
        {
            if (this.templateMask != mask)
            {
                ICrafting icrafting = (ICrafting)this.crafters.get(j);
                // Send the long in 16-bit pieces because of the network packet limitation in MP
                icrafting.sendProgressBarUpdate(this, 0, (int)(mask & 0xFFFF));
                icrafting.sendProgressBarUpdate(this, 1, (int)((mask >> 16) & 0xFFFF));
                icrafting.sendProgressBarUpdate(this, 2, (int)((mask >> 32) & 0xFFFF));
                icrafting.sendProgressBarUpdate(this, 3, (int)((mask >> 48) & 0xFFFF));
            }
        }

        this.templateMask = mask;
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        if (var >= 0 && var <= 3)
        {
            this.templateMask &= ~(0xFFFFL << (var * 16));
            this.templateMask |= (((long)val) << (var * 16));
            this.tetc.setTemplateMask(this.templateMask);
        }
    }
}
