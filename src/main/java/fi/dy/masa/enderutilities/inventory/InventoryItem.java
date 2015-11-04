package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItem implements IInventory
{
    protected ItemStack containerStack;
    protected int invSize;
    protected ItemStack[] items;
    protected EntityPlayer player;
    protected boolean isRemote;
    protected String customInventoryName;
    protected int stackLimit;

    public InventoryItem(ItemStack containerStack, int invSize, World world, EntityPlayer player)
    {
        this.containerStack = containerStack;
        this.invSize = invSize;
        this.player = player;
        this.isRemote = world.isRemote;
        this.stackLimit = 64;
    }

    /**
     * Returns the ItemStack storing the contents of this inventory
     */
    public ItemStack getContainerItemStack()
    {
        return this.containerStack;
    }

    /**
     * Sets the ItemStack that stores the contents of this inventory.
     * NOTE: You MUST set it to null when the inventory is invalid/not accessible
     * ie. when the container ItemStack reference isn't valid anymore!!
     */
    public void setContainerItemStack(ItemStack stack)
    {
        this.containerStack = stack;
        this.readFromContainerItemStack();
    }

    public void setCustomInventoryName(String name)
    {
        this.customInventoryName = name;
    }

    public void setInventoryStackLimit(int stackLimit)
    {
        this.stackLimit = stackLimit;
    }

    /**
     * Read the inventory contents from the container ItemStack
     */
    public void readFromContainerItemStack()
    {
        this.items = new ItemStack[this.getSizeInventory()]; // This obviously also needs to happen on the client side

        // Only read the contents on the server side, they get synced to the client via the open Container
        ItemStack containerStack = this.getContainerItemStack();
        if (this.isRemote == false && containerStack != null && this.isUseableByPlayer(this.player) == true)
        {
            UtilItemModular.readItemsFromContainerItem(containerStack, this.items);
        }
    }

    /**
     * Writes the inventory contents to the container ItemStack
     */
    public void writeToContainerItemStack()
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (this.isRemote == false && containerStack != null && this.isUseableByPlayer(this.player) == true)
        {
            UtilItemModular.writeItemsToContainerItem(containerStack, this.items, true);
        }
    }

    @Override
    public int getSizeInventory()
    {
        return this.invSize;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum)
    {
        if (slotNum < this.items.length)
        {
            return this.items[slotNum];
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItem.getStackInSlot(): Invalid slot number: " + slotNum);
        }

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        //EnderUtilities.logger.info("InventoryItem.setInventorySlotContents(" + slotNum + ", " + newStack + ")");
        if (slotNum < this.items.length)
        {
            this.items[slotNum] = newStack;
            this.writeToContainerItemStack();
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItem.setInventorySlotContents(): Invalid slot number: " + slotNum);
        }
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //EnderUtilities.logger.info("InventoryItem.decrStackSize(" + slotNum + ", " + maxAmount + ")");
        ItemStack stack = null;

        if (slotNum < this.items.length)
        {
            if (this.items[slotNum] != null)
            {
                if (this.items[slotNum].stackSize >= maxAmount)
                {
                    stack = this.items[slotNum].splitStack(maxAmount);

                    if (this.items[slotNum].stackSize <= 0)
                    {
                        this.items[slotNum] = null;
                    }
                }
                else
                {
                    stack = this.items[slotNum];
                    this.items[slotNum] = null;
                }
            }
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItem.decrStackSize(): Invalid slot number: " + slotNum);
            return null;
        }

        this.writeToContainerItemStack();

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        ItemStack stack = null;
        if (slotNum < this.items.length)
        {
            stack = this.items[slotNum];
            this.items[slotNum] = null;
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.stackLimit;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        return this.getContainerItemStack() != null;
    }

    @Override
    public String getInventoryName()
    {
        if (this.customInventoryName != null)
        {
            return this.customInventoryName;
        }

        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null)
        {
            return containerStack.getDisplayName();
        }

        return "";
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        if (this.customInventoryName != null)
        {
            return true;
        }

        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack != null)
        {
            return containerStack.hasDisplayName();
        }

        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (containerStack == null)
        {
            System.out.println("false - null");
            return false;
        }

        NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(containerStack);
        return ownerData == null || ownerData.canAccess(player) == true;
    }

    @Override
    public void markDirty()
    {
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
        this.writeToContainerItemStack();
    }
}
