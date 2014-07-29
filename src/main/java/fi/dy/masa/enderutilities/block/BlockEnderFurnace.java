package fi.dy.masa.enderutilities.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.gui.GuiIds;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.reference.tileentity.ReferenceTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class BlockEnderFurnace extends BlockContainer
{
	private final Random random = new Random();
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;

	public BlockEnderFurnace()
	{
		super(Material.rock);
		this.setHardness(10.0f);
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypePiston);
		this.setBlockName(ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public Item getItemDropped(int p1, Random r, int p3)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}

	// Returns a new instance of a block's tile entity class. Called on placing the block.
	@Override
	public TileEntity createNewTileEntity(World world, int i)
	{
		return new TileEntityEnderFurnace();
	}

	// Called when the block is placed in the world.
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		int rot = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		if (rot == 0) { world.setBlockMetadataWithNotify(x, y, z, 2, 2); }
		if (rot == 1) { world.setBlockMetadataWithNotify(x, y, z, 5, 2); }
		if (rot == 2) { world.setBlockMetadataWithNotify(x, y, z, 3, 2); }
		if (rot == 3) { world.setBlockMetadataWithNotify(x, y, z, 4, 2); }

		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			if (entity instanceof EntityPlayer)
			{
				((TileEntityEnderFurnace)te).setOwner((EntityPlayer)entity);
			}

			if (stack.hasDisplayName())
			{
				((TileEntityEnderFurnace)te).setInventoryName(stack.getDisplayName());
			}
		}
	}

	// Called whenever the block is added into the world. Args: world, x, y, z
	@Override
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
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		PlayerInteractEvent e = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, x, y, z, side, world);
		if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
		{
			return false;
		}

		// FIXME debug
		//System.out.printf("x: %d y: %d z: %d hitX: %f hitY: %f hitZ: %f\n", x, y, z, hitX, hitY, hitZ);
		TileEntity te = (TileEntityEnderFurnace)world.getTileEntity(x, y, z);
		if (te == null || te instanceof TileEntityEnderFurnace == false)
		{
			return false;
		}

		if (world.isRemote == false)
		{
			player.openGui(EnderUtilities.instance, GuiIds.GUI_ID_ENDER_FURNACE, world, x, y, z);
		}

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

		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntity t = world.getTileEntity(x, y, z);

		if (t != null && t instanceof TileEntityEnderFurnace)
		{
			TileEntityEnderFurnace te = (TileEntityEnderFurnace)t;

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
	
		super.breakBlock(world, x, y, z, block, meta);
	}

	// If this returns true, then comparators facing away from this block will use the value from
	// getComparatorInputOverride instead of the actual redstone signal strength.
	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	// If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
	// strength when this block inputs to a comparator.
	@Override
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
	@Override
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
	@Override
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

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(Textures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".side");
		this.iconTop = iconRegister.registerIcon(Textures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".top");
		// FIXME how can we do the front texture based on state? Needs TESR?
		this.iconFront = iconRegister.registerIcon(Textures.getTileName(ReferenceItem.NAME_ITEM_ENDER_FURNACE) + ".front.off");
	}
}
