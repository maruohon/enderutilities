package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.tileentity.TileEntityInserter;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerInserter extends ContainerCustomSlotClick
{
    private final TileEntityInserter tef;
    private final IItemHandler invFilters;
    private SlotRange filterSlots = new SlotRange(0, 0);

    public ContainerInserter(EntityPlayer player, TileEntityInserter te)
    {
        super(player, te.getWrappedInventoryForContainer(player));

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
                slot.insertItem(stackCursor.copy(), false);
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
