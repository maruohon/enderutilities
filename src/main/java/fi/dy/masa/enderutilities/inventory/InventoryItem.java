package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class InventoryItem implements IInventory
{
    protected ItemStack containerStack;
    protected int invSize;
    protected ItemStack[] items;
    /** The NBTTagList tag name storing the items in the containerStack */
    protected String itemsTagName;
    protected EntityPlayer player;
    protected boolean isRemote;
    protected String customInventoryName;
    protected int stackLimit;
    protected boolean allowCustomStackSizes;
    protected UUID containerUUID;
    protected IInventory hostInventory;

    public InventoryItem(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        this(containerStack, invSize, isRemote, player, "Items");
    }

    public InventoryItem(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player, String tagName)
    {
        this(containerStack, invSize, isRemote, player, tagName, null, null);
    }

    public InventoryItem(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player, String tagName, UUID containerUUID, IInventory hostInv)
    {
        this.containerStack = containerStack;
        this.invSize = invSize;
        this.player = player;
        this.isRemote = isRemote;
        this.stackLimit = 64;
        this.containerUUID = containerUUID;
        this.hostInventory = hostInv;
        this.itemsTagName = tagName;
        this.initInventory();
    }

    protected void initInventory()
    {
        this.items = new ItemStack[this.getSizeInventory()]; // This obviously also needs to happen on the client side
    }

    /**
     * Sets the NBTTagList tag name that stores the items of this inventory in the container ItemStack
     * @param tagName
     */
    public void setItemStorageTagName(String tagName)
    {
        if (tagName != null)
        {
            this.itemsTagName = tagName;
        }
    }

    /**
     * Sets the host inventory and the UUID of the container ItemStack, so that the correct
     * container ItemStack can be fetched from the host inventory.
     */
    public void setHostInventory(IInventory inv, UUID uuid)
    {
        this.hostInventory = inv;
        this.containerUUID = uuid;
    }

    /**
     * Returns the ItemStack storing the contents of this inventory
     */
    public ItemStack getContainerItemStack()
    {
        //System.out.println("InventoryItem#getContainerItemStack() - " + (this.isRemote ? "client" : "server"));
        if (this.containerUUID != null && this.hostInventory != null)
        {
            return InventoryUtils.getItemStackByUUID(this.hostInventory, this.containerUUID, "UUID");
        }

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

    public boolean getAllowCustomStackSizes()
    {
        return this.allowCustomStackSizes;
    }

    public void setAllowCustomStackSizes(boolean allow)
    {
        this.allowCustomStackSizes = allow;
    }

    /**
     * Read the inventory contents from the container ItemStack
     */
    public void readFromContainerItemStack()
    {
        //System.out.println("InventoryItem#readFromContainerItemStack() - " + (this.isRemote ? "client" : "server"));

        // Only read the contents on the server side, they get synced to the client via the open Container
        if (this.isRemote == false)
        {
            this.initInventory();

            ItemStack stack = this.getContainerItemStack();
            if (stack != null && this.isUseableByPlayer(this.player) == true)
            {
                UtilItemModular.readItemsFromContainerItem(stack, this.items, this.itemsTagName);
            }
        }
    }

    /**
     * Writes the inventory contents to the container ItemStack
     */
    protected void writeToContainerItemStack()
    {
        if (this.isRemote == false)
        {
            //System.out.println("InventoryItem#writeToContainerItemStack() - " + (this.isRemote ? "client" : "server"));
            ItemStack stack = this.getContainerItemStack();
            if (stack != null && this.isUseableByPlayer(this.player) == true)
            {
                UtilItemModular.writeItemsToContainerItem(stack, this.items, this.itemsTagName, true);
            }
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
        //System.out.println("InventoryItem#getStackInSlot(" + slotNum + ") - " + (this.isRemote ? "client" : "server"));
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
        //System.out.println("InventoryItem#setInventorySlotContents(" + slotNum + ", " + newStack + ") - " + (this.isRemote ? "client" : "server"));
        if (slotNum < this.items.length)
        {
            this.items[slotNum] = newStack;

            this.markDirty();
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItem.setInventorySlotContents(): Invalid slot number: " + slotNum);
        }
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //System.out.println("InventoryItem#decrStackSize(" + slotNum + ", " + maxAmount + ") - " + (this.isRemote ? "client" : "server"));
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

            this.markDirty();
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItem.decrStackSize(): Invalid slot number: " + slotNum);
            return null;
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        //System.out.println("InventoryItem#getStackInSlotOnClosing(" + slotNum + ") - " + (this.isRemote ? "client" : "server"));
        ItemStack stack = null;
        if (slotNum < this.items.length)
        {
            stack = this.items[slotNum];
            this.items[slotNum] = null;

            this.markDirty();
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        //System.out.println("InventoryItem#getInventoryStackLimit() - " + (this.isRemote ? "client" : "server"));
        return this.stackLimit;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItem#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    @Override
    public String getInventoryName()
    {
        if (this.customInventoryName != null)
        {
            return this.customInventoryName;
        }

        ItemStack stack = this.getContainerItemStack();
        if (stack != null)
        {
            return stack.getDisplayName();
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

        ItemStack stack = this.getContainerItemStack();
        if (stack != null)
        {
            return stack.hasDisplayName();
        }

        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        //System.out.println("InventoryItem#isUseableByPlayer() - " + (this.isRemote ? "client" : "server"));
        ItemStack stack = this.getContainerItemStack();
        if (stack == null)
        {
            //System.out.println("isUseableByPlayer(): false - containerStack == null");
            return false;
        }

        NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(stack);
        return ownerData == null || ownerData.canAccess(player) == true;
    }

    @Override
    public void markDirty()
    {
        if (this.isRemote == false)
        {
            //System.out.println("InventoryItem#markDirty() - " + (this.isRemote ? "client" : "server"));
            this.writeToContainerItemStack();
        }
    }

    @Override
    public void openInventory()
    {
        //System.out.println("InventoryItem#openInventory() - " + (this.isRemote ? "client" : "server"));
    }

    @Override
    public void closeInventory()
    {
        //System.out.println("InventoryItem#closeInventory() - " + (this.isRemote ? "client" : "server"));
        //this.writeToContainerItemStack();
    }
}
