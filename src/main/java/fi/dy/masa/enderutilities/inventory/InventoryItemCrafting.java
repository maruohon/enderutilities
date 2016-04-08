package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class InventoryItemCrafting extends InventoryCrafting
{
    protected final EntityPlayer player;
    protected final ItemStack[] items;
    protected final int invSize;
    protected final int stackLimit;
    protected final boolean isRemote;
    /** The NBTTagList tag name storing the items in the containerStack */
    protected String itemsTagName;
    protected ItemStack containerStack;
    protected Container container;
    protected IModularInventoryHolder callback;

    public InventoryItemCrafting(Container container, int width, int height, ItemStack containerStack, boolean isRemote,
            EntityPlayer player, IModularInventoryHolder callback, String tagName)
    {
        super(container, width, height);
        this.container = container;
        this.containerStack = containerStack;
        this.invSize = width * height;
        this.player = player;
        this.isRemote = isRemote;
        this.stackLimit = 64;
        this.callback = callback;
        this.itemsTagName = tagName;
        this.items = new ItemStack[this.invSize];
    }

    public void setCallback(IModularInventoryHolder callback)
    {
        this.callback = callback;
    }

    public ItemStack getContainerItemStack()
    {
        if (this.callback != null)
        {
            return this.callback.getContainerStack();
        }

        return this.containerStack;
    }

    protected void clearInventory()
    {
        for (int i = 0; i < this.items.length; i++)
        {
            this.items[i] = null;
        }
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
     * Sets the ItemStack that stores the contents of this inventory.
     * NOTE: You MUST set it to null when the inventory is invalid/not accessible
     * ie. when the container ItemStack reference isn't valid anymore!!
     */
    public void setContainerItemStack(ItemStack stack)
    {
        this.containerStack = stack;
        this.readFromContainerItemStack();
    }

    /**
     * Read the inventory contents from the container ItemStack
     */
    public void readFromContainerItemStack()
    {
        //System.out.println("InventoryItemCrafting#readFromContainerItemStack() - " + (this.isRemote ? "client" : "server"));

        // Only read the contents on the server side, they get synced to the client via the open Container
        if (this.isRemote == false)
        {
            this.clearInventory();

            ItemStack stack = this.getContainerItemStack();
            if (stack != null && this.isUseableByPlayer(this.player) == true)
            {
                NBTUtils.readStoredItemsFromTag(stack.getTagCompound(), this.items, this.itemsTagName);
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
            //System.out.println("InventoryItemCrafting#writeToContainerItemStack() - " + (this.isRemote ? "client" : "server"));
            ItemStack stack = this.getContainerItemStack();
            if (stack != null && this.isUseableByPlayer(this.player) == true)
            {
                NBTUtils.writeItemsToContainerItem(stack, this.items, this.itemsTagName, true);
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
        //System.out.println("InventoryItemCrafting#getStackInSlot(" + slotNum + ") - " + (this.isRemote ? "client" : "server"));
        if (slotNum < this.items.length)
        {
            return this.items[slotNum];
        }
        /*else
        {
            EnderUtilities.logger.warn("InventoryItemCrafting.getStackInSlot(): Invalid slot number: " + slotNum);
        }*/

        return null;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack newStack)
    {
        //System.out.println("InventoryItemCrafting#setInventorySlotContents(" + slotNum + ", " + newStack + ") - " + (this.isRemote ? "client" : "server"));
        if (slotNum < this.items.length)
        {
            this.items[slotNum] = newStack;

            this.markDirty();
        }
        else
        {
            EnderUtilities.logger.warn("InventoryItemCrafting.setInventorySlotContents(): Invalid slot number: " + slotNum);
        }
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //System.out.println("InventoryItemCrafting#decrStackSize(" + slotNum + ", " + maxAmount + ") - " + (this.isRemote ? "client" : "server"));
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
            EnderUtilities.logger.warn("InventoryItemCrafting.decrStackSize(): Invalid slot number: " + slotNum);
            return null;
        }

        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotNum)
    {
        //System.out.println("InventoryItemCrafting#getStackInSlotOnClosing(" + slotNum + ") - " + (this.isRemote ? "client" : "server"));
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
        //System.out.println("InventoryItemCrafting#getInventoryStackLimit() - " + (this.isRemote ? "client" : "server"));
        ItemStack stack = this.getContainerItemStack();
        if (stack != null && stack.getItem() == EnderUtilitiesItems.enderPart)
        {
            int tier = ((IModule) stack.getItem()).getModuleTier(stack);
            if (tier >= 6 && tier <= 12)
            {
                return (int)Math.pow(2, tier);
            }
        }

        return this.stackLimit;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("InventoryItemCrafting#isItemValidForSlot(" + slotNum + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        //System.out.println("InventoryItemCrafting#isUseableByPlayer() - " + (this.isRemote ? "client" : "server"));
        ItemStack stack = this.getContainerItemStack();
        if (stack == null)
        {
            //System.out.println("InventoryItemCrafting#isUseableByPlayer(): false - containerStack == null");
            return false;
        }

        OwnerData ownerData = OwnerData.getPlayerDataFromItem(stack);
        return ownerData == null || ownerData.canAccess(player) == true;
    }

    @Override
    public void markDirty()
    {
        if (this.isRemote == false)
        {
            //System.out.println("InventoryItemCrafting#markDirty() - " + (this.isRemote ? "client" : "server"));
            this.writeToContainerItemStack();
        }

        this.container.onCraftMatrixChanged(this);
    }
}
