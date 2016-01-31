package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

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
}
