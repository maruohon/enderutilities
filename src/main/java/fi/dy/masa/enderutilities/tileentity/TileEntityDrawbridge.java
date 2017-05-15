package fi.dy.masa.enderutilities.tileentity;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiDrawbridge;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerDrawbridge;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.ItemUtils;

public class TileEntityDrawbridge extends TileEntityEnderUtilitiesInventory
{
    private static final int MAX_LENGTH_NORMAL = 64;
    private static final int MAX_LENGTH_ADVANCED = 32;
    private ItemStackHandlerDrawbridge itemHandlerDrawbridge;
    private boolean advanced;
    private boolean redstoneState;
    private State state = State.IDLE;
    private int position;
    private int delay = 4;
    private int maxLength = 1;
    private BlockInfo[] blocks = new BlockInfo[MAX_LENGTH_ADVANCED];
    private FakePlayer fakePlayer;

    public TileEntityDrawbridge()
    {
        super(ReferenceNames.NAME_TILE_DRAWBRIDGE);

        this.initStorage();
    }

    private void initStorage()
    {
        this.itemHandlerDrawbridge  = new ItemStackHandlerDrawbridge(0, MAX_LENGTH_ADVANCED, 1, true, "Items", this);
        this.itemHandlerBase        = this.itemHandlerDrawbridge;
        this.itemHandlerExternal    = new ItemHandlerWrapperDrawbridge(this.itemHandlerDrawbridge);
    }

    public ItemStackHandlerDrawbridge getInventoryDrawbridge()
    {
        return this.itemHandlerDrawbridge;
    }

    public boolean isAdvanced()
    {
        return this.advanced;
    }

    public void setIsAdvanced(boolean advanced)
    {
        this.advanced = advanced;
    }

    public int getSlotCount()
    {
        return this.isAdvanced() ? this.maxLength : 1;
    }

    public int getMaxLength()
    {
        return this.maxLength;
    }

    public void setMaxLength(int length)
    {
        this.maxLength = MathHelper.clamp(length, 1, this.isAdvanced() ? MAX_LENGTH_ADVANCED : MAX_LENGTH_NORMAL);

        if (this.isAdvanced() == false)
        {
            this.setStackLimit(length);
        }
    }

    public int getDelay()
    {
        return this.delay;
    }

    public void setDelay(int delay)
    {
        this.delay = MathHelper.clamp(delay, 1, 72000);
    }

    public void setStackLimit(int limit)
    {
        this.getBaseItemHandler().setStackLimit(MathHelper.clamp(limit, 1, MAX_LENGTH_NORMAL));
    }

