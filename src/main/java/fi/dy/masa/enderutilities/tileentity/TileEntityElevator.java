package fi.dy.masa.enderutilities.tileentity;

import java.util.List;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class TileEntityElevator extends TileEntityEnderUtilities
{
    public static Predicate<TileEntity> isMatchingElevator(final EnumDyeColor color)
    {
        return new Predicate<TileEntity> ()
        {
            public boolean apply(TileEntity te)
            {
                if ((te instanceof TileEntityElevator) == false)
                {
                    return false;
                }

                IBlockState state = te.getWorld().getBlockState(te.getPos());

                return state.getBlock() instanceof BlockElevator && state.getValue(BlockElevator.COLOR) == color;
            }
        };
    }

    private boolean redstoneState;

    public TileEntityElevator()
    {
        super(ReferenceNames.NAME_TILE_ENDER_ELEVATOR);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.redstoneState = nbt.getBoolean("Redstone");
    }

    @Override
    public NBTTagCompound writeToNBTCustom(NBTTagCompound nbt)
    {
        // Override without calling super, because the Elevator
        // doesn't use/need rotation, owner data etc.
        nbt.setBoolean("Redstone", this.redstoneState);

        if (this.camoState != null)
        {
            nbt.setInteger("Camo", Block.getStateId(this.camoState));
        }

        return nbt;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean redstone = worldIn.isBlockPowered(pos);

        if (redstone != this.redstoneState && redstone)
        {
            this.activateByRedstone(worldIn.isBlockIndirectlyGettingPowered(pos) < 8);
        }

        this.redstoneState = redstone;
    }

    public void activateByRedstone(boolean goingUp)
    {
        List<Entity> entities = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().up()));

        for (Entity entity : entities)
        {
            this.activateForEntity(entity, goingUp);
        }
    }

    public void activateForEntity(Entity entity, boolean goingUp)
    {
        Vec3d posDifference = this.getMoveVector(goingUp);

        if (posDifference != null)
        {
            entity.setPositionAndUpdate(entity.posX + posDifference.xCoord, entity.posY + posDifference.yCoord, entity.posZ + posDifference.zCoord);
            this.getWorld().playSound(null, entity.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.3f, 1.8f);
        }
    }

    private Vec3d getMoveVector(boolean goingUp)
    {
        BlockPos center = goingUp ? this.getPos().up() : this.getPos().down();
        int rangeHorizontal = 64;
        int rangeVerticalUp = goingUp ? 256 : 0;
        int rangeVerticalDown = goingUp ? 0 : 256;

        IBlockState state = this.getWorld().getBlockState(this.getPos());

        if ((state.getBlock() instanceof BlockElevator) == false)
        {
            EnderUtilities.logger.warn("Wrong block in {}#getMoveVector", this.getClass().getSimpleName());
            return null;
        }

        List<BlockPosDistance> elevators = PositionUtils.getTileEntityPositions(this.getWorld(), center,
                rangeHorizontal, rangeVerticalUp, rangeVerticalDown, isMatchingElevator(state.getValue(BlockElevator.COLOR)));

        if (elevators.size() > 0)
        {
            World world = this.getWorld();
            BlockPos posFound = elevators.get(0).pos;
            AxisAlignedBB bbThis = world.getBlockState(this.getPos()).getBoundingBox(world, this.getPos());
            AxisAlignedBB bbFound = world.getBlockState(posFound).getBoundingBox(world, posFound);
            double yDiff = (posFound.getY() + bbFound.maxY) - (this.getPos().getY() + bbThis.maxY);
            return new Vec3d(posFound.getX() - this.getPos().getX(), yDiff, posFound.getZ() - this.getPos().getZ());
        }

        return null;
    }

    @Override
    protected boolean hasCamouflageAbility()
    {
        return true;
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }
}
