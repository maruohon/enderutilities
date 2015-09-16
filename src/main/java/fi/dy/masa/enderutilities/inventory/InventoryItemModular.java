package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
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
    protected InventoryItemModularModules moduleInventory;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player)
    {
        this.player = player;
        this.isRemote = player.worldObj.isRemote;
        this.containerItemUUID = NBTUtils.getOrCreateUUIDFromItemStack(containerStack, "UUID");
        this.moduleInventory = new InventoryItemModularModules(this, player);

        // Call these directly here so that the ItemStack arrays get created on the client side
        this.moduleInventory.readStorageModulesFromContainerItem();
        this.readItemsFromStorageModule();
    }

    public InventoryItemModularModules getModuleInventory()
    {
        return this.moduleInventory;
    }

    /**
     * (Re-)reads all the storage modules from the container item and then the inventory contents
     * from the selected storage module.
     */
    public void updateContainerItems()
    {
        if (this.isRemote == false)
        {
            this.moduleInventory.readStorageModulesFromContainerItem();
            this.readItemsFromStorageModule();
        }
    }

    public UUID getContainerUUID()
    {
        return this.containerItemUUID;
    }

    /**
     * Saves the inventory to the storage module and then the storage modules to the container item
     */
    public void saveInventory()
    {
        if (this.isRemote == false)
        {
            this.writeItemsToStorageModule();
            this.moduleInventory.writeStorageModulesToContainerItem();
        }
    }

    /**
     * Initializes the inventory's ItemStack storage and reads the ItemStacks from the selected storage module
     */
    public void readItemsFromStorageModule()
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

        ItemStack storageStack = this.getSelectedStorageModuleStack();
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

    protected ItemStack getSelectedStorageModuleStack()
    {
        int index = this.getSelectedStorageModuleIndex();
        return index >= 0 && index < this.moduleInventory.getSizeInventory() ? this.moduleInventory.getStackInSlot(index) : null;
    }

    /**
     * Returns whether the item inventory is accessible.
     */
    public boolean isItemInventoryAccessible()
    {
        // Can only store items when there is a valid storage module (= Memory Card) installed
        // and currently selected and the player has access rights to it.
        ItemStack storageStack = this.getSelectedStorageModuleStack();
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

    /**
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    protected void writeItemsToStorageModule()
    {
        if (this.getSelectedStorageModuleStack() != null)
        {
            UtilItemModular.writeItemsToContainerItem(this.getSelectedStorageModuleStack(), this.storedItems);
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
        if (this.getSelectedStorageModuleStack() != null && this.getSelectedStorageModuleStack().hasDisplayName())
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
        if (this.getSelectedStorageModuleStack() != null && this.getSelectedStorageModuleStack().hasDisplayName())
        {
            return this.getSelectedStorageModuleStack().getDisplayName();
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
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            // The actual inventory size for stored items
            return ((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack);
        }
        return 0;
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
        ItemStack stack = null;
        if (slotNum < this.storedItems.length)
        {
            stack = this.storedItems[slotNum];
            this.storedItems[slotNum] = null;
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
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModular.getStackInSlot(): Invalid slot number: " + slotNum);
        }

        return null;
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //EnderUtilities.logger.info("InventoryItemModular.decrStackSize(" + slotNum + ", " + maxAmount + ")");
        ItemStack stack = null;

        if (slotNum < this.storedItems.length)
        {
            if (this.storedItems[slotNum] != null)
            {
                if (this.storedItems[slotNum].stackSize >= maxAmount)
                {
                    stack = this.storedItems[slotNum].splitStack(maxAmount);

                    if (this.storedItems[slotNum].stackSize <= 0)
                    {
                        this.storedItems[slotNum] = null;
                    }
                }
                else
                {
                    stack = this.storedItems[slotNum];
                    this.storedItems[slotNum] = null;
                }
            }
        }
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModular.decrStackSize(): Invalid slot number: " + slotNum);
        }

        this.saveInventory();

        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        if (slotNum < this.storedItems.length)
        {
            this.storedItems[slotNum] = newStack;
        }
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModular.setInventorySlotContents(): Invalid slot number: " + slotNum);
        }

        this.saveInventory();
    }

    @Override
    public void markDirty()
    {
        this.moduleInventory.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
        this.moduleInventory.openInventory();
    }

    @Override
    public void closeInventory()
    {
        this.moduleInventory.closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        // Don't allow putting the bag inside itself
        //if (this.containerItemUUID.equals(NBTUtils.getUUIDFromItemStack(stack, "UUID")))
        // Don't allow putting any Handy Bags inside other Handy Bags
        if (stack.getItem() == EnderUtilitiesItems.handyBag)
        {
            return false;
        }

        // Item inventory slots, check if the inventory can be accessed
        return this.isItemInventoryAccessible();
    }
}
