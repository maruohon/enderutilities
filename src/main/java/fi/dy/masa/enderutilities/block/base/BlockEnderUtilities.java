package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;

public class BlockEnderUtilities extends Block
{
    public static final EnumFacing DEFAULT_FACING = EnumFacing.NORTH;
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    public static final PropertyDirection FACING_H = BlockHorizontal.FACING;

    protected String blockName;
    protected String[] unlocalizedNames;
    protected String[] tooltipNames;
    protected boolean enabled = true;
    public PropertyDirection propFacing = FACING_H;

    public BlockEnderUtilities(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(material);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setHarvestLevel("pickaxe", harvestLevel);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setSoundType(SoundType.STONE);
        this.blockName = name;
        this.unlocalizedNames = this.generateUnlocalizedNames();
        this.tooltipNames = this.generateTooltipNames();
    }

    public String getBlockName()
    {
        return this.blockName;
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

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public BlockEnderUtilities setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public ItemBlock createItemBlock()
    {
        return new ItemBlockEnderUtilities(this);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        IBlockState state = world.getBlockState(pos).withRotation(Rotation.CLOCKWISE_90);
        world.setBlockState(pos, state, 3);
        return true;
    }
}
