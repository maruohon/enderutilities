package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular implements IInventory
{
    /** The ItemStack of the item that is hosting this inventory */ 
    protected ItemStack containerStack;
    protected ItemInventoryModular containerItem;

    /** The ItemStacks containing the stored items inside the selected storage module */
    protected ItemStack[] storedItems;
    /** The ItemStacks containing the storage modules themselves */
    protected ItemStack[] storageModules;
    /** The index of the currently selected storage module */
    protected int selectedModule;

    //protected boolean dirty;
    public boolean isRemote;

    public InventoryItemModular(ItemStack containerStack, World world)
    {
        this.isRemote = world.isRemote;
        this.updateContainerItems(containerStack);
    }

    public void updateContainerItems(ItemStack containerStack)
    {
        this.containerStack = containerStack;

        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            this.containerItem = (ItemInventoryModular)containerStack.getItem();
            this.selectedModule = UtilItemModular.getStoredModuleSelection(this.containerStack, ModuleType.TYPE_MEMORY_CARD);
        }
        else
        {
            this.containerItem = null;
            this.selectedModule = -1;
        }

        this.readStorageModulesFromContainerItem();
        this.readItemsFromStorageModule();
    }

    /**
     * Saves the inventory and/or the storage modules to the container item
     * and then updates the inventory contents depending on which slot was changed.
     */
    protected void saveInventoryAndUpdate(int slotNum)
    {
        EnderUtilities.logger.info("InventoryItemModular.saveInventoryAndUpdate(" + slotNum + ")"); // FIXME debug
        if (this.slotIsItemInventory(slotNum) == true)
        {
            this.writeItemsToStorageModule();
            this.writeStorageModulesToContainerItem();
        }
        else if (this.slotIsStorageModule(slotNum) == true)
        {
            this.writeStorageModulesToContainerItem();
            this.readItemsFromStorageModule();
        }
    }

    /**
     * Initializes the inventory's ItemStack storage and reads the ItemStacks from the selected storage module
     */
    private void readStorageModulesFromContainerItem()
    {
        EnderUtilities.logger.info("InventoryItemModular.readStorageModulesFromContainerItem()"); // FIXME debug
        if (this.containerStack != null)
        {
            this.storageModules = new ItemStack[this.containerItem.getMaxModules(this.containerStack, ModuleType.TYPE_MEMORY_CARD)];
            UtilItemModular.readItemsFromContainerItem(this.containerStack, this.storageModules);
        }
        else
        {
            this.storageModules = new ItemStack[0];
        }
    }

    /**
     * Initializes the inventory's ItemStack storage and reads the ItemStacks from the selected storage module
     */
    private void readItemsFromStorageModule()
    {
        EnderUtilities.logger.info("InventoryItemModular.readItemsFromStorageModule()"); // FIXME debug
        if (this.containerStack != null && this.containerItem != null)
        {
            this.storedItems = new ItemStack[this.containerItem.getSizeInventory(this.containerStack)];
        }
        else
        {
            this.storedItems = new ItemStack[0];
        }

        if (this.getStorageModuleStack() != null)
        {
            UtilItemModular.readItemsFromContainerItem(this.getStorageModuleStack(), this.storedItems);
        }
    }

    /**
     * Returns the number of storage module slots
     */
    public int getStorageModuleCount()
    {
        return this.storageModules.length;
    }

    /**
     * Returns the index (0..n) of the currently selected storage module.
     * Will return -1 if there is currently no container item present.
     */
    public int getSelectedStorageModule()
    {
        return this.selectedModule;
    }

    /**
     * Returns whether the given slotNum belongs to a storage module slot
     */
    public boolean slotIsStorageModule(int slotNum)
    {
        return slotNum >= this.storedItems.length && (slotNum - this.storedItems.length) < this.storageModules.length;
    }

    /**
     * Returns whether the given slotNum belongs to a slot inside the regular item inventory
     */
    public boolean slotIsItemInventory(int slotNum)
    {
        return slotNum < this.storedItems.length;
    }

    protected ItemStack getStorageModuleStack()
    {
        if (this.storageModules != null && this.selectedModule < this.storageModules.length)
        {
            return this.storageModules[this.selectedModule];
        }
        return null;
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    protected void writeStorageModulesToContainerItem()
    {
        if (this.containerStack != null)
        {
            EnderUtilities.logger.info("InventoryItemModular.writeStorageModulesToContainerItem(): writing modules to bag..."); // FIXME debug
            UtilItemModular.writeItemsToContainerItem(this.containerStack, this.storageModules);
        }
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    protected void writeItemsToStorageModule()
    {
        if (this.getStorageModuleStack() != null)
        {
            EnderUtilities.logger.info("InventoryItemModular.writeItemsToStorageModule(): writing items to module..."); // FIXME debug
            UtilItemModular.writeItemsToContainerItem(this.getStorageModuleStack(), this.storedItems);
            //UtilItemModular.setModuleStackBySlotNumber(this.containerStack, this.selectedModule, this.getStorageModuleStack());
        }

        /*if (this.containerStack != null)
        {
            EnderUtilities.logger.info("writeItemsToStorageModule(): writing modules to bag..."); // FIXME debug
            UtilItemModular.writeItemsToContainerItem(this.containerStack, this.storageModules);
        }*/
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
        if (this.getStorageModuleStack() != null && this.getStorageModuleStack().hasDisplayName())
        {
            return true;
        }

        return this.containerStack != null ? this.containerStack.hasDisplayName() : false;
    }

    @Override
    public String getInventoryName()
    {
        // First check if the selected storage module has been named, and use that name
        if (this.getStorageModuleStack() != null && this.getStorageModuleStack().hasDisplayName())
        {
            return this.getStorageModuleStack().getDisplayName();
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
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        EnderUtilities.logger.info("InventoryItemModular.getStackInSlotOnClosing(" + slotNum + ")"); // FIXME debug
        ItemStack stack;
        if (slotNum < this.storedItems.length)
        {
            stack = this.storedItems[slotNum];
            this.storedItems[slotNum] = null;
        }
        else
        {
            slotNum -= this.storedItems.length;
            if (slotNum < this.storageModules.length)
            {
                stack = this.storageModules[slotNum];
                this.storageModules[slotNum] = null;
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
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum < this.storedItems.length)
        {
            return this.storedItems[slotNum];
        }

        slotNum -= this.storedItems.length;

        if (slotNum < this.storageModules.length)
        {
            return this.storageModules[slotNum];
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
        int origSlotNum = slotNum;
        EnderUtilities.logger.info("InventoryItemModular.decrStackSize(" + slotNum + ", " + maxAmount + ")"); // FIXME debug
        ItemStack[] stacks;
        ItemStack stack = null;

        if (slotNum < this.storedItems.length)
        {
            stacks = this.storedItems;
        }
        else
        {
            if ((slotNum - this.storedItems.length) < this.storageModules.length)
            {
                stacks = this.storageModules;
                slotNum -= this.storedItems.length;
            }
            else
            {
                EnderUtilities.logger.warn("InventoryItemModular.decrStackSize(): Invalid slot number: " + slotNum);
                return null;
            }
        }

        if (stacks[slotNum] != null)
        {
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
        }

        this.saveInventoryAndUpdate(origSlotNum);

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        int origSlotNum = slotNum;
        EnderUtilities.logger.info("InventoryItemModular.setInventorySlotContents(" + slotNum + ", " + newStack + ")"); // FIXME debug
        if (slotNum < this.storedItems.length)
        {
            this.storedItems[slotNum] = newStack;
        }
        else
        {
            slotNum -= this.storedItems.length;

            if (slotNum < this.storageModules.length)
            {
                this.storageModules[slotNum] = newStack;
            }
            else
            {
                EnderUtilities.logger.warn("InventoryItemModular.setInventorySlotContents(): Invalid slot number: " + slotNum);
            }
        }

        this.saveInventoryAndUpdate(origSlotNum);
    }

    @Override
    public void markDirty()
    {
        //EnderUtilities.logger.info("InventoryItemModular.markDirty()"); // FIXME debug
        //this.dirty = true;
        //this.writeItemsToStorageModule();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
        EnderUtilities.logger.info("InventoryItemModular.openInventory()"); // FIXME debug
    }

    @Override
    public void closeInventory()
    {
        EnderUtilities.logger.info("InventoryItemModular.closeInventory()"); // FIXME debug
        //this.writeItemsToStorageModule();
        //this.writeStorageModulesToContainerItem();
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        // Can only store items when there is a valid storage module (= Memory Card) installed and currently selected
        return this.getStorageModuleStack() != null;
    }

}
