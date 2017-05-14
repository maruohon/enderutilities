package fi.dy.masa.enderutilities.tileentity;

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
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.gui.client.GuiQuickStackerAdvanced;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IItemHandlerModifiableProvider;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.ItemHandlerModifiableMuxer;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStackerAdvanced;
import fi.dy.masa.enderutilities.inventory.item.InventoryItem;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.item.ItemQuickStacker.Result;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.SlotRange;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityQuickStackerAdvanced extends TileEntityEnderUtilitiesInventory implements IModularInventoryHolder, IItemHandlerModifiableProvider
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

    private final ItemStackHandlerTileEntity inventoryFiltersAreaMode;
    private InventoryItemCallback inventoryFiltersBound;
    private InventoryItem inventoryFiltersBoundTemp;
    private final FilterSettings filtersAreaMode;
    private FilterSettings filtersBound;
    private boolean isAreaMode;
    private short enabledTargetsMask;
    private byte selectedTarget;
    private long slotMask;
    protected final Map<UUID, Long> clickTimes;

    public TileEntityQuickStackerAdvanced()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, NUM_TARGET_INVENTORIES * 2, 1, false, "Items", this);
        this.inventoryFiltersAreaMode = new ItemStackHandlerTileEntity(1, 36, 1, false, "FilterItems", this);
        this.filtersAreaMode = new FilterSettings(this.inventoryFiltersAreaMode);
        this.clickTimes = new HashMap<UUID, Long>();
    }

    private void initStorage(boolean isRemote)
    {
        this.inventoryFiltersBound = new InventoryItemCallback(null, 36, 1, false, isRemote, this, "FilterItems");
        this.inventoryFiltersBoundTemp = new InventoryItem(null, 36, 1, false, isRemote, "FilterItems");
        this.filtersBound = new FilterSettings(this.inventoryFiltersBound);
        this.readFilterSettingsFromModule(this.getContainerStack());
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainer(this.getBaseItemHandler(),
                new ItemHandlerWrapperQuickStackerAdvanced(this.getBaseItemHandler()));
    }

    public IItemHandler getFilterInventory()
    {
        return new ItemHandlerModifiableMuxer(this);
    }

    @Override
    public IItemHandler getInventory()
    {
        return this.getInventoryModifiable();
    }

    @Override
    public IItemHandlerModifiable getInventoryModifiable()
    {
        if (this.isAreaMode())
        {
            return this.inventoryFiltersAreaMode;
        }

        return this.inventoryFiltersBound;
    }

    public FilterSettings getSelectedFilterSettings()
    {
        if (this.isAreaMode())
        {
            return this.filtersAreaMode;
        }

        return this.filtersBound;
    }

    public FilterSettings getFilterSettings(int filterId)
    {
        if (this.isAreaMode())
        {
            return this.filtersAreaMode;
        }

        ItemStack container = this.getContainerStack(filterId);

        NBTTagCompound nbt = null;
        if (container != null)
        {
            nbt = NBTUtils.getCompoundTag(container, "AdvancedQuickStacker", false);
        }

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        FilterSettings filter = new FilterSettings(this.inventoryFiltersBoundTemp);
        this.inventoryFiltersBoundTemp.setContainerItemStack(container);
        filter.deserializeNBT(nbt);

        return filter;
    }

    public ItemStack getContainerStack()
    {
        //System.out.printf("getContainerStack: %s\n", this.worldObj.isRemote ? "client" : "server");
        return this.getContainerStack(this.selectedTarget);
    }

    public ItemStack getContainerStack(int filterId)
    {
        if (filterId < NUM_TARGET_INVENTORIES)
        {
            return this.getBaseItemHandler().getStackInSlot(filterId + NUM_TARGET_INVENTORIES);
        }

        return null;
    }

    public boolean isInventoryAccessible(EntityPlayer player)
    {
        return this.isAreaMode || this.getContainerStack() != null;
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        this.readFilterSettingsFromModule(this.getContainerStack());
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
        return this.filtersAreaMode.getAsBitMask();
    }

    public void setAreaModeSettings(byte mask)
    {
        this.filtersAreaMode.setFromBitMask(mask);
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
        this.readFilterSettingsFromModule(this.getContainerStack());
    }

    public long getEnabledSlotsMask()
    {
        return this.slotMask;
    }

    public void setEnabledSlotsMask(long mask)
    {
        this.slotMask = mask;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.isAreaMode = nbt.getBoolean("AreaMode");
        this.enabledTargetsMask = nbt.getByte("EnabledTargets");
        this.selectedTarget = nbt.getByte("SelectedTarget");
        this.slotMask = nbt.getLong("EnabledSlots");
        this.filtersAreaMode.deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("AreaMode", this.isAreaMode());
        nbt.setByte("EnabledTargets", (byte)this.enabledTargetsMask);
        nbt.setByte("SelectedTarget", this.selectedTarget);
        nbt.setLong("EnabledSlots", this.slotMask);
        nbt.merge(this.filtersAreaMode.serializeNBT());

        return nbt;
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        super.readItemsFromNBT(nbt);

        this.inventoryFiltersAreaMode.deserializeNBT(nbt);
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        nbt.merge(this.inventoryFiltersAreaMode.serializeNBT());
    }

    private void readFilterSettingsFromModule(ItemStack container)
    {
        if (this.isAreaMode() == false)
        {
            NBTTagCompound nbt = null;
            if (container != null)
            {
                nbt = NBTUtils.getCompoundTag(container, "AdvancedQuickStacker", false);
            }

            if (nbt == null)
            {
                nbt = new NBTTagCompound();
            }

            this.inventoryFiltersBound.setContainerItemStack(container);
            this.filtersBound.deserializeNBT(nbt);
        }
    }

    @Override
    public void onLoad()
    {
        this.initStorage(this.getWorld().isRemote);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        if (this.getWorld().isRemote == false)
        {
            Long last = this.clickTimes.get(player.getUniqueID());
            // Double left clicked fast enough - do the action
            if (last != null && this.getWorld().getTotalWorldTime() - last < 8)
            {
                // Area mode
                if (this.isAreaMode())
                {
                    quickStackToInventories(this.getWorld(), player, this.slotMask,
                            PositionUtils.getTileEntityPositions(this.getWorld(), this.getPos(), 16, 16, 16), this.getSelectedFilterSettings());
                }
                else
                {
                    this.quickStackToTargetInventories(player);
                }

                this.clickTimes.remove(player.getUniqueID());
            }
            else
            {
                this.clickTimes.put(player.getUniqueID(), this.getWorld().getTotalWorldTime());
            }
        }
    }

    protected void quickStackToTargetInventories(EntityPlayer player)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        boolean movedSome = false;

        for (int slot = 0, bit = 0x1; slot < NUM_TARGET_INVENTORIES; slot++)
        {
            if ((this.enabledTargetsMask & bit) != 0)
            {
                ItemStack lcStack = this.getBaseItemHandler().getStackInSlot(slot);
                if (lcStack != null)
                {
                    TargetData target = TargetData.getTargetFromItem(lcStack);
                    if (target != null && this.getWorld().isBlockLoaded(target.pos, true) &&
                        target.dimension == this.getWorld().provider.getDimension() &&
                        PositionUtils.isWithinRange(target.pos, this.getPos(), 32, 32, 32) && target.isTargetBlockUnchanged())
                    {
                        TileEntity te = this.getWorld().getTileEntity(target.pos);
                        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing))
                        {
                            IItemHandler externalInv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.facing);
                            if (externalInv != null)
                            {
                                FilterSettings filter = this.getFilterSettings(slot);
                                Result result = quickStackItems(playerInv, externalInv, this.slotMask, player.isSneaking() == false, filter);

                                if (result != Result.MOVED_NONE)
                                {
                                    Effects.spawnParticlesFromServer(player.getEntityWorld().provider.getDimension(), target.pos, EnumParticleTypes.VILLAGER_HAPPY);
                                    movedSome = true;
                                }
                            }
                        }
                    }
                }
            }

            bit <<= 1;
        }

        if (movedSome)
        {
            player.getEntityWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.5f, 1.8f);
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
            if ((slotMask & bit) != 0 && stack != null && (filter == null || filter.itemAllowedByFilter(stack)))
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

        if (movedAll && ret == Result.MOVED_SOME)
        {
            ret = Result.MOVED_ALL;
        }

        return ret;
    }

    public static void quickStackToInventories(World world, EntityPlayer player, long enabledSlotsMask, List<BlockPosDistance> positions)
    {
        quickStackToInventories(world, player, enabledSlotsMask, positions, null);
    }

    public static void quickStackToInventories(World world, EntityPlayer player, long enabledSlotsMask, List<BlockPosDistance> positions, FilterSettings filter)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        boolean movedSome = false;

        for (BlockPosDistance posDist : positions)
        {
            TileEntity te = world.getTileEntity(posDist.pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP))
            {
                IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

                if (inv != null)
                {
                    Result result = quickStackItems(playerInv, inv, enabledSlotsMask, player.isSneaking() == false, filter);

                    if (result != Result.MOVED_NONE)
                    {
                        Effects.spawnParticlesFromServer(world.provider.getDimension(), posDist.pos, EnumParticleTypes.VILLAGER_HAPPY);
                        movedSome = true;
                    }

                    if (result == Result.MOVED_ALL)
                    {
                        break;
                    }
                }
            }
        }

        if (movedSome)
        {
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.5f, 1.8f);
        }
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
                return stack.getItem() == EnderUtilitiesItems.LINK_CRYSTAL && ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK;
            }

            return stack.getItem() == EnderUtilitiesItems.ENDER_PART && ((IModule)stack.getItem()).getModuleType(stack) == ModuleType.TYPE_MEMORY_CARD_MISC;
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == GUI_ACTION_SET_ACTIVE_TARGET)
        {
            if (element >= 0 && element < NUM_TARGET_INVENTORIES)
            {
                this.inventoryFiltersBound.onContentsChanged(element);
                this.setSelectedTarget((byte)element);
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
        FilterSettings filter = this.getSelectedFilterSettings();

        switch (element)
        {
            case 0: filter.toggleIsEnabled(); break;
            case 1: filter.toggleIsBlacklist(); break;
            case 2: filter.toggleMatchMeta(); break;
            case 3: filter.toggleMatchNBT(); break;
        }

        if (this.isAreaMode() == false)
        {
            ItemStack container = this.getContainerStack();
            if (container != null)
            {
                NBTTagCompound nbt = NBTUtils.getCompoundTag(container, "AdvancedQuickStacker", true);
                nbt.merge(filter.serializeNBT());
            }
        }
    }

    @Override
    public ContainerQuickStackerAdvanced getContainer(EntityPlayer player)
    {
        return new ContainerQuickStackerAdvanced(player, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiQuickStackerAdvanced(this.getContainer(player), this);
    }

    public class FilterSettings implements INBTSerializable<NBTTagCompound>
    {
        private final ItemStackHandlerBasic filterInventory;
        private boolean enabled = true;
        private boolean isBlacklist;
        private boolean matchMeta = true;
        private boolean matchNBT;

        private FilterSettings(ItemStackHandlerBasic filterInventory)
        {
            this.filterInventory = filterInventory;
        }

        public IItemHandlerModifiable getFilterInventory()
        {
            return this.filterInventory;
        }

        public boolean isEnabled()
        {
            return this.enabled;
        }

        public boolean isBlacklist()
        {
            return this.isBlacklist;
        }

        public boolean getMatchMeta()
        {
            return this.matchMeta;
        }

        public boolean getMatchNBT()
        {
            return this.matchNBT;
        }

        public void toggleIsEnabled()
        {
            this.enabled = ! this.enabled;
        }

        public void toggleIsBlacklist()
        {
            this.isBlacklist = ! this.isBlacklist;
        }

        public void toggleMatchMeta()
        {
            this.matchMeta = ! this.matchMeta;
        }

        public void toggleMatchNBT()
        {
            this.matchNBT = ! this.matchNBT;
        }

        public boolean itemAllowedByFilter(ItemStack stack)
        {
            if (this.enabled == false)
            {
                return true;
            }

            if (InventoryUtils.matchingStackFoundInSlotRange(this.filterInventory,
                    new SlotRange(this.filterInventory), stack, ! this.matchMeta, ! this.matchNBT))
            {
                return this.isBlacklist == false;
            }

            return this.isBlacklist;
        }

        public byte getAsBitMask()
        {
            byte mask = 0;

            if (this.isEnabled())    { mask |= 0x01; }
            if (this.isBlacklist())  { mask |= 0x02; }
            if (this.getMatchMeta()) { mask |= 0x04; }
            if (this.getMatchNBT())  { mask |= 0x08; }

            return mask;
        }

        public void setFromBitMask(byte mask)
        {
            this.enabled        = (mask & 0x01) != 0;
            this.isBlacklist    = (mask & 0x02) != 0;
            this.matchMeta      = (mask & 0x04) != 0;
            this.matchNBT       = (mask & 0x08) != 0;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            NBTTagCompound tag = nbt.getCompoundTag("FilterSettings");
            this.enabled = tag.getBoolean("Ignored") == false;
            this.isBlacklist = tag.getBoolean("IsBlacklist");
            this.matchMeta = tag.getBoolean("IgnoreMeta") == false;
            this.matchNBT = tag.getBoolean("MatchNBT");
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("Ignored", ! this.enabled);
            tag.setBoolean("IsBlacklist", this.isBlacklist);
            tag.setBoolean("IgnoreMeta", ! this.matchMeta);
            tag.setBoolean("MatchNBT", this.matchNBT);

            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("FilterSettings", tag);

            return nbt;
        }

        @Override
        public String toString()
        {
            return String.format("Filter{enabled=%s,isBlacklist:%s,matchMeta=%s,matchNBT=%s}\n",
                    this.enabled, this.isBlacklist, this.matchMeta, this.matchNBT);
        }
    }
}
