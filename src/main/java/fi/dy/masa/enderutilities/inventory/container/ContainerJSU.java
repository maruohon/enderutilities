package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerScrollable;
import fi.dy.masa.enderutilities.tileentity.TileEntityJSU;

public class ContainerJSU extends ContainerLargeStacks implements IScrollableInventory
{
    public static final int GUI_ACTION_SCROLL_MOVE  = 0;
    public static final int GUI_ACTION_SCROLL_SET   = 1;
    protected final TileEntityJSU tejsu;
    private int startRow;

    public ContainerJSU(EntityPlayer player, TileEntityJSU te)
    {
        super(player, te.getWrappedInventoryForContainer(player));
        this.tejsu = te;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 139);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 17;

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 54);

        for (int row = 0; row < 6; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlotToContainer(new SlotItemHandlerScrollable(this.inventory, row * 9 + column, posX + column * 18, posY + row * 18, this));
            }
        }
    }

    @Override
    public int getSlotOffset()
    {
        return this.startRow * 9;
    }

    private int getMaxStartRow()
    {
        return (TileEntityJSU.INV_SIZE / 9) - 6;
    }

    private void setStartRow(int rowStart)
    {
        this.startRow = MathHelper.clamp(rowStart, 0, this.getMaxStartRow());
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SCROLL_MOVE)
        {
            this.setStartRow(this.startRow + element);
        }
        else if (action == GUI_ACTION_SCROLL_SET)
        {
            this.setStartRow(element);
        }

        this.detectAndSendChanges();
    }
}