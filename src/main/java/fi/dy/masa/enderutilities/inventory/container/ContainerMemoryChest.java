package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTile;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class ContainerMemoryChest extends ContainerTile implements ICustomSlotSync
{
    protected TileEntityMemoryChest temc;
    private final boolean[] lockedLast;
    private final NonNullList<ItemStack> templateStacksLast;
    private boolean isPublic;

    public ContainerMemoryChest(EntityPlayer player, TileEntityMemoryChest te)
    {
        super(player, te);

        this.temc = te;
        this.inventoryNonWrapped = te.getInventory();

        int numSlots = this.inventoryNonWrapped.getSlots();
        this.lockedLast = new boolean[numSlots];
        this.templateStacksLast = NonNullList.withSize(numSlots, ItemStack.EMPTY);

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, this.inventorySlots.get(this.inventorySlots.size() - 1).yPos + 32);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 26;
        final int slots = this.inventoryNonWrapped.getSlots();

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), slots);

        for (int slot = 0, x = posX; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, x, posY));
            x += 18;

            if (slot % 9 == 8)
            {
                x = posX;
                posY += 18;
            }
        }
    }

    @Override
    protected boolean transferStackToPrioritySlots(EntityPlayer player, int slotNum, boolean reverse)
    {
        boolean ret = false;
        ItemStackHandlerLockable inv = this.temc.getInventory();

        // Try to shift-click to locked slots first
        for (int i = 0; i < inv.getSlots(); i++)
        {
            if (inv.isSlotLocked(i))
            {
                ret |= this.transferStackToSlotRange(player, slotNum, new MergeSlotRange(i, 1), reverse);
            }
        }

        return ret;
    }

    @Override
    public void detectAndSendChanges()
    {
        boolean isPublic = this.temc.isPublic();

        for (int j = 0; j < this.listeners.size(); ++j)
        {
            IContainerListener listener = this.listeners.get(j);

            if (this.isPublic != isPublic)
            {
                listener.sendProgressBarUpdate(this, 0, isPublic ? 1 : 0);
            }
        }

        this.isPublic = isPublic;

        this.syncLockableSlots(this.temc.getInventory(), 0, 1, this.lockedLast, this.templateStacksLast);

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        switch (id)
        {
            case 0:
                this.temc.setIsPublic(data == 1);
                break;
            case 1:
                this.temc.getInventory().setSlotLocked(data & 0xFF, (data & 0x8000) != 0);
                break;
        }
    }

    @Override
    public void putCustomStack(int typeId, int slotNum, ItemStack stack)
    {
        this.temc.getInventory().setTemplateStackInSlot(slotNum, stack);
    }
}
