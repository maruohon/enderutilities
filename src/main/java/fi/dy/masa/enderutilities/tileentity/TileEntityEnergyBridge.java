package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockPos;
import fi.dy.masa.enderutilities.util.BlockUtils;

public class TileEntityEnergyBridge extends TileEntityEnderUtilities
{
    public boolean isActive;
    public boolean isMaster;
    public int timer;
    public byte meta;

    @SideOnly(Side.CLIENT)
    public int beamYMin;
    @SideOnly(Side.CLIENT)
    public int beamYMax;

    public TileEntityEnergyBridge()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        byte f = nbt.getByte("Flags");
        this.isActive = (f & 0x01) == 0x01;
        this.isMaster = (f & 0x02) == 0x02;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("Flags", (byte)((this.isMaster ? 0x02 : 0x00) | (this.isActive ? 0x01 : 0x00)));
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setByte("f", (byte)((this.isMaster ? 0x02 : 0x00) | (this.isActive ? 0x01 : 0x00)));

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();
        byte f = nbt.getByte("f");

        this.setState((f & 0x01) == 0x01);
        this.setMaster((f & 0x02) == 0x02);

        // meta is used for TESR rendering, cached for performance
        this.meta = (byte)this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        this.getBeamEndPoints();

        super.onDataPacket(net, packet);
    }

    public void setState(boolean state)
    {
        this.isActive = state;
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void setMaster(boolean isMaster)
    {
        this.isMaster = isMaster;
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void updateEntity()
    {
        /*if (this.isMaster == true && ++this.timer >= 20)
        {
            this.timer = 0;
        }*/
    }

    public void tryAssembleMultiBlock(World world, int x, int y, int z)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        if (world.provider.dimensionId == 1)
        {
            this.tryAssembleMultiBlock(world, x, y, z, 4, 0, true);
        }
        else
        {
            this.tryAssembleMultiBlock(world, x, y, z, 1, 1, false);
        }
    }

    public void disassembleMultiblock(World world, int x, int y, int z, int oldMeta)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        if (world.provider.dimensionId == 1)
        {
            this.disassembleMultiblock(world, x, y, z, 4, 0, oldMeta);
        }
        else
        {
            this.disassembleMultiblock(world, x, y, z, 1, 1, oldMeta);
        }
    }

    public void tryAssembleMultiBlock(World world, int x, int y, int z, int height, int masterMeta, boolean requireEnderCrystal)
    {
        List<BlockPos> positions = new ArrayList<BlockPos>();
        if (this.getBlockPositions(world, x, y, z, height, masterMeta, positions) == false || positions.size() != 6)
        {
            return;
        }

        if (this.isStructureValid(world, x, y, z, height, masterMeta, requireEnderCrystal, positions) == true)
        {
            this.activateMultiBlock(world, positions);

            // Master block
            BlockPos pos = positions.get(0);
            this.setMaster(world, pos, true);
        }
    }

    public void activateMultiBlock(World world, List<BlockPos> blockPositions)
    {
        for (int i = 0; i < 5; i++)
        {
            this.setState(world, blockPositions.get(i), true);
        }
    }

    public boolean getBlockPositions(World world, int x, int y, int z, int height, int masterMeta, List<BlockPos> blockPositions)
    {
        blockPositions.clear();

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);

        if (block != EnderUtilitiesBlocks.machine_1 || (meta != masterMeta && meta != 2) || (te instanceof TileEntityEnergyBridge) == false)
        {
            return false;
        }

        BlockPos posMaster = new BlockPos(x, y, z);
        BlockPos posResonatorBase = new BlockPos(x, y, z); // position of the middle block in the y-plane of the resonators
        ForgeDirection dir = ForgeDirection.getOrientation(((TileEntityEnergyBridge)te).getRotation());

        // The given location is a resonator, not the master block; get the master block's location
        if (meta != masterMeta)
        {
            posMaster.add(0, height - 1, 0);
            posMaster.offset(dir, 3);
            posResonatorBase.offset(dir, 3);
        }
        else
        {
            posResonatorBase.add(0, -(height - 1), 0);
        }

        blockPositions.add(posMaster);
        blockPositions.add(new BlockPos(posResonatorBase, ForgeDirection.NORTH, 3));
        blockPositions.add(new BlockPos(posResonatorBase, ForgeDirection.SOUTH, 3));
        blockPositions.add(new BlockPos(posResonatorBase, ForgeDirection.EAST, 3));
        blockPositions.add(new BlockPos(posResonatorBase, ForgeDirection.WEST, 3));
        blockPositions.add(posResonatorBase);

        return true;
    }

    public boolean isStructureValid(World world, int x, int y, int z, int height, int masterMeta, boolean requireEnderCrystal, List<BlockPos> blockPositions)
    {
        Block blockEb = EnderUtilitiesBlocks.machine_1;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;
        boolean isValid = false;

        if (BlockUtils.blockMatches(world, blockPositions.get(0), blockEb, masterMeta, classTEEB, ForgeDirection.UNKNOWN) &&
            BlockUtils.blockMatches(world, blockPositions.get(1), blockEb, 2, classTEEB, ForgeDirection.SOUTH) &&
            BlockUtils.blockMatches(world, blockPositions.get(2), blockEb, 2, classTEEB, ForgeDirection.NORTH) &&
            BlockUtils.blockMatches(world, blockPositions.get(3), blockEb, 2, classTEEB, ForgeDirection.WEST) &&
            BlockUtils.blockMatches(world, blockPositions.get(4), blockEb, 2, classTEEB, ForgeDirection.EAST))
        {
            if (requireEnderCrystal == false)
            {
                isValid = true;
            }
            else
            {
                double xd = blockPositions.get(5).posX;
                double yd = blockPositions.get(5).posY;
                double zd = blockPositions.get(5).posZ;
                double d = 0.0d;
                List<Entity> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class, AxisAlignedBB.getBoundingBox(xd - d, yd - d, zd - d, xd + d, yd + d, zd + d));

                if (list.size() == 1)
                {
                    isValid = true;
                }
            }
        }

        // Our machine blocks are all in the right configuration, now just check that there are no other obstructing blocks in the area
        if (isValid == true)
        {
            return this.isObstructed(world, blockEb, height, blockPositions) == false;
        }

        return false;
    }

    public boolean isObstructed(World world, Block blockEb, int height, List<BlockPos> blockPositions)
    {
        if (blockPositions.size() != 6)
        {
            return true;
        }

        BlockPos pos = new BlockPos(blockPositions.get(1)); // North side resonator
        pos.add(-3, 0, 0); // move the position to the north-west corner (lower coordinates corner)

        int ty = pos.posY;

        // The whole box containing the resonators must be air, other than the resonators themselves
        for (int tx = pos.posX; tx <= pos.posX + 6; tx++)
        {
            for (int tz = pos.posZ; tz <= pos.posZ + 6; tz++)
            {
                Block b = world.getBlock(tx, ty, tz);
                if (b.isAir(world, tx, ty, tz) == false && b != Blocks.fire && (b != blockEb || blockPositions.contains(new BlockPos(tx, ty, tz)) == false))
                {
                    return true;
                }
            }
        }

        ty = pos.posY + 1;
        pos = new BlockPos(blockPositions.get(0)); // Master block
        // The column under the transmitter must be air
        for (; ty < pos.posY; ty++)
        {
            Block b = world.getBlock(pos.posX, ty, pos.posZ);
            if (b.isAir(world, pos.posX, ty, pos.posZ) == false && b != Blocks.fire)
            {
                return true;
            }
        }

        return false;
    }

    public void disassembleMultiblock(World world, int x, int y, int z, int height, int masterMeta, int oldMeta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te == null || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
        }

        BlockPos posMaster = new BlockPos(x, y, z); // position of the master block (the transmitter or the receiver)

        // The given location is a resonator, not the master block; get the master block's location
        if (oldMeta == 2)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(((TileEntityEnergyBridge)te).getRotation());
            posMaster.add(0, height - 1, 0);
            posMaster.offset(dir, 3);
        }

        // Get the block position list from the master block
        List<BlockPos> positions = new ArrayList<BlockPos>();
        if (this.getBlockPositions(world, x, y, z, height, masterMeta, positions) == false)
        {
            return;
        }

        this.disableMultiBlock(world, masterMeta, positions, true);
    }

    public void disableMultiBlock(World world, int masterMeta, List<BlockPos> blockPositions, boolean clearList)
    {
        if (blockPositions == null || blockPositions.size() != 6)
        {
            return;
        }

        Block blockEb = EnderUtilitiesBlocks.machine_1;
        Class<TileEntityEnergyBridge> classTEEB = TileEntityEnergyBridge.class;

        this.setStateWithCheck(world, blockPositions.get(0), blockEb, masterMeta, classTEEB, ForgeDirection.UNKNOWN, false);
        this.setStateWithCheck(world, blockPositions.get(1), blockEb, 2, classTEEB, ForgeDirection.SOUTH, false);
        this.setStateWithCheck(world, blockPositions.get(2), blockEb, 2, classTEEB, ForgeDirection.NORTH, false);
        this.setStateWithCheck(world, blockPositions.get(3), blockEb, 2, classTEEB, ForgeDirection.WEST, false);
        this.setStateWithCheck(world, blockPositions.get(4), blockEb, 2, classTEEB, ForgeDirection.EAST, false);

        if (clearList)
        {
            blockPositions.clear();
        }
    }

    public void setState(World world, BlockPos pos, boolean state)
    {
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setState(state);
        }
    }

    public void setStateWithCheck(World world, BlockPos pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, ForgeDirection requiredDirection, boolean state)
    {
        if (BlockUtils.blockMatches(world, pos, requiredBlock, requiredMeta, TEClass, requiredDirection) == true)
        {
            ((TileEntityEnergyBridge)world.getTileEntity(pos.posX, pos.posY, pos.posZ)).setState(state);
        }
    }

    public void setMaster(World world, BlockPos pos, boolean state)
    {
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setMaster(state);
        }
    }

    @SideOnly(Side.CLIENT)
    public void getBeamEndPoints()
    {
        // Energy Bridge Transmitter
        if (this.meta == 0)
        {
            this.beamYMin = this.yCoord - 2;
            this.beamYMax = this.worldObj.getHeight();
        }
        // Energy Bridge Receiver
        else if (this.meta == 1)
        {
            int ty = this.yCoord;

            for (; ty >= 0; --ty)
            {
                if (this.worldObj.getBlock(this.xCoord, ty, this.zCoord) == Blocks.bedrock)
                {
                    break;
                }
            }

            this.beamYMin = ty + 1;

            for (ty = this.yCoord; ty < this.worldObj.getHeight(); ++ty)
            {
                if (this.worldObj.getBlock(this.xCoord, ty, this.zCoord) == Blocks.bedrock)
                {
                    break;
                }
            }

            this.beamYMax = ty;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0d;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }
}
