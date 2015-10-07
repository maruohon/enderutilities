package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.SlotFurnace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerTileEntityInventory
{
    private TileEntityEnderFurnace teef;
    public int burnTimeRemaining;
    public int burnTimeFresh;
    public int cookTime;
    public int cookTimeFresh;
    public int fuelProgress;
    public int smeltingProgress;
    public int outputBufferAmount;
    public boolean outputToEnderChest;

    public ContainerEnderFurnace(InventoryPlayer inventoryPlayer, TileEntityEnderFurnace te)
    {
        super(inventoryPlayer, te);
        this.teef = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.addSlotToContainer(new SlotGeneric(this.inventory, 0, 34, 17));
        this.addSlotToContainer(new SlotGeneric(this.inventory, 1, 34, 53));
        this.addSlotToContainer(new SlotFurnace(this.inventoryPlayer.player, this.inventory, 2, 88, 35));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        ICrafting icrafting;
        for (int i = 0; i < this.crafters.size(); ++i)
        {
            icrafting = (ICrafting)this.crafters.get(i);

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
                icrafting.sendProgressBarUpdate(this, 0, c << 8 | b);
            }

            if (this.teef.getOutputBufferAmount() != this.outputBufferAmount)
            {
                icrafting.sendProgressBarUpdate(this, 1, this.teef.getOutputBufferAmount());
            }

            if (this.teef.outputToEnderChest != this.outputToEnderChest)
            {
                icrafting.sendProgressBarUpdate(this, 2, this.teef.outputToEnderChest ? 1 : 0);
            }

            this.burnTimeRemaining = this.teef.burnTimeRemaining;
            this.burnTimeFresh = this.teef.burnTimeFresh;
            this.cookTime = this.teef.cookTime;
            this.outputBufferAmount = this.teef.getOutputBufferAmount();
            this.outputToEnderChest = this.teef.outputToEnderChest;
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting icrafting)
    {
        super.addCraftingToCrafters(icrafting);

        int b = 0;
        if (this.teef.burnTimeFresh != 0)
        {
            b = 100 * this.teef.burnTimeRemaining / this.teef.burnTimeFresh;
        }

        int c = 100 * this.teef.cookTime / TileEntityEnderFurnace.COOKTIME_DEFAULT;

        icrafting.sendProgressBarUpdate(this, 0, c << 8 | b);
        icrafting.sendProgressBarUpdate(this, 1, this.teef.getOutputBufferAmount());
        icrafting.sendProgressBarUpdate(this, 2, this.teef.outputToEnderChest ? 1 : 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int var, int val)
    {
        switch(var)
        {
            case 0:
                this.fuelProgress = val & 0x7F; // value is 0..100, mask it to he lowest 7 bits (0..127)
                this.smeltingProgress = (val >> 8) & 0x7F;
                break;
            case 1:
                this.outputBufferAmount = val;
                break;
            case 2:
                this.outputToEnderChest = (val == 1);
                break;
            default:
        }
    }
}
