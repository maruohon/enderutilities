package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiASU;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerASU;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSize;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityASU extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    private ItemStackHandlerTileEntity itemHandlerReference;
    private ItemHandlerWrapperASU itemHandlerASU;
    private int tier = 1;

    public TileEntityASU()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ASU);
    }

    private int getInvSize()
    {
        return this.tier;
    }

    private void initStorage()
    {
        this.itemHandlerBase        = new ItemStackHandlerTileEntity(0, this.getInvSize(), 0, true, "Items", this);
        this.itemHandlerReference   = new ItemStackHandlerTileEntity(1, 1, 1024, true, "ItemsRef", this);
        this.itemHandlerASU         = new ItemHandlerWrapperASU(this.itemHandlerBase, this.itemHandlerReference);
        this.itemHandlerExternal    = this.itemHandlerASU;
    }

    @Override
    public void setStorageTier(int tier)
    {
        this.tier = MathHelper.clamp(tier, 1, 9);

        this.initStorage();
    }

    @Override
    public int getStorageTier()
    {
        return this.tier;
    }

    public IItemHandler getReferenceInventory()
    {
        return this.itemHandlerReference;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.setStorageTier(nbt.getByte("Tier"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.itemHandlerReference.deserializeNBT(nbt);
        this.itemHandlerBase.setStackLimit(this.itemHandlerASU.getInventoryStackLimit());

        super.readItemsFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Tier", (byte) this.tier);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.itemHandlerReference.serializeNBT());

        super.writeItemsToNBT(nbt);
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.tier);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.tier = tag.getByte("tier");

        this.initStorage();

        super.handleUpdateTag(tag);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        if (inventoryId == 1)
        {
            this.itemHandlerBase.setStackLimit(this.itemHandlerASU.getInventoryStackLimit());
        }
    }

    private class ItemHandlerWrapperASU extends ItemHandlerWrapperSize
    {
        private final IItemHandler referenceInv;

        public ItemHandlerWrapperASU(IItemHandler baseHandler, IItemHandler referenceInv)
        {
            super(baseHandler);
            this.referenceInv = referenceInv;
        }

        @Override
        public int getInventoryStackLimit()
        {
            ItemStack stack = this.referenceInv.getStackInSlot(0);
            return stack != null ? stack.stackSize : 0;
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            return this.getInventoryStackLimit();
        }
    }

    @Override
    public ContainerASU getContainer(EntityPlayer player)
    {
        return new ContainerASU(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiASU(this.getContainer(player), this);
    }
}
