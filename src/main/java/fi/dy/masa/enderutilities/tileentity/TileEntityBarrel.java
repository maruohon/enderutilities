package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.block.BlockBarrel;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiBarrel;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerLockable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerBarrel;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperCreative;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart.ItemPartType;
import fi.dy.masa.enderutilities.network.message.ISyncableTile;
import fi.dy.masa.enderutilities.network.message.MessageSyncTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TileEntityBarrel extends TileEntityEnderUtilitiesInventory implements ISyncableTile
{
    private ItemStackHandlerLockable itemHandlerLockable;
    private ItemHandlerBarrelUpgrades itemHandlerUpgrades;
    private Map<UUID, Long> rightClickTimes = new HashMap<UUID, Long>();
    private List<EnumFacing> labels = new ArrayList<EnumFacing>();
    private int labelMask;
    private int maxStacks = 64;
    private boolean hasStructureUpgrade;
    private boolean hasVoidUpgrade;
    private ItemStack cachedStack = ItemStack.EMPTY;
    public ItemStack renderStack = ItemStack.EMPTY;
    public String cachedStackSizeString;
    public float cachedFullness;

    public TileEntityBarrel()
    {
        super(ReferenceNames.NAME_TILE_BARREL, true);

        this.initStorage();
    }

    private void initStorage()
    {
        int maxUpgrades = Configs.barrelMaxCapacityUpgrades;
        this.itemHandlerLockable    = new ItemStackHandlerLockable(0, 1, 4096, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerLockable;
        this.itemHandlerUpgrades    = new ItemHandlerBarrelUpgrades(1, 4, maxUpgrades, true, "ItemsUpgrades", this);
        this.itemHandlerExternal    = new ItemHandlerBarrel(this.itemHandlerLockable, this);
    }

    public ItemStackHandlerLockable getInventoryBarrel()
    {
        return this.itemHandlerLockable;
    }

    public IItemHandler getUpgradeInventory()
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerUpgrades, this.itemHandlerUpgrades, true);
    }

    public boolean retainsContentsWhenBroken()
    {
        return this.hasStructureUpgrade;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    /**
     * @return true if there are more items in the barrel than what can be spawned in the world
     */
    public boolean isOverSpillCapacity()
    {
        ItemStack stack = this.itemHandlerLockable.getStackInSlot(0);
        return stack.isEmpty() == false && stack.getCount() > 4096;
    }

    public List<EnumFacing> getLabeledFaces()
    {
        return this.labels;
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        this.itemHandlerUpgrades.deserializeNBT(nbt);

        super.readItemsFromNBT(nbt);

        this.hasStructureUpgrade = this.itemHandlerUpgrades.getStackInSlot(1).isEmpty() == false;
        this.hasVoidUpgrade = this.itemHandlerUpgrades.getStackInSlot(3).isEmpty() == false;
        ItemStack stack = this.itemHandlerLockable.getStackInSlot(0);
        this.cachedStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.itemHandlerUpgrades.serializeNBT());

        super.writeItemsToNBT(nbt);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setCreative(nbt.getBoolean("Creative"));
        this.setLabelsFromMask(nbt.getByte("Labels"));

        this.updateLabels(false);
        this.setMaxStacksFromUpgrades();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        nbt.setByte("Labels", (byte) this.labelMask);
        nbt.setBoolean("Creative", this.isCreative());

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        ItemStack stack = this.itemHandlerExternal.getStackInSlot(0);

        if (stack.isEmpty() == false)
        {
            nbt.setTag("st", NBTUtils.storeItemStackInTag(stack, new NBTTagCompound()));
        }

        stack = this.itemHandlerLockable.getTemplateStackInSlot(0);

        if (stack.isEmpty() == false)
        {
            // The template stack is always size 1, no need to use our handler for it
            nbt.setTag("stl", stack.writeToNBT(new NBTTagCompound()));
        }

        nbt.setBoolean("cr", this.isCreative());
        nbt.setByte("la", (byte) this.getLabelMask(true));
        nbt.setBoolean("lo", this.itemHandlerLockable.isSlotLocked(0));
        nbt.setInteger("mxs", this.maxStacks);
        nbt.setBoolean("stu", this.hasStructureUpgrade);

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.hasStructureUpgrade = tag.getBoolean("stu");
        this.setCreative(tag.getBoolean("cr"));
        this.setLabelsFromMask(tag.getByte("la"));
        this.itemHandlerLockable.setSlotLocked(0, tag.getBoolean("lo"));

        if (tag.hasKey("stl", Constants.NBT.TAG_COMPOUND))
        {
            this.itemHandlerLockable.setTemplateStackInSlot(0, new ItemStack(tag.getCompoundTag("stl")));
        }

        if (tag.hasKey("st", Constants.NBT.TAG_COMPOUND))
        {
            this.itemHandlerLockable.setStackInSlot(0, NBTUtils.loadItemStackFromTag(tag.getCompoundTag("st")));
        }

        this.setMaxStacks(tag.getInteger("mxs"));
        this.updateCachedStack();

        super.handleUpdateTag(tag);
    }

    @Override
    public void rotate(Rotation rotationIn)
    {
        List<EnumFacing> newList = new ArrayList<EnumFacing>();

        for (EnumFacing side : this.labels)
        {
            newList.add(rotationIn.rotate(side));
        }

        this.labels.clear();
        this.labels.addAll(newList);
        this.labelMask = this.getLabelMask(false);

        super.rotate(rotationIn);
    }

    @Override
    public void setFacing(EnumFacing facing)
    {
        super.setFacing(facing);

        this.updateLabels(true);
    }

    private void updateLabels(boolean notifyBlockUpdate)
    {
        if (this.getWorld() != null && this.getWorld().isRemote == false)
        {
            if (this.itemHandlerUpgrades.getStackInSlot(0).isEmpty())
            {
                this.labels.clear();
            }

            if (this.labels.contains(this.getFacing()) == false)
            {
                this.labels.add(this.getFacing());
            }

            this.labelMask = this.getLabelMask(false);

            if (notifyBlockUpdate)
            {
                IBlockState state = this.getWorld().getBlockState(this.getPos());
                this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
            }
        }
    }

    private void setMaxStacksFromUpgrades()
    {
        ItemStack stackCapacityUpgrades = this.itemHandlerUpgrades.getStackInSlot(2);
        int upgrades = stackCapacityUpgrades.isEmpty() == false ? stackCapacityUpgrades.getCount() : 0;

        this.setMaxStacks(64 + upgrades * Configs.barrelCapacityUpgradeStacksPer);
    }

    private void setMaxStacks(int maxStacks)
    {
        this.maxStacks = maxStacks;
        ItemStack stack = this.itemHandlerLockable.getStackInSlot(0);
        this.itemHandlerLockable.setStackLimit(this.maxStacks * (stack.isEmpty() == false ? stack.getMaxStackSize() : 64));
    }

    public int getLabelMask(boolean cached)
    {
        if (cached)
        {
            return this.labelMask;
        }

        int mask = 0;

        for (EnumFacing side : this.labels)
        {
            mask |= 1 << side.getIndex();
        }

        return mask;
    }

    private void setLabelsFromMask(int mask)
    {
        this.labels.clear();
        this.labelMask = mask;

        for (EnumFacing side : EnumFacing.values())
        {
            if ((mask & (1 << side.getIndex())) != 0)
            {
                this.labels.add(side);
            }
        }
    }

    private boolean tryApplyLabel(EntityPlayer player, EnumHand hand, EnumFacing side)
    {
        if (this.labels.contains(side) == false && side != this.getFacing() &&
            this.tryApplyUpgrade(player, hand, 0, EnderUtilitiesItems.ENDER_PART, 70))
        {
            this.labels.add(side);
            return true;
        }

        return false;
    }

    private boolean tryApplyUpgrade(EntityPlayer player, EnumHand hand, int slot, Item item, int meta)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItem() == item && stack.getMetadata() == meta)
        {
            if (this.itemHandlerUpgrades.insertItem(slot, new ItemStack(EnderUtilitiesItems.ENDER_PART, 1, meta), false).isEmpty())
            {
                if (player.capabilities.isCreativeMode == false)
                {
                    stack.shrink(1);
                    player.setHeldItem(hand, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }

                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_PLACE, SoundCategory.BLOCKS, 1f, 1f);

                return true;
            }
        }

        return false;
    }

    private boolean tryToggleLocked(ItemStack stack)
    {
        if (ItemEnderPart.itemMatches(stack, ItemPartType.STORAGE_KEY))
        {
            this.itemHandlerLockable.setTemplateStackInSlot(0, this.itemHandlerLockable.getStackInSlot(0));
            this.itemHandlerLockable.toggleSlotLocked(0);

            int[] ints = new int[] { this.itemHandlerLockable.isSlotLocked(0) ? 1 : 0 };
            ItemStack[] stacks = new ItemStack[] { this.itemHandlerLockable.getTemplateStackInSlot(0) };
            this.sendPacketToWatchers(new MessageSyncTileEntity(this.getPos(), ints, stacks));
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.7f, 1f);

            return true;
        }

        return false;
    }

    private boolean tryToggleCreativeMode(EntityPlayer player, ItemStack stack)
    {
        if (ItemEnderPart.itemMatches(stack, ItemPartType.CREATIVE_STORAGE_KEY))
        {
            return this.toggleCreativeMode(player, true);
        }

        return false;
    }

    @Override
    public boolean onRightClickBlock(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.isEmpty() == false)
        {
            if (this.tryToggleLocked(stack) ||
                this.tryApplyLabel(player, hand, side) ||
                this.tryApplyUpgrade(player, hand, 1, EnderUtilitiesItems.ENDER_PART, 71) || // Structure Upgrade
                this.tryApplyUpgrade(player, hand, 2, EnderUtilitiesItems.ENDER_PART, 72) || // Capacity Upgrade
                this.tryApplyUpgrade(player, hand, 3, EnderUtilitiesItems.ENDER_PART, 73) || // Void Upgrade
                this.tryToggleCreativeMode(player, stack))
            {
                return true;
            }

            stack = InventoryUtils.tryInsertItemStackToInventory(this.itemHandlerExternal, stack);
            player.setHeldItem(hand, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }

        long time = System.currentTimeMillis();
        Long last = this.rightClickTimes.get(player.getUniqueID());

        if (last != null && time - last.longValue() < 300)
        {
            InventoryUtils.tryMoveMatchingItems(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), this.itemHandlerExternal);
            player.openContainer.detectAndSendChanges();
        }

        this.rightClickTimes.put(player.getUniqueID(), time);

        // When this method returns false, then the GUI is opened
        return player.isSneaking() == false;
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        long time = System.currentTimeMillis();
        Long last = this.rightClickTimes.get(player.getUniqueID());

        // Prevent the weird double clicking issue by adding a minimum threshold
        if (last != null && time - last.longValue() < 80)
        {
            return;
        }

        this.rightClickTimes.put(player.getUniqueID(), time);

        int amount = player.isSneaking() ? 1 : 64;
        ItemStack stack = this.itemHandlerExternal.extractItem(0, amount, false);

        if (stack.isEmpty() == false)
        {
            EnumFacing side = this.getFacing();
            RayTraceResult rayTrace = EntityUtils.getRayTraceFromPlayer(this.getWorld(), player, false);

            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && this.getPos().equals(rayTrace.getBlockPos()))
            {
                side = rayTrace.sideHit;
            }

            EntityUtils.dropItemStacksInWorld(this.getWorld(), this.getSpawnedItemPosition(side), stack, -1, true, false);
        }
    }

    private boolean toggleCreativeMode(EntityPlayer player, boolean isStorageKey)
    {
        if (isStorageKey || player.capabilities.isCreativeMode)
        {
            this.setCreative(! this.isCreative());
            this.markDirty();

            IBlockState state = this.getWorld().getBlockState(this.getPos());
            state = state.withProperty(BlockBarrel.CREATIVE, this.isCreative());
            this.getWorld().setBlockState(this.getPos(), state);

            if (isStorageKey)
            {
                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.7f, 1f);
            }

            return true;
        }

        return false;
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 1)
        {
            this.toggleCreativeMode(player, false);
        }
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        if (this.getWorld() != null && this.getWorld().isRemote)
        {
            return;
        }

        // Main inventory contents changed
        if (inventoryId == 0)
        {
            ItemStack stack = this.itemHandlerLockable.getStackInSlot(0);

            if (InventoryUtils.areItemStacksEqual(stack, this.cachedStack) == false)
            {
                this.setMaxStacksFromUpgrades();
                this.cachedStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();

                this.sendPacketToWatchers(new MessageSyncTileEntity(this.getPos(), this.cachedStack));
            }
            else if (stack.isEmpty() == false && stack.getCount() != this.cachedStack.getCount())
            {
                this.cachedStack.setCount(stack.getCount());
                this.sendPacketToWatchers(new MessageSyncTileEntity(this.getPos(), this.cachedStack.getCount(), this.maxStacks));
            }
        }
        // Upgrades changed
        else if (inventoryId == 1)
        {
            if (slot == 0)
            {
                ItemStack stack = this.itemHandlerUpgrades.getStackInSlot(0);

                // Clear all labels if the label slot's stack size shrinks below the labeled face count
                if (stack.isEmpty() || stack.getCount() < this.labels.size() - 1)
                {
                    this.labels.clear();
                    this.labels.add(this.getFacing());
                }

                this.updateLabels(true);
            }
            else if (slot == 1)
            {
                this.hasStructureUpgrade = this.itemHandlerUpgrades.getStackInSlot(1).isEmpty() == false;
                this.notifyBlockUpdate(this.getPos());
            }
            else if (slot == 2)
            {
                this.setMaxStacksFromUpgrades();
            }
            else if (slot == 3)
            {
                this.hasVoidUpgrade = this.itemHandlerUpgrades.getStackInSlot(3).isEmpty() == false;
                this.notifyBlockUpdate(this.getPos());
            }

            int stackSize = this.cachedStack.isEmpty() ? 0 : this.cachedStack.getCount();
            this.sendPacketToWatchers(new MessageSyncTileEntity(this.getPos(), stackSize, this.maxStacks));
        }
    }

    @Override
    public void syncTile(int[] intValues, ItemStack[] stacks)
    {
        // Toggling the locked status
        if (stacks.length == 1 && intValues.length == 1)
        {
            this.itemHandlerLockable.setSlotLocked(0, intValues[0] == 1);
            this.itemHandlerLockable.setTemplateStackInSlot(0, stacks[0]);
        }
        // Stored item has changed
        else if (stacks.length == 1)
        {
            this.itemHandlerLockable.setStackInSlot(0, stacks[0]);
        }
        // Stored item amount has changed, but the item type not
        else if (intValues.length == 2)
        {
            this.setMaxStacks(intValues[1]);

            if (this.itemHandlerLockable.getStackInSlot(0).isEmpty() == false)
            {
                this.itemHandlerLockable.getStackInSlot(0).setCount(intValues[0]);
            }
        }

        this.updateCachedStack();
    }

    private void updateCachedStack()
    {
        ItemStack stackStored = this.itemHandlerLockable.getStackInSlot(0);
        ItemStack stackTemplate = this.itemHandlerLockable.getTemplateStackInSlot(0);

        // The renderStack is used in the TESR renderer
        this.renderStack = stackStored.isEmpty() == false ? stackStored : stackTemplate;

        // If there are actually items stored, then display the amount
        if (stackStored.isEmpty() == false)
        {
            int count = stackStored.getCount();
            int max = stackStored.getMaxStackSize();
            int stacks = count / max;
            int remainder = count % max;

            if (max == 1 || count <= max)
            {
                this.cachedStackSizeString = String.valueOf(count);
            }
            else if (remainder != 0)
            {
                this.cachedStackSizeString = String.format("%dx%d + %d", stacks, max, remainder);
            }
            else
            {
                this.cachedStackSizeString = String.format("%dx%d", stacks, max);
            }

            this.cachedFullness = (float) count / ((float) this.maxStacks * max);
        }
        // No items stored (possibly a template/locked stack)
        else
        {
            this.cachedStackSizeString = "-";
            this.cachedFullness = 0f;
        }
    }

    protected Vec3d getSpawnedItemPosition(EnumFacing side)
    {
        double x = this.getPos().getX() + 0.5 + side.getFrontOffsetX() * 0.625;
        double y = this.getPos().getY() + 0.5 + side.getFrontOffsetY() * 0.5;
        double z = this.getPos().getZ() + 0.5 + side.getFrontOffsetZ() * 0.625;

        if (side == EnumFacing.DOWN)
        {
            y -= 0.25;
        }

        return new Vec3d(x, y, z);
    }

    private class ItemHandlerBarrel extends ItemHandlerWrapperCreative
    {
        private final TileEntityBarrel te;

        public ItemHandlerBarrel(IItemHandler baseHandler, TileEntityBarrel te)
        {
            super(baseHandler, te);

            this.te = te;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            stack = super.insertItem(slot, stack, simulate);

            // Barrel is full (the super handler can't insert, but the item is identical), and has a void upgrade
            if (this.te.isCreative() == false &&
                this.te.hasVoidUpgrade &&
                this.getStackInSlot(slot).getCount() == this.getInventoryStackLimit() &&
                stack.isEmpty() == false &&
                InventoryUtils.areItemStacksEqual(stack, this.getStackInSlot(slot)))
            {
                return ItemStack.EMPTY;
            }

            return stack;
        }
    }

    private class ItemHandlerBarrelUpgrades extends ItemStackHandlerTileEntity
    {
        private final TileEntityBarrel te;

        public ItemHandlerBarrelUpgrades(int inventoryId, int invSize, int stackLimit,
                boolean allowCustomStackSizes, String tagName, TileEntityBarrel te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);

            this.te = te;
        }

        @Override
        public int getItemStackLimit(int slot, ItemStack stack)
        {
            if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.ENDER_PART)
            {
                int meta = stack.getMetadata();

                if (meta == 71 || meta == 73) // Barrel Structural Upgrade or Void Upgrade
                {
                    return 1;
                }
                else if (meta == 72) // Barrel Capacity Upgrade
                {
                    return this.getInventoryStackLimit();
                }
            }

            return 64;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.ENDER_PART)
            {
                int meta = stack.getMetadata();

                switch (slot)
                {
                    case 0: return meta == 70; // Barrel Label
                    case 1: return meta == 71; // Barrel Structural Upgrade
                    case 2: return meta == 72; // Barrel Capacity Upgrade
                    case 3: return meta == 73; // Barrel Void Upgrade
                    default: return false;
                }
            }

            return false;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            // Capacity upgrades can only be removed for those upgrades that are currently not needed
            if (slot == 2)
            {
                ItemStack stackStored = this.te.itemHandlerLockable.getStackInSlot(0);
                ItemStack upgradeStack = this.getStackInSlot(2);

                if (stackStored.isEmpty() == false && stackStored.getCount() > 4096 && upgradeStack.isEmpty() == false)
                {
                    int needed = (int) Math.ceil(((double) (stackStored.getCount() - 4096) / stackStored.getMaxStackSize()) / Configs.barrelCapacityUpgradeStacksPer);
                    int toRemove = Math.min(amount, upgradeStack.getCount() - needed);

                    if (toRemove > 0)
                    {
                        return super.extractItem(slot, toRemove, simulate);
                    }

                    return ItemStack.EMPTY;
                }
            }

            return super.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public ContainerBarrel getContainer(EntityPlayer player)
    {
        return new ContainerBarrel(player, this);
    }

    @Override
    public Object getGui(EntityPlayer player)
    {
        return new GuiBarrel(this.getContainer(player), this);
    }
}
