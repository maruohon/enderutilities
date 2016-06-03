package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.util.SlotRange;

public class ContainerQuickStackerAdvanced extends ContainerTileEntityInventory
{
    private final TileEntityQuickStackerAdvanced teqsa;
    private final IItemHandler inventoryFilters;
    private SlotRange filterSlots = new SlotRange(0, 0);
    protected short[] valuesLast = new short[4];
    protected long enabledSlotsMask;

    public ContainerQuickStackerAdvanced(EntityPlayer player, TileEntityQuickStackerAdvanced te)
    {
        super(player, te);

        this.teqsa = te;
        this.inventoryFilters = te.getFilterInventory();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(23, 174);
        this.addOffhandSlot(5, 156);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int posX = 23;
        int posY = 17;

        posX = 23;
        posY = 17;
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), this.inventory.getSlots());

        // The Link Crystal slots
        for (int slot = 0; slot < 9; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.inventory, slot, posX + slot * 18, posY, ModuleType.TYPE_LINKCRYSTAL));
        }

        posY = 35;
        // The Memory Card slots
        for (int slot = 0; slot < 9; slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerModule(this.inventory, slot + 9, posX + slot * 18, posY, ModuleType.TYPE_MEMORY_CARD_MISC));
        }

        this.filterSlots = new SlotRange(this.inventorySlots.size(), this.inventoryFilters.getSlots());

        posY = 83;

        // Filter slots
        for (int row = 0; row < 4; row++)
        {
            for (int column = 0; column < 9; column++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryFilters, row * 9 + column, posX + column * 18, posY + row * 18));
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
            if (slot == null || slot.getItemHandler() != this.inventoryFilters ||
                this.teqsa.isInventoryAccessible(player) == false)
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
                        if (slotTmp != null && slotTmp.getItemHandler() == this.inventoryFilters &&
                            this.teqsa.isInventoryAccessible(player))
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

        if (player instanceof EntityPlayerMP)
        {
            this.detectAndSendChanges();
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
        // Middle click
        else if (clickType == ClickType.CLONE && dragType == 2)
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;
            if (invSlotNum == -1 || (invSlotNum >= 36 && invSlotNum != 40))
            {
                return null;
            }

            long mask = this.teqsa.getEnabledSlotsMask();
            mask ^= (0x1L << invSlotNum);
            this.teqsa.setEnabledSlotsMask(mask);

            return null;
        }

        ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);

        if (player instanceof EntityPlayerMP)
        {
            this.detectAndSendChanges();
        }

        return stack;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);

        listener.sendProgressBarUpdate(this, 0, this.teqsa.isAreaMode() ? 1 : 0);
        listener.sendProgressBarUpdate(this, 1, this.teqsa.getAreaModeSettings());
        listener.sendProgressBarUpdate(this, 2, this.teqsa.getEnabledTargetsMask());
        listener.sendProgressBarUpdate(this, 3, this.teqsa.getSelectedTarget());

        long mask = this.teqsa.getEnabledSlotsMask();
        listener.sendProgressBarUpdate(this, 4, (short)((mask >>> 32) & 0xFFFF));
        listener.sendProgressBarUpdate(this, 5, (short)((mask >>> 16) & 0xFFFF));
        listener.sendProgressBarUpdate(this, 6, (short)(mask & 0xFFFF));
    }

    @Override
    public void detectAndSendChanges()
    {
        byte areaMode = this.teqsa.isAreaMode() ? (byte)1 : (byte)0;
        long mask = this.teqsa.getEnabledSlotsMask();

        for (int i = 0; i < this.listeners.size(); i++)
        {
            if (areaMode != this.valuesLast[0])
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 0, areaMode);
            }

            if (this.teqsa.getAreaModeSettings() != this.valuesLast[1])
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 1, this.teqsa.getAreaModeSettings());
            }

            if (this.teqsa.getEnabledTargetsMask() != this.valuesLast[2])
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 2, this.teqsa.getEnabledTargetsMask());
            }

            if (this.teqsa.getSelectedTarget() != this.valuesLast[3])
            {
                this.listeners.get(i).sendProgressBarUpdate(this, 3, this.teqsa.getSelectedTarget());
            }

            if (mask != this.enabledSlotsMask)
            {
                // In multiplayer the data can only be a short
                this.listeners.get(i).sendProgressBarUpdate(this, 4, (short)((mask >>> 32) & 0xFFFF));
                this.listeners.get(i).sendProgressBarUpdate(this, 5, (short)((mask >>> 16) & 0xFFFF));
                this.listeners.get(i).sendProgressBarUpdate(this, 6, (short)(mask & 0xFFFF));
            }
        }

        this.valuesLast[0] = areaMode;
        this.valuesLast[1] = this.teqsa.getAreaModeSettings();
        this.valuesLast[2] = this.teqsa.getEnabledTargetsMask();
        this.valuesLast[3] = this.teqsa.getSelectedTarget();
        this.enabledSlotsMask = mask;

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);

        switch (id)
        {
            case 0:
                this.teqsa.setIsAreaMode(data != 0);
                break;
            case 1:
                this.teqsa.setAreaModeSettings((byte)data);
                break;
            case 2:
                this.teqsa.setEnabledTargetsMask((short)data);
                break;
            case 3:
                this.teqsa.setSelectedTarget((byte)data);
                break;
            case 4:
                this.enabledSlotsMask = (((long)data) << 32) & 0xFFFF00000000L;
                break;
            case 5:
                this.enabledSlotsMask |= (((long)data) << 16) & 0xFFFF0000L;
                break;
            case 6:
                this.enabledSlotsMask |= ((long)data) & 0xFFFFL;
                this.teqsa.setEnabledSlotsMask(this.enabledSlotsMask);
                break;
            default:
        }
    }
}
