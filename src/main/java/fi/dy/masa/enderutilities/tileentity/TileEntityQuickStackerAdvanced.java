package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiQuickStackerAdvanced;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStackerAdvanced;
import fi.dy.masa.enderutilities.item.ItemQuickStacker.Result;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityQuickStackerAdvanced extends TileEntityEnderUtilitiesInventory
{
    public static final int NUM_TARGET_INVENTORIES            = 9;
    public static final int GUI_ACTION_TOGGLE_TARGET_TYPE     = 0;
    public static final int GUI_ACTION_TOGGLE_COLUMNS         = 1;
    public static final int GUI_ACTION_TOGGLE_ROWS            = 2;
    public static final int GUI_ACTION_TOGGLE_TARGET_ENABLED  = 3;
    public static final int GUI_ACTION_SET_ACTIVE_TARGET      = 4;
    public static final int GUI_ACTION_TOGGLE_FILTER_SETTINGS = 5;

    public static final int BIT_ENABLED     = 0x01;
    public static final int BIT_WHITELIST   = 0x02;
    public static final int BIT_META        = 0x04;
    public static final int BIT_NBT         = 0x08;

    private final IItemHandler inventoryFilters;
    private boolean isAreaMode;
    private byte areaModeSettings;
    private short enabledTargetsMask;
    private byte selectedTarget;
    private long slotMask;
    protected final Map<UUID, Long> clickTimes;

    public TileEntityQuickStackerAdvanced()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, NUM_TARGET_INVENTORIES * 2, 1, false, "Items", this);
        this.inventoryFilters = new ItemStackHandlerTileEntity(1, 36, 1, false, "FilterItems", this);
        this.clickTimes = new HashMap<UUID, Long>();
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.getBaseItemHandler(), new ItemHandlerWrapperQuickStackerAdvanced(this.getBaseItemHandler()));
    }

    public IItemHandler getFilterInventory()
    {
        return this.inventoryFilters;
    }

    public FilterSettings getFilterSettings(int filterId)
    {
        return new FilterSettings(this.itemHandlerBase.getStackInSlot(filterId + NUM_TARGET_INVENTORIES));
    }

    public boolean isAreaMode()
    {
        return this.isAreaMode;
    }

    public void setIsAreaMode(boolean isAreaMode)
    {
        this.isAreaMode = isAreaMode;
    }

    public byte getAreaModeSettings()
    {
        return this.areaModeSettings;
    }

    public void setAreaModeSettings(byte settings)
    {
        this.areaModeSettings = settings;
    }

    public short getEnabledTargetsMask()
    {
        return this.enabledTargetsMask;
    }

    public void setEnabledTargetsMask(short enabledTargetsMask)
    {
        this.enabledTargetsMask = enabledTargetsMask;
    }

    public byte getSelectedTarget()
    {
        return this.selectedTarget;
    }

    public void setSelectedTarget(byte selectedTarget)
    {
        this.selectedTarget = selectedTarget;
    }

    public long getEnabledSlotsMask()
    {
        return this.slotMask;
    }

    public void setEnabledSlotsMask(long mask)
    {
        this.slotMask = mask;
    }

    public boolean getFilterEnabled()
    {
        if (this.isAreaMode)
        {
            return (this.areaModeSettings & BIT_ENABLED) != 0;
        }

        return this.getFilterSettings(this.selectedTarget).getIsEnabled();
    }

    public boolean getFilterIsWhitelist()
    {
        if (this.isAreaMode)
        {
            return (this.areaModeSettings & BIT_WHITELIST) != 0;
        }

        return this.getFilterSettings(this.selectedTarget).getIsWhitelist();
    }

    public boolean getFilterMatchMeta()
    {
        if (this.isAreaMode)
        {
            return (this.areaModeSettings & BIT_META) != 0;
        }

        return this.getFilterSettings(this.selectedTarget).getMatchMeta();
    }

    public boolean getFilterMatchNBT()
    {
        if (this.isAreaMode)
        {
            return (this.areaModeSettings & BIT_NBT) != 0;
        }

        return this.getFilterSettings(this.selectedTarget).getMatchNBT();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.isAreaMode = nbt.getBoolean("AreaMode");
        this.areaModeSettings = nbt.getByte("AreaModeSettings");
        this.enabledTargetsMask = nbt.getByte("EnabledTargets");
        this.selectedTarget = nbt.getByte("SelectedTarget");
        this.slotMask = nbt.getLong("EnabledSlots");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("AreaMode", this.isAreaMode);
        nbt.setByte("AreaModeSettings", this.areaModeSettings);
        nbt.setByte("EnabledTargets", (byte)this.enabledTargetsMask);;
        nbt.setByte("SelectedTarget", this.selectedTarget);
        nbt.setLong("EnabledSlots", this.slotMask);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        if (this.worldObj.isRemote == false)
        {
            Long last = this.clickTimes.get(player.getUniqueID());
            // Double left clicked fast enough (< 5 ticks) - do the action
            if (last != null && this.worldObj.getTotalWorldTime() - last < 5)
            {
                // Area mode
                if (this.isAreaMode == true)
                {
                    quickStackToInventories(this.getWorld(), player, this.slotMask,
                            getTileEntityPositions(this.getWorld(), this.getPos(), 32, 32));
                }
                else
                {
                    this.quickStackToTargetInventories(player);
                }

                player.worldObj.playSound(null, this.getPos(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.2f, 1.8f);
                this.clickTimes.remove(player.getUniqueID());
            }
            else
            {
                this.clickTimes.put(player.getUniqueID(), this.worldObj.getTotalWorldTime());
            }
        }
    }

    protected void quickStackToTargetInventories(EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for (int i = 0, bit = 0x1; i < NUM_TARGET_INVENTORIES; i++)
        {
            if ((this.enabledTargetsMask & bit) != 0)
            {
                ItemStack lcStack = this.itemHandlerBase.getStackInSlot(i);
                if (lcStack != null)
                {
                    TargetData target = TargetData.getTargetFromItem(lcStack);
                    if (target != null && this.getWorld().isBlockLoaded(target.pos, true) &&
                        PositionUtils.isWithinRange(target.pos, this.getPos(), 32, 32) && target.isTargetBlockUnchanged() == true)
                    {
                        TileEntity te = this.getWorld().getTileEntity(target.pos);
                        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing))
                        {
                            IItemHandler externalInv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing);
                            if (externalInv != null)
                            {
                                FilterSettings filter = this.getFilterSettings(i);
                                quickStackItems(playerInv, externalInv, this.slotMask, player.isSneaking() == false, filter);
                            }
                        }
                    }
                }
            }

            bit <<= 1;
        }
    }

    /**
     * Tries to move all items from enabled slots in the player's inventory to the given external inventory
     */
    public static Result quickStackItems(IItemHandler playerInv, IItemHandler externalInv, long slotMask, boolean matchingOnly, FilterSettings filter)
    {
        Result ret = Result.MOVED_NONE;
        boolean movedAll = true;

        long bit = 0x1;
        for (int slotPlayer = 0; slotPlayer < playerInv.getSlots(); slotPlayer++)
        {
            ItemStack stack = playerInv.getStackInSlot(slotPlayer);

            // Only take from slots that have been enabled
            if ((slotMask & bit) != 0 && stack != null && (filter == null || filter.itemAllowedByFilter(stack) == true))
            {
                stack = playerInv.extractItem(slotPlayer, 64, false);
                if (stack == null)
                {
                    continue;
                }

                if (matchingOnly == false || InventoryUtils.getSlotOfLastMatchingItemStack(externalInv, stack) != -1)
                {
                    int sizeOrig = stack.stackSize;
                    stack = InventoryUtils.tryInsertItemStackToInventory(externalInv, stack);

                    if (ret == Result.MOVED_NONE && (stack == null || stack.stackSize != sizeOrig))
                    {
                        ret = Result.MOVED_SOME;
                    }
                }

                // Return the items that were left over
                if (stack != null)
                {
                    playerInv.insertItem(slotPlayer, stack, false);
                    movedAll = false;
                }
            }

            bit <<= 1;
        }

        if (movedAll == true && ret == Result.MOVED_SOME)
        {
            ret = Result.MOVED_ALL;
        }

        return ret;
    }

    public static void quickStackToInventories(World world, EntityPlayer player, long enabledSlotsMask, List<BlockPosDistance> positions)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for (BlockPosDistance posDist : positions)
        {
            TileEntity te = world.getTileEntity(posDist.pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP) == true)
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

                if (inv != null)
                {
                    Result result = TileEntityQuickStackerAdvanced.quickStackItems(playerInv, inv, enabledSlotsMask, player.isSneaking() == false, null);

                    if (result != Result.MOVED_NONE)
                    {
                        world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.2f, 1.8f);
                        Effects.spawnParticlesFromServer(world.provider.getDimension(), posDist.pos, EnumParticleTypes.VILLAGER_HAPPY);
                    }

                    if (result == Result.MOVED_ALL)
                    {
                        break;
                    }
                }
            }
        }
    }

    public static List<BlockPosDistance> getTileEntityPositions(World world, BlockPos centerPos, int rangeH, int rangeV)
    {
        List<BlockPosDistance> posDist = new ArrayList<BlockPosDistance>();

        for (int cx = (centerPos.getX() - rangeH) >> 4; cx <= ((centerPos.getX() - rangeH) >> 4); cx++)
        {
            for (int cz = (centerPos.getZ() - rangeH) >> 4; cz <= ((centerPos.getZ() - rangeH) >> 4); cz++)
            {
                Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
                if (chunk != null)
                {
                    for (BlockPos pos : chunk.getTileEntityMap().keySet())
                    {
                        if (PositionUtils.isWithinRange(pos, centerPos, rangeH, rangeV))
                        {
                            posDist.add(new BlockPosDistance(pos, centerPos));
                        }
                    }
                }
            }
        }

        Collections.sort(posDist);

        return posDist;
    }

    private class ItemHandlerWrapperQuickStackerAdvanced extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperQuickStackerAdvanced(IItemHandler baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            if (slot < NUM_TARGET_INVENTORIES)
            {
                // Only allow Block type Link Crystals
                return stack.getItem() == EnderUtilitiesItems.linkCrystal && ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK;
            }

            return stack.getItem() == EnderUtilitiesItems.enderPart && ((IModule)stack.getItem()).getModuleType(stack) == ModuleType.TYPE_MEMORY_CARD_MISC;
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SET_ACTIVE_TARGET)
        {
            if (element >= 0 && element < NUM_TARGET_INVENTORIES)
            {
                this.selectedTarget = (byte)element;
            }
        }
        else if (action == GUI_ACTION_TOGGLE_TARGET_ENABLED)
        {
            this.enabledTargetsMask ^= (1 << element);
        }
        else if (action == GUI_ACTION_TOGGLE_TARGET_TYPE)
        {
            this.isAreaMode = ! this.isAreaMode;
        }
        else if (action == GUI_ACTION_TOGGLE_ROWS)
        {
            if (element >= 0 && element <= 3)
            {
                this.slotMask ^= (0x1FFL << (element * 9));
            }
        }
        else if (action == GUI_ACTION_TOGGLE_COLUMNS)
        {
            // Player inventory
            if (element >= 0 && element < 9)
            {
                // toggle the bits for the slots in the selected column of the inventory
                this.slotMask ^= (0x08040201L << element);
            }
        }
        else if (action == GUI_ACTION_TOGGLE_FILTER_SETTINGS)
        {
            this.toggleFilterSettings(element);
        }
    }

    private void toggleFilterSettings(int element)
    {
        if (this.isAreaMode)
        {
            switch (element)
            {
                case 0: this.areaModeSettings ^= BIT_ENABLED; break;
                case 1: this.areaModeSettings ^= BIT_WHITELIST; break;
                case 2: this.areaModeSettings ^= BIT_META; break;
                case 3: this.areaModeSettings ^= BIT_NBT; break;
            }
        }
        else
        {
            FilterSettings filter = this.getFilterSettings(this.getSelectedTarget());

            switch (element)
            {
                case 0: filter.toggleIsEnabled(); break;
                case 1: filter.toggleIsWhitelist(); break;
                case 2: filter.toggleMatchMeta(); break;
                case 3: filter.toggleMatchNBT(); break;
            }
        }
    }

    @Override
    public ContainerQuickStackerAdvanced getContainer(EntityPlayer player)
    {
        return new ContainerQuickStackerAdvanced(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiQuickStackerAdvanced(this.getContainer(player), this);
    }

    public static class FilterSettings
    {
        private ItemStack containerStack;

        private FilterSettings(ItemStack containerStack)
        {
            this.containerStack = containerStack;
        }

        private NBTTagCompound getTag(boolean create)
        {
            if (this.containerStack == null)
            {
                return new NBTTagCompound();
            }

            NBTTagCompound nbt = NBTUtils.getCompoundTag(this.containerStack, "QuickStackerAdvanced", create);
            if (nbt == null)
            {
                nbt = new NBTTagCompound();
            }

            return nbt;
        }

        public boolean getIsEnabled()
        {
            return this.getTag(false).getBoolean("Enabled");
        }

        public boolean getIsWhitelist()
        {
            return this.getTag(false).getBoolean("IsWhitelist");
        }

        public boolean getMatchMeta()
        {
            return this.getTag(false).getBoolean("MatchMeta");
        }

        public boolean getMatchNBT()
        {
            return this.getTag(false).getBoolean("MatchNBT");
        }

        public void toggleIsEnabled()
        {
            NBTTagCompound nbt = this.getTag(true);
            nbt.setBoolean("Enabled", ! nbt.getBoolean("Enabled"));
        }

        public void toggleIsWhitelist()
        {
            NBTTagCompound nbt = this.getTag(true);
            nbt.setBoolean("IsWhitelist", ! nbt.getBoolean("IsWhitelist"));
        }

        public void toggleMatchMeta()
        {
            NBTTagCompound nbt = this.getTag(true);
            nbt.setBoolean("MatchMeta", ! nbt.getBoolean("MatchMeta"));
        }

        public void toggleMatchNBT()
        {
            NBTTagCompound nbt = this.getTag(true);
            nbt.setBoolean("MatchNBT", ! nbt.getBoolean("MatchNBT"));
        }

        public boolean itemAllowedByFilter(ItemStack stack)
        {
            if (this.containerStack == null)
            {
                return true;
            }

            NBTTagCompound nbt = NBTUtils.getCompoundTag(this.containerStack, "QuickStackerAdvanced", false);
            if (nbt == null)
            {
                return false;
            }

            boolean enabled = nbt.getBoolean("Enabled");
            if (enabled == false)
            {
                return false;
            }

            boolean isWhitelist = nbt.getBoolean("IsWhitelist");
            boolean matchMeta = nbt.getBoolean("UseMeta");
            boolean matchNBT = nbt.getBoolean("UseNBT");

            if (stack.getItem() == this.containerStack.getItem() &&
                (matchMeta == false || stack.getMetadata() == this.containerStack.getMetadata()) &&
                (matchNBT == false || ItemStack.areItemStackTagsEqual(stack, this.containerStack) == true))
            {
                return isWhitelist;
            }

            return isWhitelist == false;
        }
    }
}
