package fi.dy.masa.enderutilities.tileentity;

import java.util.List;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

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
    private IBlockState camoState;

    public TileEntityElevator()
    {
        super(ReferenceNames.NAME_TILE_ENDER_ELEVATOR);
    }

    /**
     * @return the current camouflage block state. If none is set, then Blocks.AIR.getDefaultState() is returned.
     */
    public IBlockState getCamoState()
    {
        return this.camoState != null ? this.camoState : Blocks.AIR.getDefaultState();
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.redstoneState = nbt.getBoolean("Redstone");

        if (nbt.hasKey("Camo", Constants.NBT.TAG_COMPOUND))
        {
            this.camoState = NBTUtils.readBlockStateFromTag(nbt.getCompoundTag("Camo"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("Redstone", this.redstoneState);

        if (this.camoState != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTUtils.writeBlockStateToTag(this.camoState, tag);
            nbt.setTag("Camo", tag);
        }

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        if (this.camoState != null)
        {
            nbt.setInteger("camo", Block.getStateId(this.camoState));
        }

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        if (tag.hasKey("camo", Constants.NBT.TAG_INT))
        {
            this.camoState = Block.getStateById(tag.getInteger("camo"));
            this.notifyBlockUpdate();
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    public void onRightClick(EntityPlayer player, EnumHand hand, EnumFacing side)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack != null)
        {
            // Right clicking with an ItemBlock
            if (stack.getItem() instanceof ItemBlock)
            {
                ItemBlock item = (ItemBlock) stack.getItem();
                int meta = item.getMetadata(stack.getMetadata());
                this.camoState = item.block.getStateForPlacement(this.getWorld(), this.getPos(), EnumFacing.UP, 0.5f, 1f, 0.5f, meta, player, stack);
                this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
                this.notifyBlockUpdate();
            }
        }
        // Sneaking with an empty hand, clear the camo block
        else if (player.isSneaking())
        {
            this.camoState = null;
            this.getWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1f, 1f);
            this.notifyBlockUpdate();
        }
    }

    private void notifyBlockUpdate()
    {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 3);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean redstone = worldIn.isBlockPowered(pos);

        if (redstone != this.redstoneState && redstone == true)
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
}
