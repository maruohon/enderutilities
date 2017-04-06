package fi.dy.masa.enderutilities.inventory.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular extends InventoryItemPermissions
{
    protected ItemStack modularItemStack;
    protected InventoryItemMemoryCards moduleInventory;
    protected ModuleType moduleType;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player, boolean allowCustomStackSizes, ModuleType moduleType)
    {
        this(containerStack, player, ((ItemInventoryModular) containerStack.getItem()).getSizeInventory(containerStack),
              allowCustomStackSizes, ((ItemInventoryModular) containerStack.getItem()).getSizeModuleInventory(containerStack), moduleType);
    }

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player, int mainInvSize, boolean allowCustomStackSizes, int moduleInvSize, ModuleType moduleType)
    {
        super(containerStack, mainInvSize, 64, allowCustomStackSizes, player.getEntityWorld().isRemote, player);

        this.modularItemStack = containerStack;
        this.moduleType = moduleType;
        this.containerUUID = NBTUtils.getUUIDFromItemStack(containerStack, "UUID", true);
        this.hostInventory = null;

        this.moduleInventory = new InventoryItemMemoryCards(this, containerStack, moduleInvSize, player.getEntityWorld().isRemote);
        this.moduleInventory.readFromContainerItemStack();

        this.readFromContainerItemStack();
    }

    public InventoryItem getModuleInventory()
    {
        return this.moduleInventory;
    }

    public ItemStack getModularItemStack()
    {
        //System.out.println("InventoryItemModular#getModularItemStack() - " + (this.isRemote ? "client" : "server"));
        if (this.hostInventory != null && this.containerUUID != null)
        {
            return InventoryUtils.getItemStackByUUID(this.hostInventory, this.containerUUID, "UUID");
        }

        return this.modularItemStack;
    }

    public void setModularItemStack(ItemStack stack)
    {
        this.modularItemStack = stack;
    }

    @Override
    public ItemStack getContainerItemStack()
    {
        //System.out.println("InventoryItemModular#getContainerItemStack() - " + (this.isRemote ? "client" : "server"));
        return this.getSelectedModuleStack();
    }

    public void readFromSelectedModuleStack()
    {
        super.readFromContainerItemStack();
    }

    @Override
    public void readFromContainerItemStack()
    {
        //System.out.println("InventoryItemModular#readFromContainerItemStack() - " + (this.isRemote ? "client" : "server"));
        //this.setMainInventoryStackLimit();

        // This also does "this.moduleInventory.readFromContainerItemStack();"
        //this.moduleInventory.setContainerItemStack(this.getModularItemStack());

        this.moduleInventory.readFromContainerItemStack();
        this.readFromSelectedModuleStack();
    }

    @Override
    protected void writeToContainerItemStack()
    {
        //System.out.println("InventoryItemModular#writeToContainerItemStack() - " + (this.isRemote ? "client" : "server"));
        super.writeToContainerItemStack();
        this.moduleInventory.writeToContainerItemStack();
    }

    /**
     * Returns the index (0..n) of the currently selected storage module.
     * Will return -1 if there is currently no container item present.
     */
    public int getSelectedModuleIndex()
    {
        if (this.getModularItemStack() == null)
        {
            return -1;
        }

        return UtilItemModular.getStoredModuleSelection(this.getModularItemStack(), this.moduleType);
    }

    protected ItemStack getSelectedModuleStack()
    {
        //System.out.println("InventoryItemModular#getSelectedModuleStack() - " + (this.isRemote ? "client" : "server"));
        //return UtilItemModular.getModuleStackBySlotNumber(this.getModularItemStack(), this.getSelectedModuleIndex(), this.moduleType);
        int index = this.getSelectedModuleIndex();
        return index >= 0 && index < this.moduleInventory.getSlots() ? this.moduleInventory.getStackInSlot(index) : null;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.getInventoryStackLimitFromContainerStack(this.getSelectedModuleStack());
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return super.isItemValidForSlot(slotNum, stack);
        }

        ItemStack modularStack = this.getModularItemStack();
        // Don't allow nesting the same type of items as the container item inside itself
        if (modularStack != null && modularStack.getItem() == stack.getItem())
        {
            return false;
        }

        return super.isItemValidForSlot(slotNum, stack);
    }

    @Override
    public boolean isAccessibleBy(Entity entity)
    {
        ItemStack stack = this.getModularItemStack();
        if (stack == null)
        {
            //System.out.println("isUseableByPlayer(): false - containerStack == null");
            return false;
        }

        return super.isAccessibleBy(entity);
    }
}
