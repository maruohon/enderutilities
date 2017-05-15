package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import fi.dy.masa.enderutilities.inventory.ICustomSlotSync;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class ContainerASU extends ContainerLargeStacksTile implements ICustomSlotSync
{
    protected final TileEntityASU teasu;
    private final boolean[] lockedLast = new boolean[9];
    private final NonNullList<ItemStack> templateStacksLast = NonNullList.withSize(9, ItemStack.EMPTY);
    private int stackLimitLast;
    private int slotCountLast;

    public ContainerASU(EntityPlayer player, TileEntityASU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.teasu = te;
        this.itemHandlerLargeStacks = te.getInventoryASU();
        this.slotCountLast = te.getInvSize();

        this.reAddSlots();
    }

    private void reAddSlots()
    {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 27;
        int slots = this.inventory.getSlots();

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), slots);

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        int stackLimit = this.itemHandlerLargeStacks.getInventoryStackLimit();
        int slotCount = this.inventory.getSlots();

        for (int i = 0; i < this.listeners.size(); i++)
        {
            if (stackLimit != this.stackLimitLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 0, stackLimit & 0xFFFF);
                this.listeners.get(i).sendProgressBarUpdate(this, 1, stackLimit >>> 16);
            }

            if (slotCount != this.slotCountLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 3, slotCount);
            }
        }

        if (slotCount != this.slotCountLast)
        {
            this.reAddSlots();
        }

        this.stackLimitLast = stackLimit;
        this.slotCountLast = slotCount;

        this.syncLockableSlots(this.teasu.getInventoryASU(), 0, 2, this.lockedLast, this.templateStacksLast);

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        switch (id)
        {
            case 0:
                this.stackLimitLast = data;
                break;
            case 1:
                this.teasu.setStackLimit((data << 16) | this.stackLimitLast);
                break;
            case 2:
                this.teasu.getInventoryASU().setSlotLocked(data & 0xF, (data & 0x8000) != 0);
                break;
            case 3:
                this.teasu.setInvSize(data);
                this.reAddSlots();
                break;
        }
    }

    @Override
    public void putCustomStack(int typeId, int slotNum, ItemStack stack)
    {
        this.teasu.getInventoryASU().setTemplateStackInSlot(slotNum, stack);
    }
}
