package fi.dy.masa.enderutilities.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.capabilities.EnderUtilitiesCapabilities;
import fi.dy.masa.enderutilities.capabilities.IPortalCooldownCapability;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.event.TickHandler;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class BlockEnderUtilitiesPortal extends BlockEnderUtilitiesTileEntity
{
    protected static final AxisAlignedBB PORTAL_BOUNDS_NS = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
    protected static final AxisAlignedBB PORTAL_BOUNDS_WE = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
    protected static final AxisAlignedBB PORTAL_BOUNDS_UD = new AxisAlignedBB(0.0D, 0.375D, 0.0D, 1.0D, 0.625D, 1.0D);

    public BlockEnderUtilitiesPortal(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.propFacing = FACING;
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING));
        this.setSoundType(SoundType.GLASS);
        this.setBlockUnbreakable();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { this.blockName };
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 0x7));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityPortal();
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        switch (state.getValue(FACING).getAxis())
        {
            case X:
                return PORTAL_BOUNDS_WE;
            case Z:
                return PORTAL_BOUNDS_NS;
            default:
                return PORTAL_BOUNDS_UD;
        }
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        EnumFacing facing = state.getValue(FACING);
        return side == facing || side == facing.getOpposite();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return false;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand)
    {
        Effects.spawnParticlesAround(worldIn, EnumParticleTypes.PORTAL, pos, 2, rand);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        if (worldIn.isRemote == false)
        {
            if (entityIn instanceof EntityPlayer)
            {
                TickHandler.instance().addPlayerToTeleport((EntityPlayer) entityIn);
            }
            else
            {
                this.teleportEntity(worldIn, pos, state, entityIn);
            }
        }
    }

    public void teleportEntity(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        if (world.isRemote == false)
        {
            TileEntityPortal te = getTileEntitySafely(world, pos, TileEntityPortal.class);

            if (te != null && te.getDestination() != null &&
                entity.getEntityBoundingBox().intersects(state.getBoundingBox(world, pos).offset(pos)))
            {
                final long currentTime = world.getTotalWorldTime();

                // The entity needs to be outside a portal for 10 ticks
                if (this.isEntityUnderPortalCooldown(entity, currentTime, 10) == false)
                {
                    OwnerData ownerData = te.getOwner();

                    if (ownerData == null || ownerData.canAccess(entity))
                    {
                        TargetData target = te.getDestination();

                        if (te.targetIsPortal())
                        {
                            World worldDst = entity.getServer().getWorld(target.dimension);

                            if (worldDst == null || worldDst.getBlockState(target.pos).getBlock() != EnderUtilitiesBlocks.PORTAL)
                            {
                                return;
                            }
                        }

                        entity = TeleportEntity.teleportEntityUsingTarget(entity, target, false, true, true);
                    }
                }

                if (entity != null)
                {
                    this.updatePortalCooldown(entity, currentTime);
                }
            }
        }
    }

    private boolean isEntityUnderPortalCooldown(Entity entity, long currentTime, int cooldown)
    {
        IPortalCooldownCapability cap = entity.getCapability(EnderUtilitiesCapabilities.CAPABILITY_PORTAL_COOLDOWN, null);
        return cap != null && ((currentTime - cap.getLastInPortalTime()) < cooldown);
    }

    private void updatePortalCooldown(Entity entity, long currentTime)
    {
        IPortalCooldownCapability cap = entity.getCapability(EnderUtilitiesCapabilities.CAPABILITY_PORTAL_COOLDOWN, null);

        if (cap != null)
        {
            cap.setLastInPortalTime(currentTime);
        }
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        // NO-OP to not call updateTick() from here
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.isRemote == false && this.checkCanStayAndScheduleBreaking(worldIn, pos, state) == false)
        {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.2f, 0.8f);
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (worldIn.isRemote == false && this.checkCanStayAndScheduleBreaking(worldIn, pos, state) == false)
        {
            worldIn.scheduleBlockUpdate(pos, this, 0, 0);
        }
    }

    /**
     * Returns false if the block can't stay and should be broken,
     * and also schedules the checks for all the adjacent blocks.
     */
    private boolean checkCanStayAndScheduleBreaking(World world, BlockPos pos, IBlockState state)
    {
        EnumFacing facing = state.getValue(FACING);
        BlockPos[] positions = PositionUtils.getAdjacentPositions(pos, facing, false);
        boolean canStay = true;

        for (BlockPos posTmp : positions)
        {
            Block block = world.getBlockState(posTmp).getBlock();

            if (block != this && block != EnderUtilitiesBlocks.PORTAL_FRAME)
            {
                for (BlockPos posTmp2 : positions)
                {
                    block = world.getBlockState(posTmp2).getBlock();

                    if (block == this)
                    {
                        world.scheduleBlockUpdate(posTmp2, block, 0, 0);
                    }
                }

                canStay = false;
                break;
            }
        }

        return canStay;
    }
}
