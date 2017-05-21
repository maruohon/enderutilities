package fi.dy.masa.enderutilities.inventory.item;

import java.util.UUID;
import net.minecraft.entity.Entity;
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
    protected ItemStack containerStack = ItemStack.EMPTY;
    protected boolean isRemote;
    protected UUID containerUUID;
    protected IItemHandler hostInventory;

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes, boolean isRemote)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, "Items");
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, String tagName)
    {
        this(containerStack, invSize, stackLimit, allowCustomStackSizes, isRemote, tagName, null, null);
    }

    public InventoryItem(ItemStack containerStack, int invSize, int stackLimit, boolean allowCustomStackSizes,
            boolean isRemote, String tagName, UUID containerUUID, IItemHandler hostInv)
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
        this.items.clear();
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

            if (stack.isEmpty() == false && stack.hasTagCompound() && this.isCurrentlyAccessible())
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

            if (stack.isEmpty() == false && this.isCurrentlyAccessible())
            {
                NBTUtils.writeItemsToContainerItem(stack, this.items, this.getItemStorageTagName(), true);
            }
        }
    }

    public boolean isCurrentlyAccessible()
    {
        //System.out.println("InventoryItem#isCurrentlyAccessible() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack().isEmpty() == false;
    }

    public boolean isAccessibleBy(Entity entity)
    {
        //System.out.println("InventoryItem#isAccessibleByPlayer() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack().isEmpty() == false;
    }

    public boolean isAccessibleBy(UUID uuid)
    {
        //System.out.println("InventoryItem#isAccessibleBy() - " + (this.isRemote ? "client" : "server"));
        return this.getContainerItemStack().isEmpty() == false;
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
        if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.ENDER_PART)
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
        return this.getContainerItemStack().isEmpty() == false && this.isCurrentlyAccessible();
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
