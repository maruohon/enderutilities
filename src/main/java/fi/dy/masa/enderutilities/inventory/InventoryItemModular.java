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
    protected InventoryItemMemoryCards moduleInventory;
    protected ModuleType moduleType;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player, ModuleType moduleType)
    {
        this(containerStack, ((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack), player, ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType), moduleType);
    }

    public InventoryItemModular(ItemStack containerStack, int mainInvSize, EntityPlayer player, int moduleInvSize, ModuleType moduleType)
    {
        super(containerStack, mainInvSize, player.worldObj.isRemote, player);

        this.modularItemStack = containerStack;
        this.containerUUID = NBTUtils.getOrCreateUUIDFromItemStack(containerStack, "UUID");
        this.moduleType = moduleType;

        this.moduleInventory = new InventoryItemMemoryCards(this, containerStack, moduleInvSize, player.worldObj.isRemote, player);
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
        // TODO add host inventory callback, ie. getting the stack by the UUID
        return this.modularItemStack;
    }

    public void setModularItemStack(ItemStack stack)
    {
        this.modularItemStack = stack;
    }

    @Override
    public ItemStack getContainerItemStack()
    {
        return this.getSelectedModuleStack();
    }

    @Override
    public void readFromContainerItemStack()
    {
        this.moduleInventory.setContainerItemStack(this.getModularItemStack());
        //this.moduleInventory.readFromContainerItemStack(); // done above
        this.setMainInventoryStackLimit();
        super.readFromContainerItemStack();
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
        return UtilItemModular.getModuleStackBySlotNumber(this.getModularItemStack(), this.getSelectedModuleIndex(), this.moduleType);
    }

    /**
     * Set the stack limit of the main inventory based on the container ItemStack
     */
    public void setMainInventoryStackLimit()
    {
        int limit = 64;

        ItemStack stack = this.getModularItemStack();
        if (stack != null && stack.getItem() instanceof ItemInventoryModular)
        {
            limit = ((ItemInventoryModular)stack.getItem()).getInventoryStackLimit(stack);
        }

        this.setInventoryStackLimit(limit);
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        ItemStack stack = this.getSelectedModuleStack();
        if (stack != null && stack.hasDisplayName())
        {
            return true;
        }

        stack = this.getModularItemStack();
        return stack != null && stack.hasDisplayName();
    }

    @Override
    public String getInventoryName()
    {
        ItemStack stack = this.getSelectedModuleStack();
        // First check if the selected storage module has been named, and use that name
        if (stack != null && stack.hasDisplayName())
        {
            return stack.getDisplayName();
        }

        stack = this.getModularItemStack();
        // If the storage module didn't have a name, but the item itself does, then use that name
        if (stack != null && stack.hasDisplayName())
        {
            return stack.getDisplayName();
        }

        if (stack.getItem() instanceof ItemEnderUtilities)
        {
            return Reference.MOD_ID + ".container." + ((ItemEnderUtilities)stack.getItem()).name;
        }

        return stack.getItem().getUnlocalizedName(stack);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.moduleInventory.markDirty();
    }

    @Override
    public void openInventory()
    {
        super.openInventory();
        this.moduleInventory.openInventory();
    }

    @Override
    public void closeInventory()
    {
        super.closeInventory();
        this.moduleInventory.closeInventory();
    }
}
