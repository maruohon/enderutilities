package fi.dy.masa.enderutilities.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.effects.Sounds;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class BlockMolecularExciter extends BlockEnderUtilities
{
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockMolecularExciter(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.propFacing = FACING;
        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING)
                .withProperty(POWERED, false));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING, POWERED });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_MOLECULAR_EXCITER
        };
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.getFront(meta & 0x7))
                .withProperty(POWERED, (meta & 0x8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getIndex() | (state.getValue(POWERED) ? 0x8 : 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, ItemStack stack)
    {
        return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        if (worldIn.isRemote == false && worldIn.isBlockPowered(pos))
        {
            this.scheduleBlockUpdate(worldIn, pos, state, 1, true);
        }
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.isRemote == false && worldIn.isBlockPowered(pos))
        {
            convertBlockToFallingBlockEntity(worldIn, pos.offset(state.getValue(FACING)));
        }
    }

    public static void convertBlockToFallingBlockEntity(World world, BlockPos pos)
    {
        if (world.getWorldBorder().contains(pos) && world.isAirBlock(pos) == false &&
            world.getBlockState(pos).getBlockHardness(world, pos) >= 0F)
        {
            world.spawnEntity(EntityFallingBlockEU.convertBlockToEntity(world, pos));
            world.playSound(null, pos, Sounds.MOLECULAR_EXCITER, SoundCategory.BLOCKS, 1f, 1f);
        }
    }
}
