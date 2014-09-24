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
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class BlockEnderFurnace extends BlockContainer
{
	private final Random random = new Random();
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
		super(Material.rock);
		this.setHardness(10.0f);
		this.setHarvestLevel("pickaxe", 2);
		this.setStepSound(soundTypePiston);
		this.setBlockName(ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}
/*
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}
*/
	@Override
	public int damageDropped(int meta)
	{
		return meta;
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
		switch(rot)
		{
			case 0: rot = 2; break;
			case 1: rot = 5; break;
			case 2: rot = 3; break;
			case 3: rot = 4; break;
			default:
		}

		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			((TileEntityEnderFurnace)te).setRotation((byte)rot);

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

	// Called upon block activation (right click on the block.)
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		PlayerInteractEvent e = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, x, y, z, side, world);
		if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
		{
			return false;
		}

		TileEntity te = (TileEntityEnderFurnace)world.getTileEntity(x, y, z);
		if (te == null || te instanceof TileEntityEnderFurnace == false)
		{
			return false;
		}

		if (world.isRemote == false)
		{
			player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_ENDER_FURNACE, world, x, y, z);
		}

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

			// Drop the items from the output buffer
			if (te.getOutputBufferAmount() > 0 && te.getOutputBufferStack() != null)
			{
				int amount = te.getOutputBufferAmount();
				ItemStack stack = te.getOutputBufferStack();
				float f = this.random.nextFloat() * 0.8F + 0.1F;
				float f1 = this.random.nextFloat() * 0.8F + 0.1F;
				float f2 = this.random.nextFloat() * 0.8F + 0.1F;
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
					entityitem.motionX = (double)((float)this.random.nextGaussian() * f3);
					entityitem.motionY = (double)((float)this.random.nextGaussian() * f3 + 0.2F);
					entityitem.motionZ = (double)((float)this.random.nextGaussian() * f3);
					world.spawnEntityInWorld(entityitem);
					amount -= num;
				}
			}

			//world.func_147453_f(x, y, z, block); // this gets called in World.removeTileEntity(), via super.breakBlock() below
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

	// A randomly called display update to be able to add particles or other items for display
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			if (((TileEntityEnderFurnace)te).isActive == true)
			{
				Particles.spawnParticlesAround(world, "portal", x, y, z, 2, this.random);
			}
		}
	}

	// Gets an item for the block being called on. Args: world, x, y, z
	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z)
	{
		return Item.getItemFromBlock(EnderUtilitiesBlocks.enderFurnace);
	}

	@Override
	@SideOnly(Side.CLIENT)
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

	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
		if (side == 0 || side == 1)
		{
			return this.iconTop;
		}

		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderFurnace)
		{
			int rotation = ((TileEntityEnderFurnace)te).getRotation();

			if (side == rotation)
			{
				if (((TileEntityEnderFurnace)te).isActive == true)
				{
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
				return this.iconFrontOff;
			}
		}

		return this.blockIcon;
    }

	@Override
	@SideOnly(Side.CLIENT)
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
