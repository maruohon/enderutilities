package fi.dy.masa.enderutilities.inventory.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncCustomSlot;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class ContainerMemoryChest extends ContainerTileEntityInventory implements ICustomSlotSync
{
    protected TileEntityMemoryChest temc;
    protected List<ItemStack> templateStacksLast;
    protected long templateMask;
    protected boolean isPublic;

    public ContainerMemoryChest(EntityPlayer player, TileEntityMemoryChest te)
    {
        super(player, te);
        this.temc = te;
        this.templateStacksLast = new ArrayList<ItemStack>();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, this.inventorySlots.get(this.inventorySlots.size() - 1).yDisplayPosition + 32);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 8;
        int posY = 26;

        int tier = this.temc.getStorageTier();
        int rows = TileEntityMemoryChest.INV_SIZES[tier] / 9;

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);
    }

    @Override
    protected boolean transferStackToPrioritySlots(EntityPlayer player, int slotNum, boolean reverse)
    {
        boolean ret = false;
        long mask = this.temc.getTemplateMask();
        long bit = 0x1;

        // Try to shift-click to locked slots first
        for (int i = 0; i < this.inventorySlots.size(); i++, bit <<= 1)
        {
            if ((mask & bit) != 0)
            {
                ret |= this.transferStackToSlotRange(player, slotNum, new MergeSlotRange(i, 1), reverse);
            }
        }

        return ret;
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        // Middle click
        if (((clickType == ClickType.CLONE && dragType == 2) ||
            (clickType == ClickType.QUICK_CRAFT && dragType == 9)) &&
            slotNum >= 0 && slotNum < this.inventory.getSlots())
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;

            if (invSlotNum != -1)
            {
                ItemStack stackSlot = this.inventory.getStackInSlot(invSlotNum);
                ItemStack stackCursor = this.player.inventory.getItemStack();

                if (stackCursor != null && stackSlot == null && (this.temc.getTemplateMask() & (1L << invSlotNum)) == 0)
                {
                    this.temc.setTemplateStack(invSlotNum, stackCursor);
                }
                else
                {
                    this.temc.setTemplateStack(invSlotNum, stackSlot);
                }

                this.temc.toggleTemplateMask(invSlotNum);
            }

            return null;
        }

        ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);

        this.detectAndSendChanges();

        return stack;
    }

    public TileEntityMemoryChest getTileEntity()
    {
        return this.temc;
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
        this.temc.setTemplateStack(slotNum, stack);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.temc.getWorld().isRemote == true)
        {
            return;
        }

        for (int i = 0; i < this.templateStacksLast.size(); i++)
        {
            ItemStack currentStack = this.temc.getTemplateStack(i);
            ItemStack prevStack = this.templateStacksLast.get(i);

            if (ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
            {
                prevStack = currentStack != null ? currentStack.copy() : null;
                this.templateStacksLast.set(i, prevStack);

                for (int j = 0; j < this.listeners.size(); j++)
                {
                    IContainerListener listener = this.listeners.get(j);
                    if (listener instanceof EntityPlayerMP)
                    {
                        PacketHandler.INSTANCE.sendTo(new MessageSyncCustomSlot(this.windowId, 0, i, prevStack), (EntityPlayerMP)listener);
                    }
                }
            }
        }

        long mask = this.temc.getTemplateMask();
        boolean isPublic = this.temc.isPublic();

        for (int j = 0; j < this.listeners.size(); ++j)
        {
            IContainerListener listener = this.listeners.get(j);

            if (this.templateMask != mask)
            {
                // Send the long in 16-bit pieces because of the network packet limitation in MP
                listener.sendProgressBarUpdate(this, 0, (int)(mask & 0xFFFF));
                listener.sendProgressBarUpdate(this, 1, (int)((mask >> 16) & 0xFFFF));
                listener.sendProgressBarUpdate(this, 2, (int)((mask >> 32) & 0xFFFF));
                listener.sendProgressBarUpdate(this, 3, (int)((mask >> 48) & 0xFFFF));
            }

            if (this.isPublic != isPublic)
            {
                listener.sendProgressBarUpdate(this, 4, isPublic ? 1 : 0);
            }
        }

        this.templateMask = mask;
        this.isPublic = isPublic;
    }

    @Override
    public void updateProgressBar(int var, int val)
    {
        if (var >= 0 && var <= 3)
        {
            this.templateMask &= ~(0xFFFFL << (var * 16));
            this.templateMask |= (((long)val) << (var * 16));
            this.temc.setTemplateMask(this.templateMask);
        }
        else if (var == 4)
        {
            this.temc.setIsPublic(val == 1);
        }
    }
}
