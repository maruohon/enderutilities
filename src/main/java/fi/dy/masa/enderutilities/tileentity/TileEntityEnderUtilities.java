package fi.dy.masa.enderutilities.tileentity;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.base.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;

public class TileEntityEnderUtilities extends TileEntity
{
    /**
     * The current "data version" the TileEntities are saved as.
     * This is only used by select TEs that need to do some compatibility conversion
     * of some of their data at some point. So increment this whenever a new release
     * is made that needs such conversions.
     */
    public static final int DATA_VERSION = 6600;
    protected String tileEntityName;
    protected EnumFacing facing;
    protected OwnerData ownerData;
    protected IBlockState camoState;
    protected IBlockState camoStateExtended;
    protected NBTTagCompound camoData;

    public TileEntityEnderUtilities(String name)
    {
        this.facing = BlockEnderUtilities.DEFAULT_FACING;
        this.tileEntityName = name;
    }

    public String getTEName()
    {
        return this.tileEntityName;
    }

    public void setFacing(EnumFacing facing)
    {
        this.facing = facing;
        this.markDirty();

        if (this.getWorld() != null && this.getWorld().isRemote == false)
        {
            this.notifyBlockUpdate(this.getPos());
        }
    }

    public EnumFacing getFacing()
    {
        return this.facing;
    }

    @Override
    public void mirror(Mirror mirrorIn)
    {
        this.rotate(mirrorIn.toRotation(this.facing));
    }

    @Override
    public void rotate(Rotation rotationIn)
    {
        this.setFacing(rotationIn.rotate(this.getFacing()));
    }

    /**
     * @return the current camouflage IBlockState. If none is set, then Blocks.AIR.getDefaultState() is returned.
     */
    public IBlockState getCamoState()
    {
        return this.camoState != null ? this.camoState : Blocks.AIR.getDefaultState();
    }

    public IBlockState getCamoExtendedState()
    {
        return this.camoStateExtended != null ? this.camoStateExtended : this.getCamoState();
    }

    public void setOwner(Entity entity)
    {
        this.ownerData = entity != null ? new OwnerData(entity) : null;
    }

    public void setIsPublic(boolean isPublic)
    {
        if (this.ownerData == null)
        {
            this.ownerData = new OwnerData("", isPublic);
        }
        else
        {
            this.ownerData.setIsPublic(isPublic);
        }
    }

    public void setPlacementProperties(World world, BlockPos pos, @Nonnull ItemStack stack, @Nonnull NBTTagCompound tag)
    {
    }

    public String getOwnerName()
    {
        return this.ownerData != null ? this.ownerData.getOwnerName() : "";
    }

    @Nullable
    public UUID getOwnerUUID()
    {
        return this.ownerData != null ? this.ownerData.getOwnerUUID() : null;
    }

    public boolean isPublic()
    {
        return this.ownerData == null || this.ownerData.getIsPublic();
    }

    public boolean isOwner(EntityPlayer player)
    {
        return this.ownerData != null && this.ownerData.isOwner(player);
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.ownerData == null || this.ownerData.canAccess(player);
    }

    public boolean isMovableBy(EntityPlayer player)
    {
        return this.isPublic() || this.isOwner(player);
    }

    /**
     * @return true if something happened, and further processing (such as opening the GUI) should not happen
     */
    public boolean onRightClickBlock(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (this.hasCamouflageAbility() && player.getHeldItemMainhand().isEmpty())
        {
            return this.tryApplyCamouflage(player, hand, side, hitX, hitY, hitZ);
        }

        return false;
    }

    private boolean tryApplyCamouflage(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stackOffHand = player.getHeldItemOffhand();

        // Sneaking with an empty hand, clear the camo block
        if (player.isSneaking())
        {
            this.removeCamouflage();
            return true;
        }
        // Apply camouflage when right clicking with an empty main hand, and a block in the off hand
        else if (player.getHeldItemOffhand().isEmpty() == false && stackOffHand.getItem() instanceof ItemBlock)
        {
            return this.applyCamouflage(player, stackOffHand, side, hitX, hitY, hitZ);
        }

        return false;
    }

