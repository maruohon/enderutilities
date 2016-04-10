package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;

public class TileEntityEnergyBridge extends TileEntityEnderUtilities implements ITickable
{
    protected boolean isActive;
    protected boolean isPowered;
    protected int timer;
    protected int blockType;

    @SideOnly(Side.CLIENT)
    public int beamYMin;
    @SideOnly(Side.CLIENT)
    public int beamYMax;
    @SideOnly(Side.CLIENT)
    AxisAlignedBB renderBB;

    public TileEntityEnergyBridge()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
        this.timer = 0;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        byte f = nbt.getByte("Flags");
        this.isActive = (f & 0x80) != 0;
        this.blockType = f & 0x03;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Flags", (byte)((this.isActive ? 0x80 : 0x00) | this.blockType));
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("f", (byte)((this.isPowered ? 0x40 : 0x00) | (this.isActive ? 0x80 : 0x00) | this.blockType));

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();
        byte f = nbt.getByte("f");

        this.isActive = ((f & 0x80) != 0);
        this.isPowered = ((f & 0x40) != 0);
        this.blockType = f & 0x03;
        this.getBeamEndPoints();

        super.onDataPacket(net, packet);
    }

    protected void setActiveState(boolean isActive)
    {
        this.isActive = isActive;
        IBlockState state = this.worldObj.getBlockState(this.getPos());
        this.worldObj.notifyBlockUpdate(this.getPos(), state, state, 3);
    }

    public boolean getIsActive()
    {
        return this.isActive;
    }

    protected void setPoweredState(boolean value)
    {
        if (this.isPowered != value)
        {
            this.isPowered = value;
            IBlockState state = this.worldObj.getBlockState(this.getPos());
            this.worldObj.notifyBlockUpdate(this.getPos(), state, state, 3);
        }
    }

    public boolean getIsPowered()
    {
        return this.isPowered;
    }

    public Type getType()
    {
        return Type.fromMeta(this.blockType);
    }

    public void setType(int meta)
    {
        this.blockType = meta;
    }

    @Override
    public void update()
    {
        // Master blocks (Transmitter or Receiver) re-validate the multiblock every 2 seconds

        Type type = this.getType();
        if (this.worldObj.isRemote == false && (type == Type.TRANSMITTER || type == Type.RECEIVER) && ++this.timer >= 40)
        {
            this.tryAssembleMultiBlock(this.worldObj, this.getPos(), type);
            this.timer = 0;
        }
    }

    public void tryAssembleMultiBlock(World worldIn, BlockPos pos)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        /*if (worldIn.provider.getDimensionId() == 1)
        {
            this.tryAssembleMultiBlock(worldIn, pos, Type.TRANSMITTER);
        }
        else
        {
            this.tryAssembleMultiBlock(worldIn, pos, Type.RECEIVER);
        }*/

        this.tryAssembleMultiBlock(worldIn, pos, this.getType());
    }

    protected void tryAssembleMultiBlock(World worldIn, BlockPos pos, Type type)
    {
        List<BlockPos> positions = new ArrayList<BlockPos>();

        if (this.getBlockPositions(worldIn, pos, type, positions) == false || positions.size() != 6)
        {
            return;
        }

        Type masterType = worldIn.provider.getDimension() == 1 ? Type.TRANSMITTER : Type.RECEIVER;
        boolean isValid = this.isStructureValid(worldIn, pos, masterType, positions);

        if (isValid == true)
        {
            if (this.isActive == false)
            {
                this.activateMultiBlock(worldIn, positions);
                EnergyBridgeTracker.addBridgeLocation(positions.get(0), worldIn.provider.getDimension());
            }

            this.updatePoweredState(worldIn, positions);
        }
        // This gets called from the periodic validation via update()
        else if (this.isActive == true)
        {
            this.disassembleMultiblock(worldIn, pos, type);
        }
    }

    protected void activateMultiBlock(World worldIn, List<BlockPos> positions)
    {
        for (int i = 0; i < 5; i++)
        {
            this.setState(worldIn, positions.get(i), true);
        }
    }

    protected boolean getBlockPositions(World worldIn, BlockPos pos, Type type, List<BlockPos> positions)
    {
        positions.clear();

        TileEntity te = worldIn.getTileEntity(pos);

        if ((te instanceof TileEntityEnergyBridge) == false)
        {
            return false;
        }

        // position of the middle block in the y-plane of the resonators
        BlockPos posResonatorBase = pos;
        BlockPos posMaster = pos;
        EnumFacing facing = EnumFacing.getFront(((TileEntityEnergyBridge)te).getRotation());

        int yOffset = type == Type.TRANSMITTER ? 3 : 0;

        // The given location is a resonator, not the master block; get the master block's location
        if (type == Type.RESONATOR)
        {
            posMaster = posMaster.add(0, yOffset, 0).offset(facing, 3);
            posResonatorBase = posResonatorBase.offset(facing, 3);
        }
        else
        {
            posResonatorBase = posResonatorBase.add(0, -yOffset, 0);
        }

        positions.add(posMaster);
        positions.add(posResonatorBase.offset(EnumFacing.NORTH, 3));
        positions.add(posResonatorBase.offset(EnumFacing.SOUTH, 3));
        positions.add(posResonatorBase.offset(EnumFacing.EAST, 3));
        positions.add(posResonatorBase.offset(EnumFacing.WEST, 3));
        positions.add(posResonatorBase);

        return true;
    }

    protected boolean isStructureValid(World world, BlockPos pos, Type type, List<BlockPos> positions)
    {
        Block blockEb = EnderUtilitiesBlocks.blockEnergyBridge;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;
        boolean isValid = false;

        if (BlockUtils.blockMatches(world, positions.get(0), blockEb, type.getMeta(), classTEEB, null) &&
            BlockUtils.blockMatches(world, positions.get(1), blockEb, Type.RESONATOR.getMeta(), classTEEB, EnumFacing.SOUTH) &&
            BlockUtils.blockMatches(world, positions.get(2), blockEb, Type.RESONATOR.getMeta(), classTEEB, EnumFacing.NORTH) &&
            BlockUtils.blockMatches(world, positions.get(3), blockEb, Type.RESONATOR.getMeta(), classTEEB, EnumFacing.WEST) &&
            BlockUtils.blockMatches(world, positions.get(4), blockEb, Type.RESONATOR.getMeta(), classTEEB, EnumFacing.EAST))
        {
            if (type != Type.TRANSMITTER)
            {
                isValid = true;
            }
            else
            {
                double xd = positions.get(5).getX();
                double yd = positions.get(5).getY();
                double zd = positions.get(5).getZ();
                double d = 1.0d;
                List<EntityEnderCrystal> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(xd - d, yd - d, zd - d, xd + d, yd + d, zd + d));

                if (list.size() == 1)
                {
                    isValid = true;
                }
            }
        }

        // Our machine blocks are all in the right configuration, now just check that there are no other obstructing blocks in the area
        if (isValid == true)
        {
            return this.isObstructed(world, blockEb, type, positions) == false;
        }

        return false;
    }

    protected boolean isObstructedQuadrant(World world, BlockPos basePos, EnumFacing facing, BlockPos ... positions)
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

    protected boolean isObstructed(World worldIn, Block blockEb, Type type, List<BlockPos> positions)
    {
        if (positions.size() != 6)
        {
            return true;
        }

        BlockPos posMaster = positions.get(0);
        BlockPos posResonatorMiddle = positions.get(5);

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

        if (this.isObstructedQuadrant(worldIn, posResonatorMiddle, EnumFacing.EAST, positionsToCheck) == true ||
            this.isObstructedQuadrant(worldIn, posResonatorMiddle, EnumFacing.SOUTH, positionsToCheck) == true ||
            this.isObstructedQuadrant(worldIn, posResonatorMiddle, EnumFacing.WEST, positionsToCheck) == true ||
            this.isObstructedQuadrant(worldIn, posResonatorMiddle, EnumFacing.NORTH, positionsToCheck) == true)
        {
            return true;
        }

        // Transmitter
        if (type == Type.TRANSMITTER)
        {
            // Check the two blocks below the transmitter
            if (this.isObstructedQuadrant(worldIn, posMaster, EnumFacing.EAST,
                    new BlockPos[] {new BlockPos(0, -1, 0), new BlockPos(0, -2, 0)}) == true)
            {
                return true;
            }
        }
        // Receiver: check the column below the Receiver down to bedrock
        else
        {
            for (int y = posMaster.getY() - 1; y >= 0; y--)
            {
                BlockPos pos = new BlockPos(posMaster.getX(), y, posMaster.getZ());
                IBlockState state = worldIn.getBlockState(pos);

                if (worldIn.isAirBlock(pos) == false && state.getLightOpacity(worldIn, pos) > 3)
                {
                    if (state.getBlock() != Blocks.BEDROCK)
                    {
                        return true;
                    }

                    break;
                }
            }
        }

        // Check the column above the master block up to world height or first bedrock block
        for (int y = posMaster.getY() + 1; y <= worldIn.getActualHeight(); y++)
        {
            BlockPos pos = new BlockPos(posMaster.getX(), y, posMaster.getZ());
            IBlockState state = worldIn.getBlockState(pos);

            if (worldIn.isAirBlock(pos) == false && state.getLightOpacity(worldIn, pos) > 3)
            {
                if (state.getBlock() != Blocks.BEDROCK)
                {
                    return true;
                }

                break;
            }
        }

        /*if (world.canBlockSeeTheSky(posMaster.posX, posMaster.posY, posMaster.posZ) == false)
        {
            return true;
        }*/

        return false;
    }

    public void disassembleMultiblock(World worldIn, BlockPos pos)
    {
        this.disassembleMultiblock(worldIn, pos, this.getType());
    }

    public void disassembleMultiblock(World worldIn, BlockPos pos, Type type)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
        }

        BlockPos posMaster = pos;

        // The given location is a resonator, not the master block; get the master block's location
        if (type == Type.RESONATOR)
        {
            EnumFacing dir = EnumFacing.getFront(((TileEntityEnergyBridge)te).getRotation());
            type = this.worldObj.provider.getDimension() == 1 ? Type.TRANSMITTER : Type.RECEIVER;
            int yOffset = type == Type.TRANSMITTER ? 3 : 0;
            posMaster = pos.add(0, yOffset, 0).offset(dir, 3);
        }

        // Get the block position list from the master block
        List<BlockPos> positions = new ArrayList<BlockPos>();
        if (this.getBlockPositions(worldIn, posMaster, type, positions) == false)
        {
            return;
        }

        this.disableMultiBlock(worldIn, type, positions);
    }

    protected void disableMultiBlock(World worldIn, Type type, List<BlockPos> blockPositions)
    {
        if (blockPositions == null || blockPositions.size() != 6)
        {
            return;
        }

        Block blockEb = EnderUtilitiesBlocks.blockEnergyBridge;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;

        this.setStateWithCheck(worldIn, blockPositions.get(0), blockEb, type, classTEEB, null, false);
        this.setStateWithCheck(worldIn, blockPositions.get(1), blockEb, Type.RESONATOR, classTEEB, EnumFacing.SOUTH, false);
        this.setStateWithCheck(worldIn, blockPositions.get(2), blockEb, Type.RESONATOR, classTEEB, EnumFacing.NORTH, false);
        this.setStateWithCheck(worldIn, blockPositions.get(3), blockEb, Type.RESONATOR, classTEEB, EnumFacing.WEST, false);
        this.setStateWithCheck(worldIn, blockPositions.get(4), blockEb, Type.RESONATOR, classTEEB, EnumFacing.EAST, false);

        EnergyBridgeTracker.removeBridgeLocation(blockPositions.get(0), worldIn.provider.getDimension());
    }

    protected void setState(World worldIn, BlockPos pos, boolean state)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setActiveState(state);
        }
    }

    protected void setStateWithCheck(World worldIn, BlockPos pos, Block requiredBlock, Type type, Class <? extends TileEntity> TEClass,
            EnumFacing requiredDirection, boolean state)
    {
        if (BlockUtils.blockMatches(worldIn, pos, requiredBlock, type.getMeta(), TEClass, requiredDirection) == true)
        {
            ((TileEntityEnergyBridge)worldIn.getTileEntity(pos)).setActiveState(state);
        }
    }

    protected void updatePoweredState(World worldIn, List<BlockPos> positions)
    {
        if (positions == null || positions.size() != 6)
        {
            return;
        }

        int dim = worldIn.provider.getDimension();
        boolean powered = EnergyBridgeTracker.dimensionHasEnergyBridge(dim) == true && (dim == 1 || EnergyBridgeTracker.dimensionHasEnergyBridge(1) == true);

        for (int i = 0; i < 5; ++i)
        {
            this.updatePoweredState(worldIn, positions.get(i), powered);
        }
    }

    protected void updatePoweredState(World world, BlockPos pos, boolean value)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setPoweredState(value);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void getBeamEndPoints()
    {
        int posX = this.getPos().getX();
        int posY = this.getPos().getY();
        int posZ = this.getPos().getZ();
        int y = posY;

        // Energy Bridge Transmitter
        if (Type.fromMeta(this.getBlockMetadata()) == Type.TRANSMITTER)
        {
            this.beamYMin = posY - 2;
        }
        // Energy Bridge Receiver
        else if (Type.fromMeta(this.getBlockMetadata()) == Type.RECEIVER)
        {
            for (y = posY; y >= 0; y--)
            {
                if (this.worldObj.getBlockState(new BlockPos(posX, y, posZ)).getBlock() == Blocks.BEDROCK)
                {
                    break;
                }
            }

            this.beamYMin = y + 1;
        }

        for (y = posY; y < this.worldObj.getHeight(); y++)
        {
            if (this.worldObj.getBlockState(new BlockPos(posX, y, posZ)).getBlock() == Blocks.BEDROCK)
            {
                break;
            }
        }

        this.beamYMax = y;
        this.renderBB = new AxisAlignedBB(posX - 4d, this.beamYMin - 4d, posZ - 4d, posX + 4d, this.beamYMax + 4d, posZ + 4d);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0d;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.renderBB != null ? this.renderBB : INFINITE_EXTENT_AABB;
    }

    public enum Type
    {
        RESONATOR,
        RECEIVER,
        TRANSMITTER,
        INVALID;

        public static Type fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : INVALID;
        }

        public int getMeta()
        {
            return this.ordinal();
        }
    }
}
