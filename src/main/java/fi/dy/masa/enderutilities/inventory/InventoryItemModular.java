package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class InventoryItemModular implements IInventory
{
    /** The ItemStack of the actual modular item */ 
    private ItemStack containerStack;
    private ItemInventoryModular containerItem;
    /** The ItemStack of the selected storage module inside the containerStack. Can be null! */
    private ItemStack storageStack;

    /** The ItemStacks containing the stored items inside the selected storage module */
    private ItemStack[] itemStacks;
    /** The ItemStacks containing the storage modules themselves */
    private ItemStack[] moduleStacks;

    private boolean dirty;

    public InventoryItemModular(ItemStack containerStack)
    {
        this.updateContainerItems(containerStack);
    }

    private void updateContainerItems(ItemStack containerStack)
    {
        this.containerStack = containerStack;

        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            this.containerItem = (ItemInventoryModular)containerStack.getItem();

            this.storageStack = this.containerItem.getSelectedModuleStack(this.containerStack, ModuleType.TYPE_MEMORY_CARD);
            this.readItemsFromStorageModule();
            this.readStorageModulesFromContainerItem();
        }
        else
        {
            this.containerItem = null;
            this.storageStack = null;
        }
    }

    public int getStorageModuleCount()
    {
        return this.moduleStacks.length;
    }

    /**
     * Initializes the inventory's ItemStack storage and reads the ItemStacks from the selected storage module
     */
    private void readStorageModulesFromContainerItem()
    {
        this.moduleStacks = new ItemStack[this.containerItem.getMaxModules(this.containerStack, ModuleType.TYPE_MEMORY_CARD)];

        if (this.containerStack != null)
        {
            InventoryUtils.readItemsFromContainerItem(this.containerStack, this.moduleStacks);
        }
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    private void writeStorageModulesToContainerItem()
    {
        //System.out.println("writeStorageModulesToContainerItem()");
        if (this.containerStack != null)
        {
            InventoryUtils.writeItemsToContainerItem(this.containerStack, this.moduleStacks);
        }
    }

    /**
     * Initializes the inventory's ItemStack storage and reads the ItemStacks from the selected storage module
     */
    private void readItemsFromStorageModule()
    {
        this.itemStacks = new ItemStack[this.containerItem.getSizeInventory(this.containerStack)];

        if (this.storageStack != null)
        {
            InventoryUtils.readItemsFromContainerItem(this.storageStack, this.itemStacks);
        }
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    private void writeItemsToStorageModule()
    {
        if (this.storageStack != null)
        {
            //System.out.println("writeItemsToStorageModule()");
            InventoryUtils.writeItemsToContainerItem(this.storageStack, this.itemStacks);
        }

        //this.containerItem.setSelectedModuleStack(this.containerStack, ModuleType.TYPE_MEMORY_CARD, this.storageStack);
    }

    /**
     * Returns the ItemStack holding the container item
     */
    public ItemStack getContainerItemStack()
    {
        return this.containerStack;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        if (this.storageStack != null && this.storageStack.hasDisplayName())
        {
            return true;
        }

        return this.containerStack != null ? this.containerStack.hasDisplayName() : false;
    }

    @Override
    public String getInventoryName()
    {
        // First check if the selected storage module has been named, and use that name
        if (this.storageStack != null && this.storageStack.hasDisplayName())
        {
            return this.storageStack.getDisplayName();
        }

        // If the storage module didn't have a name, but the item itself does, then use that name
        if (this.containerStack != null && this.containerStack.hasDisplayName())
        {
            return this.containerStack.getDisplayName();
        }

        return Reference.MOD_ID + "container." + this.containerItem != null ? this.containerItem.name : "null";
    }

    @Override
    public int getSizeInventory()
    {
        int size = 0;
        if (this.containerItem != null)
        {
            // The actual inventory size for stored items
            size += this.containerItem.getSizeInventory(this.containerStack);
            // The number of slots for the storage modules (= Memory Cards)
            size += this.containerItem.getMaxModules(this.containerStack, ModuleType.TYPE_MEMORY_CARD);
        }
        return size;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.containerItem != null ? this.containerItem.getInventoryStackLimit(this.containerStack) : 64;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum < this.itemStacks.length)
        {
            return this.itemStacks[slotNum];
        }

        slotNum -= this.itemStacks.length;

        if (slotNum < this.moduleStacks.length)
        {
            return this.moduleStacks[slotNum];
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItemModular.getStackInSlot(): Invalid slot number: " + slotNum);
        }

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        ItemStack[] stacks;
        if (slotNum < this.itemStacks.length)
        {
            stacks = this.itemStacks;
        }
        else
        {
            if ((slotNum - this.itemStacks.length) < this.moduleStacks.length)
            {
                stacks = this.moduleStacks;
                slotNum -= this.itemStacks.length;
            }
            else
            {
                EnderUtilities.logger.warn("InventoryItemModular.decrStackSize(): Invalid slot number: " + slotNum);
                return null;
            }
        }

        if (stacks[slotNum] != null)
        {
            ItemStack stack;

            if (stacks[slotNum].stackSize >= maxAmount)
            {
                stack = stacks[slotNum].splitStack(maxAmount);

                if (stacks[slotNum].stackSize <= 0)
                {
                    stacks[slotNum] = null;
                }
            }
            else
            {
                stack = stacks[slotNum];
                stacks[slotNum] = null;
            }

            //this.writeItemsToStorageModule();
            return stack;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        ItemStack stack;
        if (slotNum < this.itemStacks.length)
        {
            stack = this.itemStacks[slotNum];
            this.itemStacks[slotNum] = null;
        }
        else
        {
            slotNum -= this.itemStacks.length;
            if (slotNum < this.moduleStacks.length)
            {
                stack = this.moduleStacks[slotNum];
                this.moduleStacks[slotNum] = null;
            }
            else
            {
                EnderUtilities.logger.warn("InventoryItemModular.getStackInSlotOnClosing(): Invalid slot number: " + slotNum);
                return null;
            }
        }

        //this.writeItemsToStorageModule();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        if (slotNum < this.itemStacks.length)
        {
            this.itemStacks[slotNum] = newStack;
        }
        else
        {
            slotNum -= this.itemStacks.length;

            if (slotNum < this.moduleStacks.length)
            {
                this.moduleStacks[slotNum] = newStack;
            }
            else
            {
                EnderUtilities.logger.warn("InventoryItemModular.setInventorySlotContents(): Invalid slot number: " + slotNum);
            }
        }

        //this.writeItemsToStorageModule();
    }

    @Override
    public void markDirty()
    {
        this.dirty = true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
        //System.out.println("closeInventory()");
        this.writeItemsToStorageModule();
        this.writeStorageModulesToContainerItem();
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        // Can only store items when there is a valid storage module (= Memory Card) installed and currently selected
        return this.storageStack != null;
    }

}