    private void removeCamouflage()
    {
        if (this.getWorld().isRemote == false && this.camoState != null)
        {
            this.camoState = null;
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1f, 1f);
            this.notifyBlockUpdate(this.getPos());
            // Check light changes in case the camo block emits light
            this.getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
            this.markDirty();
        }
    }

    private boolean applyCamouflage(EntityPlayer player, ItemStack stackOffhand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (stackOffhand.getItem() instanceof ItemBlock &&
            ((ItemBlock) stackOffhand.getItem()).getBlock() != null)
        {
            World world = this.getWorld();
            BlockPos posSelf = this.getPos();
            Block block = ((ItemBlock) stackOffhand.getItem()).getBlock();
            int meta = stackOffhand.getItem().getMetadata(stackOffhand.getMetadata());
            IBlockState state = block.getStateForPlacement(world, posSelf, side, hitX, hitY, hitZ, meta, player, EnumHand.OFF_HAND);

            if (state != this.camoState)
            {
                if (this.getWorld().isRemote == false)
                {
                    this.camoState = state;

                    this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                    this.notifyBlockUpdate(this.getPos());

                    // Check light changes in case the camo block emits light
                    this.getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
                    this.markDirty();
                }

                return true;
            }
        }

        return false;
    }

    public void onLeftClickBlock(EntityPlayer player)
    {
    }

    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
    }

    public void onScheduledBlockUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }

    public void scheduleBlockUpdate(int delay, boolean force)
    {
        World world = this.getWorld();

        if (world != null && (force || world.isUpdateScheduled(this.getPos(), this.getBlockType()) == false))
        {
            //System.out.printf("scheduleBlockUpdate(), actually scheduling for %s\n", this.getPos());
            world.scheduleUpdate(this.getPos(), this.getBlockType(), delay);
        }
    }

    protected void notifyBlockUpdate(BlockPos pos)
    {
        IBlockState state = this.getWorld().getBlockState(pos);
        this.getWorld().notifyBlockUpdate(pos, state, state, 3);
    }

    public boolean isPowered()
    {
        return false;
    }

    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Rotation", Constants.NBT.TAG_BYTE))
        {
            this.facing = EnumFacing.getFront(nbt.getByte("Rotation"));
        }

        /*
        if (nbt.hasKey("Camo", Constants.NBT.TAG_COMPOUND))
        {
            this.camoState = NBTUtils.readBlockStateFromTag(nbt.getCompoundTag("Camo"));
        }
        */

        if (nbt.hasKey("Camo", Constants.NBT.TAG_INT))
        {
            this.camoState = Block.getStateById(nbt.getInteger("Camo"));
        }

        if (nbt.hasKey("CamoData", Constants.NBT.TAG_COMPOUND))
        {
            this.camoData = nbt.getCompoundTag("CamoData");
        }

        this.ownerData = OwnerData.getOwnerDataFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.readFromNBTCustom(nbt); // This call needs to be at the super-most custom TE class
    }

    protected NBTTagCompound writeToNBTCustom(NBTTagCompound nbt)
    {
        nbt.setString("Version", Reference.MOD_VERSION);
        nbt.setByte("Rotation", (byte) this.facing.getIndex());

        if (this.camoState != null)
        {
            /*
            NBTTagCompound tag = new NBTTagCompound();
            NBTUtils.writeBlockStateToTag(this.camoState, tag);
            nbt.setTag("Camo", tag);
            */
            nbt.setInteger("Camo", Block.getStateId(this.camoState));

            if (this.camoData != null)
            {
                nbt.setTag("CamoData", this.camoData);
            }
        }

        if (this.ownerData != null)
        {
            this.ownerData.writeToNBT(nbt);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        this.writeToNBTCustom(nbt);

        return nbt;
    }

    /**
     * Get the data used for syncing the TileEntity to the client.
     * The data returned from this method doesn't have the position,
     * the position will be added in getUpdateTag() which calls this method.
     */
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt.setByte("r", (byte)(this.facing.getIndex() & 0x07));

        if (this.camoState != null)
        {
            nbt.setInteger("Camo", Block.getStateId(this.camoState));

            if (this.camoData != null)
            {
                nbt.setTag("CD", this.camoData);
            }
        }

        if (this.ownerData != null)
        {
            nbt.setString("o", this.ownerData.getOwnerName());
            nbt.setBoolean("pu", this.ownerData.getIsPublic());
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        // The tag from this method is used for the initial chunk packet,
        // and it needs to have the TE position!
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", this.getPos().getX());
        nbt.setInteger("y", this.getPos().getY());
        nbt.setInteger("z", this.getPos().getZ());

        // Add the per-block data to the tag
        return this.getUpdatePacketTag(nbt);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        if (this.getWorld() != null)
        {
            return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdatePacketTag(new NBTTagCompound()));
        }

        return null;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        if (tag.hasKey("r"))
        {
            this.setFacing(EnumFacing.getFront((byte)(tag.getByte("r") & 0x07)));
        }

        if (tag.hasKey("o", Constants.NBT.TAG_STRING))
        {
            this.ownerData = new OwnerData(tag.getString("o"), tag.getBoolean("pu"));
        }

        if (tag.hasKey("Camo", Constants.NBT.TAG_INT))
        {
            this.camoState = Block.getStateById(tag.getInteger("Camo"));

            World world = this.getWorld();
            BlockPos pos = this.getPos();
            IBlockState stateSelf = world.getBlockState(pos);

            // Temporarily place the target block to be able to grab its data
            if (this.camoState != null && this.camoState.getBlock() != Blocks.AIR)
            {
                try
                {
                    BlockUtils.setBlockToAirWithoutSpillingContents(world, pos, 16);

                    if (world.setBlockState(pos, this.camoState, 16))
                    {
                        if (tag.hasKey("CD", Constants.NBT.TAG_COMPOUND))
                        {
                            this.camoData = tag.getCompoundTag("CD");
                            TileEntity te = world.getTileEntity(pos);

                            if (te != null)
                            {
                                te.handleUpdateTag(this.camoData);
                            }
                        }

                        this.camoState = this.camoState.getActualState(world, pos);
                        this.camoStateExtended = this.camoState.getBlock().getExtendedState(this.camoState, world, pos);

                        BlockUtils.setBlockToAirWithoutSpillingContents(world, pos, 16);
                        world.setBlockState(pos, stateSelf, 16);
                        this.validate(); // re-validate after being removed by the setBlockState() to air
                        world.setTileEntity(pos, this);
                    }
                }
                catch (Exception e)
                {
                    EnderUtilities.logger.warn("Exception while trying to grab the Extended state for a camo block: {}", this.camoState, e);
                }
            }
        }
        else
        {
            this.camoState = null;
            this.camoStateExtended = null;
        }

        this.getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
        this.notifyBlockUpdate(this.getPos());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        this.handleUpdateTag(packet.getNbtCompound());
    }

    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    protected void sendPacketToWatchers(IMessage message)
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
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.getPos() + ")@" + System.identityHashCode(this);
    }

    public boolean hasGui()
    {
        return true;
    }

    protected boolean hasCamouflageAbility()
    {
        return false;
    }

    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return null;
    }

    public Object getGui(EntityPlayer player)
    {
        return null;
    }
}