    @Override
    public void setPlacementProperties(World world, BlockPos pos, ItemStack stack, NBTTagCompound tag)
    {
        if (tag.hasKey("drawbridge.delay", Constants.NBT.TAG_INT))
        {
            this.setDelay(tag.getInteger("drawbridge.delay"));
        }

        if (tag.hasKey("drawbridge.length", Constants.NBT.TAG_BYTE))
        {
            this.setMaxLength(tag.getByte("drawbridge.length"));
        }

        this.markDirty();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.advanced = nbt.getBoolean("Advanced");
        this.redstoneState = nbt.getBoolean("Powered");
        this.position = nbt.getByte("Position");
        this.delay = nbt.getByte("Delay");
        this.setMaxLength(nbt.getByte("Length"));
        this.state = State.fromId(nbt.getByte("State"));

        this.readBlockInfoFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        nbt.setBoolean("Advanced", this.isAdvanced());
        nbt.setBoolean("Powered", this.redstoneState);
        nbt.setByte("Position", (byte) this.position);
        nbt.setByte("Delay", (byte) this.delay);
        nbt.setByte("Length", (byte) this.maxLength);
        nbt.setByte("State", (byte) this.state.getId());

        this.writeBlockInfoToNBT(nbt);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);
        nbt.setByte("len", (byte) this.maxLength);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        this.setMaxLength(tag.getByte("len"));
    }

    private void writeBlockInfoToNBT(NBTTagCompound nbt)
    {
        int length = this.isAdvanced() ? this.maxLength : 1;
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < length; i++)
        {
            if (this.blocks[i] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("pos", (byte) i);
                tag.setString("name", this.blocks[i].getState().getBlock().getRegistryName().toString());
                tag.setByte("meta", (byte) this.blocks[i].getState().getBlock().getMetaFromState(this.blocks[i].getState()));

                if (this.blocks[i].getTileEntityNBT() != null)
                {
                    tag.setTag("nbt", this.blocks[i].getTileEntityNBT());
                }

                list.appendTag(tag);
            }
        }

        nbt.setTag("Blocks", list);
    }

    private void readBlockInfoFromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Blocks", Constants.NBT.TAG_LIST))
        {
            NBTTagList list = nbt.getTagList("Blocks", Constants.NBT.TAG_COMPOUND);
            int length = list.tagCount();

            for (int i = 0; i < length; i++)
            {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                int pos = tag.getByte("pos");

                if (pos >= 0 && pos < MAX_LENGTH_ADVANCED)
                {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("name")));

                    if (block != null)
                    {
                        @SuppressWarnings("deprecation")
                        IBlockState state = block.getStateFromMeta(tag.getByte("meta"));
                        this.blocks[i] = new BlockInfo(state, tag.getCompoundTag("nbt"));
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    private IBlockState getPlacementStateForPosition(int invPosition, World world, BlockPos pos, FakePlayer player)
    {
        if (this.blocks[invPosition] != null)
        {
            return this.blocks[invPosition].getState();
        }

        ItemStack stack = this.itemHandlerDrawbridge.getStackInSlot(invPosition);

        if (stack.isEmpty() == false && stack.getItem() instanceof ItemBlock)
        {
            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            int meta = itemBlock.getMetadata(stack.getMetadata());
            player.rotationYaw = this.getFacing().getHorizontalAngle();

            return itemBlock.block.getStateForPlacement(world, pos, EnumFacing.UP, 0.5f, 1f, 0.5f, meta, player);
        }

        return null;
    }

    private boolean extendOneBlock(int position, FakePlayer player, boolean playPistonSound)
    {
        int invPosition = this.isAdvanced() ? position : 0;
        World world = this.getWorld();
        BlockPos pos = this.getPos().offset(this.getFacing(), position + 1);
        IBlockState placementState = this.getPlacementStateForPosition(invPosition, world, pos, player);
        ItemStack stack = this.itemHandlerDrawbridge.getStackInSlot(invPosition);

        if (placementState != null && stack.isEmpty() == false && world.isBlockLoaded(pos, world.isRemote == false) &&
            world.getBlockState(pos).getBlock().isReplaceable(world, pos))
        {
            if (world.mayPlace(placementState.getBlock(), pos, true, EnumFacing.UP, null) == false)
            {
                return false;
            }

            if (playPistonSound && world.setBlockState(pos, placementState) == false)
            {
                return false;
            }
            else if (playPistonSound == false && BlockUtils.setBlockStateWithPlaceSound(world, pos, placementState, 3) == false)
            {
                return false;
            }

            NBTTagCompound nbt = this.blocks[invPosition] != null ? this.blocks[invPosition].getTileEntityNBT() : null;
            stack = this.itemHandlerDrawbridge.extractItem(invPosition, 1, false);

            // This fixes TE data loss on the placed blocks in case blocks with stored TE data
            // were manually placed into the slots, and not taken from the world by the drawbridge
            if (nbt == null && stack.isEmpty() == false && stack.getTagCompound() != null &&
                stack.getTagCompound().hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
            {
                nbt = stack.getTagCompound().getCompoundTag("BlockEntityTag");
            }

            if (placementState.getBlock().hasTileEntity(placementState) && nbt != null)
            {
                TileEntity te = world.getTileEntity(pos);

                if (te != null)
                {
                    // Re-creating the TE from NBT and then calling World#setTileEntity() causes
                    // TileEntity#validate() and TileEntity#onLoad() to get called for the TE
                    // from Chunk#addTileEntity(), which should hopefully be more mod
                    // friendly than just doing te.readFromNBT(tag).
                    te = TileEntity.create(world, nbt);

                    if (te != null)
                    {
                        te.setPos(pos);
                        world.setTileEntity(pos, te);
                        te.markDirty();
                    }
                }
            }

            if (playPistonSound)
            {
                world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, 0.8f);
            }

            return true;
        }

        return false;
    }

    private boolean contractOneBlock(int position, FakePlayer player, boolean playPistonSound)
    {
        int invPosition = this.isAdvanced() ? position : 0;
        World world = this.getWorld();
        BlockPos pos = this.getPos().offset(this.getFacing(), position + 1);
        IBlockState state = world.getBlockState(pos);

        if (world.isBlockLoaded(pos, world.isRemote == false) &&
            state.getBlock().isAir(state, world, pos) == false &&
            state.getBlockHardness(world, pos) >= 0f)
        {
            ItemStack stack = BlockUtils.getPickBlockItemStack(world, pos, player, EnumFacing.UP);

            if (stack.isEmpty() == false)
            {
                NBTTagCompound nbt = null;

                if (state.getBlock().hasTileEntity(state))
                {
                    TileEntity te = world.getTileEntity(pos);

                    if (te != null)
                    {
                        ItemUtils.storeTileEntityInStack(stack, te, false);
                        nbt = te.writeToNBT(new NBTTagCompound());
                    }
                }

                if (this.itemHandlerDrawbridge.insertItem(invPosition, stack, false).isEmpty())
                {
                    if (this.blocks[invPosition] == null)
                    {
                        this.blocks[invPosition] = new BlockInfo(state, nbt);
                    }

                    world.restoringBlockSnapshots = true;

                    if (playPistonSound == false)
                    {
                        BlockUtils.setBlockToAirWithBreakSound(world, pos);
                    }
                    else
                    {
                        world.setBlockToAir(pos);
                        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5f, 0.7f);
                    }

                    world.restoringBlockSnapshots = false;

                    return true;
                }
            }
        }

        return false;
    }

    private boolean takeAllBlocksFromWorld()
    {
        if (this.state == State.IDLE)
        {
            for (int offset = 0; offset < this.maxLength; offset++)
            {
                this.contractOneBlock(offset, this.getPlayer(), false);
            }

            return true;
        }

        return false;
    }

    @Nonnull
    private FakePlayer getPlayer()
    {
        if (this.fakePlayer == null)
        {
            int dim = this.getWorld().provider.getDimension();

            this.fakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(),
                    new GameProfile(new UUID(dim, dim), Reference.MOD_ID + ":drawbridge"));
        }

        return this.fakePlayer;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean powered = false;

        for (EnumFacing facing : EnumFacing.values())
        {
            if (facing != this.getFacing() && worldIn.isSidePowered(pos.offset(facing), facing))
            {
                powered = true;
                break;
            }
        }

        if (powered != this.redstoneState)
        {
            this.state = powered ? State.EXTEND : State.CONTRACT;
            this.redstoneState = powered;
            this.scheduleBlockUpdate(this.delay, true);
        }
    }

    @Override
    public void onScheduledBlockUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this.state == State.EXTEND)
        {
            while (this.position < this.maxLength)
            {
                if (this.extendOneBlock(this.position++, this.getPlayer(), true))
                {
                    break;
                }
            }

            if (this.position >= this.maxLength)
            {
                this.position = this.maxLength - 1;
                this.state = State.IDLE;
            }
            else
            {
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
        else if (this.state == State.CONTRACT)
        {
            while (this.position >= 0)
            {
                if (this.contractOneBlock(this.position--, this.getPlayer(), true))
                {
                    break;
                }
            }

            if (this.position < 0)
            {
                this.position = 0;
                this.state = State.IDLE;
            }
            else
            {
                this.scheduleBlockUpdate(this.delay, false);
            }
        }
    }

    private void changeInventorySize(int changeAmount)
    {
        int newSize = MathHelper.clamp(this.getSlotCount() + changeAmount, 1, MAX_LENGTH_ADVANCED);

        // Shrinking the inventory, only allowed if there are no items in the slots-to-be-removed
        if (changeAmount < 0)
        {
            int changeFinal = 0;

            for (int slot = this.getSlotCount() - 1; slot >= newSize && slot >= 1; slot--)
            {
                if (this.itemHandlerDrawbridge.getStackInSlot(slot).isEmpty())
                {
                    changeFinal--;
                }
                else
                {
                    break;
                }
            }

            newSize = MathHelper.clamp(this.getSlotCount() + changeFinal, 1, MAX_LENGTH_ADVANCED);
        }

        if (newSize >= 1 && newSize <= MAX_LENGTH_ADVANCED)
        {
            this.setMaxLength(newSize);
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        // Take block states from world
        if (action == 0)
        {
            this.takeAllBlocksFromWorld();
        }
        // Change delay
        else if (action == 1)
        {
            this.setDelay(this.delay + element);
        }
        // Change max length
        else if (action == 2 && this.state == State.IDLE)
        {
            if (this.isAdvanced())
            {
                this.changeInventorySize(element);
            }
            else
            {
                this.setMaxLength(this.maxLength + element);
            }

            // If the device is in the extended idle state, set the position to the end of
            // the newly set length.
            if (this.redstoneState)
            {
                this.position = (this.maxLength - 1);
            }
        }

        this.markDirty();
    }

    private class ItemStackHandlerDrawbridge extends ItemStackHandlerTileEntity
    {
        public ItemStackHandlerDrawbridge(int inventoryId, int invSize, int stackLimit, boolean allowCustomStackSizes,
                String tagName, TileEntityEnderUtilitiesInventory te)
        {
            super(inventoryId, invSize, stackLimit, allowCustomStackSizes, tagName, te);
        }

        @Override
        public int getSlots()
        {
            return TileEntityDrawbridge.this.getSlotCount();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            ItemStack stack = super.extractItem(slot, amount, simulate);

            // Clear the stored block info to prevent duplicating or transmuting stuff
            if (simulate == false && this.getStackInSlot(slot).isEmpty())
            {
                TileEntityDrawbridge.this.blocks[slot] = null;
            }

            return stack;
        }
    }

    private class ItemHandlerWrapperDrawbridge extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperDrawbridge(IItemHandler baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getItem() instanceof ItemBlock;
        }
    }

    private static class BlockInfo
    {
        private final IBlockState blockState;
        private final NBTTagCompound tileEntityData;

        BlockInfo(IBlockState state, NBTTagCompound tileEntityNBT)
        {
            this.blockState = state;
            this.tileEntityData = tileEntityNBT;
        }

        public IBlockState getState()
        {
            return this.blockState;
        }

        @Nullable
        public NBTTagCompound getTileEntityNBT()
        {
            return this.tileEntityData;
        }
    }

    private enum State
    {
        IDLE        (0),
        EXTEND      (1),
        CONTRACT    (2);

        private final int id;

        private State(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        public static State fromId(int id)
        {
            return values()[id % values().length];
        }
    }

    @Override
    public ContainerDrawbridge getContainer(EntityPlayer player)
    {
        return new ContainerDrawbridge(player, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiDrawbridge(this.getContainer(player), this);
    }
}
