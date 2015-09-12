package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular implements IInventory
{
    protected EntityPlayer player;
    public boolean isRemote;

    protected UUID containerItemUUID;
    /** The ItemStacks containing the stored items inside the selected storage module */
    protected ItemStack[] storedItems;
    /** The ItemStacks containing the storage modules themselves */
    protected ItemStack[] storageModules;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player)
    {
        this.player = player;
        this.isRemote = player.worldObj.isRemote;
        this.containerItemUUID = NBTUtils.getOrCreateUUIDFromItemStack(containerStack, "UUID");

        // Call these directly here so that the ItemStack arrays get created on the client side
        this.readStorageModulesFromContainerItem();
        this.readItemsFromStorageModule();
    }

    /**
     * (Re-)reads all the inventory contents from the container item and storage module
     */
    public void updateContainerItems()
    {
        if (this.isRemote == true)
        {
            return;
        }

        this.readStorageModulesFromContainerItem();
        this.readItemsFromStorageModule();
    }

    public UUID getContainerUUID()
    {
        return this.containerItemUUID;
    }

    /**
     * Saves the inventory and/or the storage modules to the container item
     * and then updates the inventory contents depending on which slot was changed.
     */
    protected void saveInventoryAndUpdate(int slotNum)
    {
        if (this.isRemote == true)
        {
            return;
        }

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
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof IModular)
        {
            this.storageModules = new ItemStack[((IModular)containerStack.getItem()).getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD)];
            UtilItemModular.readItemsFromContainerItem(containerStack, this.storageModules);
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
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            this.storedItems = new ItemStack[((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack)];
        }
        else
        {
            this.storedItems = new ItemStack[0];
        }

        ItemStack storageStack = this.getStorageModuleStack();
        if (storageStack != null)
        {
            NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(storageStack);
            if (ownerData == null || ownerData.canAccess(this.player) == true)
            {
                UtilItemModular.readItemsFromContainerItem(storageStack, this.storedItems);
            }
        }
    }

    /**
     * Returns the number of storage module slots
     */
    public int getStorageModuleSlotCount()
    {
        return this.storageModules.length;
    }

    /**
     * Returns the index (0..n) of the currently selected storage module.
     * Will return -1 if there is currently no container item present.
     */
    public int getSelectedStorageModuleIndex()
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            return UtilItemModular.getStoredModuleSelection(containerStack, ModuleType.TYPE_MEMORY_CARD);
        }

        return -1;
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

    /**
     * Returns whether the item inventory is accessible.
     */
    public boolean isItemInventoryAccessible()
    {
        // Can only store items when there is a valid storage module (= Memory Card) installed
        // and currently selected and the player has access rights to it.
        ItemStack storageStack = this.getStorageModuleStack();
        if (storageStack == null)
        {
            return false;
        }

        NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(storageStack);
        return ownerData == null || ownerData.canAccess(this.player) == true;
    }

    /**
     * Returns whether the storage module slots are accessible.
     * Used while rendering to render the slots as darker when the opened bag is not accessible.
     */
    public boolean isModuleInventoryAccessible()
    {
        return this.getContainerItemStack() != null;
    }

    protected ItemStack getStorageModuleStack()
    {
        int index = this.getSelectedStorageModuleIndex();
        if (this.storageModules != null && index >= 0 && index < this.storageModules.length)
        {
            return this.storageModules[index];
        }

        return null;
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    protected void writeStorageModulesToContainerItem()
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null)
        {
            UtilItemModular.writeItemsToContainerItem(containerStack, this.storageModules);
        }
    }

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    protected void writeItemsToStorageModule()
    {
        if (this.getStorageModuleStack() != null)
        {
            UtilItemModular.writeItemsToContainerItem(this.getStorageModuleStack(), this.storedItems);
        }
    }

    /**
     * Returns the ItemStack holding the container item
     */
    public ItemStack getContainerItemStack()
    {
        return InventoryUtils.getItemStackByUUID(this.player.inventory, this.containerItemUUID, "UUID");
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        if (this.getStorageModuleStack() != null && this.getStorageModuleStack().hasDisplayName())
        {
            return true;
        }

        ItemStack containerStack = this.getContainerItemStack();
        return containerStack != null ? containerStack.hasDisplayName() : false;
    }

    @Override
    public String getInventoryName()
    {
        // First check if the selected storage module has been named, and use that name
        if (this.getStorageModuleStack() != null && this.getStorageModuleStack().hasDisplayName())
        {
            return this.getStorageModuleStack().getDisplayName();
        }

        ItemStack containerStack = this.getContainerItemStack();
        // If the storage module didn't have a name, but the item itself does, then use that name
        if (containerStack != null && containerStack.hasDisplayName())
        {
            return containerStack.getDisplayName();
        }

        if (containerStack.getItem() instanceof ItemEnderUtilities)
        {
            return Reference.MOD_ID + ".container." + ((ItemEnderUtilities)containerStack.getItem()).name;
        }

        return Reference.MOD_ID + ".container.null";
    }

    @Override
    public int getSizeInventory()
    {
        int size = 0;
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular && containerStack.getItem() instanceof IModular)
        {
            // The actual inventory size for stored items
            size += ((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack);
            // The number of slots for the storage modules (= Memory Cards)
            size += ((IModular)containerStack.getItem()).getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
        }
        return size;
    }

    @Override
    public int getInventoryStackLimit()
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            return ((ItemInventoryModular)containerStack.getItem()).getInventoryStackLimit(containerStack);
        }

        return 64;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
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
                //EnderUtilities.logger.warn("InventoryItemModular.getStackInSlotOnClosing(): Invalid slot number: " + slotNum);
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
            //EnderUtilities.logger.warn("InventoryItemModular.getStackInSlot(): Invalid slot number: " + slotNum);
        }

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        int origSlotNum = slotNum;
        //EnderUtilities.logger.info("InventoryItemModular.decrStackSize(" + slotNum + ", " + maxAmount + ")");
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
                //EnderUtilities.logger.warn("InventoryItemModular.decrStackSize(): Invalid slot number: " + slotNum);
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
                //EnderUtilities.logger.warn("InventoryItemModular.setInventorySlotContents(): Invalid slot number: " + slotNum);
            }
        }

        this.saveInventoryAndUpdate(origSlotNum);
    }

    @Override
    public void markDirty()
    {
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
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        // If the given slot is a storage module slot, check that the item is a valid Memory Card module
        if (this.slotIsStorageModule(slotNum) == true)
        {
            return (stack.getItem() instanceof IModule && ((IModule)stack.getItem()).getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD));
        }

        // Don't allow putting the bag inside itself
        if (this.containerItemUUID.equals(NBTUtils.getUUIDFromItemStack(stack, "UUID")))
        {
            return false;
        }

        // Item inventory slots, check if the inventory can be accessed
        return this.isItemInventoryAccessible();
    }

}
