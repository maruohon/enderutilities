package fi.dy.masa.minecraft.mods.enderutilities.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import fi.dy.masa.minecraft.mods.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;
import fi.dy.masa.minecraft.mods.enderutilities.util.TeleportEntity;

public class EnderLasso extends Item
{
	public EnderLasso()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Reference.NAME_ITEM_ENDER_LASSO);
		this.setTextureName(Reference.MOD_ID + ":" + this.getUnlocalizedName());
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}

/*
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		System.out.println("onItemRightClick");
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
		if (movingobjectposition == null)
		{
			System.out.println("onItemRightClick: mop: null");
			return stack;
		}

		System.out.println("onItemRightClick: mop: " + movingobjectposition.toString());
		if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
		{
			System.out.println("onItemRightClick: hit entity");
			System.out.println(movingobjectposition.entityHit.toString()); // FIXME debug
		}
		return stack;
	}
*/

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return false;
		}

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		if (player.isSneaking() == true)
		{
			// Sneaking and targeting a block: store the location
			if (Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				String strSide = "top";

				// Adjust the target block position
				if (side == 0) { --y; strSide = "bottom"; }
				if (side == 1) { ++y; }
				if (side == 2) { --z; strSide = "east"; }
				if (side == 3) { ++z; strSide = "west"; }
				if (side == 4) { --x; strSide = "north"; }
				if (side == 5) { ++x; strSide = "south"; }

				System.out.printf("x: %d,  y: %d, z: %d side: %d strSide: %s\n", x, y, z, side, strSide); // FIXME debug
				nbt.setInteger("dim", player.dimension);
				nbt.setInteger("x", x);
				nbt.setInteger("y", y);
				nbt.setInteger("z", z);
				nbt.setString("side", strSide);
				stack.setTagCompound(nbt);
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null)
		{
			list.add("No target set");
			return;
		}

		String side	= nbt.getString("side");
		int dim		= nbt.getInteger("dim");
		int x		= nbt.getInteger("x");
		int y		= nbt.getInteger("y");
		int z		= nbt.getInteger("z");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		list.add(String.format("dim: %s%d%s x: %s%d%s, y: %s%d%s, z: %s%d%s",
					dimPre, dim, rst, coordPre, x, rst, coordPre, y, rst, coordPre, z, rst));
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	public void teleportEntity(ItemStack stack, EntityLiving entity, int dim)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || ! nbt.hasKey("x") || ! nbt.hasKey("y") || ! nbt.hasKey("z") || ! nbt.hasKey("dim")
				|| entity.riddenByEntity != null || entity.ridingEntity != null)
		{
			return;
		}
		double x = (double)nbt.getInteger("x");
		double y = (double)nbt.getInteger("y");
		double z = (double)nbt.getInteger("z");
		int targetDim = nbt.getInteger("dim");

		double entX = entity.posX;
		double entY = entity.posY;
		double entZ = entity.posZ;

		World world = entity.worldObj;
		world.playSoundEffect(entX, entY, entZ, "mob.endermen.portal", 0.5F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);

		// Spawn some particles
		for (int i = 0; i < 20; i++)
		{
			double offX = (Math.random() - 0.5d) * 1.0d;
			double offY = (Math.random() - 0.5d) * 1.0d;
			double offZ = (Math.random() - 0.5d) * 1.0d;

			double velX = (Math.random() - 0.5d) * 1.0d;
			double velY = (Math.random() - 0.5d) * 1.0d;
			double velZ = (Math.random() - 0.5d) * 1.0d;
			world.spawnParticle("portal", entX + offX, entY + offY, entZ + offZ, velX, velY, velZ);
		}

		// TODO: Stop the mob AI: is this correct?
		entity.setMoveForward(0.0f);
		entity.getNavigator().clearPathEntity();

		// FIXME change the dimension, check for chunk loaded etc.
		if (entX != 0.0d) //dim != targetDim)
		{
			//entity.travelToDimension(targetDim);
			TeleportEntity.transferEntityToDimension(entity, targetDim, x, y, z);
		}
		else
		{
			entity.setLocationAndAngles(x + 0.5d, y, z + 0.5d, entity.rotationYaw, entity.rotationPitch);
		}
	}
}
