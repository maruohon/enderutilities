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
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.event.TickHandler;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
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
        return null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World worldIn, BlockPos pos)
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

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SideOnly(Side.CLIENT)
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
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { this.blockName };
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

            if (te != null && entity.getEntityBoundingBox().intersectsWith(state.getBoundingBox(world, pos).offset(pos)))
            {
                TargetData target = te.getDestination();

                if (target != null)
                {
                    OwnerData owner = te.getOwner();

                    if (owner == null || owner.canAccess(entity))
                    {
                        TeleportEntity.teleportEntityUsingTarget(entity, target, true, true);
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        EnumFacing facing = state.getValue(FACING);

        for (EnumFacing side : EnumFacing.values())
        {
            if (side.getAxis() != facing.getAxis())
            {
                Block block = worldIn.getBlockState(pos.offset(side)).getBlock();

                if (block != this && block != EnderUtilitiesBlocks.blockPortalFrame)
                {
                    worldIn.setBlockToAir(pos);
                    break;
                }
            }
        }
    }
}
