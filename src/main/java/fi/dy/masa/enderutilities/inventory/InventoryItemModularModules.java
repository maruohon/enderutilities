package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModularModules implements IInventory
{
    protected EntityPlayer player;
    protected boolean isRemote;
    protected InventoryItemModular containerInventory;
    /** The ItemStacks containing the storage modules */
    protected ItemStack[] storageModules;

    public InventoryItemModularModules(InventoryItemModular inv, EntityPlayer player)
    {
        this.containerInventory = inv;
        this.player = player;
        this.isRemote = player.worldObj.isRemote;
        this.readStorageModulesFromContainerItem();
    }

    /**
     * Saves the storage modules to the container item and then updates the inventory contents.
     */
    protected void saveInventoryAndUpdateContainer()
    {
        if (this.isRemote == false)
        {
            this.writeStorageModulesToContainerItem();
            this.containerInventory.readItemsFromStorageModule();
        }
    }

    /**
     * Initializes the storage module array and reads the ItemStacks from the container item
     */
    public void readStorageModulesFromContainerItem()
    {
        ItemStack containerStack = this.containerInventory.getContainerItemStack();
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
     * Writes the inventory contents to the selected storage module, and then writes that updated storage module to the container item
     */
    public void writeStorageModulesToContainerItem()
    {
        ItemStack containerStack = this.containerInventory.getContainerItemStack();
        if (containerStack != null)
        {
            UtilItemModular.writeItemsToContainerItem(containerStack, this.storageModules, true);
        }
    }

    @Override
    public int getSizeInventory()
    {
        return this.storageModules.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum < this.storageModules.length)
        {
            return this.storageModules[slotNum];
        }
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModularModules.getStackInSlot(): Invalid slot number: " + slotNum);
        }

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        //EnderUtilities.logger.info("InventoryItemModularModules.setInventorySlotContents(" + slotNum + ", " + newStack + ")");
        if (slotNum < this.storageModules.length)
        {
            this.storageModules[slotNum] = newStack;
            this.saveInventoryAndUpdateContainer();
        }
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModularModules.setInventorySlotContents(): Invalid slot number: " + slotNum);
        }
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //EnderUtilities.logger.info("InventoryItemModularModules.decrStackSize(" + slotNum + ", " + maxAmount + ")");
        ItemStack stack = null;

        if (slotNum < this.storageModules.length)
        {
            if (this.storageModules[slotNum] != null)
            {
                if (this.storageModules[slotNum].stackSize >= maxAmount)
                {
                    stack = this.storageModules[slotNum].splitStack(maxAmount);

                    if (this.storageModules[slotNum].stackSize <= 0)
                    {
                        this.storageModules[slotNum] = null;
                    }
                }
                else
                {
                    stack = this.storageModules[slotNum];
                    this.storageModules[slotNum] = null;
                }
            }
        }
        else
        {
            //EnderUtilities.logger.warn("InventoryItemModularModules.decrStackSize(): Invalid slot number: " + slotNum);
            return null;
        }

        this.saveInventoryAndUpdateContainer();

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        ItemStack stack = null;
        if (slotNum < this.storageModules.length)
        {
            stack = this.storageModules[slotNum];
            this.storageModules[slotNum] = null;
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        return (stack.getItem() instanceof IModule && ((IModule)stack.getItem()).getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD));
    }

    @Override
    public String getInventoryName()
    {
        return "";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
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
}
