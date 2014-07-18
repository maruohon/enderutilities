package fi.dy.masa.enderutilities.util.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;

public class TeleportPlayer
{
	public static boolean teleportPlayerAndMountsInSameDimension(EntityPlayerMP player, double x, double y, double z, float tpDamagePlayer, float tpDamageEntity)
	{
		if (player.ridingEntity != null)
		{
			boolean nonliving = false;
			Entity e;
			for (e = player.ridingEntity; e != null; e = e.ridingEntity)
			{
				if (e.ridingEntity == null)
				{
					break;
				}

				if (e instanceof EntityLiving == false)
				{
					nonliving = true;
				}
			}

			// TODO: Do we allow teleporting while riding non-living entities?
			if (nonliving == false || true)
			{
				if (e instanceof EntityLiving)
				{
					EntityLiving living = (EntityLiving)e;
					living.setMoveForward(0.0f);
					living.getNavigator().clearPathEntity();
				}

				e.setPosition(x, y + 0.5d, z);
				e.fallDistance = 0.0f;

				// TODO: Add a config option to decide if the ridingEntity should take damage
				e.attackEntityFrom(DamageSource.fall, tpDamageEntity);

				// TODO: Add a config option to decide if the rider should take damage when riding
				//player.attackEntityFrom(DamageSource.fall, teleportDamage);

				// FIXME this part of code doesn't get executed on the client side (mop is null there)
				// So currently we can't do particles :/
				TeleportEntity.addEnderSoundsAndParticles(x, y, z, player.worldObj);
			}
		}
		else if (player.ridingEntity == null)
		{
			player.setPositionAndUpdate(x, y, z);
			player.fallDistance = 0.0f;
			player.attackEntityFrom(DamageSource.fall, tpDamagePlayer);

			// FIXME this part of code doesn't get executed on the client side (mop is null there)
			// So currently we can't do particles :/
			TeleportEntity.addEnderSoundsAndParticles(x, y, z, player.worldObj);
		}

		return true;
	}
}
