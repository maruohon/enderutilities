package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerLargeStacksTile;
import fi.dy.masa.enderutilities.inventory.container.base.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerFurnaceOutput;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerLargeStacksTile
{
    protected TileEntityEnderFurnace teef;
    public int burnTimeRemaining;
    public int burnTimeFresh;
    public int cookTime;
    public int cookTimeFresh;
    public int fuelProgress;
    public int smeltingProgress;
    public boolean outputToEnderChest;

    public ContainerEnderFurnace(EntityPlayer player, TileEntityEnderFurnace te)
    {
        super(player, te.getWrappedInventoryForContainer(player), te);
        this.teef = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 34, 17));
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 1, 34, 53));
        this.addSlotToContainer(new SlotItemHandlerFurnaceOutput(this.player, this.inventory, 2, 88, 35));
        this.customInventorySlots = new MergeSlotRange(0, this.inventorySlots.size());
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        IContainerListener listener;
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            listener = this.listeners.get(i);

            // Note: the value gets truncated to a short in non-local SMP
            if (this.teef.burnTimeRemaining != this.burnTimeRemaining
                || this.teef.burnTimeFresh != this.burnTimeFresh
                || this.teef.cookTime != this.cookTime)
            {
                int b = 0, c = 0;
                if (this.teef.burnTimeFresh != 0)
                {
                    b = 100 * this.teef.burnTimeRemaining / this.teef.burnTimeFresh;
                }
                c = 100 * this.teef.cookTime / TileEntityEnderFurnace.COOKTIME_DEFAULT;

                // smelting progress and fuel burning progress are both 0..100, we send the smelting progress in the upper byte of the short
                listener.sendProgressBarUpdate(this, 0, c << 8 | b);
            }

            if (this.teef.outputToEnderChest != this.outputToEnderChest)
            {
                listener.sendProgressBarUpdate(this, 1, this.teef.outputToEnderChest ? 1 : 0);
            }

            this.burnTimeRemaining = this.teef.burnTimeRemaining;
            this.burnTimeFresh = this.teef.burnTimeFresh;
            this.cookTime = this.teef.cookTime;
            this.outputToEnderChest = this.teef.outputToEnderChest;
        }
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        int b = 0;
        if (this.teef.burnTimeFresh != 0)
        {
            b = 100 * this.teef.burnTimeRemaining / this.teef.burnTimeFresh;
        }

        int c = 100 * this.teef.cookTime / TileEntityEnderFurnace.COOKTIME_DEFAULT;

        listener.sendProgressBarUpdate(this, 0, c << 8 | b);
        listener.sendProgressBarUpdate(this, 1, this.teef.outputToEnderChest ? 1 : 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int var, int val)
    {
        switch(var)
        {
            case 0:
                this.fuelProgress = val & 0x7F; // value is 0..100, mask it to he lowest 7 bits (0..127)
                this.smeltingProgress = MathHelper.clamp((val >>> 8) & 0x7F, 0, 100);
                break;
            case 1:
                this.outputToEnderChest = (val == 1);
                break;
            default:
        }
    }
}
