package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.ItemPickupManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerPickupManager extends ContainerLargeStacks implements IContainerModularItem
{
    public static final int NUM_MODULE_SLOTS = 3;
    public EntityPlayer player;
    public InventoryItem inventoryItemTransmit;
    public InventoryItemModules inventoryItemModules;
    public InventoryItem inventoryItemFilters;
    protected UUID containerUUID;
    protected SlotRange filterSlots;

    public ContainerPickupManager(EntityPlayer player, ItemStack containerStack)
    {
        super(player.inventory, new InventoryItem(containerStack, 1, player.worldObj.isRemote, player, "TransmitItems"));
        this.player = player;
        this.containerUUID = NBTUtils.getUUIDFromItemStack(containerStack, "UUID", true);
        this.filterSlots = new SlotRange(0, 0);

        this.inventoryItemModules = new InventoryItemModules(containerStack, NUM_MODULE_SLOTS, player.worldObj.isRemote, player);
        this.inventoryItemModules.setHostInventory(player.inventory, this.containerUUID);
        this.inventoryItemModules.readFromContainerItemStack();

        byte preset = NBTUtils.getByte(containerStack, ItemPickupManager.TAG_NAME_CONTAINER, ItemPickupManager.TAG_NAME_PRESET_SELECTION);
        this.inventoryItemFilters = new InventoryItem(containerStack, 36, player.worldObj.isRemote, player, "FilterItems_" + preset);
        this.inventoryItemFilters.setHostInventory(player.inventory, this.containerUUID);
        this.inventoryItemFilters.setInventoryStackLimit(1);
        this.inventoryItemFilters.readFromContainerItemStack();

        this.inventoryItemTransmit = (InventoryItem)this.inventory;
        this.inventoryItemTransmit.setHostInventory(player.inventory, this.containerUUID);
        this.inventoryItemTransmit.setInventoryStackLimit(1024);
        this.inventoryItemTransmit.readFromContainerItemStack();

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int start = this.inventorySlots.size();
        int posX = 8;
        int posY = 29;

        // The item transmit slot
        this.addSlotToContainer(new SlotGeneric(this.inventoryItemTransmit, 0, 89, posY));

        this.customInventorySlots = new SlotRange(start, 1);
        start = this.inventorySlots.size();

        posY = 47;
        // Item tranport filter slots
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemFilters, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        posY = 123;
        // Inventory filter slots
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemFilters, i * 9 + j + 18, posX + j * 18, posY + i * 18));
            }
        }

        this.filterSlots = new SlotRange(start, 36);

        posX = 116;
        posY = 29;
        // The Storage Module slots
        for (int i = 0; i < NUM_MODULE_SLOTS; i++)
        {
            this.addSlotToContainer(new SlotModule(this.inventoryItemModules, i, posX + i * 18, posY, ModuleType.TYPE_LINKCRYSTAL, this));
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return InventoryUtils.getItemStackByUUID(this.player.inventory, this.containerUUID, "UUID");
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        this.inventory.closeInventory();
        this.inventoryItemModules.closeInventory();
        this.inventoryItemFilters.closeInventory();
    }

    protected boolean fakeSlotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        Slot slot = (slotNum >= 0 && slotNum < this.inventorySlots.size()) ? this.getSlot(slotNum) : null;
        ItemStack stackCursor = player.inventory.getItemStack();

        // Regular left click or right click
        if ((type == 0 || type == 1) && (button == 0 || button == 1))
        {
            if (slot == null || slot.inventory != this.inventoryItemFilters)
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
            if (type == 5 && (button == 2 || button == 6))
            {
                if (stackCursor != null)
                {
                    ItemStack stackTmp = stackCursor.copy();
                    stackTmp.stackSize = 1;

                    for (int i : this.draggedSlots)
                    {
                        if (this.getSlot(i).inventory == this.inventoryItemFilters)
                        {
                            this.getSlot(i).putStack(stackTmp.copy());
                        }
                    }
                }

                this.isDragging = false;
            }
            // This gets called for each slot that was dragged over
            else if (type == 5 && (button == 1 || button == 5))
            {
                this.draggedSlots.add(slotNum);
            }
        }
        // Starting a left or right click drag
        else if (type == 5 && (button == 0 || button == 4))
        {
            this.isDragging = true;
            this.draggingRightClick = button == 4;
            this.draggedSlots.clear();
        }

        return false;
    }

    @Override
    public ItemStack slotClick(int slotNum, int button, int type, EntityPlayer player)
    {
        if (this.isSlotInRange(this.filterSlots, slotNum) == true)
        {
            this.fakeSlotClick(slotNum, button, type, player);
            return null;
        }

        // (Starting) or ending a drag and the dragged slots include at least one of our fake slots
        if (slotNum == -999 && type == 5)
        {
            for (int i : this.draggedSlots)
            {
                if (this.isSlotInRange(this.filterSlots, i) == true)
                {
                    this.fakeSlotClick(slotNum, button, type, player);
                    return null;
                }
            }
        }

        ItemStack modularStackPre = this.getModularItem();
        ItemStack stack = super.slotClick(slotNum, button, type, player);
        ItemStack modularStackPost = this.getModularItem();

        // The Bag's stack changed after the click, re-read the inventory contents.
        if (modularStackPre != modularStackPost)
        {
            //System.out.println("slotClick() - updating container");
            this.inventoryItemTransmit.readFromContainerItemStack();
            this.inventoryItemModules.readFromContainerItemStack();
            this.inventoryItemFilters.readFromContainerItemStack();
        }

        this.detectAndSendChanges();

        return stack;
    }
}
