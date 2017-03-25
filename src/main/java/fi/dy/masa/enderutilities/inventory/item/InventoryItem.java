package fi.dy.masa.enderutilities.inventory.item;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class InventoryItem extends ItemStackHandlerBasic
{
    protected ItemStack containerStack;
    protected boolean isRemote;
    protected UUID containerUUID;
    protected IItemHandler hostInventory;

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, "Items");
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, String tagName)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, player, tagName, null, null);
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, EntityPlayer player, String tagName, UUID containerUUID, IItemHandler hostInv)
    {
        super(invSize, stackLimit, allowCustomStackSizes, tagName);
        this.containerStack = containerStack;
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
        for (int i = 0; i < this.items.length; i++)
        {
            this.items[i] = null;
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
            if (stack != null && stack.hasTagCompound() == true && this.isCurrentlyAccessible() == true)
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
            if (stack != null && this.isCurrentlyAccessible() == true)
            {
                NBTUtils.writeItemsToContainerItem(stack, this.items, this.getItemStorageTagName(), true);
            }
        }
    }

    public boolean isCurrentlyAccessible()
    {
        //System.out.println("InventoryItem#isCurrentlyAccessible() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    public boolean isAccessibleBy(Entity entity)
    {
        //System.out.println("InventoryItem#isAccessibleByPlayer() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    public boolean isAccessibleBy(UUID uuid)
    {
        //System.out.println("InventoryItem#isAccessibleBy() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack() != null;
    }

    public boolean isPrivate()
    {
        OwnerData owner = OwnerData.getOwnerDataFromItem(this.getContainerItemStack());
        return owner != null && owner.getIsPublic() == false;
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
        return this.getContainerItemStack() != null && this.isCurrentlyAccessible();
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
