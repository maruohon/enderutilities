package fi.dy.masa.enderutilities.tileentity;

import java.util.List;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.BlockEnergyBridge;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.WorldUtils;

public class TileEntityEnergyBridge extends TileEntityEnderUtilities implements ITickable
{
    private static final BlockInfo[] STRUCTURE_TRANSMITTER = new BlockInfo[5];
    private static final BlockInfo[] STRUCTURE_RECEIVER = new BlockInfo[5];
    protected boolean isActive;
    protected boolean isPowered;
    protected int timer;
    protected Type type = Type.RESONATOR;

    public int beamYMin;
    public int beamYMax;
    AxisAlignedBB renderBB;

    static
    {
        IBlockState defaultState = EnderUtilitiesBlocks.ENERGY_BRIDGE.getDefaultState();
        IProperty<BlockEnergyBridge.BridgeType> type = BlockEnergyBridge.TYPE;
        BlockEnergyBridge.BridgeType transmitter = BlockEnergyBridge.BridgeType.TRANSMITTER;
        BlockEnergyBridge.BridgeType receiver = BlockEnergyBridge.BridgeType.RECEIVER;
        BlockEnergyBridge.BridgeType resonator = BlockEnergyBridge.BridgeType.RESONATOR;

        STRUCTURE_TRANSMITTER[0] = new BlockInfo(BlockPos.ORIGIN.up(3),    defaultState.withProperty(type, transmitter).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.NORTH));
        STRUCTURE_TRANSMITTER[1] = new BlockInfo(BlockPos.ORIGIN.north(3), defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.SOUTH));
        STRUCTURE_TRANSMITTER[2] = new BlockInfo(BlockPos.ORIGIN.south(3), defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.NORTH));
        STRUCTURE_TRANSMITTER[3] = new BlockInfo(BlockPos.ORIGIN.east(3),  defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.WEST));
        STRUCTURE_TRANSMITTER[4] = new BlockInfo(BlockPos.ORIGIN.west(3),  defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.EAST));

        STRUCTURE_RECEIVER[0] = new BlockInfo(BlockPos.ORIGIN,          defaultState.withProperty(type, receiver).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.NORTH));
        STRUCTURE_RECEIVER[1] = new BlockInfo(BlockPos.ORIGIN.north(3), defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.SOUTH));
        STRUCTURE_RECEIVER[2] = new BlockInfo(BlockPos.ORIGIN.south(3), defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.NORTH));
        STRUCTURE_RECEIVER[3] = new BlockInfo(BlockPos.ORIGIN.east(3),  defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.WEST));
        STRUCTURE_RECEIVER[4] = new BlockInfo(BlockPos.ORIGIN.west(3),  defaultState.withProperty(type, resonator).withProperty(BlockEnderUtilities.FACING_H, EnumFacing.EAST));
    }

    public TileEntityEnergyBridge()
    {
        super(ReferenceNames.NAME_TILE_ENERGY_BRIDGE);
        this.timer = 0;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        byte f = nbt.getByte("Flags");
        this.isActive = (f & 0x80) != 0;
        this.isPowered = (f & 0x40) != 0;
        this.type = Type.fromMeta(f & 0x03);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Flags", (byte)((this.isActive ? 0x80 : 0x00) | (this.isPowered ? 0x40 : 0x00) | this.type.getMeta()));

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("f", (byte)((this.isActive ? 0x80 : 0x00) | (this.isPowered ? 0x40 : 0x00) | this.type.getMeta()));

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        byte f = tag.getByte("f");

        this.isActive = ((f & 0x80) != 0);
        this.isPowered = ((f & 0x40) != 0);
        this.type = Type.fromMeta(f & 0x03);
        this.getBeamEndPoints();

        super.handleUpdateTag(tag);
    }

    @Override
    public EnumFacing getFacing()
    {
        if (this.getType() == Type.RESONATOR)
        {
            return super.getFacing();
        }
        else
        {
            return EnumFacing.NORTH;
        }
    }

    private void setActiveState(boolean isActive)
    {
        this.isActive = isActive;
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
    }

    public boolean getIsActive()
    {
        return this.isActive;
    }

    private void setPoweredState(boolean value)
    {
        if (this.isPowered != value)
        {
            this.isPowered = value;
            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
        }
    }

    public boolean getIsPowered()
    {
        return this.isPowered;
    }

    public Type getType()
    {
        return this.type;
    }

    public void setType(int meta)
    {
        this.type = Type.fromMeta(meta);
    }

    @Override
    public void update()
    {
        // Master blocks (Transmitter or Receiver) re-validate the multiblock every 5 seconds

        if (this.getWorld().isRemote == false && this.getType() != Type.RESONATOR && ++this.timer >= 100)
        {
            this.tryAssembleMultiBlock();
            this.timer = 0;
        }
    }

    public void tryAssembleMultiBlock()
    {
        World world = this.getWorld();
        Type masterType = this.getMasterType();
        BlockPos center = this.getCenterPos();

        boolean isValid = this.isStructureValid(world, center, masterType);

        if (isValid)
        {
            if (this.isActive == false)
            {
                this.activateMultiBlock(world, center, masterType);
                EnergyBridgeTracker.addBridgeLocation(world, this.getMasterPos(center));
            }

            this.updatePoweredState(world, center, masterType);
        }
        // This gets called from the periodic validation via update()
        else if (this.isActive)
        {
            this.disableMultiBlock(world, center, masterType);
        }
    }

    private BlockPos getCenterPos()
    {
        switch (this.getType())
        {
            case TRANSMITTER: return this.getPos().down(3);
            case RECEIVER: return this.getPos();
            case RESONATOR: return this.getPos().offset(this.getFacing(), 3);
        }

        return this.getPos();
    }

    private Type getMasterType()
    {
        return WorldUtils.isEndDimension(this.getWorld()) ? Type.TRANSMITTER : Type.RECEIVER;
    }

    private BlockPos getMasterPos(BlockPos center)
    {
        return WorldUtils.isEndDimension(this.getWorld()) ? center.up(3) : center;
    }

    private BlockInfo[] getStructure(Type masterType)
    {
        return masterType == Type.TRANSMITTER ? STRUCTURE_TRANSMITTER : STRUCTURE_RECEIVER;
    }

    private boolean isStructureValid(World world, BlockPos center, Type masterType)
    {
        BlockInfo[] structure = this.getStructure(masterType);
        boolean isValid = false;

        for (BlockInfo info : structure)
        {
            BlockPos posTmp = center.add(info.getPos());
            IBlockState state = world.getBlockState(posTmp);

            if (state.getBlock() != EnderUtilitiesBlocks.ENERGY_BRIDGE ||
                state.getActualState(world, posTmp).withProperty(BlockEnergyBridge.ACTIVE, false) != info.getBlockState())
            {
                return false;
            }
        }

        if (masterType == Type.TRANSMITTER)
        {
            double xd = center.getX();
            double yd = center.getY();
            double zd = center.getZ();
            double d = 1.0d;
            List<EntityEnderCrystal> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class,
                    new AxisAlignedBB(xd - d, yd - d, zd - d, xd + d, yd + d, zd + d));

            if (list.size() >= 1)
            {
                isValid = true;
            }
        }
        else
        {
            isValid = true;
        }

        // Our machine blocks are all in the right configuration, now just check that there are no other obstructing blocks in the area
        if (isValid)
        {
            return this.isObstructed(world, center, masterType) == false;
        }

        return false;
    }

    private boolean isObstructedQuadrant(World world, BlockPos basePos, EnumFacing facing, BlockPos ... positions)
    {
        EnumFacing dirNext = facing.rotateY(); // the direction 90 degrees clock wise

        for (BlockPos pos : positions)
        {
            int x = pos.getX() * facing.getFrontOffsetX() + pos.getZ() * facing.getFrontOffsetZ();
            int y = pos.getY();
            int z = pos.getX() * dirNext.getFrontOffsetX() + pos.getZ() * dirNext.getFrontOffsetZ();

            if (world.isAirBlock(basePos.add(x, y, z)) == false)
            {
                return true;
            }
        }

        return false;
    }

    private boolean isObstructed(World world, BlockPos center, Type masterType)
    {
        BlockPos posMaster = this.getMasterPos(center);

        // Block positions in one quadrant of the area that needs to be clear for the resonators, relative to the middle block
        BlockPos positionsToCheck[] = new BlockPos[] {
                                                        new BlockPos(1, 0, 0),
                                                        new BlockPos(2, 0, 0),
                                                        new BlockPos(1, 0, 3),
                                                        new BlockPos(1, 0, 2),
                                                        new BlockPos(2, 0, 2),
                                                        new BlockPos(2, 0, 1),
                                                        new BlockPos(3, 0, 1)
                                                    };

        if (this.isObstructedQuadrant(world, center, EnumFacing.EAST,  positionsToCheck) ||
            this.isObstructedQuadrant(world, center, EnumFacing.SOUTH, positionsToCheck) ||
            this.isObstructedQuadrant(world, center, EnumFacing.WEST,  positionsToCheck) ||
            this.isObstructedQuadrant(world, center, EnumFacing.NORTH, positionsToCheck))
        {
            return true;
        }

        // Transmitter
        if (masterType == Type.TRANSMITTER)
        {
            // Check the two blocks below the transmitter
            if (this.isObstructedQuadrant(world, posMaster, EnumFacing.EAST, new BlockPos(0, -1, 0), new BlockPos(0, -2, 0)))
            {
                return true;
            }
        }
        // Receiver: check the column below the Receiver down to bedrock
        else if (this.isVerticalBeamObstructed(world, posMaster.getX(), posMaster.getZ(), posMaster.getY() - 1, 0))
        {
            return true;
        }

        int top = world.getChunkFromChunkCoords(posMaster.getX() >> 4, posMaster.getZ() >> 4).getTopFilledSegment() + 15;

        return this.isVerticalBeamObstructed(world, posMaster.getX(), posMaster.getZ(), posMaster.getY() + 1, top);
    }

    private boolean isVerticalBeamObstructed(World world, int x, int z, int yStart, int yEnd)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, yStart, z);
        int increment = yStart < yEnd ? 1 : -1;
        yEnd += increment; // Nudge the end by one because we use != for checking it

        // Check that there are no light obstructing blocks before hitting the first bedrock block
        for ( ; pos.getY() != yEnd; )
        {
            IBlockState state = world.getBlockState(pos);

            if (world.isAirBlock(pos) == false && state.getLightOpacity(world, pos) > 3)
            {
                return state.getBlock() != Blocks.BEDROCK;
            }

            pos.setY(pos.getY() + increment);
        }

        return false;
    }

    public void disassembleMultiblock()
    {
        this.disableMultiBlock(this.getWorld(), this.getCenterPos(), this.getMasterType());
    }

    private void activateMultiBlock(World world, BlockPos center, Type masterType)
    {
        this.setActiveStateForStructure(world, center, masterType, true);
    }

    private void disableMultiBlock(World world, BlockPos center, Type masterType)
    {
        this.setActiveStateForStructure(world, center, masterType, false);
        EnergyBridgeTracker.removeBridgeLocation(world, this.getMasterPos(center));
    }

    private void setActiveStateForStructure(World world, BlockPos center, Type masterType, boolean active)
    {
        BlockInfo[] structure = this.getStructure(masterType);

        for (BlockInfo info : structure)
        {
            this.setActiveState(world, center.add(info.getPos()), active);
        }
    }

    private void setActiveState(World worldIn, BlockPos pos, boolean state)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge) te).setActiveState(state);
        }
    }

    private void updatePoweredState(World world, BlockPos center, Type masterType)
    {
        BlockInfo[] structure = this.getStructure(masterType);
        int dim = world.provider.getDimension();
        boolean powered = EnergyBridgeTracker.dimensionHasEnergyBridge(dim) &&
                            (WorldUtils.isEndDimension(world) || EnergyBridgeTracker.endHasEnergyBridges());

        for (BlockInfo info : structure)
        {
            this.updatePoweredState(world, center.add(info.getPos()), powered);
        }
    }

    private void updatePoweredState(World world, BlockPos pos, boolean value)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge) te).setPoweredState(value);
        }
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }

    private void getBeamEndPoints()
    {
        int posX = this.getPos().getX();
        int posY = this.getPos().getY();
        int posZ = this.getPos().getZ();
        int top = world.getChunkFromChunkCoords(posX >> 4, posZ >> 4).getTopFilledSegment() + 15;

        // Energy Bridge Transmitter
        if (Type.fromMeta(this.getBlockMetadata()) == Type.TRANSMITTER)
        {
            this.beamYMin = posY - 2;
        }
        // Energy Bridge Receiver
        else if (Type.fromMeta(this.getBlockMetadata()) == Type.RECEIVER)
        {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, posY, posZ);

            for ( ; pos.getY() >= 0; )
            {
                if (this.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK)
                {
                    break;
                }

                pos.setY(pos.getY() - 1);
            }

            this.beamYMin = pos.getY() + 1;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, posY, posZ);
        int y = 512;

        for ( ; pos.getY() <= top; )
        {
            if (this.getWorld().getBlockState(pos).getBlock() == Blocks.BEDROCK)
            {
                y = pos.getY();
                break;
            }

            pos.setY(pos.getY() + 1);
        }

        this.beamYMax = y;
        this.renderBB = new AxisAlignedBB(posX - 4d, -256d, posZ - 4d, posX + 4d, 512d, posZ + 4d);
    }

    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0d;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.renderBB != null ? this.renderBB : INFINITE_EXTENT_AABB;
    }

    private static class BlockInfo
    {
        private final BlockPos pos;
        private final IBlockState state;

        public BlockInfo(BlockPos posRelative, IBlockState state)
        {
            this.pos = posRelative;
            this.state = state;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public IBlockState getBlockState()
        {
            return this.state;
        }
    }

    public enum Type
    {
        RESONATOR (0),
        RECEIVER (1),
        TRANSMITTER (2);

        private final int meta;

        private Type(int meta)
        {
            this.meta = meta;
        }

        public static Type fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : RESONATOR;
        }

        public int getMeta()
        {
            return this.meta;
        }
    }
}
