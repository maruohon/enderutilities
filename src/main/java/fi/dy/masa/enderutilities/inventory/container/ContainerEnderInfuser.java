package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;

public class ContainerEnderInfuser extends ContainerTileEntityInventory
{
    private TileEntityEnderInfuser teef;
    public int amountStored;
    public int meltingProgress; // 0..100, 100 being 100% done; input item consumed and stored amount increased @ 100
    public int chargeProgress; // 0..100, 100 being 100% done; used for the filling animation only
    public int ciCapacity; // chargeableItemCapacity
    public int ciStarting; // chargeableItemStartingCharge
    public int ciCurrent; // chargeableItemCurrentCharge
    public int ciCurrentLast;

    public ContainerEnderInfuser(EntityPlayer player, TileEntityEnderInfuser te)
    {
        super(player, te);
        this.teef = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 94);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 44, 24));
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 1, 134, 8));
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 2, 134, 66));
        this.customInventorySlots = new MergeSlotRange(0, this.inventorySlots.size());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.teef.chargeableItemCurrentCharge != this.ciCurrent)
        {
            this.ciCurrent = this.teef.chargeableItemCurrentCharge;
            this.ciCapacity = this.teef.chargeableItemCapacity;
            this.ciStarting = this.teef.chargeableItemStartingCharge;
            this.updateChargingProgress();
        }

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener listener = this.listeners.get(i);

            // The values need to fit into a short, where these get truncated to in non-local SMP

            if (this.teef.amountStored != this.amountStored)
            {
                listener.sendProgressBarUpdate(this, 0, this.teef.amountStored);
            }

            if (this.teef.meltingProgress != this.meltingProgress)
            {
                listener.sendProgressBarUpdate(this, 1, this.teef.meltingProgress);
            }

            if (this.ciCurrentLast != this.ciCurrent)
            {
                listener.sendProgressBarUpdate(this, 2, this.chargeProgress);
            }
        }

        this.ciCurrentLast = this.ciCurrent;
        this.amountStored = this.teef.amountStored;
        this.meltingProgress = this.teef.meltingProgress;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        this.updateChargingProgress();
        listener.sendProgressBarUpdate(this, 0, this.amountStored);
        listener.sendProgressBarUpdate(this, 1, this.meltingProgress);
        listener.sendProgressBarUpdate(this, 2, this.chargeProgress);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int var, int val)
    {
        switch(var)
        {
            case 0:
                this.teef.amountStored = val;
                break;
            case 1:
                this.teef.meltingProgress = val;
                break;
            case 2:
                this.chargeProgress = val;
                break;
            default:
        }
    }

    private void updateChargingProgress()
    {
        if (this.ciCapacity != this.ciStarting)
        {
            this.chargeProgress = (this.ciCurrent - this.ciStarting) * 100 / (this.ciCapacity - this.ciStarting);
        }
        else
        {
            this.chargeProgress = 0;
        }
    }
}
