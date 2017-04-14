package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;

public class ContainerASU extends ContainerLargeStacksTile
{
    protected TileEntityASU teasu;
    private int stackLimitLast;

    public ContainerASU(EntityPlayer player, TileEntityASU te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.teasu = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 57);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 27;
        int slots = this.teasu.getStorageTier();

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), slots);

        for (int slot = 0; slot < slots; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, slot, posX + slot * 18, posY));
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.isClient == false)
        {
            int stackLimit = this.teasu.getBaseItemHandler().getInventoryStackLimit();
 
            for (int i = 0; i < this.listeners.size(); i++)
            {
                if (stackLimit != this.stackLimitLast)
                {
                    this.listeners.get(i).sendProgressBarUpdate(this, 0, stackLimit & 0xFFFF);
                    this.listeners.get(i).sendProgressBarUpdate(this, 1, stackLimit >>> 16);
                }
            }

            this.stackLimitLast = stackLimit;

            super.detectAndSendChanges();
        }
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
                this.teasu.getBaseItemHandler().setStackLimit((data << 16) | this.stackLimitLast);
                break;
        }
    }
}
