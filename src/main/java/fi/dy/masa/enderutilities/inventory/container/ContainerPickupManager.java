package fi.dy.masa.enderutilities.inventory.container;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModules;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotModuleModularItem;
import fi.dy.masa.enderutilities.item.ItemPickupManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSyncSlot;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ContainerPickupManager extends ContainerLargeStacks implements IContainerItem
{
    // Note: This includes the capacitor, which is not accessible through the GUI though
    public static final int NUM_MODULES = 4;
    public static final int NUM_LINK_CRYSTAL_SLOTS = 3;
    public InventoryItem inventoryItemTransmit;
    public InventoryItemModules inventoryItemModules;
    public InventoryItem inventoryItemFilters;
    protected UUID containerUUID;
    protected SlotRange filterSlots;

    public ContainerPickupManager(EntityPlayer player, ItemStack containerStack)
    {
        super(player, new InventoryItem(containerStack, 1, 1024, true, player.worldObj.isRemote, player, ItemPickupManager.TAG_NAME_TX_INVENTORY));
        this.containerUUID = NBTUtils.getUUIDFromItemStack(containerStack, "UUID", true);
        this.filterSlots = new SlotRange(0, 0);

        this.inventoryItemModules = new InventoryItemModules(containerStack, NUM_MODULES, player.worldObj.isRemote, player);
        this.inventoryItemModules.setHostInventory(new PlayerInvWrapper(player.inventory), this.containerUUID);
        this.inventoryItemModules.readFromContainerItemStack();

        byte preset = NBTUtils.getByte(containerStack, ItemPickupManager.TAG_NAME_CONTAINER, ItemPickupManager.TAG_NAME_PRESET_SELECTION);
        this.inventoryItemFilters = new InventoryItem(containerStack, 36, 1, false, player.worldObj.isRemote, player, ItemPickupManager.TAG_NAME_FILTER_INVENTORY_PRE + preset);
        this.inventoryItemFilters.setHostInventory(new PlayerInvWrapper(player.inventory), this.containerUUID);
        this.inventoryItemFilters.readFromContainerItemStack();

        this.inventoryItemTransmit = (InventoryItem)this.inventory;
        this.inventoryItemTransmit.setHostInventory(new PlayerInvWrapper(player.inventory), this.containerUUID);
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
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryItemTransmit, 0, 89, posY));

        this.customInventorySlots = new MergeSlotRange(start, 1);
        start = this.inventorySlots.size();

        posY = 47;
        // Item tranport filter slots
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryItemFilters, i * 9 + j, posX + j * 18, posY + i * 18));
            }
        }

        posY = 123;
        // Inventory filter slots
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventoryItemFilters, i * 9 + j + 18, posX + j * 18, posY + i * 18));
            }
        }

        this.filterSlots = new SlotRange(start, 36);

        posX = 116;
        posY = 29;
        start = this.inventorySlots.size();
        // The Storage Module slots
        int first = UtilItemModular.getFirstIndexOfModuleType(this.inventoryItemModules.getContainerItemStack(), ModuleType.TYPE_LINKCRYSTAL);
        for (int slot = first, i = 0; i < NUM_LINK_CRYSTAL_SLOTS; slot++, i++)
        {
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItemModules, slot, posX + i * 18, posY, ModuleType.TYPE_LINKCRYSTAL, this));
        }

        this.addMergeSlotRangePlayerToExt(start, NUM_LINK_CRYSTAL_SLOTS);
    }

    @Override
    public ItemStack getContainerItem()
    {
        return InventoryUtils.getItemStackByUUID(new PlayerMainInvWrapper(this.player.inventory), this.containerUUID, "UUID");
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    protected boolean fakeSlotClick(int slotNum, int button, ClickType clickType, EntityPlayer player)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);
        ItemStack stackCursor = player.inventory.getItemStack();

        // Regular or shift + left click or right click
        if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1))
        {
            if (slot == null || slot.getItemHandler() != this.inventoryItemFilters)
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
                        if (slotTmp != null && slotTmp.getItemHandler() == this.inventoryItemFilters)
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
        if (this.isSlotInRange(this.filterSlots, slotNum) == true)
        {
            this.fakeSlotClick(slotNum, dragType, clickType, player);
            return null;
        }

        // (Starting) or ending a drag and the dragged slots include at least one of our fake slots
        if (clickType == ClickType.QUICK_CRAFT && slotNum == -999)
        {
            for (int i : this.draggedSlots)
            {
                if (this.isSlotInRange(this.filterSlots, i) == true)
                {
                    this.fakeSlotClick(i, dragType, clickType, player);
                    return null;
                }
            }
        }

        ItemStack modularStackPre = this.getContainerItem();

        ItemStack stack = super.slotClick(slotNum, dragType, clickType, player);

        ItemStack modularStackPost = this.getContainerItem();

        if (player.worldObj.isRemote == false && modularStackPost != null && modularStackPost.getItem() == EnderUtilitiesItems.pickupManager)
        {
            boolean sent = ((ItemPickupManager)modularStackPost.getItem()).tryTransportItemsFromTransportSlot(this.inventoryItemTransmit, player, modularStackPost);

            // The change is not picked up by detectAndSendChanges() because the items are transported out
            // immediately, so the client side container will get out of sync without a forced sync
            if (sent == true && player instanceof EntityPlayerMP)
            {
                PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, 0, this.getSlot(0).getStack()), (EntityPlayerMP)player);
            }
        }

        // The modular stack changed after the click, re-read the inventory contents.
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
