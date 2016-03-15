package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.reference.Reference;

public class TileEntityEnderUtilitiesInventory extends TileEntityEnderUtilities
{
    protected ItemStackHandlerTileEntity itemHandlerBase;
    protected IItemHandler itemHandlerExternal;
    protected String customInventoryName;

    public TileEntityEnderUtilitiesInventory(String name)
    {
        super(name);
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
    public IItemHandler getBaseItemHandler()
    {
        return this.itemHandlerBase;
    }

    /**
     * Returns an inventory wrapper for use in Containers/Slots.<br>
     * <b>NOTE:</b> Override this for any TileEntity that doesn't have a valid
     * IItemHandler in the itemHandlerExternal field!!
     */
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.itemHandlerExternal);
    }

    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.itemHandlerBase.deserializeNBT(nbt);
    }

    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.itemHandlerBase.serializeNBT());
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        if (nbt.hasKey("CustomName", Constants.NBT.TAG_STRING) == true)
        {
            this.customInventoryName = nbt.getString("CustomName");
        }

        this.readItemsFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        this.writeItemsToNBT(nbt);

        if (this.hasCustomName() == true)
        {
            nbt.setString("CustomName", this.customInventoryName);
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) this.itemHandlerExternal;
        }

        return super.getCapability(capability, facing);
    }

    public void inventoryChanged(int inventoryId, int slot) { }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        if (this.worldObj.getTileEntity(this.getPos()) != this)
        {
            return false;
        }

        if (player.getDistanceSq(this.getPos()) >= 64.0d)
        {
            return false;
        }

        return true;
    }

    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return null;
    }
}
