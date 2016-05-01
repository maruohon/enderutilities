package fi.dy.masa.enderutilities.inventory.item;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class InventoryItem extends ItemStackHandlerBasic
{
    protected ItemStack containerStack;
    protected final EntityPlayer player;
    //protected String customInventoryName;
    protected boolean isRemote;
    protected UUID containerUUID;
    protected IItemHandler hostInventory;

    public InventoryItem(ItemStack containerStack, int invSize, boolean isRemote, EntityPlayer player)
    {
        this(containerStack, invSize, 64, false, isRemote, player, "Items");
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes, boolean isRemote, EntityPlayer player)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, "Items");
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes, boolean isRemote, EntityPlayer player, String tagName)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, tagName, null, null);
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes, boolean isRemote, EntityPlayer player, String tagName, UUID containerUUID, IItemHandler hostInv)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.containerStack = containerStack;
        this.player = player;
        this.isRemote = isRemote;
        this.containerUUID = containerUUID;
        this.hostInventory = hostInv;
    }

    public void setIsRemote(boolean isRemote)
    {
        this.isRemote = isRemote;
    }

    protected void clearInventory()
    {
        for (int i = 0; i < this.invSize; i++)
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
            this.tagName = tagName;
        }
    }

    public UUID getContainerUUID()
    {
        return this.containerUUID;
    }

    public void setHostInventory(IItemHandler inv)
    {
        this.hostInventory = inv;
    }

    /**
     * Sets the host inventory and the UUID of the container ItemStack, so that the correct
     * container ItemStack can be fetched from the host inventory.
     */
    public void setHostInventory(IItemHandler inv, UUID uuid)
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

    /**
     * Read the inventory contents from the container ItemStack
     */
    public void readFromContainerItemStack()
    {
        // Only read the contents on the server side, they get synced to the client via the open Container
        if (this.isRemote == false)
        {
            //System.out.println("InventoryItem#readFromContainerItemStack() - " + (this.isRemote ? "client" : "server"));
            this.clearInventory();

            ItemStack stack = this.getContainerItemStack();
            if (stack != null && stack.hasTagCompound() == true && this.isUseableByPlayer(this.player) == true)
            {
                this.deserializeNBT(stack.getTagCompound());
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
                NBTUtils.writeItemsToContainerItem(stack, this.items, this.tagName, true);
            }
        }
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        //System.out.println("InventoryItem#isUseableByPlayer() - " + (this.isRemote ? "client" : "server"));
        ItemStack stack = this.getContainerItemStack();
        if (stack == null)
        {
            //System.out.println("isUseableByPlayer(): false - containerStack == null");
            return false;
        }

        OwnerData ownerData = OwnerData.getPlayerDataFromItem(stack);
        return ownerData == null || ownerData.canAccess(player) == true;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.getInventoryStackLimitFromContainerStack(this.getContainerItemStack());
    }

    public int getInventoryStackLimitFromContainerStack(ItemStack stack)
    {
        if (stack != null && stack.getItem() == EnderUtilitiesItems.enderPart)
        {
            int tier = ((IModule) stack.getItem()).getModuleTier(stack);
            if (tier >= 6 && tier <= 12)
            {
                return (int)Math.pow(2, tier);
            }
        }

        return super.getInventoryStackLimit();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        //System.out.println("InventoryItem#isItemValidForSlot(" + slot + ", " + stack + ") - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    @Override
    public void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        if (this.isRemote == false)
        {
            //System.out.println("InventoryItem#markDirty() - " + (this.isRemote ? "client" : "server"));
            this.writeToContainerItemStack();
        }
    }
}
