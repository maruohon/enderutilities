package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiBarrel;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IItemHandlerSize;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerBarrel;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityBarrel extends TileEntityEnderUtilitiesInventory
{
    private ItemHandlerBarrelUpgrades itemHandlerUpgrades;
    private Map<UUID, Long> rightClickTimes = new HashMap<UUID, Long>();
    private List<EnumFacing> labels = new ArrayList<EnumFacing>();
    private int labelMask;
    private int maxStacks = 64;
    private boolean creative;
    private ItemStack cachedStack;
    public String cachedStackSizeString;

    public TileEntityBarrel()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_BARREL);

        this.initStorage();
    }

    private void initStorage()
    {
        int maxUpgrades = Configs.barrelMaxCapacityUpgrades;
        int maxStack = maxUpgrades * Configs.barrelCapacityUpgradeStacksPer * 64 + 4096;
        this.itemHandlerBase        = new ItemHandlerBarrel(0, 1, maxStack, true, "Items", this);
        this.itemHandlerUpgrades    = new ItemHandlerBarrelUpgrades(1, 3, maxUpgrades, true, "ItemsUpgrades", this);
        this.itemHandlerExternal    = this.itemHandlerBase;
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

    public IItemHandler getUpgradeInventory()
    {
        return new ItemHandlerWrapperContainerBarrel(this.itemHandlerUpgrades);
    }

    public boolean isCreative()
    {
        return this.creative;
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
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.creative = nbt.getBoolean("Creative");
        this.setLabelsFromMask(nbt.getByte("Labels"));
        this.updateBarrelProperties(true);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        nbt.setByte("Labels", (byte) this.labelMask);
        nbt.setBoolean("Creative", this.creative);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        if (this.cachedStack != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            this.cachedStack.writeToNBT(tag);
            tag.setInteger("ac", this.cachedStack.stackSize);
            nbt.setTag("st", tag);
        }

        nbt.setBoolean("cr", this.creative);
        nbt.setByte("la", (byte) this.getLabelMask(true));

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.initStorage();

        if (tag.hasKey("st", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound tmp = tag.getCompoundTag("st");
            this.cachedStack = ItemStack.loadItemStackFromNBT(tmp);

            if (this.cachedStack != null && tmp.hasKey("ac", Constants.NBT.TAG_INT))
            {
                this.cachedStack.stackSize = tmp.getInteger("ac");
            }
        }

        this.creative = tag.getBoolean("cr");
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

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        // Main inventory contents changed
        if (inventoryId == 0)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(0);

            if (InventoryUtils.areItemStacksEqual(stack, this.cachedStack) == false)
            {
                
            }
            else if (stack != null && stack.stackSize != this.cachedStack.stackSize)
            {
                
            }
        }
        // Upgrades changed
        else if (inventoryId == 1)
        {
            this.updateBarrelProperties(true);
        }
    }

    public void updateBarrelProperties(boolean updateLabels)
    {
        ItemStack stackCapacityUpgrades = this.itemHandlerUpgrades.getStackInSlot(2);
        int upgrades = stackCapacityUpgrades != null ? stackCapacityUpgrades.stackSize : 0;
        this.maxStacks = 64 + upgrades * Configs.barrelCapacityUpgradeStacksPer;

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

            stack = InventoryUtils.tryInsertItemStackToInventory(this.itemHandlerBase, stack);
            player.setHeldItem(hand, stack);
        }

        long time = System.currentTimeMillis();
        Long last = this.rightClickTimes.get(player.getUniqueID());

        if (last != null && time - last.longValue() < 300)
        {
            InventoryUtils.tryMoveMatchingItems(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), this.itemHandlerBase);
            player.openContainer.detectAndSendChanges();
        }

        this.rightClickTimes.put(player.getUniqueID(), time);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        int amount = player.isSneaking() ? 1 : 64;
        ItemStack stack = this.itemHandlerBase.extractItem(0, amount, false);

        if (stack != null)
        {
            RayTraceResult rayTrace = EntityUtils.getRayTraceFromPlayer(this.getWorld(), player, false);

            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && this.getPos().equals(rayTrace.getBlockPos()))
            {
                EntityUtils.dropItemStacksInWorld(this.getWorld(), this.getSpawnedItemPosition(rayTrace.sideHit), stack, -1, true, false);
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

    private class ItemHandlerWrapperContainerBarrel extends ItemHandlerWrapperContainer implements IItemHandlerSize
    {
        private final ItemHandlerBarrelUpgrades barrel;

        public ItemHandlerWrapperContainerBarrel(ItemHandlerBarrelUpgrades baseHandler)
        {
            super(baseHandler, baseHandler);

            this.barrel = baseHandler;
        }

        @Override
        public int getInventoryStackLimit()
        {
            return this.barrel.getInventoryStackLimit();
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            return this.barrel.getItemStackLimit(stack);
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
        public boolean canExtractFromSlot(int slot)
        {
            // Capacity Upgrades can only be removed when the stored stackSIze is less than the default capacity
            if (slot == 2)
            {
                return this.te.isOverSpillCapacity() == false;
            }

            return true;
        }
    }

    private class ItemHandlerBarrel extends ItemStackHandlerTileEntity
    {
        private final TileEntityBarrel te;

        public ItemHandlerBarrel(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes, String tagName, TileEntityBarrel te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);

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

        @Override
        public int getInventoryStackLimit()
        {
            ItemStack stack = this.getStackInSlot(0);
            return this.te.getMaxStacks() * (stack != null ? stack.getMaxStackSize() : 64);
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            return this.getInventoryStackLimit();
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 1)
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
