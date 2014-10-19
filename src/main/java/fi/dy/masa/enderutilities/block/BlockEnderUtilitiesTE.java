package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTE extends BlockEnderUtilities implements ITileEntityProvider
{
	public BlockEnderUtilitiesTE(float hardness)
	{
		super(hardness);
	}

	public BlockEnderUtilitiesTE(float hardness, Material material)
	{
		super(hardness, material);
	}

	// Returns a new instance of a block's tile entity class. Called on placing the block.
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}

	/*
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	{
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta)
	{
	}
*/

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te == null || te instanceof TileEntityEnderUtilitiesInventory == false)
		{
			return;
		}

		TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;
		if (stack.getTagCompound() != null)
		{
			te.readFromNBT(stack.getTagCompound());
		}
		else
		{
			int rot = MathHelper.floor_double((double)(livingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			switch(rot)
			{
				case 0: rot = 2; break;
				case 1: rot = 5; break;
				case 2: rot = 3; break;
				case 3: rot = 4; break;
				default:
			}

			teeui.setRotation((byte)rot);

			if (livingBase instanceof EntityPlayer)
			{
				teeui.setOwner((EntityPlayer)livingBase);
			}

			if (stack.hasDisplayName())
			{
				teeui.setInventoryName(stack.getDisplayName());
			}
		}
	}

	// Called whenever the block is added into the world. Args: world, x, y, z
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		//super.onBlockAdded(world, x, y, z);
		//this.func_149930_e(world, x, y, z);
		this.onNeighborBlockChange(world, x, y, z, this);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		super.onNeighborBlockChange(world, x, y, z, block);
		/*
		if (world.isRemote == true)
		{
			return;
		}

		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null && te instanceof TileEntityEnderUtilities)
		{
		}
		*/
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

		TileEntity te = world.getTileEntity(x, y, z);
		if (te == null || te instanceof TileEntityEnderUtilitiesInventory == false)
		{
			return false;
		}

		if (world.isRemote == false)
		{
			player.openGui(EnderUtilities.instance, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntity te = world.getTileEntity(x, y, z);

		if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
		{
			TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

			for (int i1 = 0; i1 < teeui.getSizeInventory(); ++i1)
			{
				ItemStack itemstack = teeui.getStackInSlot(i1);

				if (itemstack != null)
				{
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

					while (itemstack.stackSize > 0)
					{
						int j1 = world.rand.nextInt(21) + 10;

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
						entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
						entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);
						world.spawnEntityInWorld(entityitem);
					}
				}
			}

			//world.func_147453_f(x, y, z, block); // this gets called in World.removeTileEntity()
		}
	
		super.breakBlock(world, x, y, z, block, meta);
		world.removeTileEntity(x, y, z);
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
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IInventory == false)
		{
			return 0;
		}

		return Container.calcRedstoneFromInventory((IInventory)te);
	}

	// Gets an item for the block being called on. Args: world, x, y, z
	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return Item.getItemFromBlock(this);
	}
}
