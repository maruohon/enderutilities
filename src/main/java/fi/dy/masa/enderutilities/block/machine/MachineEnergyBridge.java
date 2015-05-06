package fi.dy.masa.enderutilities.block.machine;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.util.BlockPos;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, livingBase, stack);

        if (world.isRemote == false)
        {
            this.tryAssembleMultiBlock(world, x, y, z);
        }
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
    {
        super.onBlockPreDestroy(world, x, y, z, oldMeta);

        if (world.isRemote == false)
        {
            this.tryDisassembleMultiblock(world, x, y, z, oldMeta);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z, Block block, int meta)
    {
        return 12;
    }

    public void tryAssembleMultiBlock(World world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);

        if (block != EnderUtilitiesBlocks.machine_1 || meta > 2 || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
        }

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

    public void tryDisassembleMultiblock(World world, int x, int y, int z, int oldMeta)
    {
        // The End has the transmitter, and in a slightly different position than the receivers are
        if (world.provider.dimensionId == 1)
        {
            this.tryDisassembleMultiblock(world, x, y, z, 4, 0, oldMeta);
        }
        else
        {
            this.tryDisassembleMultiblock(world, x, y, z, 1, 1, oldMeta);
        }
    }

    public void tryAssembleMultiBlock(World world, int x, int y, int z, int height, int masterMeta, boolean requireEnderCrystal)
    {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);

        if (block != EnderUtilitiesBlocks.machine_1 || (meta != masterMeta && meta != 2) || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
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

        BlockPos posNorth = new BlockPos(posResonatorBase, ForgeDirection.NORTH, 3);
        BlockPos posSouth = new BlockPos(posResonatorBase, ForgeDirection.SOUTH, 3);
        BlockPos posEast = new BlockPos(posResonatorBase, ForgeDirection.EAST, 3);
        BlockPos posWest = new BlockPos(posResonatorBase, ForgeDirection.WEST, 3);
        boolean isValid = false;

        if (this.isValidBlock(world, posNorth, 2, ForgeDirection.SOUTH) && this.isValidBlock(world, posSouth, 2, ForgeDirection.NORTH) &&
            this.isValidBlock(world, posEast, 2, ForgeDirection.WEST) && this.isValidBlock(world, posWest, 2, ForgeDirection.EAST) &&
            this.isValidBlock(world, new BlockPos(posMaster), masterMeta, ForgeDirection.UNKNOWN))
        {
            if (requireEnderCrystal == false)
            {
                isValid = true;
            }
            else
            {
                double xd = posResonatorBase.posX;
                double yd = posResonatorBase.posY;
                double zd = posResonatorBase.posZ;
                double d = 0.0d;
                List<Entity> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class, AxisAlignedBB.getBoundingBox(xd - d, yd - d, zd - d, xd + d, yd + d, zd + d));

                if (list.size() == 1)
                {
                    isValid = true;
                }
            }
        }

        if (isValid == true)
        {
            this.setState(world, posNorth, true);
            this.setState(world, posSouth, true);
            this.setState(world, posEast, true);
            this.setState(world, posWest, true);
            this.setState(world, posMaster, true);
        }
    }

    public void tryDisassembleMultiblock(World world, int x, int y, int z, int height, int masterMeta, int oldMeta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te == null || (te instanceof TileEntityEnergyBridge) == false)
        {
            return;
        }

        BlockPos posMaster = new BlockPos(x, y, z); // position of the master block (the transmitter or the receiver)
        BlockPos posResonatorBase = new BlockPos(x, y, z); // position of the middle block in the y-plane of the resonators
        ForgeDirection dir = ForgeDirection.getOrientation(((TileEntityEnergyBridge)te).getRotation());

        // The given location is a resonator, not the master block; get the master block's location
        if (oldMeta == 2)
        {
            posMaster.add(0, height - 1, 0);
            posMaster.offset(dir, 3);
            posResonatorBase.offset(dir, 3);
        }
        else
        {
            posResonatorBase.add(0, -(height - 1), 0);
        }

        this.setStateWithCheck(world, new BlockPos(posResonatorBase, ForgeDirection.NORTH, 3), 2, ForgeDirection.SOUTH, false);
        this.setStateWithCheck(world, new BlockPos(posResonatorBase, ForgeDirection.SOUTH, 3), 2, ForgeDirection.NORTH, false);
        this.setStateWithCheck(world, new BlockPos(posResonatorBase, ForgeDirection.EAST, 3), 2, ForgeDirection.WEST, false);
        this.setStateWithCheck(world, new BlockPos(posResonatorBase, ForgeDirection.WEST, 3), 2, ForgeDirection.EAST, false);
        this.setStateWithCheck(world, new BlockPos(posMaster), masterMeta, ForgeDirection.UNKNOWN, false);
    }

    public boolean isValidBlock(World world, BlockPos pos, int requiredMeta, ForgeDirection requiredDirection)
    {
        Block block = world.getBlock(pos.posX, pos.posY, pos.posZ);
        int meta = world.getBlockMetadata(pos.posX, pos.posY, pos.posZ);
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);

        if (block == EnderUtilitiesBlocks.machine_1 && meta == requiredMeta && te instanceof TileEntityEnergyBridge
            && (requiredDirection == ForgeDirection.UNKNOWN || ForgeDirection.getOrientation(((TileEntityEnergyBridge)te).getRotation()).equals(requiredDirection)))
        {
            return true;
        }

        return false;
    }

    public void setState(World world, BlockPos pos, boolean state)
    {
        TileEntity te = world.getTileEntity(pos.posX, pos.posY, pos.posZ);
        if (te instanceof TileEntityEnergyBridge)
        {
            ((TileEntityEnergyBridge)te).setState(state);
        }
    }

    public void setStateWithCheck(World world, BlockPos pos, int requiredMeta, ForgeDirection requiredDirection, boolean state)
    {
        if (this.isValidBlock(world, pos, requiredMeta, requiredDirection) == true)
        {
            ((TileEntityEnergyBridge)world.getTileEntity(pos.posX, pos.posY, pos.posZ)).setState(state);
        }
    }
}
