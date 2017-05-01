package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.block.BlockInserter;
import fi.dy.masa.enderutilities.gui.client.GuiInserter;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerInserter;
import fi.dy.masa.enderutilities.network.message.ISyncableTile;
import fi.dy.masa.enderutilities.network.message.MessageSyncTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityInserter extends TileEntityEnderUtilitiesInventory implements ISyncableTile
{
    private final ItemHandlerWrapperInserter itemHandlerInserter;
    private final ItemStackHandlerTileEntity itemHandlerFilters;
    private final List<EnumFacing> enabledSides = new ArrayList<EnumFacing>();
    private final List<EnumFacing> validSides = new ArrayList<EnumFacing>();
    private EnumFacing facingOpposite = EnumFacing.SOUTH;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORED;
    private int filterMask;
    private int delay = 4;
    private int outputSideIndex;
    private boolean isFiltered;
    private ItemStack[] cachedFilterStacks = new ItemStack[0];

    public TileEntityInserter()
    {
        super(ReferenceNames.NAME_TILE_INSERTER);

        this.filterMask = FilterSetting.MATCH_META.getBitMask();
        this.itemHandlerBase    = new ItemStackHandlerTileEntity(0,  1,  1, false, "Items", this);
        this.itemHandlerFilters = new ItemStackHandlerTileEntity(1, 27, 64, false, "ItemsFilter", this);
        this.itemHandlerInserter = new ItemHandlerWrapperInserter(this.itemHandlerBase);
        this.itemHandlerExternal = this.itemHandlerInserter;
    }

    public boolean isFiltered()
    {
        return this.isFiltered;
    }

    public void setIsFiltered(boolean isFiltered)
    {
        this.isFiltered = isFiltered;
    }

    public int getUpdateDelay()
    {
        return this.delay;
    }

    public void setUpdateDelay(int delay)
    {
        this.delay = MathHelper.clamp(delay, 0, 72000); // max 1 hour
    }

    public void setStackLimit(int limit)
    {
        this.itemHandlerBase.setStackLimit(MathHelper.clamp(limit, 1, 64));
    }

    public boolean isFilterSettingEnabled(FilterSetting setting)
    {
        return setting.isEnabledInBitmask(this.filterMask);
    }

    public int getFilterMask()
    {
        return this.filterMask;
    }

    public void setFilterMask(int mask)
    {
        this.filterMask = mask;
    }

    public int getRedstoneModeIntValue()
    {
        return this.redstoneMode.getIntValue();
    }

    public void setRedstoneModeFromInteger(int ordinal)
    {
        this.redstoneMode = RedstoneMode.fromInt(ordinal);
    }

    public IItemHandler getFilterInventory()
    {
        return this.itemHandlerFilters;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        //System.out.printf("onNeighborBlockChange(), scheduling(?) for %s\n", this.getPos());
        this.updateValidSides(true);
        this.scheduleBlockUpdate(this.delay, false);
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

            this.updateValidSides(true);
        }
    }

    private void updateValidSides(boolean markDirtyAndSync)
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

            if (markDirtyAndSync)
            {
                this.markDirty();
                this.syncSideConfigs();
            }
        }
    }

    private void syncSideConfigs()
    {
        this.sendSyncPacket(new MessageSyncTileEntity(this.getPos(), this.getCombinesSideMask()));
    }

    public ImmutableList<EnumFacing> getEnabledOutputSides()
    {
        return ImmutableList.copyOf(this.enabledSides);
    }

    public ImmutableList<EnumFacing> getValidOutputSides()
    {
        return ImmutableList.copyOf(this.validSides);
    }

    private int getSideMask(List<EnumFacing> list)
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

    private int getCombinesSideMask()
    {
        return (this.getSideMask(this.validSides) << 6) | this.getSideMask(this.enabledSides);
    }

    private void setSidesFromCombinedMask(int mask)
    {
        this.setSidesFromMask(mask         & 0x3F, this.enabledSides);
        this.setSidesFromMask((mask >>> 6) & 0x3F, this.validSides);
    }

    @Override
    public void setFacing(EnumFacing facing)
    {
        this.facingOpposite = facing.getOpposite();

        super.setFacing(facing);

        this.syncSideConfigs();
    }

    @Override
    public void rotate(Rotation rotationIn)
    {
        List<EnumFacing> newList = new ArrayList<EnumFacing>();

        for (EnumFacing side : this.enabledSides)
        {
            newList.add(rotationIn.rotate(side));
        }

        this.enabledSides.clear();
        this.enabledSides.addAll(newList);
        this.updateValidSides(false);

        super.rotate(rotationIn);
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.itemHandlerFilters.deserializeNBT(nbt);
        this.inventoryChanged(this.itemHandlerFilters.getInventoryId(), 0); // Update the cached array of stacks

        super.readItemsFromNBT(nbt);
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.itemHandlerFilters.serializeNBT());

        super.writeItemsToNBT(nbt);
    }

    @Override
    public void setPlacementProperties(World world, BlockPos pos, ItemStack stack, NBTTagCompound tag)
    {
        if (tag.hasKey("inserter.stack_limit", Constants.NBT.TAG_BYTE))
        {
            this.setStackLimit(tag.getByte("inserter.stack_limit"));
        }

        if (tag.hasKey("inserter.delay", Constants.NBT.TAG_INT))
        {
            this.setUpdateDelay(tag.getInteger("inserter.delay"));
        }

        if (tag.hasKey("inserter.redstone_mode", Constants.NBT.TAG_BYTE))
        {
            this.setRedstoneModeFromInteger(tag.getByte("inserter.redstone_mode"));
        }

        this.markDirty();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setSidesFromCombinedMask(nbt.getShort("Sides"));
        this.delay = nbt.getInteger("Delay");
        this.setStackLimit(nbt.getByte("StackLimit"));
        int mask = nbt.getShort("SettingsMask");

        this.filterMask      =  mask & 0x0F;
        this.isFiltered      = (mask & 0x80) != 0;
        this.outputSideIndex = (mask >>> 8) & 0x7;
        this.setRedstoneModeFromInteger((mask >>> 12) & 0x3);

        this.facingOpposite = this.getFacing().getOpposite();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setShort("Sides", (short) this.getCombinesSideMask());
        nbt.setInteger("Delay", this.delay);
        nbt.setByte("StackLimit", (byte) this.itemHandlerBase.getInventoryStackLimit());

        int mask = this.filterMask;
        if (this.isFiltered) { mask |= 0x80; }
        mask |= (this.outputSideIndex << 8);
        mask |= (this.redstoneMode.getIntValue() << 12);
        nbt.setShort("SettingsMask", (short) mask);

        super.writeToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);
        nbt.setShort("sd", (short) this.getCombinesSideMask());

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.setSidesFromCombinedMask(tag.getShort("sd"));

        super.handleUpdateTag(tag);

        this.updateFilterState(true);
    }

    private void updateFilterState(boolean updateHilightBoxes)
    {
        if (this.getWorld() != null)
        {
            World world = this.getWorld();
            IBlockState state = world.getBlockState(this.getPos());

            if (state.getBlock() == EnderUtilitiesBlocks.INSERTER)
            {
                this.isFiltered = state.getValue(BlockInserter.TYPE) == BlockInserter.InserterType.FILTERED;

                if (updateHilightBoxes)
                {
                    EnderUtilitiesBlocks.INSERTER.updateBlockHilightBoxes(world, this.getPos(), this.getFacing());
                }

                world.notifyBlockUpdate(this.getPos(), state, state, 3);
            }
        }
    }

    public void onNeighborTileChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        // When a tile changes on the input side, schedule a new tile tick, if necessary.
        // Don't schedule when we are part of a "pipe line", unless we are the first one,
        // because we try to operate on a push-basis.
        if (this.shouldOperate() && neighbor.equals(this.getPos().offset(this.getFacing().getOpposite())) &&
            world.getBlockState(neighbor).getBlock() != this.getBlockType())
        {
            //System.out.printf("onNeighborTileChange(), scheduling(?) for %s\n", this.getPos());
            this.scheduleBlockUpdate(this.delay, false);
        }
    }

    private boolean shouldOperate()
    {
        return this.redstoneMode.shouldOperate(this.getWorld().isBlockPowered(this.getPos()));
    }

    @Override
    public void onScheduledBlockUpdate(World world, BlockPos pos, IBlockState state, Random rand)
    {
        //System.out.printf("onScheduledBlockUpdate() @ %s (%s)\n", pos, world.isRemote ? "c" : "s");
        if (this.shouldOperate() == false)
        {
            return;
        }

        ItemStack stack = this.itemHandlerBase.getStackInSlot(0);

        // Currently holding items, try to push them out
        if (stack != null)
        {
            if (this.tryPushOutItems(world, pos))
            {
                // Schedule a new update, if we managed to push at least some items out
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
        // Not holding items, try to pull items in
        else
        {
            if (this.tryPullInItems(world, pos))
            {
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
    }

    /**
     * Tries to pull in items from an inventory on the input side.<br>
     * <b>NOTE: ONLY call this when the internal slot is empty, as this method doesn't check it!</b>
     * @return true if the operation succeeded and at least some items were moved, false otherwise
     */
    private boolean tryPullInItems(World world, BlockPos posSelf)
    {
        //System.out.printf("tryPullInItems() @ %s\n", posSelf);
        TileEntity te = world.getTileEntity(posSelf.offset(this.facingOpposite));

        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getFacing()))
        {
            IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getFacing());

            if (inv != null)
            {
                ItemStack stack;

                if (this.isFiltered && this.isFilterSettingEnabled(FilterSetting.IS_INPUT_FILTER))
                {
                    stack = this.tryPullInItemsThatPassFilters(inv);
                }
                else
                {
                    stack = InventoryUtils.getItemsFromFirstNonEmptySlot(inv, this.itemHandlerBase.getInventoryStackLimit(), false);
                }

                if (stack != null)
                {
                    this.itemHandlerBase.insertItem(0, stack, false);
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    private ItemStack tryPullInItemsThatPassFilters(IItemHandler inv)
    {
        int slots = inv.getSlots();

        for (int slot = 0; slot < slots; slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);

            if (stack != null && this.itemAllowedByFilters(stack))
            {
                return inv.extractItem(slot, this.itemHandlerBase.getInventoryStackLimit(), false);
            }
        }

        return null;
    }

    private boolean itemAllowedByFilters(@Nonnull ItemStack stack)
    {
        boolean match = InventoryUtils.matchingStackFoundInArray(
                this.cachedFilterStacks, stack,
                this.isFilterSettingEnabled(FilterSetting.MATCH_META) == false,
                this.isFilterSettingEnabled(FilterSetting.MATCH_NBT) == false);

        return match == this.isFilterSettingEnabled(FilterSetting.IS_WHITELIST);
    }

    /**
     * Tries to push out items to the next valid output side, or as a fallback to the front side.<br>
     * <b>NOTE: Only call this if there are items currently in the internal inventory!</b>
     * @param world
     * @param posSelf
     */
    private boolean tryPushOutItems(World world, BlockPos posSelf)
    {
        //System.out.printf("tryPushOutItems() @ %s\n", posSelf);
        boolean shouldPushToSides = true;

        if (this.isFiltered && this.isFilterSettingEnabled(FilterSetting.IS_INPUT_FILTER) == false)
        {
            shouldPushToSides = this.itemAllowedByFilters(this.itemHandlerBase.getStackInSlot(0));
        }

        if (shouldPushToSides)
        {
            int numValidSides = this.validSides.size();

            for (int i = 0; i < numValidSides; i++)
            {
                if (this.outputSideIndex >= numValidSides)
                {
                    this.outputSideIndex = 0;
                }

                // At least one valid output side
                if (this.outputSideIndex < numValidSides)
                {
                    EnumFacing side = this.validSides.get(this.outputSideIndex);
                    this.outputSideIndex++;

                    if (this.tryPushOutItemsToSide(world, posSelf, side))
                    {
                        return true;
                    }
                }
            }
        }

        // Fall back to the front facing
        return this.tryPushOutItemsToSide(world, posSelf, this.getFacing());
    }

    private boolean tryPushOutItemsToSide(World world, BlockPos posSelf, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(posSelf.offset(side));

        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getFacing()))
        {
            IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getFacing());

            if (inv != null)
            {
                ItemStack stack = this.itemHandlerBase.extractItem(0, 64, false);
                int sizeOrig = stack.stackSize;
                boolean movedSome = false;
                stack = InventoryUtils.tryInsertItemStackToInventory(inv, stack);

                // Return the items that couldn't be moved
                if (stack != null)
                {
                    movedSome = stack.stackSize != sizeOrig;
                    this.itemHandlerBase.insertItem(0, stack, false);
                }

                return stack == null || movedSome;
            }
        }

        return false;
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        // Filter inventory
        if (inventoryId == 1)
        {
            this.cachedFilterStacks = InventoryUtils.createInventorySnapshotOfNonEmptySlots(this.itemHandlerFilters);
            this.scheduleBlockUpdate(this.delay, false);
        }
    }

    private class ItemHandlerWrapperInserter implements IItemHandler
    {
        private final IItemHandler baseHandler;

        private ItemHandlerWrapperInserter(IItemHandler baseHandler)
        {
            this.baseHandler = baseHandler;
        }

        @Override
        public int getSlots()
        {
            return this.baseHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.baseHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (simulate == false)
            {
                TileEntityInserter.this.scheduleBlockUpdate(TileEntityInserter.this.delay, false);
            }

            return this.baseHandler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return null;
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int actionIn, int element)
    {
        GuiAction action = GuiAction.fromInt(actionIn);

        switch (action)
        {
            case CHANGE_REDSTONE_MODE:
                int ord = this.redstoneMode.getIntValue() + element;

                if (ord >= RedstoneMode.values().length)
                {
                    ord = 0;
                }
                else if (ord < 0)
                {
                    ord = RedstoneMode.values().length - 1;
                }

                this.setRedstoneModeFromInteger(ord);
                this.scheduleBlockUpdate(this.delay, false);
                break;

            case CHANGE_DELAY:
                this.setUpdateDelay(this.delay + element);
                break;

            case CHANGE_STACK_LIMIT:
                this.setStackLimit(this.itemHandlerBase.getInventoryStackLimit() + element);
                break;

            case CHANGE_FILTERS:
                this.filterMask = (this.filterMask ^ element) & 0xF;
                this.scheduleBlockUpdate(this.delay, false);
                break;
        }

        this.markDirty();
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
        IS_INPUT_FILTER (0x01),
        IS_WHITELIST    (0x02),
        MATCH_META      (0x04),
        MATCH_NBT       (0x08);

        private final int bitMask;

        private FilterSetting(int bitMask)
        {
            this.bitMask = bitMask;
        }

        public boolean isEnabledInBitmask(int mask)
        {
            return (mask & this.bitMask) != 0;
        }

        public int getBitMask()
        {
            return this.bitMask;
        }
    }

    public enum RedstoneMode
    {
        IGNORED (0, true),
        LOW     (1, false),
        HIGH    (2, true);

        private final int intValue;
        private final boolean operateWhenPowered;

        private RedstoneMode(int intValue, boolean operateWhenPowered)
        {
            this.intValue = intValue;
            this.operateWhenPowered = operateWhenPowered;
        }

        public boolean shouldOperate(boolean isPowered)
        {
            return this == IGNORED || this.operateWhenPowered == isPowered;
        }

        public int getIntValue()
        {
            return this.intValue;
        }

        public static RedstoneMode fromInt(int intValue)
        {
            for (RedstoneMode mode : values())
            {
                if (mode.getIntValue() == intValue)
                {
                    return mode;
                }
            }

            return IGNORED;
        }
    }

    public enum GuiAction
    {
        CHANGE_DELAY,
        CHANGE_STACK_LIMIT,
        CHANGE_FILTERS,
        CHANGE_REDSTONE_MODE;

        public static GuiAction fromInt(int action)
        {
            return values()[action % values().length];
        }
    }

    @Override
    public void syncTile(int[] values, ItemStack[] stacks)
    {
        if (values.length == 1)
        {
            this.setSidesFromCombinedMask(values[0]);
            this.updateFilterState(true);
        }
    }
}
