package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderBucket extends Item
{
	public ItemEnderBucket()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BUCKET);
		this.setTextureName(Reference.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return stack;
		}

		// FIXME the boolean flag does what exactly? In vanilla it seems to indicate that the bucket is empty.
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

		if (movingobjectposition == null || movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
		{
			return stack;
		}

		int x = movingobjectposition.blockX;
		int y = movingobjectposition.blockY;
		int z = movingobjectposition.blockZ;

		// Spawn safe zone checks etc.
		if (world.canMineBlock(player, x, y, x) == false)
		{
			return stack;
		}

		short nbtAmount = 0;
		Block nbtBlock = null;
		String nbtBlockName = "";

		Block targetBlock;
		String targetBlockName;
		Material targetMaterial;

		targetBlock = world.getBlock(x, y, z);
		if (targetBlock == null)
		{
			return stack;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt != null)
		{
			nbtBlockName = nbt.getString("fluid");
			nbtAmount = nbt.getShort("amount");

			if (nbtBlockName.length() > 0)
			{
				nbtBlock = Block.getBlockFromName(nbtBlockName);
			}
		}
		else
		{
			nbt = new NBTTagCompound();
		}

		targetMaterial = targetBlock.getMaterial();
		targetBlockName = Block.blockRegistry.getNameForObject(targetBlock);

		// Empty bucket, or same fluid and not sneaking: try to pick up fluid (sneaking allows emptying a bucket into the same fluid)
		// FIXME is this a sufficient block type check?
		if (nbtAmount == 0 || (targetBlock == nbtBlock && player.isSneaking() == false))
		{
			// Bail out if we don't have space or we can't change the block
			if ((ReferenceItem.ENDER_BUCKET_MAX_AMOUNT - nbtAmount) < 1000 || player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, stack) == false)
			{
				return stack;
			}

			if (targetMaterial.isLiquid() == true)
			{
				if (world.setBlockToAir(x, y, z) == true)
				{
					nbtAmount += 1000;
					nbt.setShort("amount", nbtAmount);

					if (nbtBlockName.length() == 0)
					{
						nbt.setString("fluid", targetBlockName);
					}

					stack.setTagCompound(nbt);
				}
			}

			return stack;
		}

		// Different fluid, or other block type, we try to place a fluid block in the world

		// No fluid stored, or we can't place fluid here
		if (nbtAmount < 1000 || player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, stack) == false)
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

		// We need to convert water and lava to the flowing variant, otherwise we get non-flowing source blocks
		if (nbtBlock == Blocks.water) { nbtBlock = Blocks.flowing_water; }
		else if (nbtBlock == Blocks.lava) { nbtBlock = Blocks.flowing_lava; }

		if (this.tryPlaceContainedFluid(world, x, y, z, nbtBlock) == true)
		{
			nbtAmount -= 1000;
			nbt.setShort("amount", nbtAmount);
			if (nbtAmount == 0)
			{
				stack.setTagCompound(null);
			}
		}

		return stack;
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

	// Attempts to place the fluid contained inside the bucket.
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
				// Set a replaceable block to air, and drop the items
				world.func_147480_a(x, y, z, true);
			}

			world.setBlock(x, y, z, fluid, 0, 3);
			//world.notifyBlockChange(x, y, z, fluid); // FIXME this doesn't work
		}

		return true;
	}
}
