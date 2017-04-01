package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiInserter;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerInserter;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;

public class TileEntityInserter extends TileEntityEnderUtilitiesInventory
{
    private final ItemStackHandlerTileEntity itemHandlerFilters;
    private final List<EnumFacing> enabledSides = new ArrayList<EnumFacing>();
    private final List<EnumFacing> validSides = new ArrayList<EnumFacing>();
    private int filterMask;
    private boolean isFiltered;

    public TileEntityInserter()
    {
        super(ReferenceNames.NAME_TILE_INSERTER);

        this.filterMask = FilterSetting.IS_WHITELIST.getBitMask() | FilterSetting.MATCH_META.getBitMask();
        this.itemHandlerBase    = new ItemStackHandlerTileEntity(0,  1, 64, false, "Items", this);
        this.itemHandlerFilters = new ItemStackHandlerTileEntity(1, 27, 64, false, "ItemsFilter", this);
        this.itemHandlerExternal = this.itemHandlerBase;

        this.initStorage();
    }

    private void initStorage()
    {
    }

    public boolean isFiltered()
    {
        return this.isFiltered;
    }

    public void setIsFiltered(boolean filtered)
    {
        this.isFiltered = filtered;
    }

    @Override
    public void setFacing(EnumFacing facing)
    {
        super.setFacing(facing);

        this.markDirtyAndSync();
    }

    @Override
    public void rotate(Rotation rotationIn)
    {
        super.rotate(rotationIn);

        List<EnumFacing> newList = new ArrayList<EnumFacing>();

        for (EnumFacing side : this.enabledSides)
        {
            newList.add(rotationIn.rotate(side));
        }

        this.enabledSides.clear();
        this.enabledSides.addAll(newList);
        this.updateValidSides();
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        this.updateValidSides();
    }

    public void onNeighborTileChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        // When a tile changes on the input side, schedule a new tile tick, if necessary
        if (neighbor.equals(this.getPos().offset(this.getFacing().getOpposite())))
        {
        }
    }

    public void toggleOutputSide(EnumFacing side)
    {
        if (side != this.getFacing() && side != this.getFacing().getOpposite())
        {
            if (this.enabledSides.contains(side))
            {
                this.enabledSides.remove(side);
            }
            else
            {
                this.enabledSides.add(side);
            }

            this.updateValidSides();
            this.markDirtyAndSync();
        }
    }

    public void updateValidSides()
    {
        if (this.getWorld() != null && this.getWorld().isRemote == false)
        {
            this.validSides.clear();

            World world = this.getWorld();
            BlockPos pos = this.getPos();

            for (EnumFacing side : this.enabledSides)
            {
                TileEntity te = world.getTileEntity(pos.offset(side));

                if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()))
                {
                    this.validSides.add(side);
                }
            }

            this.markDirtyAndSync(); // TODO add custom integer sync
        }
    }

    public ImmutableList<EnumFacing> getEnabledOutputSides()
    {
        return ImmutableList.copyOf(this.enabledSides);
    }

    public ImmutableList<EnumFacing> getValidOutputSides()
    {
        return ImmutableList.copyOf(this.validSides);
    }

    public int getSideMask(List<EnumFacing> list)
    {
        int mask = 0;

        for (EnumFacing side : list)
        {
            mask |= 1 << side.getIndex();
        }

        return mask;
    }

    private void setSidesFromMask(int mask, List<EnumFacing> list)
    {
        list.clear();

        for (EnumFacing side : EnumFacing.values())
        {
            if ((mask & (1 << side.getIndex())) != 0)
            {
                list.add(side);
            }
        }
    }

    public boolean isFilterSettingEnabled(FilterSetting setting)
    {
        return setting.getFromBitmask(this.filterMask);
    }

    public IItemHandler getFilterInventory()
    {
        return this.itemHandlerFilters;
    }

    private void markDirtyAndSync()
    {
        if (this.getWorld() != null && this.getWorld().isRemote == false)
        {
            this.markDirty();

            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
        }
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.isFiltered = nbt.getBoolean("Filtered");
        this.filterMask = nbt.getByte("FilterMask");
        this.setSidesFromMask(nbt.getByte("Sides"), this.enabledSides);

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
        nbt.setBoolean("Filtered", this.isFiltered);
        nbt.setByte("FilterMask", (byte) this.filterMask);
        nbt.setByte("Sides", (byte) this.getSideMask(this.enabledSides));

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        this.updateValidSides();

        nbt = super.getUpdatePacketTag(nbt);
        nbt.setBoolean("flt", this.isFiltered);
        nbt.setByte("esd", (byte) this.getSideMask(this.enabledSides));
        nbt.setByte("vsd", (byte) this.getSideMask(this.validSides));

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.isFiltered = tag.getBoolean("flt");
        this.setSidesFromMask(tag.getByte("esd"), this.enabledSides);
        this.setSidesFromMask(tag.getByte("vsd"), this.validSides);

        super.handleUpdateTag(tag);

        if (this.getWorld() != null)
        {
            World world = this.getWorld();
            IBlockState state = world.getBlockState(this.getPos());

            if (state.getBlock() == EnderUtilitiesBlocks.INSERTER)
            {
                EnderUtilitiesBlocks.INSERTER.updateBlockHilightBoxes(world, this.getPos(), this.getFacing());
            }
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    @Override
    public ContainerInserter getContainer(EntityPlayer player)
    {
        return new ContainerInserter(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiInserter(this.getContainer(player), this);
    }

    public enum FilterSetting
    {
        IS_WHITELIST    (0x01),
        MATCH_META      (0x02),
        MATCH_NBT       (0x04);

        private final int bitMask;

        private FilterSetting(int bitMask)
        {
            this.bitMask = bitMask;
        }

        public boolean getFromBitmask(int mask)
        {
            return (mask & this.bitMask) != 0;
        }

        public int getBitMask()
        {
            return this.bitMask;
        }
    }
}
