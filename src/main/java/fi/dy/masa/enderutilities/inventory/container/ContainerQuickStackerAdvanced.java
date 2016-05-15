package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerQuickStackerAdvanced extends ContainerTileEntityInventory
{
    private final IItemHandler inventoryFilters;
    private SlotRange filterSlots = new SlotRange(0, 0);
    public int activeModulesMask;
    public int selectedPreset;

    public ContainerQuickStackerAdvanced(EntityPlayer player, TileEntityQuickStackerAdvanced te)
    {
        super(player, te);

        this.inventoryFilters = te.getFilterInventory();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(43, 163);
        this.addOffhandSlot(7, 221);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 42;
        int posY = 36;
        int start = this.inventorySlots.size();

        // Filter slots, group 1
        for (int row = 0; row < 2; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryFilters, row * 9 + column, posX + column * 18, posY + row * 18));
            }
        }

        posY = 94;
        // Filter slots, group 2
        for (int row = 0; row < 2; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryFilters, row * 9 + column + 18, posX + column * 18, posY + row * 18));
            }
        }

        this.filterSlots = new SlotRange(start, 36);

        posX = 7;
        posY = 18;
        start = this.inventorySlots.size();

        // The Link Crystal slots
        for (int slot = 0; slot < TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.inventory, slot, posX, posY + slot * 18, ModuleType.TYPE_LINKCRYSTAL));
        }

        this.addMergeSlotRangePlayerToExt(start, TileEntityQuickStackerAdvanced.NUM_LINK_CRYSTALS);
    }

    protected boolean fakeSlotClick(int slotNum, int button, ClickType clickType, EntityPlayer player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackCursor = player.inventory.getItemStack();

        // Regular or shift + left click or right click
        if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1))
        {
            if (slot == null || slot.getItemHandler() != this.inventoryFilters)
            {
                return false;
            }

            if (stackCursor != null)
            {
                ItemStack stackTmp = stackCursor.copy();
                stackTmp.stackSize = 1;
                slot.putStack(stackTmp);
            }
            else
            {
                slot.putStack(null);
            }

            return true;
        }
        else if (this.isDragging == true)
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
                        if (slotTmp != null && slotTmp.getItemHandler() == this.inventoryFilters)
                        {
                            slotTmp.putStack(stackTmp.copy());
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
        if (this.filterSlots.contains(slotNum) == true)
        {
            this.fakeSlotClick(slotNum, dragType, clickType, player);
            return null;
        }

        // (Starting) or ending a drag and the dragged slots include at least one of our fake slots
        if (clickType == ClickType.QUICK_CRAFT && slotNum == -999)
        {
            for (int i : this.draggedSlots)
            {
                if (this.filterSlots.contains(i) == true)
                {
                    this.fakeSlotClick(i, dragType, clickType, player);
                    return null;
                }
            }
        }

        return super.slotClick(slotNum, dragType, clickType, player);
    }
}
