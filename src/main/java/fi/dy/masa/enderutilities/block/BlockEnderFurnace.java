package fi.dy.masa.enderutilities.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.reference.ReferenceTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class BlockEnderFurnace extends BlockEnderUtilitiesTE
{
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconFrontOff;
	@SideOnly(Side.CLIENT)
	private IIcon iconFrontOnSlow;
	@SideOnly(Side.CLIENT)
	private IIcon iconFrontOnFast;
	@SideOnly(Side.CLIENT)
	private IIcon iconFrontOnNofuel;

	public BlockEnderFurnace()
	{
		super(6.0f, Material.rock);
		this.setHarvestLevel("pickaxe", 2);
		this.setBlockName(ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
	}
/*
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}
*/

	// Returns a new instance of a block's tile entity class. Called on placing the block.
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		if (meta == 0) { return new TileEntityEnderFurnace(); }
		if (meta == 1) { return new TileEntityToolWorkstation(); }
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntity te = world.getTileEntity(x, y, z);

		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;

			// Drop the items from the output buffer
			if (teef.getOutputBufferAmount() > 0 && teef.getOutputBufferStack() != null)
			{
				int amount = teef.getOutputBufferAmount();
				ItemStack stack = teef.getOutputBufferStack();
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
				double xd = x + f;
				double yd = y + f1;
				double zd = z + f2;
				int num;
				int max = stack.getMaxStackSize();

				while (amount > 0)
				{
					num = Math.min(amount, max);
					EntityItem entityitem = new EntityItem(world, xd, yd, zd, new ItemStack(stack.getItem(), num, stack.getItemDamage()));
					if (stack.hasTagCompound() == true)
					{
						entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
					}
					float f3 = 0.05F;
					entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
					entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
					entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);
					world.spawnEntityInWorld(entityitem);
					amount -= num;
				}
			}
		}
	
		super.breakBlock(world, x, y, z, block, meta);
	}

	// A randomly called display update to be able to add particles or other items for display
	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			if (((TileEntityEnderFurnace)te).isActive == true)
			{
				Particles.spawnParticlesAround(world, "portal", x, y, z, 2, rand);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
    public IIcon getIcon(int side, int meta)
    {
		// These are for the rendering in ItemBlock form in inventories etc.
		if (side == 0 || side == 1)
		{
			return this.iconTop;
		}
		if (side == 3)
		{
			return this.iconFrontOff;
		}

		return this.blockIcon;
    }

	@SideOnly(Side.CLIENT)
	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
		if (side == 0 || side == 1)
		{
			return this.iconTop;
		}

		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace && side == ((TileEntityEnderFurnace)te).getRotation())
		{
			if (((TileEntityEnderFurnace)te).isActive == false)
			{
				return this.iconFrontOff;
			}

			if (((TileEntityEnderFurnace)te).usingFuel == true)
			{
				if (((TileEntityEnderFurnace)te).operatingMode == 1)
				{
					return this.iconFrontOnFast;
				}
				return this.iconFrontOnSlow;
			}
			return this.iconFrontOnNofuel;
		}

		return this.blockIcon;
    }

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".side");
		this.iconTop = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".top");
		this.iconFrontOff = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".front.off");
		this.iconFrontOnSlow = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".front.on.slow");
		this.iconFrontOnFast = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".front.on.fast");
		this.iconFrontOnNofuel = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".front.on.nofuel");
	}
}
