package fi.dy.masa.enderutilities.block;

import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityElevator;

public class BlockElevatorSlab extends BlockElevator
{
    public static final AxisAlignedBB BOUNDS_SLAB_BOTTOM   = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    public static final AxisAlignedBB BOUNDS_SLAB_TOP      = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final AxisAlignedBB BOUNDS_LAYER_BOTTOM  = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2 / 16D, 1.0D);
    public static final AxisAlignedBB BOUNDS_LAYER_TOP     = new AxisAlignedBB(0.0D, 14D / 16D, 0.0D, 1.0D, 1.0D, 1.0D);

    public static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.<EnumBlockHalf>create("half", EnumBlockHalf.class);

    public BlockElevatorSlab(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(COLOR, EnumDyeColor.WHITE)
                .withProperty(HALF, EnumBlockHalf.BOTTOM));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { COLOR, HALF }, new IUnlistedProperty<?>[] { CAMOBLOCKSTATE, CAMOBLOCKSTATEEXTENDED });
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new ItemBlockEnderUtilities(this)
        {
            @Override
            public String getItemStackDisplayName(ItemStack stack)
            {
                String name = super.getItemStackDisplayName(stack);
                return name.replace("{COLOR}", EnumDyeColor.byMetadata(stack.getMetadata()).getName());
            }

            @Override
            public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                    EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
            {
                if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
                {
                    TileEntityElevator te = getTileEntitySafely(world, pos, TileEntityElevator.class);

                    if (te != null)
                    {
                        te.setIsTopHalf(newState.getValue(HALF) == EnumBlockHalf.TOP);
                    }

                    return true;
                }
                else
                {
                    return false;
                }
            }
        };
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);

        boolean top = facing == EnumFacing.DOWN || (facing.getAxis().isHorizontal() && hitY >= 0.5f);
        state = state.withProperty(HALF, top ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);

        return state;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        // Don't try to set the facing as the elevator doesn't have one, which is what the super would do
        TileEntityElevator te = getTileEntitySafely(world, pos, TileEntityElevator.class);

        if (te != null)
        {
            state = state.withProperty(HALF, te.isTopHalf() ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
        }

        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        if (this == EnderUtilitiesBlocks.ELEVATOR_SLAB)
        {
            state = state.getActualState(blockAccess, pos);
            return state.getValue(HALF) == EnumBlockHalf.TOP ? BOUNDS_SLAB_TOP : BOUNDS_SLAB_BOTTOM;
        }
        else if (this == EnderUtilitiesBlocks.ELEVATOR_LAYER)
        {
            state = state.getActualState(blockAccess, pos);
            return state.getValue(HALF) == EnumBlockHalf.TOP ? BOUNDS_LAYER_TOP : BOUNDS_LAYER_BOTTOM;
        }

        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
}
