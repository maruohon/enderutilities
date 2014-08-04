package fi.dy.masa.enderutilities.util.teleport;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.NetworkRegistry;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.ItemNBTHelper;

public class TeleportEntity
{
	public static void addTeleportSoundsAndParticles(World world, double x, double y, double z)
	{
		if (world.isRemote == true)
		{
			return;
		}

		world.playSoundEffect(x, y, z, "mob.endermen.portal", 0.8F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);

		PacketHandler.INSTANCE.sendToAllAround(new MessageAddEffects(MessageAddEffects.EFFECT_TELEPORT, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND, x, y, z),
			new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 24.0d));
	}

	public static boolean canTeleportEntity(Entity entity)
	{
		// TODO Add a blacklist for entities

		return true;
	}

	public static void teleportEntityRandomly(EntityLivingBase entity, double maxDist)
	{
		if (entity == null || canTeleportEntity(entity) == false || entity.worldObj.isRemote == true)
		{
			return;
		}

		// Sound and particles on the original location
		TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

		// Do the actual teleportation only on the server side
		if (entity.worldObj.isRemote == true)
		{
			return;
		}

		double deltaYaw = 0.0d;
		double deltaPitch = 0.0d;
		double x = 0.0d;
		double y = 0.0d;
		double z = 0.0d;
		//maxDist *= Math.random();
		maxDist = maxDist - (Math.random() * maxDist / 2.0d);

		// Try to find a free spot (non-colliding with blocks)
		for (int i = 0; i < 10; i++)
		{
			deltaYaw = ((Math.random() * 360.0f) / 180.0d) * Math.PI;
			//deltaPitch = ((90.0d - (Math.random() * 180.0d)) / 180.0d) * Math.PI; // free range on the y-direction
			deltaPitch = ((Math.random() * 90.0d) / 180.0d) * Math.PI; // only from the same level upwards
			x = entity.posX;
			y = entity.posY;
			z = entity.posZ;
			x += Math.cos(deltaPitch) * Math.cos(deltaYaw) * maxDist;
			z += Math.cos(deltaPitch) * Math.sin(deltaYaw) * maxDist;
			y += Math.sin(deltaPitch) * maxDist;

			if (entity.worldObj.getBlock((int)x, (int)y, (int)z) == Blocks.air &&
				entity.worldObj.getBlock((int)x, (int)y + 1, (int)z) == Blocks.air)
			//if (entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty() == true)
			{
				//entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
				entity.setPositionAndUpdate(x, y, z);

				// Sound and particles on the new, destination location.
				//TODO: Since this only happens on the server side, we currently get no particles here. Maybe add custom packets for effects?
				TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, x, y, z);
				return;
			}
		}
	}

	public static void playerTeleportSelfWithProjectile(EntityPlayer player, Entity entity, MovingObjectPosition mop, float teleportDamage, boolean allowMounts, boolean allowRiders)
	{
		double x = entity.posX;
		double y = entity.posY;
		double z = entity.posZ;
		// Hit an entity
		if (mop.entityHit != null)
		{
			x = mop.entityHit.posX;
			y = mop.entityHit.posY;
			z = mop.entityHit.posZ;
		}
		// Hit a block
		else if (mop.hitVec != null)
		{
			//x = mop.blockX;
			//y = mop.blockY;
			//z = mop.blockZ;
			x = mop.hitVec.xCoord;
			y = mop.hitVec.yCoord;
			z = mop.hitVec.zCoord;

			ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
			x += (dir.offsetX * 0.5d);
			z += (dir.offsetZ * 0.5d);
			if (dir.offsetY < 0)
			{
				y -= (0.5d + player.getDefaultEyeHeight());
			}
		}

		EnderTeleportEvent event = new EnderTeleportEvent(player, x, y, z, teleportDamage);
		if (MinecraftForge.EVENT_BUS.post(event) == false)
		{
			int victim = 0;
			// Player is riding something, inflict fall damage to the bottom most entity
			if (player.ridingEntity != null)
			{
				victim = 1;
			}

			Entity e = TeleportEntity.teleportEntity(player, x, y, z, entity.dimension, allowMounts, allowRiders);

			if (e != null)
			{
				if (victim == 1)
				{
					EntityUtils.getBottomEntity(e).attackEntityFrom(DamageSource.fall, teleportDamage);
				}
				else
				{
					e.attackEntityFrom(DamageSource.fall, teleportDamage);
				}
			}
		}
	}

	public static Entity teleportEntityUsingItem(Entity entity, ItemStack stack)
	{
		if (entity.worldObj.isRemote == true || stack == null)
		{
			return null;
		}

		ItemNBTHelper target = new ItemNBTHelper();
		if (target.readTargetTagFromNBT(stack.getTagCompound()) != null)
		{
			return TeleportEntity.teleportEntity(entity, target.posX + 0.5d, target.posY, target.posZ + 0.5d, target.dimension, true, true);
		}

		return null;
	}

	public static Entity teleportEntity(Entity entity, double x, double y, double z, int dimDst, boolean allowMounts, boolean allowRiders)
	{
		if (entity == null || entity.worldObj.isRemote == true) { return null; }
		if (allowMounts == false && entity.ridingEntity != null) { return null; }
		if (allowRiders == false && entity.riddenByEntity != null) { return null; }

		Entity current, ret = null;
		boolean reCreate = EntityUtils.doesEntityStackHavePlayers(entity);

		Entity riddenBy, teleported, previous = null;
		current = EntityUtils.getBottomEntity(entity);

		// Teleport all the entities in this 'stack', starting from the bottom most entity
		while (current != null)
		{
			riddenBy = current.riddenByEntity;
			if (current.riddenByEntity != null)
			{
				current.riddenByEntity.mountEntity((Entity)null);
			}

			// Store the new instance of the original target entity for return
			if (current == entity)
			{
				teleported = TeleportEntity.teleportEntity(current, x, y, z, dimDst, reCreate);
				ret = teleported;
			}
			else
			{
				teleported = TeleportEntity.teleportEntity(current, x, y, z, dimDst, reCreate);
			}

			if (teleported == null)
			{
				break;
			}

			if (previous != null)
			{
				teleported.mountEntity(previous);
			}

			teleported.fallDistance = 0.0f;
			current = riddenBy;
			previous = teleported;
		}

		return ret;
	}

	private static Entity teleportEntity(Entity entity, double x, double y, double z, int dimDst, boolean forceRecreate)
	{
		if (entity == null || entity.isDead == true || canTeleportEntity(entity) == false || entity.worldObj.isRemote == true)
		{
			return null;
		}

		// Sound and particles on the original location
		TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

		if (entity.worldObj.isRemote == false && entity.worldObj instanceof WorldServer)
		{
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);
			if (worldServerDst == null)
			{
				FMLLog.warning("[Ender Utilities] teleportEntity(): worldServerDst == null");
				return null;
			}

			//System.out.println("Is loaded: " + worldServerDst.getChunkProvider().chunkExists((int)x >> 4, (int)z >> 4)); // FIXME debug

			IChunkProvider chunkProvider = worldServerDst.getChunkProvider();
			if (chunkProvider == null)
			{
				return null;
			}

			if (chunkProvider.chunkExists((int)x >> 4, (int)z >> 4) == false)
			{
				//worldServerDst.theChunkProviderServer.loadChunk((int)x >> 4, (int)z >> 4);
				chunkProvider.loadChunk((int)x >> 4, (int)z >> 4);
			}

			if (entity instanceof EntityLiving)
			{
				((EntityLiving)entity).setMoveForward(0.0f);
				((EntityLiving)entity).getNavigator().clearPathEntity();
			}
			// FIXME debug
			//System.out.printf("entity.worldObj: %s %s\n", entity.worldObj.toString(), entity.worldObj.getClass().getSimpleName());
			//double d = (x - entity.posX) * (x - entity.posX) + (y - entity.posY) * (y - entity.posY) + (z - entity.posZ) * (z - entity.posZ);
			//System.out.printf("Tp distance: %.4f\n", MathHelper.sqrt_double(d));

			if (entity.dimension != dimDst || (entity.worldObj instanceof WorldServer && entity.worldObj != worldServerDst))
			{
				entity = TeleportEntity.transferEntityToDimension(entity, dimDst, x, y, z);
			}
			else
			{
				if (entity instanceof EntityPlayerMP)
				{
					//((EntityPlayer)entity).setPositionAndUpdate(x, y, z);
					((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
				}
				else if (entity instanceof EntityPlayer)
				{
					((EntityPlayer)entity).setPositionAndUpdate(x, y, z);
				}
				// Forcing a recreate even in the same dimension, mainly used when teleporting mounted entities where a player is one of them
				else if (forceRecreate == true)
				{
					entity = TeleportEntity.reCreateEntity(entity, x, y, z);
				}
				else if (entity instanceof EntityLivingBase)
				{
					((EntityLivingBase)entity).setPositionAndUpdate(x, y, z);
				}
				else
				{
					entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
				}
			}
		}

		if (entity != null)
		{
			// Final position
			TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, x, y, z);
		}

		return entity;
	}

	public static Entity reCreateEntity(Entity entitySrc, double x, double y, double z)
	{
		if (entitySrc.worldObj.isRemote == true)
		{
			return null;
		}

		WorldServer worldServerDst = MinecraftServer.getServer().worldServerForDimension(entitySrc.dimension);

		if (worldServerDst == null)
		{
			FMLLog.warning("[Ender Utilities] reCreateEntity(): worldServerDst == null");
			return null;
		}

		entitySrc.worldObj.removeEntity(entitySrc); // Note: this will also remove any entity mounts
		entitySrc.isDead = false;

		entitySrc.mountEntity((Entity)null);
		if (entitySrc.riddenByEntity != null)
		{
			entitySrc.riddenByEntity.mountEntity((Entity)null);
		}

		Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);
		if (entityDst == null)
		{
			return null;
		}

		entityDst.copyDataFrom(entitySrc, true);
		entityDst.setLocationAndAngles(x, y, z, entitySrc.rotationYaw, entitySrc.rotationPitch);
		worldServerDst.spawnEntityInWorld(entityDst);
		worldServerDst.resetUpdateEntityTick();
		entitySrc.isDead = true;

		return entityDst;
	}

	public static Entity transferEntityToDimension(Entity entitySrc, int dimDst, double x, double y, double z)
	{
		if (entitySrc == null || entitySrc.isDead == true || entitySrc.dimension == dimDst || entitySrc.worldObj.isRemote == true)
		{
			return null;
		}

		if (entitySrc instanceof EntityPlayerMP)
		{
			return TeleportEntity.transferPlayerToDimension((EntityPlayerMP)entitySrc, dimDst, x, y, z);
		}

		WorldServer worldServerSrc = MinecraftServer.getServer().worldServerForDimension(entitySrc.dimension);
		WorldServer worldServerDst = MinecraftServer.getServer().worldServerForDimension(dimDst);

		if (worldServerSrc == null || worldServerDst == null)
		{
			FMLLog.warning("[Ender Utilities] transferEntityToDimension(): worldServer[Src|Dst] == null");
			return null;
		}

		entitySrc.mountEntity((Entity)null);
		if (entitySrc.riddenByEntity != null)
		{
			entitySrc.riddenByEntity.mountEntity((Entity)null);
		}

		entitySrc.dimension = dimDst;
		Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);
		if (entityDst == null)
		{
			return null;
		}

		entityDst.copyDataFrom(entitySrc, true);
		// FIXME ugly special case to prevent the chest minecart etc from duping items
		if (entitySrc instanceof EntityMinecartContainer)
		{
			entitySrc.isDead = true;
		}
		else
		{
			entitySrc.worldObj.removeEntity(entitySrc); // Note: this will also remove any entity mounts
		}
		x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
		z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);
		entityDst.setLocationAndAngles(x, y, z, entitySrc.rotationYaw, entitySrc.rotationPitch);
		worldServerDst.spawnEntityInWorld(entityDst);
		worldServerDst.updateEntityWithOptionalForce(entityDst, false);
		entityDst.setWorld(worldServerDst);

		// Debug: this actually kills the original entity, commenting it will make clones
		entitySrc.isDead = true;

		worldServerSrc.resetUpdateEntityTick();
		worldServerDst.resetUpdateEntityTick();

		return entityDst;
	}

	public static EntityPlayerMP transferPlayerToDimension(EntityPlayerMP player, int dimDst, double x, double y, double z)
	{
		if (player == null || player.isDead == true || player.dimension == dimDst || player.worldObj.isRemote == true)
		{
			return null;
		}

		int dimSrc = player.dimension;
		x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
		z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);
		player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);

		ServerConfigurationManager serverCM = player.mcServer.getConfigurationManager();
		WorldServer worldServerSrc = MinecraftServer.getServer().worldServerForDimension(dimSrc);
		WorldServer worldServerDst = MinecraftServer.getServer().worldServerForDimension(dimDst);

		if (worldServerSrc == null || worldServerDst == null)
		{
			FMLLog.warning("[Ender Utilities] transferPlayerToDimension(): worldServer[Src|Dst] == null");
			return null;
		}

		player.dimension = dimDst;
		player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.difficultySetting, player.worldObj.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
		//worldServerSrc.removePlayerEntityDangerously(player); // this crashes
		worldServerSrc.removeEntity(player);
		player.isDead = false;

		player.mountEntity((Entity)null);
		if (player.riddenByEntity != null)
		{
			player.riddenByEntity.mountEntity((Entity)null);
		}

		worldServerDst.spawnEntityInWorld(player);
		worldServerDst.updateEntityWithOptionalForce(player, false);
		player.setWorld(worldServerDst);
		serverCM.func_72375_a(player, worldServerSrc); // remove player from the source world
		player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
		player.theItemInWorldManager.setWorld(worldServerDst);
		player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, worldServerDst);
		player.mcServer.getConfigurationManager().syncPlayerInventory(player);
		player.addExperience(0);
		player.setPlayerHealthUpdated();

		Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();
		while (iterator.hasNext())
		{
			PotionEffect potioneffect = (PotionEffect)iterator.next();
			player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
		}

		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, dimSrc, dimDst);

		return player;
	}
}
