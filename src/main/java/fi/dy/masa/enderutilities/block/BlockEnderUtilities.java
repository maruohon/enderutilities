package fi.dy.masa.enderutilities.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockEnderUtilities extends Block
{
	protected int blockIndex;

	public BlockEnderUtilities(int index, String name, float hardness)
	{
		this(index, name, hardness, Material.iron);
		this.setStepSound(soundTypeMetal);
	}

	public BlockEnderUtilities(int index, String name, float hardness, Material material)
	{
		super(material);
		this.setHardness(hardness);
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setHarvestLevel("pickaxe", 1); // Requires a Stone Pickaxe or better
		this.setBlockName(name);
		this.blockIndex = index;
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

	public int getBlockIndex()
	{
		return this.blockIndex;
	}

	// A randomly called display update to be able to add particles or other items for display
	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z)).randomDisplayTick(world, x, y, z, rand);
	}

	@SideOnly(Side.CLIENT)
	@Override
    public IIcon getIcon(int side, int meta)
    {
		return Machine.getMachine(this.blockIndex, meta).getIcon(side);
    }

	@SideOnly(Side.CLIENT)
	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderUtilities)
		{
			return Machine.getMachine(this.blockIndex, blockAccess.getBlockMetadata(x, y, z)).getIcon((TileEntityEnderUtilities)te, side);
		}

		return this.getIcon(side, blockAccess.getBlockMetadata(x, y, z));
    }

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		Machine.registerIcons(this.blockIndex, iconRegister);
	}
}
