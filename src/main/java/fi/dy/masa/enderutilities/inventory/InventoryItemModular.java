package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItemModular extends InventoryItem
{
    protected ItemStack modularItemStack;
    protected UUID containerUUID;
    protected InventoryItemMemoryCards moduleInventory;
    protected ModuleType moduleType;
    protected IInventory hostInventory;

    public InventoryItemModular(ItemStack containerStack, EntityPlayer player, ModuleType moduleType)
    {
        this(containerStack, ((ItemInventoryModular)containerStack.getItem()).getSizeInventory(containerStack), player, ((IModular)containerStack.getItem()).getMaxModules(containerStack, moduleType), moduleType);
    }

    public InventoryItemModular(ItemStack containerStack, int mainInvSize, EntityPlayer player, int moduleInvSize, ModuleType moduleType)
    {
        super(containerStack, mainInvSize, player.worldObj.isRemote, player);

        this.modularItemStack = containerStack;
        this.moduleType = moduleType;
        this.containerUUID = NBTUtils.getUUIDFromItemStack(containerStack, "UUID", true);
        this.hostInventory = null;

        this.moduleInventory = new InventoryItemMemoryCards(this, containerStack, moduleInvSize, player.worldObj.isRemote, player);
        this.moduleInventory.readFromContainerItemStack();

        this.readFromContainerItemStack();
    }

    public void setHostInventory(IInventory inv)
    {
        this.hostInventory = inv;
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
        //System.out.println("InventoryItemModular#getModularItemStack() - " + (this.isRemote ? "client" : "server"));
        if (this.hostInventory != null)
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
        ItemStack stack = this.getModularItemStack();
        if (stack != null && stack.getItem() instanceof ItemInventoryModular)
        {
            return ((ItemInventoryModular)stack.getItem()).getInventoryStackLimit(stack);
        }

        return 64;
    }

    /*@Override
    public boolean hasCustomName()
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
    public String getName()
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
    }*/

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
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        ItemStack stack = this.getModularItemStack();
        if (stack == null)
        {
            //System.out.println("isUseableByPlayer(): false - containerStack == null");
            return false;
        }

        return super.isUseableByPlayer(player);
    }

    /*@Override
    public void markDirty()
    {
        //System.out.println("InventoryItemModular#markDirty() - " + (this.isRemote ? "client" : "server"));
        super.markDirty();
        //this.moduleInventory.writeToContainerItemStack();
        //this.moduleInventory.markDirty();
    }*/
}
