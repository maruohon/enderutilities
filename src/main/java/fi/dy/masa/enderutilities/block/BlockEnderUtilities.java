package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

	// Called whenever the block is added into the world. Args: world, x, y, z
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		//super.onBlockAdded(world, x, y, z);
		//this.func_149930_e(world, x, y, z);
		this.onNeighborBlockChange(world, x, y, z, this);
	}

	// Gets an item for the block being called on. Args: world, x, y, z
	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return Item.getItemFromBlock(this);
	}
}
