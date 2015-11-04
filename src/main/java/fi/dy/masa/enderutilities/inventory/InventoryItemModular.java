package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular extends InventoryItem
{
    protected ItemStack modularItemStack;
    protected UUID containerUUID;
    protected InventoryItem moduleInventory;
    protected ModuleType moduleType;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player, ModuleType moduleType)
    {
        this(containerStack, ((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack), player, ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType), moduleType);
    }

    public InventoryItemModular(ItemStack containerStack, int mainInvSize, EntityPlayer player, int moduleInvSize, ModuleType moduleType)
    {
        super(containerStack, mainInvSize, player.worldObj, player);

        this.modularItemStack = containerStack;
        this.containerUUID = NBTUtils.getOrCreateUUIDFromItemStack(containerStack, "UUID");
        this.moduleType = moduleType;

        this.moduleInventory = new InventoryItemMemoryCards(this, containerStack, moduleInvSize, player.worldObj, player);
        this.setContainerItemStack(this.getSelectedModuleStack()); // this also calls readFromContainerItemStack()
    }

    public UUID getContainerUUID()
    {
        return this.containerUUID;
    }

    public InventoryItem getModuleInventory()
    {
        return this.moduleInventory;
    }

    public ItemStack getModularItemStack()
    {
        return this.modularItemStack;
    }

    @Override
    public void readFromContainerItemStack()
    {
        this.moduleInventory.setContainerItemStack(this.getModularItemStack());
        this.moduleInventory.readFromContainerItemStack();
        super.readFromContainerItemStack();
        this.setMainInventoryStackLimit();
    }

    @Override
    public void writeToContainerItemStack()
    {
        super.writeToContainerItemStack();
        this.moduleInventory.writeToContainerItemStack();
    }

    /**
     * Returns the index (0..n) of the currently selected storage module.
     * Will return -1 if there is currently no container item present.
     */
    public int getSelectedModuleIndex()
    {
        return UtilItemModular.getStoredModuleSelection(this.getModularItemStack(), this.moduleType);
    }

    protected ItemStack getSelectedModuleStack()
    {
        /*int index = this.getSelectedModuleIndex();
        return index >= 0 && index < this.moduleInventory.getSizeInventory() ? this.moduleInventory.getStackInSlot(index) : null;*/
        return UtilItemModular.getModuleStackBySlotNumber(this.modularItemStack, this.getSelectedModuleIndex(), this.moduleType);
    }

    /**
     * Set the stack limit of the main inventory based on the container ItemStack
     */
    public void setMainInventoryStackLimit()
    {
        int limit = 64;

        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null && containerStack.getItem() instanceof ItemInventoryModular)
        {
            limit = ((ItemInventoryModular)containerStack.getItem()).getInventoryStackLimit(containerStack);
        }

        this.setInventoryStackLimit(limit);
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        ItemStack moduleStack = this.getSelectedModuleStack();
        if (moduleStack != null && moduleStack.hasDisplayName())
        {
            return true;
        }

        ItemStack containerStack = this.getContainerItemStack();
        return containerStack != null && containerStack.hasDisplayName();
    }

    @Override
    public String getInventoryName()
    {
        ItemStack moduleStack = this.getSelectedModuleStack();
        // First check if the selected storage module has been named, and use that name
        if (moduleStack != null && moduleStack.hasDisplayName())
        {
            return moduleStack.getDisplayName();
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

        return containerStack.getItem().getUnlocalizedName(containerStack);
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
}
