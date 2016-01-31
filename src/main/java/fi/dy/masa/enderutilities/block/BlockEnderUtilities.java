package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

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
        this.setBlockName(name);
    }

    @Override
    public Block setBlockName(String name)
    {
        return super.setBlockName(ReferenceNames.getPrefixedName(name));
    }

    @Override
    public int damageDropped(int meta)
    {
        return meta;
    }
}
