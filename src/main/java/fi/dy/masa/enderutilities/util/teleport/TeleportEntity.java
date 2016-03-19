package fi.dy.masa.enderutilities.util.teleport;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TeleportEntity
{
    public static void addTeleportSoundsAndParticles(World world, double x, double y, double z)
    {
        if (world.isRemote == false)
        {
            PacketHandler.INSTANCE.sendToAllAround(new MessageAddEffects(MessageAddEffects.EFFECT_TELEPORT, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND, x, y, z),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 24.0d));
        }
    }

    public static boolean canTeleportEntity(Entity entity)
    {
        return ! EntityUtils.doesEntityStackHaveBlacklistedEntities(entity);
    }

    public static boolean teleportEntityRandomly(Entity entity, double maxDist)
    {
        if (entity == null || canTeleportEntity(entity) == false || entity.worldObj.isRemote == true)
        {
            return false;
        }

        // Sound and particles on the original location
        TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

        // Do the actual teleportation only on the server side
        if (entity.worldObj.isRemote == true)
        {
            return false;
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

            if (entity.getEntityBoundingBox() != null && entity.worldObj.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() == true)
            {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

                // Sound and particles on the new, destination location.
                TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, x, y, z);
                return true;
            }
        }

        return false;
    }

    public static boolean entityTeleportWithProjectile(Entity entity, Entity projectile, RayTraceResult rayTraceResult, float teleportDamage, boolean allowMounts, boolean allowRiders)
    {
        if (canTeleportEntity(entity) == false)
        {
            return false;
        }

        PositionHelper pos = new PositionHelper(rayTraceResult, projectile);

        // Hit a block, offset the position to not collide with the block
        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            pos.adjustPositionToTouchFace(entity, rayTraceResult.sideHit);
        }

        Entity entNew = TeleportEntity.teleportEntity(entity, pos.posX, pos.posY, pos.posZ, projectile.dimension, allowMounts, allowRiders);

        if (entNew != null)
        {
            if (teleportDamage != 0.0f)
            {
                // Inflict fall damage to the bottom most entity
                Entity bottom = EntityUtils.getBottomEntity(entNew);
                if (bottom instanceof EntityLivingBase)
                {
                    bottom.attackEntityFrom(DamageSource.fall, teleportDamage);
                }
            }

            entNew.fallDistance = 0.0f;
            return true;
        }

        return false;
    }

    public static NBTHelperTarget adjustTargetPosition(NBTHelperTarget target, Entity entity)
    {
        if (target == null || target.blockFace < 0)
        {
            return target;
        }

        float widthAdj = 0.5f;
        float heightAdj = 1.0f;

        if (entity != null)
        {
            widthAdj = entity.width / 2;
            heightAdj = entity.height;
        }

        target.dPosX += target.facing.getFrontOffsetX() * widthAdj;
        target.dPosZ += target.facing.getFrontOffsetZ() * widthAdj;

        // Targeting the bottom face of a block, adjust the position lower
        if (target.facing.equals(EnumFacing.DOWN))
        {
            target.dPosY -= heightAdj;
        }

        return target;
    }

    public static Entity teleportEntityUsingModularItem(Entity entity, ItemStack stack)
    {
        return TeleportEntity.teleportEntityUsingModularItem(entity, stack, true, true);
    }

    public static Entity teleportEntityUsingModularItem(Entity entity, ItemStack stack, boolean allowMounts, boolean allowRiders)
    {
        return TeleportEntity.teleportEntityUsingItem(entity, UtilItemModular.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL), allowMounts, allowRiders);
    }

    public static Entity teleportEntityUsingItem(Entity entity, ItemStack stack)
    {
        return TeleportEntity.teleportEntityUsingItem(entity, stack, true, true);
    }

    public static Entity teleportEntityUsingItem(Entity entity, ItemStack stack, boolean allowMounts, boolean allowRiders)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromItem(stack);
        if (target != null)
        {
            TeleportEntity.adjustTargetPosition(target, entity);

            if (target.hasAngle == true && entity != null)
            {
                entity.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, target.yaw, target.pitch);
            }

            return TeleportEntity.teleportEntity(entity, target.dPosX, target.dPosY, target.dPosZ, target.dimension, allowMounts, allowRiders);
        }

        return null;
    }

    public static Entity teleportEntity(Entity entity, double x, double y, double z, int dimDst, boolean allowMounts, boolean allowRiders)
    {
        if (entity == null || entity.worldObj == null || entity.worldObj.isRemote == true) { return null; }
        if (allowMounts == false && entity.ridingEntity != null) { return null; }
        if (allowRiders == false && entity.riddenByEntity != null) { return null; }
        if (canTeleportEntity(entity) == false) { return null; }

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

        // Post the event and check if the teleport should be allowed
        if (entity instanceof EntityLivingBase)
        {
            EnderTeleportEvent etpEvent = new EnderTeleportEvent((EntityLivingBase)entity, x, y, z, 0.0f);
            if (MinecraftForge.EVENT_BUS.post(etpEvent) == true)
            {
                return null;
            }
        }

        // Sound and particles on the original location
        TeleportEntity.addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

        if (entity.worldObj.isRemote == false && entity.worldObj instanceof WorldServer)
        {
            MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer worldServerDst = minecraftserver.worldServerForDimension(dimDst);
            if (worldServerDst == null)
            {
                EnderUtilities.logger.warn("teleportEntity(): worldServerDst == null");
                return null;
            }

            //System.out.println("Is loaded: " + worldServerDst.getChunkProvider().chunkExists((int)x >> 4, (int)z >> 4)); // FIXME debug

            int chunkX = ((int)x) >> 4;
            int chunkZ = ((int)z) >> 4;

            if (worldServerDst.getChunkProvider().chunkExists(chunkX, chunkZ) == false)
            {
                worldServerDst.getChunkProvider().loadChunk(chunkX, chunkZ);
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
                if (entity instanceof EntityPlayer)
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

        WorldServer worldServerDst = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(entitySrc.dimension);
        if (worldServerDst == null)
        {
            EnderUtilities.logger.warn("reCreateEntity(): worldServerDst == null");
            return null;
        }

        Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);
        if (entityDst == null)
        {
            return null;
        }

        entitySrc.worldObj.removeEntity(entitySrc); // Note: this will also remove any entity mounts
        entitySrc.isDead = false;

        entityDst.copyDataFromOld(entitySrc);
        if (entityDst instanceof EntityLivingBase)
        {
            ((EntityLivingBase)entityDst).setPositionAndUpdate(x, y, z);
        }
        else
        {
            entityDst.setLocationAndAngles(x, y, z, entitySrc.rotationYaw, entitySrc.rotationPitch);
        }

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

        WorldServer worldServerSrc = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(entitySrc.dimension);
        WorldServer worldServerDst = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimDst);

        if (worldServerSrc == null || worldServerDst == null)
        {
            EnderUtilities.logger.warn("transferEntityToDimension(): worldServer[Src|Dst] == null");
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

        entityDst.copyDataFromOld(entitySrc);
        // FIXME ugly special case to prevent the chest minecart etc from duping items
        if (entitySrc instanceof EntityMinecartContainer)
        {
            entitySrc.isDead = true;
        }
        else
        {
            entitySrc.worldObj.removeEntity(entitySrc); // Note: this will also remove any entity mounts
        }

        x = MathHelper.clamp_double(x, -30000000.0d, 30000000.0d);
        z = MathHelper.clamp_double(z, -30000000.0d, 30000000.0d);
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

        // Post the event and check if the teleport should be allowed
        PlayerChangedDimensionEvent pcdEvent = new PlayerChangedDimensionEvent(player, player.dimension, dimDst);
        if (MinecraftForge.EVENT_BUS.post(pcdEvent) == true)
        {
            return null;
        }

        int dimSrc = player.dimension;
        x = MathHelper.clamp_double(x, -30000000.0d, 30000000.0d);
        z = MathHelper.clamp_double(z, -30000000.0d, 30000000.0d);
        player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);

        ServerConfigurationManager serverCM = player.mcServer.getConfigurationManager();
        WorldServer worldServerSrc = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimSrc);
        WorldServer worldServerDst = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimDst);

        if (worldServerSrc == null || worldServerDst == null)
        {
            EnderUtilities.logger.warn("transferPlayerToDimension(): worldServer[Src|Dst] == null");
            return null;
        }

        player.dimension = dimDst;
        player.playerNetServerHandler.sendPacket(new SPacketRespawn(player.dimension, player.worldObj.getDifficulty(), player.worldObj.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
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
        serverCM.preparePlayer(player, worldServerSrc); // remove player from the source world
        player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(worldServerDst);
        player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, worldServerDst);
        player.mcServer.getConfigurationManager().syncPlayerInventory(player);
        player.addExperienceLevel(0);
        player.setPlayerHealthUpdated();

        Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();
        while (iterator.hasNext())
        {
            PotionEffect potioneffect = (PotionEffect)iterator.next();
            player.playerNetServerHandler.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }

        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, dimSrc, dimDst);

        return player;
    }
}
