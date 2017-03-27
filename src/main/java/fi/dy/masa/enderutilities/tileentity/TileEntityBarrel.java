package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiBarrel;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSize;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerBarrel;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperCreative;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.ISyncableTile;
import fi.dy.masa.enderutilities.network.message.MessageSyncTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityBarrel extends TileEntityEnderUtilitiesInventory implements ISyncableTile
{
    private ItemHandlerBarrelUpgrades itemHandlerUpgrades;
    private Map<UUID, Long> rightClickTimes = new HashMap<UUID, Long>();
    private List<EnumFacing> labels = new ArrayList<EnumFacing>();
    private int labelMask;
    private int maxStacks = 64;
    public ItemStack cachedStack;
    public String cachedStackSizeString;

    public TileEntityBarrel()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_BARREL, true);

        this.initStorage();
    }

    private void initStorage()
    {
        int maxUpgrades = Configs.barrelMaxCapacityUpgrades;
        this.itemHandlerBase        = new ItemStackHandlerTileEntity(0, 1, 4096, true, "Items", this);
        this.itemHandlerUpgrades    = new ItemHandlerBarrelUpgrades(1, 3, maxUpgrades, true, "ItemsUpgrades", this);
        this.itemHandlerExternal    = new ItemHandlerWrapperCreative(this.itemHandlerBase, this);
    }

    public IItemHandler getUpgradeInventory()
    {
        return new ItemHandlerWrapperContainerBarrelUpgrades(this.itemHandlerUpgrades);
    }

    public int getMaxStacks()
    {
        return this.maxStacks;
    }

    public boolean retainsContentsWhenBroken()
    {
        return this.itemHandlerUpgrades.getStackInSlot(1) != null;
    }

    /**
     * @return true if there are more items in the barrel than what can be spawned in the world
     */
    public boolean isOverSpillCapacity()
    {
        ItemStack stack = this.itemHandlerBase.getStackInSlot(0);
        return stack != null && stack.stackSize > 4096;
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
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        nbt.merge(this.itemHandlerUpgrades.serializeNBT());

        super.writeItemsToNBT(nbt);

        this.cachedStack = ItemStack.copyItemStack(this.itemHandlerBase.getStackInSlot(0));
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setCreative(nbt.getBoolean("Creative"));
        this.setLabelsFromMask(nbt.getByte("Labels"));
        this.updateBarrelProperties(true);
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

        if (stack != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            tag.setInteger("ac", stack.stackSize);
            nbt.setTag("st", tag);
        }

        nbt.setBoolean("cr", this.isCreative());
        nbt.setByte("la", (byte) this.getLabelMask(true));

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("st", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound tmp = tag.getCompoundTag("st");
            ItemStack stack = ItemStack.loadItemStackFromNBT(tmp);

            if (stack != null && tmp.hasKey("ac", Constants.NBT.TAG_INT))
            {
                stack.stackSize = tmp.getInteger("ac");
            }

            this.setCachedStack(stack);
        }

        this.setCreative(tag.getBoolean("cr"));
        this.setLabelsFromMask(tag.getByte("la"));

        super.handleUpdateTag(tag);
    }

    @Override
    public void rotate(Rotation rotationIn)
    {
        super.rotate(rotationIn);

        List<EnumFacing> newList = new ArrayList<EnumFacing>();

        for (EnumFacing side : this.labels)
        {
            newList.add(rotationIn.rotate(side));
        }

        this.labels = newList;
        this.labelMask = this.getLabelMask(false);
    }

    @Override
    public void setFacing(EnumFacing facing)
    {
        super.setFacing(facing);

        this.updateBarrelProperties(false);
    }

    public void updateBarrelProperties(boolean updateLabels)
    {
        this.updateMaxStackSize();

        if (updateLabels && this.itemHandlerUpgrades.getStackInSlot(0) == null)
        {
            this.labels.clear();
            this.labels.add(this.getFacing());
        }
        else if (this.labels.contains(this.getFacing()) == false)
        {
            this.labels.add(this.getFacing());
        }

        this.labelMask = this.getLabelMask(false);

        if (this.getWorld() != null)
        {
            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
        }
    }

    private void updateMaxStackSize()
    {
        ItemStack stackCapacityUpgrades = this.itemHandlerUpgrades.getStackInSlot(2);
        ItemStack stack = this.itemHandlerBase.getStackInSlot(0);

        int upgrades = stackCapacityUpgrades != null ? stackCapacityUpgrades.stackSize : 0;
        this.maxStacks = 64 + upgrades * Configs.barrelCapacityUpgradeStacksPer;

        this.itemHandlerBase.setStackLimit(this.maxStacks * (stack != null ? stack.getMaxStackSize() : 64));
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

    private boolean applyLabel(EntityPlayer player, EnumHand hand, @Nonnull ItemStack stack)
    {
        if (stack.getItem() == EnderUtilitiesItems.enderPart && stack.getMetadata() == 70)
        {
            RayTraceResult rayTrace = EntityUtils.getRayTraceFromPlayer(this.getWorld(), player, false);

            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && this.getPos().equals(rayTrace.getBlockPos()))
            {
                EnumFacing side = rayTrace.sideHit;

                if (this.labels.contains(side) == false && side != this.getFacing())
                {
                    this.itemHandlerUpgrades.insertItem(0, new ItemStack(EnderUtilitiesItems.enderPart, 1, 70), false);

                    if (player.capabilities.isCreativeMode == false)
                    {
                        stack.stackSize--;
                        player.setHeldItem(hand, stack.stackSize > 0 ? stack : null);
                    }

                    this.labels.add(side);
                    this.updateBarrelProperties(true);
                    this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                }
            }

            return true;
        }

        return false;
    }

    private boolean applyStructureUpgrade(EntityPlayer player, EnumHand hand, @Nonnull ItemStack stack)
    {
        if (stack.getItem() == EnderUtilitiesItems.enderPart && stack.getMetadata() == 71)
        {
            if (this.itemHandlerUpgrades.getStackInSlot(1) == null)
            {
                this.itemHandlerUpgrades.insertItem(1, new ItemStack(EnderUtilitiesItems.enderPart, 1, 71), false);

                if (player.capabilities.isCreativeMode == false)
                {
                    stack.stackSize--;
                    player.setHeldItem(hand, stack.stackSize > 0 ? stack : null);
                }

                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            }

            return true;
        }

        return false;
    }

    private boolean applyCapacityUpgrade(EntityPlayer player, EnumHand hand, @Nonnull ItemStack stack)
    {
        if (stack.getItem() == EnderUtilitiesItems.enderPart && stack.getMetadata() == 72)
        {
            if (this.itemHandlerUpgrades.insertItem(2, new ItemStack(EnderUtilitiesItems.enderPart, 1, 72), false) == null)
            {
                stack.stackSize--;
                player.setHeldItem(hand, stack.stackSize > 0 ? stack : null);
                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_PLACE, SoundCategory.BLOCKS, 1f, 1f);
            }

            return true;
        }

        return false;
    }

    public void onRightClick(EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack != null)
        {
            if (this.applyLabel(player, hand, stack))
            {
                return;
            }

            if (this.applyStructureUpgrade(player, hand, stack))
            {
                return;
            }

            if (this.applyCapacityUpgrade(player, hand, stack))
            {
                return;
            }

            stack = InventoryUtils.tryInsertItemStackToInventory(this.itemHandlerExternal, stack);
            player.setHeldItem(hand, stack);
        }

        long time = System.currentTimeMillis();
        Long last = this.rightClickTimes.get(player.getUniqueID());

        if (last != null && time - last.longValue() < 300)
        {
            InventoryUtils.tryMoveMatchingItems(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), this.itemHandlerExternal);
            player.openContainer.detectAndSendChanges();
        }

        this.rightClickTimes.put(player.getUniqueID(), time);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        int amount = player.isSneaking() ? 1 : 64;
        ItemStack stack = this.itemHandlerExternal.extractItem(0, amount, false);

        if (stack != null)
        {
            RayTraceResult rayTrace = EntityUtils.getRayTraceFromPlayer(this.getWorld(), player, false);

            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && this.getPos().equals(rayTrace.getBlockPos()))
            {
                EntityUtils.dropItemStacksInWorld(this.getWorld(), this.getSpawnedItemPosition(rayTrace.sideHit), stack, -1, true, false);
            }
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 1)
        {
            if (player.capabilities.isCreativeMode)
            {
                this.setCreative(! this.isCreative());
                this.markDirty();

                IBlockState state = this.getWorld().getBlockState(this.getPos());
                this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
            }
        }
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        // Main inventory contents changed
        if (inventoryId == 0)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(0);

            if (InventoryUtils.areItemStacksEqual(stack, this.cachedStack) == false)
            {
                this.updateMaxStackSize();
                this.cachedStack = ItemStack.copyItemStack(stack);

                this.sendSyncPacket(new MessageSyncTileEntity(this.getPos(), this.cachedStack));
            }
            else if (stack != null && stack.stackSize != this.cachedStack.stackSize)
            {
                this.cachedStack.stackSize = stack.stackSize;
                this.sendSyncPacket(new MessageSyncTileEntity(this.getPos(), this.cachedStack.stackSize));
            }
        }
        // Upgrades changed
        else if (inventoryId == 1)
        {
            this.updateBarrelProperties(true);
        }
    }

    private void sendSyncPacket(MessageSyncTileEntity message)
    {
        World world = this.getWorld();

        if (world instanceof WorldServer)
        {
            WorldServer worldServer = (WorldServer) world;
            int chunkX = this.getPos().getX() >> 4;
            int chunkZ = this.getPos().getZ() >> 4;
            PlayerChunkMap map = worldServer.getPlayerChunkMap();

            for (EntityPlayerMP player : worldServer.getPlayers(EntityPlayerMP.class, Predicates.alwaysTrue()))
            {
                if (map.isPlayerWatchingChunk(player, chunkX, chunkZ))
                {
                    PacketHandler.INSTANCE.sendTo(message, player);
                }
            }
        }
    }

    @Override
    public void syncTile(int[] intValues, ItemStack[] stacks)
    {
        if (stacks.length == 1)
        {
            this.setCachedStack(stacks[0]);
        }
        else if (intValues.length == 1 && this.cachedStack != null)
        {
            this.cachedStack.stackSize = intValues[0];
            // Update the cached item count string
            this.setCachedStack(this.cachedStack);
        }
    }

    private void setCachedStack(ItemStack stack)
    {
        this.cachedStack = stack;

        if (stack != null)
        {
            int max = stack.getMaxStackSize();
            int stacks = stack.stackSize / max;
            int remainder = stack.stackSize % max;

            if (max == 1 || stack.stackSize <= max)
            {
                this.cachedStackSizeString = String.valueOf(stack.stackSize);
            }
            else if (remainder != 0)
            {
                this.cachedStackSizeString = String.format("%dx%d + %d", stacks, max, remainder);
            }
            else
            {
                this.cachedStackSizeString = String.format("%dx%d", stacks, max);
            }
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

    private class ItemHandlerWrapperContainerBarrelUpgrades extends ItemHandlerWrapperContainer implements IItemHandlerSize
    {
        private final ItemHandlerBarrelUpgrades barrelUpgrades;

        public ItemHandlerWrapperContainerBarrelUpgrades(ItemHandlerBarrelUpgrades baseHandler)
        {
            super(baseHandler, baseHandler, true);

            this.barrelUpgrades = baseHandler;
        }

        @Override
        public int getInventoryStackLimit()
        {
            return this.barrelUpgrades.getInventoryStackLimit();
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            return this.barrelUpgrades.getItemStackLimit(stack);
        }
    }

    private class ItemHandlerBarrelUpgrades extends ItemStackHandlerTileEntity
    {
        private final TileEntityBarrel te;

        public ItemHandlerBarrelUpgrades(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName, TileEntityBarrel te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);

            this.te = te;
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            if (stack != null && stack.getItem() == EnderUtilitiesItems.enderPart)
            {
                int meta = stack.getMetadata();

                if (meta == 71) // Barrel Structural Upgrade
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
            if (stack != null && stack.getItem() == EnderUtilitiesItems.enderPart)
            {
                int meta = stack.getMetadata();

                if (slot == 0) // Barrel Label
                {
                    return meta == 70;
                }
                else if (slot == 1) // Barrel Structural Upgrade
                {
                    return meta == 71;
                }
                else if (slot == 2) // Barrel Capacity Upgrade
                {
                    return meta == 72;
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
                ItemStack stack = this.te.itemHandlerBase.getStackInSlot(0);
                ItemStack upgradeStack = this.getStackInSlot(2);

                if (stack != null && stack.stackSize > 4096 && upgradeStack != null)
                {
                    int needed = (int) Math.ceil(((double) (stack.stackSize - 4096) / stack.getMaxStackSize()) / Configs.barrelCapacityUpgradeStacksPer);
                    int toRemove = Math.min(amount, upgradeStack.stackSize - needed);

                    if (toRemove > 0)
                    {
                        return super.extractItem(slot, toRemove, simulate);
                    }

                    return null;
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
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiBarrel(this.getContainer(player), this);
    }
}
