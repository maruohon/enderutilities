package fi.dy.masa.minecraft.mods.enderutilities.items;

import java.util.List;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderBucket extends Item
{
	private static final short MAX_AMOUNT = 16000; // Can contain 16 buckets

	public EnderBucket()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_BUCKET);
		this.setTextureName(Reference.MOD_ID + ":" + this.getUnlocalizedName()); // FIXME?
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		String nbtFluid = "";
		short nbtAmount = 0;

		Block nbtFluidBlock;
		Material nbtFluidMaterial = null;

		String targetFluidName;
		Block targetBlock;
		Material targetMaterial;

		// FIXME the boolean flag does what exactly? In vanilla it seems to indicate that the bucket is empty.
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

		if (movingobjectposition == null)
		{
			System.out.println("null"); // FIXME debug
			return stack;
		}

        NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			nbtFluid = nbt.getString("fluid");
			nbtAmount = nbt.getShort("amount");
		}
		else
		{
			nbt = new NBTTagCompound();
		}

		if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			int x = movingobjectposition.blockX;
			int y = movingobjectposition.blockY;
			int z = movingobjectposition.blockZ;

			// Spawn safe zone checks etc.
			if (world.canMineBlock(player, x, y, x) == false)
			{
				return stack;
			}

			targetBlock = world.getBlock(x, y, z);
			targetMaterial = targetBlock.getMaterial();
			int meta = world.getBlockMetadata(x, y, z);
			//targetFluidName = targetBlock.getUnlocalizedName();
			targetFluidName = Block.blockRegistry.getNameForObject(targetBlock);

			if (nbtFluid.length() > 0)
			{
				if (Block.getBlockFromName(nbtFluid) != null)
				{
					nbtFluidMaterial = Block.getBlockFromName(nbtFluid).getMaterial();
				}
			}

			// Same fluid, or empty bucket
			//if (targetFluidName.equals(nbtFluid) == true || nbtAmount == 0) // FIXME needs a proper block type check?
			if (nbtAmount == 0 || targetMaterial.equals(nbtFluidMaterial) == true) // FIXME needs a proper block type check?
			{
				// Do we have space, and can we change the fluid block?
				if ((MAX_AMOUNT - nbtAmount) < 1000 || player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, stack) == false)
				{
					System.out.println("no space or can't edit"); // FIXME debug
					return stack;
				}

				// FIXME: recognize fluids properly
				if ((targetMaterial == Material.water && meta == 0) || (targetMaterial == Material.lava && meta == 0))
				{
					if (world.setBlockToAir(x, y, z) == true)
					{
						nbtAmount += 1000;
						nbt.setShort("amount", nbtAmount);
						if (nbtFluid.length() == 0)
						{
							nbt.setString("fluid", targetFluidName);
						}
						stack.setTagCompound(nbt);
					}
				}
				else
				{
					System.out.println("failed material check"); // FIXME debug
				}
				return stack;
			}
			else
			{
				System.out.println("targetFluidName: " + targetFluidName + " nbtFluid: " + nbtFluid); // FIXME Debug
			}

			// Different fluid, or other block type, we try to place a fluid block in the world

			// No fluid stored
			if (nbtAmount < 1000)
			{
				return stack;
			}

			// Don't adjust the target block for liquids, we want to replace them
			if (targetBlock.getMaterial().isLiquid() == false)
			{
				// Adjust the target block position
				if (movingobjectposition.sideHit == 0) { --y; }
				if (movingobjectposition.sideHit == 1) { ++y; }
				if (movingobjectposition.sideHit == 2) { --z; }
				if (movingobjectposition.sideHit == 3) { ++z; }
				if (movingobjectposition.sideHit == 4) { --x; }
				if (movingobjectposition.sideHit == 5) { ++x; }
			}

			// Can we place a fluid block here?
			if (player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, stack) == false)
			{
				return stack;
			}

			nbtFluidBlock = Block.getBlockFromName(nbtFluid);
			if (nbtFluidBlock == null)
			{
				FMLLog.warning("Fluid block was null");
				return stack;
			}

			if (this.tryPlaceContainedFluid(world, x, y, z, nbtFluidBlock) == true)
			{
				nbtAmount -= 1000;
				nbt.setShort("amount", nbtAmount);
				if (nbtAmount == 0)
				{
					nbt.setString("fluid", "");
				}
				stack.setTagCompound(nbt);
			}
		}

		return stack;
	}

	/**
	* Attempts to place the fluid contained inside the bucket.
	*/
	public boolean tryPlaceContainedFluid(World world, int x, int y, int z, Block fluid)
	{
		Material material = world.getBlock(x, y, z).getMaterial();

		if (world.isAirBlock(x, y, z) == false && material.isSolid() == true)
		{
			return false;
		}

		if (world.provider.isHellWorld && fluid == Blocks.flowing_water)
		{
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; ++l)
			{
				world.spawnParticle("largesmoke", (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
			}
		}
		else
		{
			if (world.isRemote == false && material.isSolid() == false && material.isLiquid() == false)
			{
				world.func_147480_a(x, y, z, true);
			}

			world.setBlock(x, y, z, fluid, 0, 3);
			//world.notifyBlockChange(x, y, z, fluid); // FIXME this doesn't work
		}

		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return false;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		String fluid = "<empty>";
		short amount = 0;
		String pre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			amount	= nbt.getShort("amount");

			if (nbt.hasKey("fluid") == true && amount > 0)
			{
				String name = Block.getBlockFromName(nbt.getString("fluid")).getLocalizedName();
				fluid = pre + name + rst;
			}
		}

		list.add("Fluid: " + fluid);
		list.add(String.format("Amount: %d mB", amount));
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}
}
