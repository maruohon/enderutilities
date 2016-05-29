package fi.dy.masa.enderutilities.tileentity;

import java.util.List;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class TileEntityEnderElevator extends TileEntityEnderUtilities
{
    public static Predicate<TileEntity> isMatchingElevator(final EnumDyeColor color)
    {
        return new Predicate<TileEntity> ()
        {
            public boolean apply(TileEntity te)
            {
                if ((te instanceof TileEntityEnderElevator) == false)
                {
                    return false;
                }

                IBlockState state = te.getWorld().getBlockState(te.getPos());

                return state.getBlock() == EnderUtilitiesBlocks.blockElevator && state.getValue(BlockElevator.COLOR) == color;
            }
        };
    }

    private boolean redstoneState;

    public TileEntityEnderElevator()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_ELEVATOR);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.redstoneState = nbt.getBoolean("Redstone");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("Redstone", this.redstoneState);
    }

    public void onNeighbourChange()
    {
        boolean redstone = this.getWorld().isBlockPowered(this.getPos());

        if (redstone != this.redstoneState && redstone == true)
        {
            this.activateByRedstone(this.getWorld().isBlockIndirectlyGettingPowered(this.getPos()) >= 8);
        }
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
        BlockPos targetPos = this.getMoveVector(goingUp);

        if (targetPos != null)
        {
            entity.setPositionAndUpdate(entity.posX + targetPos.getX(), entity.posY + targetPos.getY(), entity.posZ + targetPos.getZ());
            this.getWorld().playSound(null, entity.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.3f, 1.8f);
        }
    }

    private BlockPos getMoveVector(boolean goingUp)
    {
        BlockPos center = goingUp ? this.getPos().up() : this.getPos().down();
        int rangeHorizontal = 32;
        int rangeVerticalUp = goingUp ? 256 : 0;
        int rangeVerticalDown = goingUp ? 0 : 256;

        IBlockState state = this.getWorld().getBlockState(this.getPos());
        if (state.getBlock() != EnderUtilitiesBlocks.blockElevator)
        {
            EnderUtilities.logger.warn("Wrong block in {}#getMoveVector", this.getClass().getSimpleName());
            return null;
        }

        List<BlockPosDistance> elevators = PositionUtils.getTileEntityPositions(this.getWorld(), center,
                rangeHorizontal, rangeVerticalUp, rangeVerticalDown, isMatchingElevator(state.getValue(BlockElevator.COLOR)));

        if (elevators.size() > 0)
        {
            return elevators.get(0).pos.subtract(this.getPos());
        }

        return null;
    }
}
