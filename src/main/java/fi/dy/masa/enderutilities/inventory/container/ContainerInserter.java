package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerTileEntityInventory;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityInserter;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerInserter extends ContainerTileEntityInventory
{
    private final TileEntityInserter tef;
    private final IItemHandler invFilters;
    private SlotRange filterSlots = new SlotRange(0, 0);
    private int delayLast;
    private int stackLimitLast;
    private int filtersLast;
    private int redstoneLast;

    public ContainerInserter(EntityPlayer player, TileEntityInserter te)
    {
        super(player, te);

        this.tef = te;
        this.invFilters = te.getFilterInventory();
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, te.isFiltered() ? 115 : 59);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 8;
        int posY = 47;

        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 1);
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 80, 27));

        if (this.tef.isFiltered())
        {
            this.filterSlots = new SlotRange(this.inventorySlots.size(), 27);

            for (int row = 0; row < 3; row++)
            {
                for (int column = 0; column < 9; column++)
                {
                    this.addSlotToContainer(new SlotItemHandlerGeneric(this.invFilters, row * 9 + column, posX + column * 18, posY + row * 18));
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        int delay = this.tef.getUpdateDelay();
        int stackLimit = this.tef.getBaseItemHandler().getInventoryStackLimit();
        int filters = this.tef.getFilterMask();
        int redstone = this.tef.getRedstoneModeIntValue();

        for (int i = 0; i < this.listeners.size(); i++)
        {
            if (delay != this.delayLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 0, delay & 0xFFFF);
                this.listeners.get(i).sendProgressBarUpdate(this, 1, (delay >>> 16) & 0xFFFF);
            }

            if (stackLimit != this.stackLimitLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 2, stackLimit);
            }

            if (filters != this.filtersLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 3, filters);
            }

            if (redstone != this.redstoneLast)
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 4, redstone);
            }
        }

        this.delayLast = delay;
        this.stackLimitLast = stackLimit;
        this.filtersLast = filters;
        this.redstoneLast = redstone;

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        switch (id)
        {
            case 0:
                data = (this.tef.getUpdateDelay() & 0xFFFF0000) | data;
                this.tef.setUpdateDelay(data);
                break;
            case 1:
                data = (this.tef.getUpdateDelay() & 0xFFFF) | (data << 16);
                this.tef.setUpdateDelay(data);
                break;
            case 2:
                this.tef.getBaseItemHandler().setStackLimit(data);
                break;
            case 3:
                this.tef.setFilterMask(data);
                break;
            case 4:
                this.tef.setRedstoneModeFromInteger(data);
                break;
        }
    }

    protected boolean fakeSlotClick(int slotNum, int button, ClickType clickType, EntityPlayer player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackCursor = player.inventory.getItemStack();

        // Regular or shift + left click or right click
        if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1))
        {
            if (slot == null || slot.getItemHandler() != this.invFilters)
            {
                return false;
            }

            if (stackCursor != null)
            {
                ItemStack stackTmp = stackCursor.copy();
                stackTmp.stackSize = 1;
                slot.insertItem(stackTmp, false);
            }
            else
            {
                slot.putStack(null);
            }

            return true;
        }
        else if (this.isDragging)
        {
            // End of dragging
            if (clickType == ClickType.QUICK_CRAFT && (button == 2 || button == 6))
            {
                if (stackCursor != null)
                {
                    ItemStack stackTmp = stackCursor.copy();
                    stackTmp.stackSize = 1;

                    for (int i : this.draggedSlots)
                    {
                        SlotItemHandlerGeneric slotTmp = this.getSlotItemHandler(i);

                        if (slotTmp != null && slotTmp.getItemHandler() == this.invFilters)
                        {
                            slotTmp.insertItem(stackTmp, false);
                        }
                    }
                }

                this.isDragging = false;
            }
            // This gets called for each slot that was dragged over
            else if (clickType == ClickType.QUICK_CRAFT && (button == 1 || button == 5))
            {
                this.draggedSlots.add(slotNum);
            }
        }
        // Starting a left or right click drag
        else if (clickType == ClickType.QUICK_CRAFT && (button == 0 || button == 4))
        {
            this.isDragging = true;
            this.draggingRightClick = button == 4;
            this.draggedSlots.clear();
        }

        return false;
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        if (this.tef.isFiltered())
        {
            if (this.filterSlots.contains(slotNum))
            {
                this.fakeSlotClick(slotNum, dragType, clickType, player);
                return null;
            }

            // (Starting) or ending a drag and the dragged slots include at least one of our fake slots
            if (clickType == ClickType.QUICK_CRAFT && slotNum == -999)
            {
                for (int i : this.draggedSlots)
                {
                    if (this.filterSlots.contains(i))
                    {
                        this.fakeSlotClick(i, dragType, clickType, player);
                        return null;
                    }
                }
            }
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }
}
