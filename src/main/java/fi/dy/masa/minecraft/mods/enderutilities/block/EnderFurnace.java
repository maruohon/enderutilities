package fi.dy.masa.minecraft.mods.enderutilities.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.minecraft.mods.enderutilities.EnderUtilities;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;
import fi.dy.masa.minecraft.mods.enderutilities.tileentity.TileEntityEnderFurnace;

public class EnderFurnace extends BlockContainer
{
	private final Random random = new Random();
	private static boolean field_149934_M;
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;

	public EnderFurnace()
	{
		super(Material.rock);
		this.setHardness(10.0f);
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypePiston);
		this.setBlockName(Reference.NAME_TILE_ENDER_FURNACE);
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	public Item getItemDropped(int p1, Random r, int p3)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}

	// Returns a new instance of a block's tile entity class. Called on placing the block.
	public TileEntity createNewTileEntity(World world, int i)
	{
		return new TileEntityEnderFurnace();
	}

	// Called when the block is placed in the world.
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		int rot = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		if (rot == 0) { world.setBlockMetadataWithNotify(x, y, z, 2, 2); }
		if (rot == 1) { world.setBlockMetadataWithNotify(x, y, z, 5, 2); }
		if (rot == 2) { world.setBlockMetadataWithNotify(x, y, z, 3, 2); }
		if (rot == 3) { world.setBlockMetadataWithNotify(x, y, z, 4, 2); }

		if (stack.hasDisplayName())
		{
			// FIXME add custom TileEntity
			((TileEntityEnderFurnace)world.getTileEntity(x, y, z)).func_145951_a(stack.getDisplayName());
		}
	}

	// Called whenever the block is added into the world. Args: world, x, y, z
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		//this.func_149930_e(world, x, y, z);
	}

/*
	private void func_149930_e(World p_149930_1_, int p_149930_2_, int p_149930_3_, int p_149930_4_)
	{
		if (!p_149930_1_.isRemote)
		{
			Block block = p_149930_1_.getBlock(p_149930_2_, p_149930_3_, p_149930_4_ - 1);
			Block block1 = p_149930_1_.getBlock(p_149930_2_, p_149930_3_, p_149930_4_ + 1);
			Block block2 = p_149930_1_.getBlock(p_149930_2_ - 1, p_149930_3_, p_149930_4_);
			Block block3 = p_149930_1_.getBlock(p_149930_2_ + 1, p_149930_3_, p_149930_4_);
			byte b0 = 3;
	
			if (block.func_149730_j() && !block1.func_149730_j())
			{
				b0 = 3;
			}
	
			if (block1.func_149730_j() && !block.func_149730_j())
			{
				b0 = 2;
			}
	
			if (block2.func_149730_j() && !block3.func_149730_j())
			{
				b0 = 5;
			}
	
			if (block3.func_149730_j() && !block2.func_149730_j())
			{
				b0 = 4;
			}
	
			p_149930_1_.setBlockMetadataWithNotify(p_149930_2_, p_149930_3_, p_149930_4_, b0, 2);
		}
	}
*/
	// Called upon block activation (right click on the block.)
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote == true)
		{
			return true;
		}
		else
		{
			// FIXME debug
			//System.out.printf("x: %d y: %d z: %d hitX: %f hitY: %f hitZ: %f\n", x, y, z, hitX, hitY, hitZ);
			TileEntityEnderFurnace te = (TileEntityEnderFurnace)world.getTileEntity(x, y, z);

			if (te != null)
			{
/*
				if (x >= 1260)
				{
					ItemStack stack;
					int size = te.getSizeInventory();
					for (int j = 0; j < size; j++)
					{
						System.out.printf("activated: x: %d y: %d z: %d size: %d j: %d\n", x, y, z, size, j);
						stack = te.getStackInSlot(j);
						if (stack != null)
						{
							System.out.println("onBlockActivated(): stack not null: " + j);
						}
					}
					World wo = te.getWorldObj();
					if (wo != null && wo.isRemote == false)
					{
						System.out.println("marked");
						wo.markBlockForUpdate(x, y, z);
						int meta = wo.getBlockMetadata(x, y, z);
						//wo.notifyBlockChange(x, y, z, wo.getBlock(x, y, z));
						//wo.setBlockMetadataWithNotify(x, y, z, (++meta & 0x7), 2);
						//wo.setBlockMetadataWithNotify(x, y, z, meta, 2);
					}
				}
*/
				//TileEntity tev = world.getTileEntity(x, y, z);
				//player.func_146101_a((TileEntityFurnace)tev);
/*
				this.getNextWindowId();
		        this.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(this.currentWindowId, 2, p_146101_1_.getInventoryName(), p_146101_1_.getSizeInventory(), p_146101_1_.hasCustomInventoryName()));
		        this.openContainer = new ContainerFurnace(this.inventory, p_146101_1_);
		        this.openContainer.windowId = this.currentWindowId;
		        this.openContainer.addCraftingToCrafters(this);
*/
		        //if (te instanceof TileEntityEnderFurnace && te.getContainer(player.inventory) != null)
				//player.openGui(EnderUtilities.instance, 0, world, x, y, z);
			}

			return true;
		}
	}
