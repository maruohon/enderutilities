package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.Event.Result;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities implements ITileEntityProvider
{
	public BlockEnderUtilitiesTileEntity(float hardness)
	{
		super(hardness);
	}

	public BlockEnderUtilitiesTileEntity(float hardness, Material material)
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
		if (te == null || te instanceof TileEntityEnderUtilities == false)
		{
			return;
		}

		TileEntityEnderUtilities teeu = (TileEntityEnderUtilities)te;
		if (stack.getTagCompound() != null)
		{
			te.readFromNBT(stack.getTagCompound());
		}
		else
		{
			int rot = MathHelper.floor_double((double)(livingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			/*
			if (livingBase.rotationPitch > 45.0f)
			{
				rot = (rot << 4) | 1;
			}
			else if (livingBase.rotationPitch < -45.0f)
			{
				rot = rot << 4;
			}
			else
			{
			*/
				// {DOWN, UP, NORTH, SOUTH, WEST, EAST}
				switch(rot)
				{
					case 0: rot = 2; break;
					case 1: rot = 5; break;
					case 2: rot = 3; break;
					case 3: rot = 4; break;
					default:
				}
			//}

			teeu.setRotation((byte)rot);

			if (livingBase instanceof EntityPlayer)
			{
				teeu.setOwner((EntityPlayer)livingBase);
			}

			if (teeu instanceof TileEntityEnderUtilitiesInventory && stack.hasDisplayName())
			{
				((TileEntityEnderUtilitiesInventory)teeu).setInventoryName(stack.getDisplayName());
			}
		}
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
		if (te == null || te instanceof TileEntityEnderUtilities == false)
		{
			return false;
		}

		if (world.isRemote == false)
		{
			player.openGui(EnderUtilities.instance, 0, world, x, y, z);
		}

		return true;
	}
}
