package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityEnderUtilitiesInventory extends TileEntityEnderUtilities
{
    protected ItemStackHandlerTileEntity itemHandlerBase;
    protected IItemHandler itemHandlerExternal;
    protected String customInventoryName;
    protected boolean useWrapperHandlerForContainerExtract;
    private boolean creative;

    public TileEntityEnderUtilitiesInventory(String name)
    {
        super(name);
    }

    public TileEntityEnderUtilitiesInventory(String name, boolean useWrapperHandlerForContainerExtract)
    {
        super(name);

        this.useWrapperHandlerForContainerExtract = useWrapperHandlerForContainerExtract;
    }

    public boolean isCreative()
    {
        return this.creative;
    }

    public void setCreative(boolean isCreative)
    {
        this.creative = isCreative;
    }

    public void setInventoryName(String name)
    {
        this.customInventoryName = name;
    }

    public boolean hasCustomName()
    {
        return this.customInventoryName != null && this.customInventoryName.length() > 0;
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customInventoryName : Reference.MOD_ID + ".container." + this.tileEntityName;
    }

    /**
     * Returns the "base" IItemHandler that this TileEntity uses to store items into NBT when it saves.
     */
    public ItemStackHandlerBasic getBaseItemHandler()
    {
        return this.itemHandlerBase;
    }

    /**
     * Returns an inventory wrapper for use in Containers/Slots.<br>
     * <b>NOTE:</b> Override this for any TileEntity that doesn't have a valid
     * IItemHandler in the itemHandlerExternal field!!
     */
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainer(this.getBaseItemHandler(), this.itemHandlerExternal, this.useWrapperHandlerForContainerExtract);
    }

    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.getBaseItemHandler().deserializeNBT(nbt);
    }

    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.getBaseItemHandler().serializeNBT());
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING))
        {
            this.customInventoryName = nbt.getString("CustomName");
        }

        this.readItemsFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        this.writeItemsToNBT(nbt);

        if (this.hasCustomName())
        {
            nbt.setString("CustomName", this.customInventoryName);
        }

        return nbt;
    }

    /**
     * Stores a cached snapshot of the current inventory in a compound tag <b>InvCache</b>.
     * It is meant for tooltip use in the ItemBlocks.
     * @param nbt
     * @return
     */
    public NBTTagCompound getCachedInventory(NBTTagCompound nbt, int maxEntries)
    {
        IItemHandler inv = this.getBaseItemHandler();

        if (inv != null)
        {
            nbt = NBTUtils.storeCachedInventory(nbt, inv, maxEntries);
        }

        return nbt;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return this.itemHandlerExternal != null;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandlerExternal);
        }

        return super.getCapability(capability, facing);
    }

    public void inventoryChanged(int inventoryId, int slot) { }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        if (this.getWorld().getTileEntity(this.getPos()) != this || player.getDistanceSq(this.getPos()) >= 64.0d)
        {
            return false;
        }

        return super.isUseableByPlayer(player);
    }
}
