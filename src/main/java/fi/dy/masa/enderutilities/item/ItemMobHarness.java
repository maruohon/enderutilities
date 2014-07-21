package fi.dy.masa.enderutilities.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemMobHarness extends ItemEU
{
	public ItemMobHarness()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_MOB_HARNESS);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (world.isRemote == true)
		{
			return stack;
		}

		if (player.isSneaking() == true)
		{
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
			if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.MISS)
			{
				this.clearData(stack);
			}
		}

		return stack;
	}

	public boolean handleInteraction(ItemStack stack, EntityPlayer player, Entity entity)
	{
		if (player == null || entity == null || player.isSneaking() == false)
		{
			return false;
		}
		boolean hasTarget = this.hasTarget(stack);

		if (hasTarget == false)
		{
			// Looking up, player ridden by something and harness empty: dismount the rider
			if (player.rotationPitch > 80.0f && player.riddenByEntity != null)
			{
				player.riddenByEntity.mountEntity(null);
			}
			// Empty harness, target is riding something: dismount target
			else if (entity.ridingEntity != null)
			{
				entity.mountEntity(null);
			}
			// Empty harness, target not riding anything
			else
			{
				this.storeTarget(stack, entity);
			}
		}
		// Harness bound to something, mount the entity
		else
		{
			this.mountTarget(stack, player.worldObj, player, entity);
		}

		return true;
	}

	public boolean hasTarget(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		//if (nbt == null || nbt.hasKey("TargetUUIDLeast") == false || nbt.hasKey("TargetUUIDMost") == false)
		if (nbt == null || nbt.hasKey("TargetType") == false)
		{
			return false;
		}
		if (nbt.getByte("TargetType") == (byte)0 && (nbt.hasKey("TargetId") == false || nbt.hasKey("TargetString") == false))
		{
			return false;
		}
		if (nbt.getByte("TargetType") == (byte)1 && (nbt.hasKey("PlayerUUIDMost") == false || nbt.hasKey("PlayerUUIDLeast") == false))
		{
			return false;
		}

		return true;
	}

	public ItemStack storeTarget(ItemStack stack, Entity entity)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (entity == null) { return stack; }

		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		byte mode = (byte)0;

		if (entity instanceof EntityPlayer)
		{
			EntityPlayer targetPlayer = (EntityPlayer)entity;
			mode = 1;
			nbt.setString("TargetPlayer", ((EntityPlayer)entity).getCommandSenderName());
			nbt.setLong("PlayerUUIDMost", targetPlayer.getUniqueID().getMostSignificantBits());
			nbt.setLong("PlayerUUIDLeast", targetPlayer.getUniqueID().getLeastSignificantBits());
		}
		else
		{
			nbt.setInteger("TargetId", entity.getEntityId());
			nbt.setString("TargetString", EntityList.getEntityString(entity));
		}

		nbt.setByte("TargetType", mode);
		stack.setTagCompound(nbt);

		return stack;
	}

	public boolean mountTarget(ItemStack stack, World world, EntityPlayer player, Entity entity)
	{
		if (stack == null || player == null || entity == null || stack.getTagCompound() == null)
		{
			return false;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		byte mode = nbt.getByte("TargetType");

		// Mode 0: mount non-player living mobs
		if (mode == (byte)0)
		{
			if (nbt.hasKey("TargetId") == false || nbt.hasKey("TargetString") == false)
			{
				return false;
			}

			int targetId = nbt.getInteger("TargetId");
			String targetString = nbt.getString("TargetString");
			double radius = 4.0d;

			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player,
					AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - radius, player.posZ - radius,
					player.posX + radius, player.posY + radius, player.posZ + radius));

			for (Entity ent : list)
			{
				if (ent.getEntityId() == targetId && EntityList.getEntityString(ent).equals(targetString))
				{
					// The harness was clicked twice on the same mob, mount that mob on top of the player
					if (targetId == entity.getEntityId())
					{
						entity.mountEntity(player);
					}
					// The harness was clicked on two separate mobs, mount the stored/first one on top of the current one
					else
					{
						ent.mountEntity(entity);
					}

					break;
				}
			}
		}
		// Mode 1: mount a player
		else
		{
			if (nbt.hasKey("PlayerUUIDMost") == false || nbt.hasKey("PlayerUUIDLeast") == false)
			{
				return false;
			}

			EntityPlayerMP targetPlayer = EntityUtils.findPlayerFromUUID(new UUID(nbt.getLong("PlayerUUIDMost"), nbt.getLong("PlayerUUIDLeast")));
			if (targetPlayer == null)
			{
				return false;
			}

			// The harness was clicked twice on the same player, mount that player on top of the this player
			if (entity == targetPlayer)
			{
				targetPlayer.mountEntity(player);
			}
			else
			{
				targetPlayer.mountEntity(entity);
			}
		}

		this.clearData(stack);

		return false;
	}

	public boolean dismountEntity(EntityPlayer player, Entity entity)
	{
		if (entity == null)
		{
			if (player.riddenByEntity != null)
			{
				player.riddenByEntity.mountEntity(null);
			}
		}
		else
		{
			if (entity.ridingEntity != null)
			{
				entity.mountEntity(null);
			}
		}

		return true;
	}

	public boolean clearData(ItemStack stack)
	{
		stack.setTagCompound(null);

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null) // || this.hasTarget(stack) == false)
		{
			list.add(StatCollector.translateToLocal("gui.tooltip.notlinked"));
			return;
		}

		String pre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		String target = nbt.getByte("TargetType") == (byte)0 ? nbt.getString("TargetString") : nbt.getString("TargetPlayer");
		list.add(StatCollector.translateToLocal("gui.tooltip.linked") + ": " + pre + target + rst);
	}
}
