package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiMSU;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerMSU;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityMSU extends TileEntityEnderUtilitiesInventory implements ITieredStorage
{
    public static final int GUI_ACTION_TOGGLE_CREATIVE = 1;
    private int tier;
    private boolean creative;

    public TileEntityMSU()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MSU);
        this.initStorage();
    }

    private int getInvSize()
    {
        return this.tier == 1 ? 9 : 1;
    }

    public boolean isCreative()
    {
        return this.creative;
    }

    private void initStorage()
    {
        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, this.getInvSize(), Configs.msuMaxItems, true, "Items", this);
        this.itemHandlerExternal = new ItemHandlerWrapperMSU(this.getBaseItemHandler(), this);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainerMSU(this.getBaseItemHandler(), this.itemHandlerExternal);
    }

    @Override
    public void setStorageTier(int tier)
    {
        this.tier = MathHelper.clamp(tier, 0, 1);

        this.initStorage();
    }

    @Override
    public int getStorageTier()
    {
        return this.tier;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.tier = MathHelper.clamp(nbt.getByte("Tier"), 0, 1);
        this.creative = nbt.getBoolean("Creative");

        super.readFromNBTCustom(nbt);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        // This creates the inventories themselves...
        this.initStorage();

        // ... and this de-serializes the items from NBT into the inventory
        super.readItemsFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Tier", (byte) this.tier);
        nbt.setBoolean("Creative", this.creative);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("tier", (byte) this.tier);
        nbt.setBoolean("cr", this.creative);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.tier = tag.getByte("tier");
        this.creative = tag.getBoolean("cr");

        this.initStorage();

        super.handleUpdateTag(tag);
    }

    private class ItemHandlerWrapperMSU extends ItemHandlerWrapperSelective
    {
        private final TileEntityMSU te;

        public ItemHandlerWrapperMSU(IItemHandler baseHandler, TileEntityMSU te)
        {
            super(baseHandler);

            this.te = te;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (this.te.isCreative())
            {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (this.te.isCreative())
            {
                return super.extractItem(slot, amount, true);
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return this.te.isCreative() == false;
        }
    }

    private class ItemHandlerWrapperContainerMSU extends ItemHandlerWrapperContainer
    {
        public ItemHandlerWrapperContainerMSU(IItemHandlerModifiable baseHandler, IItemHandler wrapperHandler)
        {
            super(baseHandler, wrapperHandler);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return this.wrapperHandler.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_TOGGLE_CREATIVE)
        {
            if (player.capabilities.isCreativeMode)
            {
                this.creative = ! this.creative;
                this.markDirty();

                IBlockState state = this.getWorld().getBlockState(this.getPos());
                this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
            }
        }
    }

    @Override
    public ContainerMSU getContainer(EntityPlayer player)
    {
        return new ContainerMSU(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiMSU(this.getContainer(player), this);
    }
}
