package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular extends InventoryItem
{
    protected EntityPlayer player;
    protected UUID containerItemUUID;
    protected InventoryItem moduleInventory;
    protected int moduleInvSize;
    protected ModuleType moduleType;

    public InventoryItemModular(ItemStack stack, EntityPlayer player)
    {
        this(stack, ((ItemInventoryModular)stack.getItem()).getSizeInventory(stack), player, ((IModular)stack.getItem()).getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD), ModuleType.TYPE_MEMORY_CARD);
    }

    public InventoryItemModular(ItemStack stack, int invSize, EntityPlayer player, int moduleInvSize, ModuleType moduleType)
    {
        super(stack, invSize, player.worldObj, player);
        this.player = player;
        this.moduleInvSize = moduleInvSize;
        this.moduleType = moduleType;

        this.containerItemUUID = NBTUtils.getOrCreateUUIDFromItemStack(stack, "UUID");
        this.moduleInventory = new InventoryItemMemoryCard(this, moduleInvSize, player.worldObj, player);

        this.readFromItem();
    }

    public UUID getContainerUUID()
    {
        return this.containerItemUUID;
    }

    public InventoryItem getModuleInventory()
    {
        return this.moduleInventory;
    }

    /**
     * Returns the ItemStack of the modular item (ie. not the stack that actually stores the items,
     * in this inventory, that stack is stored as a module inside the modular item)
     */
    public ItemStack getModularItemStack()
    {
        return InventoryUtils.getItemStackByUUID(this.player.inventory, this.containerItemUUID, "UUID");
    }

    @Override
    protected ItemStack getContainerItemStack()
    {
        ItemStack modularStack = this.getModularItemStack();
        if (modularStack != null)
        {
            return this.moduleInventory.getStackInSlot(UtilItemModular.getStoredModuleSelection(modularStack, this.moduleType));
        }

        return null;
    }

    @Override
    public void readFromItem()
    {
        this.moduleInventory.readFromItem();
        super.readFromItem();
    }

    @Override
    public void writeToItem()
    {
        super.writeToItem();
        this.moduleInventory.writeToItem();
    }

    /**
     * Returns the index (0..n) of the currently selected storage module.
     * Will return -1 if there is currently no container item present.
     */
    public int getSelectedStorageModuleIndex()
    {
        ItemStack modularStack = this.getModularItemStack();
        if (modularStack != null && modularStack.getItem() instanceof ItemInventoryModular)
        {
            return UtilItemModular.getStoredModuleSelection(modularStack, this.moduleType);
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
        ItemStack moduleStack = this.getSelectedStorageModuleStack();
        if (moduleStack == null)
        {
            return false;
        }

        NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(moduleStack);
        return ownerData == null || ownerData.canAccess(this.player) == true;
    }

    /**
     * Returns whether the storage module slots are accessible.
     * Used while rendering to render the slots as darker when the opened bag is not accessible.
     */
    public boolean isModuleInventoryAccessible()
    {
        return this.getModularItemStack() != null;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        ItemStack moduleStack = this.getSelectedStorageModuleStack();
        if (moduleStack != null && moduleStack.hasDisplayName())
        {
            return true;
        }

        ItemStack modularStack = this.getModularItemStack();
        return modularStack != null ? modularStack.hasDisplayName() : false;
    }

    @Override
    public String getInventoryName()
    {
        ItemStack moduleStack = this.getSelectedStorageModuleStack();
        // First check if the selected storage module has been named, and use that name
        if (moduleStack != null && moduleStack.hasDisplayName())
        {
            return moduleStack.getDisplayName();
        }

        ItemStack modularStack = this.getModularItemStack();
        // If the storage module didn't have a name, but the item itself does, then use that name
        if (modularStack != null && modularStack.hasDisplayName())
        {
            return modularStack.getDisplayName();
        }

        if (modularStack.getItem() instanceof ItemEnderUtilities)
        {
            return Reference.MOD_ID + ".container." + ((ItemEnderUtilities)modularStack.getItem()).name;
        }

        return Reference.MOD_ID + ".container.null";
    }

    @Override
    public int getInventoryStackLimit()
    {
        ItemStack modularStack = this.getModularItemStack();
        if (modularStack != null && modularStack.getItem() instanceof ItemInventoryModular)
        {
            return ((ItemInventoryModular)modularStack.getItem()).getInventoryStackLimit(modularStack);
        }

        return 64;
    }

    @Override
    public void markDirty()
    {
        this.moduleInventory.markDirty();
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
            return true;
        }

        ItemStack modularStack = this.getModularItemStack();
        // Don't allow nesting the same type of items as the container item inside itself
        if (modularStack != null && modularStack.getItem() == stack.getItem())
        {
            return false;
        }

        // Item inventory slots, check if the inventory can be accessed
        return this.isItemInventoryAccessible();
    }
}
