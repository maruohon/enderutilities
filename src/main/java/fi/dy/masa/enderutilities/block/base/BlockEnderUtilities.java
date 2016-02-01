package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import fi.dy.masa.enderutilities.creativetab.CreativeTab;

public class BlockEnderUtilities extends Block
{
    protected String blockName;
    protected String[] unlocalizedNames;

    public BlockEnderUtilities(String name, float hardness, int harvestLevel, Material material)
    {
        super(material);
        this.setHardness(hardness);
        this.setHarvestLevel("pickaxe", harvestLevel);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setStepSound(soundTypeStone);
        this.blockName = name;
        this.unlocalizedNames = this.getUnlocalizedNames();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    protected String[] getUnlocalizedNames()
    {
        return new String[] { this.blockName };
    }
}
