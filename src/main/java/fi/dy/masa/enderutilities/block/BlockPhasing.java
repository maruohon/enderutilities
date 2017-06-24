package fi.dy.masa.enderutilities.block;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class BlockPhasing extends BlockEnderUtilities
{
    public static final PropertyBool INVERTED = PropertyBool.create("inverted");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockPhasing(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(INVERTED, false)
                .withProperty(POWERED, false));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { INVERTED, POWERED });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_PHASING,
                ReferenceNames.NAME_TILE_PHASING + "_inverted"
        };
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        this.neighborChanged(state, worldIn, pos, this, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (worldIn.isRemote == false)
        {
            boolean powered = worldIn.isBlockPowered(pos);

            if (powered != state.getValue(POWERED))
            {
                worldIn.setBlockState(pos, state.withProperty(POWERED, powered), 2);
            }
        }
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(INVERTED) ? 0x1 : 0x0;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
                .withProperty(INVERTED, (meta & 0x1) != 0)
                .withProperty(POWERED, (meta & 0x8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(INVERTED) ? 0x1 : 0x0) | (state.getValue(POWERED) ? 0x8 : 0x0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public boolean isTopSolid(IBlockState state)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return state.getValue(POWERED) == state.getValue(INVERTED);
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return this.isBlockNormalCube(worldIn.getBlockState(pos)) == false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state)
    {
        return this.isBlockNormalCube(state);
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        boolean phased = this.isBlockNormalCube(state) == false;
        return (phased && layer == BlockRenderLayer.TRANSLUCENT) || (phased == false && layer == BlockRenderLayer.SOLID);
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return this.isBlockNormalCube(state) ? 255 : 0;
    }

    @Override
    public boolean isTranslucent(IBlockState state)
    {
        return this.isBlockNormalCube(state) == false;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        return state.getValue(INVERTED) ? MapColor.ORANGE_STAINED_HARDENED_CLAY : MapColor.GRAY_STAINED_HARDENED_CLAY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState stateAdjacent = blockAccess.getBlockState(pos.offset(side));

        if (state != stateAdjacent)
        {
            return true;
        }
        else if (stateAdjacent.getBlock() == this)
        {
            return false;
        }

        return super.shouldSideBeRendered(state, blockAccess, pos, side);
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state.getValue(POWERED) == state.getValue(INVERTED) ? FULL_BLOCK_AABB : NULL_AABB;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // normal
        list.add(new ItemStack(this, 1, 1)); // inverted
    }
}
