package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;

public class BlockEnderUtilities extends Block
{
    public static final EnumFacing DEFAULT_FACING = EnumFacing.NORTH;
    public static final PropertyDirection FACING = BlockPropertiesEU.FACING;

    protected String blockName;
    protected String[] unlocalizedNames;
    protected String[] tooltipNames;

    public BlockEnderUtilities(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(material);
        this.setRegistryName(name);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setHarvestLevel("pickaxe", harvestLevel);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setSoundType(SoundType.STONE);
        this.blockName = name;
        this.unlocalizedNames = this.generateUnlocalizedNames();
        this.tooltipNames = this.generateTooltipNames();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    protected String[] generateUnlocalizedNames()
    {
        return new String[] { this.blockName };
    }

    /**
     * Generate the names used to look up tooltips for the ItemBlocks.
     * To use a common tooltip for all variants of the block, return an array with exactly one entry in it.
     * @return
     */
    protected String[] generateTooltipNames()
    {
        return this.generateUnlocalizedNames();
    }

    public String[] getUnlocalizedNames()
    {
        return this.unlocalizedNames;
    }

    public String[] getTooltipNames()
    {
        return this.tooltipNames;
    }
}
