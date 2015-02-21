package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;

public class BlockEnderUtilities extends Block
{
    public BlockEnderUtilities(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
        this.setStepSound(soundTypeStone);
    }

    public BlockEnderUtilities(int index, String name, float hardness, Material material)
    {
        super(material);
        this.setHardness(hardness);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
    }

    @Override
    public int damageDropped(IBlockState iBlockState)
    {
        return this.getMetaFromState(iBlockState);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState iBlockState)
    {
        //super.onBlockAdded(world, x, y, z);
        //this.func_149930_e(world, x, y, z);
        this.onNeighborBlockChange(world, pos, iBlockState, this);
    }
}
