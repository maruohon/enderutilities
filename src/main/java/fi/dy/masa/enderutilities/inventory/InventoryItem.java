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

    public InventoryItem(ItemStack stack, int invSize, World world, EntityPlayer player)
    {
        this.containerStack = stack;
        this.invSize = invSize;
        this.player = player;
        this.isRemote = world.isRemote;
    }

    /**
     * Returns the ItemStack holding the container item
     */
    protected ItemStack getContainerItemStack()
    {
        return this.containerStack;
    }

    /**
     * Sets the ItemStack that holds this inventory. Set to null to indicate the inventory is invalid/not accessible.
     */
    public void setContainerItemStack(ItemStack stack)
    {
        this.containerStack = stack;
        this.readFromItem();
    }

    /**
     * Read the inventory contents from the container ItemStack
     */
    public void readFromItem()
    {
        this.items = new ItemStack[this.invSize];

        ItemStack containerStack = this.getContainerItemStack();
        if (this.isRemote == false && containerStack != null)
        {
            NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(containerStack);
            if (ownerData == null || ownerData.canAccess(this.player) == true)
            {
                UtilItemModular.readItemsFromContainerItem(containerStack, this.items);
            }
        }
    }

    /**
     * Writes the inventory contents to the container ItemStack
     */
    public void writeToItem()
    {
        ItemStack containerStack = this.getContainerItemStack();
        if (this.isRemote == false && containerStack != null)
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
            this.writeToItem();
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

        this.writeToItem();

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
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        return true;
    }

    @Override
    public String getInventoryName()
    {
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
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
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
        this.writeToItem();
    }
}