/*
	// Update which block the furnace is using depending on whether or not it is burning
	public static void updateFurnaceBlockState(boolean p_149931_0_, World p_149931_1_, int p_149931_2_, int p_149931_3_, int p_149931_4_)
	{
		int l = p_149931_1_.getBlockMetadata(p_149931_2_, p_149931_3_, p_149931_4_);
		TileEntity tileentity = p_149931_1_.getTileEntity(p_149931_2_, p_149931_3_, p_149931_4_);
		field_149934_M = true;

		if (p_149931_0_)
		{
			p_149931_1_.setBlock(p_149931_2_, p_149931_3_, p_149931_4_, Blocks.lit_furnace);
		}
		else
		{
			p_149931_1_.setBlock(p_149931_2_, p_149931_3_, p_149931_4_, Blocks.furnace);
		}

		field_149934_M = false;
		p_149931_1_.setBlockMetadataWithNotify(p_149931_2_, p_149931_3_, p_149931_4_, l, 2);
	
		if (tileentity != null)
		{
			tileentity.validate();
			p_149931_1_.setTileEntity(p_149931_2_, p_149931_3_, p_149931_4_, tileentity);
		}
	}
*/
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		if (!field_149934_M)
		{
			TileEntityEnderFurnace te = (TileEntityEnderFurnace)world.getTileEntity(x, y, z);
	
			if (te != null)
			{
				for (int i1 = 0; i1 < te.getSizeInventory(); ++i1)
				{
					ItemStack itemstack = te.getStackInSlot(i1);
	
					if (itemstack != null)
					{
						float f = this.random.nextFloat() * 0.8F + 0.1F;
						float f1 = this.random.nextFloat() * 0.8F + 0.1F;
						float f2 = this.random.nextFloat() * 0.8F + 0.1F;
	
						while (itemstack.stackSize > 0)
						{
							int j1 = this.random.nextInt(21) + 10;
	
							if (j1 > itemstack.stackSize)
							{
								j1 = itemstack.stackSize;
							}
	
							itemstack.stackSize -= j1;
							EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
	
							if (itemstack.hasTagCompound())
							{
								entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (double)((float)this.random.nextGaussian() * f3);
							entityitem.motionY = (double)((float)this.random.nextGaussian() * f3 + 0.2F);
							entityitem.motionZ = (double)((float)this.random.nextGaussian() * f3);
							world.spawnEntityInWorld(entityitem);
						}
					}
				}
	
				world.func_147453_f(x, y, z, block);
			}
		}
	
		super.breakBlock(world, x, y, z, block, meta);
	}

	// If this returns true, then comparators facing away from this block will use the value from
	// getComparatorInputOverride instead of the actual redstone signal strength.
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	// If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
	// strength when this block inputs to a comparator.
	public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
	{
		return Container.calcRedstoneFromInventory((IInventory)world.getTileEntity(x, y, z));
	}
/*
	// A randomly called display update to be able to add particles or other items for display
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_)
	{
		if (this.what)
		{
			int l = p_149734_1_.getBlockMetadata(p_149734_2_, p_149734_3_, p_149734_4_);
			float f = (float)p_149734_2_ + 0.5F;
			float f1 = (float)p_149734_3_ + 0.0F + p_149734_5_.nextFloat() * 6.0F / 16.0F;
			float f2 = (float)p_149734_4_ + 0.5F;
			float f3 = 0.52F;
			float f4 = p_149734_5_.nextFloat() * 0.6F - 0.3F;
	
			if (l == 4)
			{
				p_149734_1_.spawnParticle("smoke", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				p_149734_1_.spawnParticle("flame", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 5)
			{
				p_149734_1_.spawnParticle("smoke", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
				p_149734_1_.spawnParticle("flame", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 2)
			{
				p_149734_1_.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
				p_149734_1_.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0D, 0.0D, 0.0D);
			}
			else if (l == 3)
			{
				p_149734_1_.spawnParticle("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
				p_149734_1_.spawnParticle("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0D, 0.0D, 0.0D);
			}
		}
	}
*/
	// Gets an item for the block being called on. Args: world, x, y, z
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}
/*
	// Gets the block's texture. Args: side, meta
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (side == 0 || side == 1) { return this.iconTop; }
		if (side != meta) { return this.blockIcon; }
		return this.iconFront;
	}
*/
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
		//System.out.println("\nstart");
		if (side == 0 || side == 1)
		{
			//System.out.println("top");
			return this.iconTop;
		}
		if (side != blockAccess.getBlockMetadata(x, y, z))
		{
			//System.out.println("side");
			return this.blockIcon;
		}
/*
		//System.out.println("front");
		if (x >= 1260)
		{
			TileEntityEnderFurnace te = (TileEntityEnderFurnace)blockAccess.getTileEntity(x, y, z);
			if (te != null)
			{
				ItemStack stack;
				int size = te.getSizeInventory();
				for (int i = 0; i < size; i++)
				{
					System.out.printf("getIcon(): x: %d y: %d z: %d side: %d size: %d i:%d\n", x, y, z, side, size, i);
					stack = te.getStackInSlot(i);
					if (stack != null)
					{
						System.out.println("getIcon(): stack not null: " + i);
					}
				}
				if (te.getStackInSlot(2) != null){ return this.iconTop; }
				else { return this.iconFront; }
			}
		}
*/
		return this.iconFront;
    }

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(Reference.getTileName(Reference.NAME_ITEM_ENDER_FURNACE) + ".side");
		this.iconTop = iconRegister.registerIcon(Reference.getTileName(Reference.NAME_ITEM_ENDER_FURNACE) + ".top");
		// FIXME how can we do the front texture based on state? Needs TESR?
		this.iconFront = iconRegister.registerIcon(Reference.getTileName(Reference.NAME_ITEM_ENDER_FURNACE) + ".front.off");
	}
}
