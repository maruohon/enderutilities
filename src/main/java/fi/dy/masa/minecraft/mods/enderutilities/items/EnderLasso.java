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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
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

		ForgeChunkManager fcm = new ForgeChunkManager();
		String side	= nbt.getString("side");
		int dim		= nbt.getInteger("dim");
		int x		= nbt.getInteger("x");
		int y		= nbt.getInteger("y");
		int z		= nbt.getInteger("z");

		String dimPre = "" + EnumChatFormatting.GREEN;
		String coordPre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		if (dim >= -1 && dim <= 1)
		{
			String dimStr = (dim == -1 ? "Nether" : (dim == 0 ? "Overworld" : "The End"));
			list.add(String.format("Dimension: %s%s%s", dimPre, dimStr, rst));
		}
		else
		{
			list.add(String.format("Dimension: %s%d%s", dimPre, dim, rst));
		}

		list.add(String.format("x: %s%d%s, y: %s%d%s, z: %s%d%s", coordPre, x, rst, coordPre, y, rst, coordPre, z, rst));
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
		double x = (double)nbt.getInteger("x") + 0.5d;
		double y = (double)nbt.getInteger("y");
		double z = (double)nbt.getInteger("z") + 0.5d;
		int targetDim = nbt.getInteger("dim");

		// FIXME: only allow overworld and nether until I figure out the dimension and chunk laoding stuff...
		if (targetDim != 0 && targetDim != -1)
		{
			return;
		}

		// FIXME does this chunkloading work?
		//MinecraftServer minecraftserver = MinecraftServer.getServer();
		//WorldServer worldServerDst = minecraftserver.worldServerForDimension(targetDim);

		WorldServer worldServerDst = DimensionManager.getWorld(targetDim);
		if (worldServerDst != null && worldServerDst.theChunkProviderServer != null)
		{
			worldServerDst.theChunkProviderServer.loadChunk((int)x >> 4, (int)z >> 4);
		}

		double entX = entity.posX;
		double entY = entity.posY;
		double entZ = entity.posZ;

		World world = entity.worldObj;
		world.playSoundEffect(entX, entY, entZ, "mob.endermen.portal", 0.8F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);

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

		// FIXME Check for chunk loaded etc.
		if (dim != targetDim)
		{
			TeleportEntity.transferEntityToDimension(entity, targetDim, x, y, z);
		}
		else
		{
			entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
		}
	}
}
