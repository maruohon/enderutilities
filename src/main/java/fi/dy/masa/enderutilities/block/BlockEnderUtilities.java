package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;

public class BlockEnderUtilities extends Block
{
	protected BlockEnderUtilities(float hardness)
	{
		this(hardness, Material.iron);
		this.setStepSound(soundTypeMetal);
	}

	protected BlockEnderUtilities(float hardness, Material material)
	{
		super(material);
		this.setHardness(hardness);
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setHarvestLevel("pickaxe", 1); // Requires a Stone Pickaxe or better
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}
}
