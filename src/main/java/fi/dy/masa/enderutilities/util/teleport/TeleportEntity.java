package fi.dy.masa.enderutilities.util.teleport;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionHelper;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
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
        return EntityUtils.doesEntityStackHaveBlacklistedEntities(entity) == false;
    }

    public static boolean teleportEntityRandomly(Entity entity, double maxDist)
    {
        if (entity == null || canTeleportEntity(entity) == false || entity.worldObj.isRemote == true)
        {
            return false;
        }

        // Sound and particles on the original location
        addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

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

            if (entity.getEntityBoundingBox() != null && entity.worldObj.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty() == true)
            {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

                // Sound and particles on the new, destination location.
                addTeleportSoundsAndParticles(entity.worldObj, x, y, z);
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

        Entity entNew = teleportEntity(entity, pos.posX, pos.posY, pos.posZ, projectile.dimension, allowMounts, allowRiders);

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

    public static TargetData adjustTargetPosition(TargetData target, Entity entity)
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
        return teleportEntityUsingModularItem(entity, stack, true, true);
    }

    public static Entity teleportEntityUsingModularItem(Entity entity, ItemStack stack, boolean allowMounts, boolean allowRiders)
    {
        return teleportEntityUsingItem(entity, UtilItemModular.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL), allowMounts, allowRiders);
    }

    public static Entity teleportEntityUsingItem(Entity entity, ItemStack stack)
    {
        return teleportEntityUsingItem(entity, stack, true, true);
    }

    public static Entity teleportEntityUsingItem(Entity entity, ItemStack stack, boolean allowMounts, boolean allowRiders)
    {
        TargetData target = TargetData.getTargetFromItem(stack);
        if (target != null)
        {
            return teleportEntityUsingTarget(entity, target, allowMounts, allowRiders);
        }

        return null;
    }

    public static Entity teleportEntityUsingTarget(Entity entity, TargetData target, boolean allowMounts, boolean allowRiders)
    {
        if (target == null || entity == null)
        {
            return null;
        }

        adjustTargetPosition(target, entity);

        if (target.hasRotation == true && entity != null)
        {
            if (entity instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) entity).connection.setPlayerLocation(entity.posX, entity.posY, entity.posZ, target.yaw, target.pitch);
            }
            else
            {
                entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, target.yaw, target.pitch);
            }
        }

        return teleportEntity(entity, target.dPosX, target.dPosY, target.dPosZ, target.dimension, allowMounts, allowRiders);
    }

    public static Entity teleportEntity(Entity entityIn, double x, double y, double z, int dimDst, boolean allowMounts, boolean allowRiders)
    {
        if (entityIn == null || entityIn.worldObj == null || entityIn.worldObj.isRemote == true) { return null; }
        if (allowMounts == false && entityIn.isRiding() == true) { return null; }
        if (allowRiders == false && entityIn.isBeingRidden() == true) { return null; }
        if (canTeleportEntity(entityIn) == false) { return null; }

        UUID uuidOriginal = entityIn.getUniqueID();
        Entity entity = EntityUtils.getBottomEntity(entityIn);
        List<Entity> passengers = null;
        boolean ridden = false;
        boolean reCreate = EntityUtils.doesEntityStackHavePlayers(entityIn);
        // This method gets called recursively when teleporting the passengers.
        // It is always called on the bottom entity on all of the passengers however, so we can
        // recognize the original target entity from a 'stack' with this check.
        // Note also that if the original target was already the bottom most entity, then
        // this extra logic isn't even necessary for it.
        boolean isOriginal = entity != entityIn;

        // Teleport all the entities in this 'stack', starting from the bottom most entity
        ridden = entity.isBeingRidden();

        if (ridden == true)
        {
            passengers = entity.getPassengers();

            for (Entity passenger : passengers)
            {
                passenger.dismountRidingEntity();
            }
        }

        Entity teleported = teleportEntity(entity, x, y, z, dimDst, reCreate);
        if (teleported == null)
        {
            return null;
        }

        teleported.fallDistance = 0.0f;

        if (ridden == true)
        {
            for (Entity passenger : passengers)
            {
                Entity teleportedPassenger = teleportEntity(passenger, x, y, z, dimDst, allowMounts, allowRiders);

                if (teleportedPassenger != null)
                {
                    teleportedPassenger.startRiding(teleported, true);
                }
            }
        }

        if (isOriginal == true)
        {
            teleported = EntityUtils.findEntityFromStackByUUID(teleported, uuidOriginal);
        }

        return teleported;
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
        addTeleportSoundsAndParticles(entity.worldObj, entity.posX, entity.posY, entity.posZ);

        if (entity.worldObj.isRemote == false && entity.worldObj instanceof WorldServer)
        {
            WorldServer worldServerDst = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimDst);
            if (worldServerDst == null)
            {
                EnderUtilities.logger.warn("teleportEntity(): worldServerDst == null");
                return null;
            }

            //System.out.println("Is loaded: " + worldServerDst.getChunkProvider().chunkExists((int)x >> 4, (int)z >> 4));

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

            if (entity.dimension != dimDst || (entity.worldObj instanceof WorldServer && entity.worldObj != worldServerDst))
            {
                entity = transferEntityToDimension(entity, dimDst, x, y, z);
            }
            else if (entity instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            else
            {
                //entity.setPositionAndUpdate(x, y, z);
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
        }

        if (entity != null)
        {
            // Final position
            addTeleportSoundsAndParticles(entity.worldObj, x, y, z);
        }

        return entity;
    }

    /*private static Entity reCreateEntity(Entity entitySrc, double x, double y, double z)
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

        //entityDst.copyDataFromOld(entitySrc);
        EntityUtils.copyDataFromOld(entityDst, entitySrc);

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
    }*/

    private static Entity transferEntityToDimension(Entity entitySrc, int dimDst, double x, double y, double z)
    {
        if (entitySrc == null || entitySrc.isDead == true || entitySrc.dimension == dimDst || entitySrc.worldObj.isRemote == true)
        {
            return null;
        }

        if (entitySrc instanceof EntityPlayerMP)
        {
            return transferPlayerToDimension((EntityPlayerMP)entitySrc, dimDst, x, y, z);
        }

        ChunkLoading.getInstance().loadChunkForcedWithModTicket(dimDst, (int)x >> 4, (int)z >> 4, 10);
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer worldServerSrc = server.worldServerForDimension(entitySrc.dimension);
        WorldServer worldServerDst = server.worldServerForDimension(dimDst);

        if (worldServerSrc == null || worldServerDst == null)
        {
            EnderUtilities.logger.warn("transferEntityToDimension(): worldServer[Src|Dst] == null");
            return null;
        }

        entitySrc.dimension = dimDst;
        Entity entityDst = EntityList.createEntityByName(EntityList.getEntityString(entitySrc), worldServerDst);
        if (entityDst == null)
        {
            return null;
        }

        //entityDst.copyDataFromOld(entitySrc);
        EntityUtils.copyDataFromOld(entityDst, entitySrc);

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

    private static EntityPlayer transferPlayerToDimension(EntityPlayerMP player, int dimDst, double x, double y, double z)
    {
        if (player == null || player.isDead == true || player.dimension == dimDst || player.worldObj.isRemote == true)
        {
            return null;
        }

        int dimSrc = player.dimension;
        x = MathHelper.clamp_double(x, -30000000.0d, 30000000.0d);
        z = MathHelper.clamp_double(z, -30000000.0d, 30000000.0d);
        player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer worldServerSrc = server.worldServerForDimension(dimSrc);
        WorldServer worldServerDst = server.worldServerForDimension(dimDst);

        if (worldServerSrc == null || worldServerDst == null)
        {
            EnderUtilities.logger.warn("transferPlayerToDimension(): worldServer[Src|Dst] == null");
            return null;
        }

        player.dimension = dimDst;
        player.connection.sendPacket(new SPacketRespawn(player.dimension, player.worldObj.getDifficulty(), player.worldObj.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
        player.mcServer.getPlayerList().updatePermissionLevel(player);
        //worldServerSrc.removePlayerEntityDangerously(player); // this crashes
        worldServerSrc.removeEntity(player);
        player.isDead = false;

        worldServerDst.spawnEntityInWorld(player);
        worldServerDst.updateEntityWithOptionalForce(player, false);
        player.setWorld(worldServerDst);
        player.mcServer.getPlayerList().preparePlayer(player, worldServerSrc); // remove player from the source world
        player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        player.interactionManager.setWorld(worldServerDst);
        player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
        player.mcServer.getPlayerList().updateTimeAndWeatherForPlayer(player, worldServerDst);
        player.mcServer.getPlayerList().syncPlayerInventory(player);
        player.addExperienceLevel(0);
        player.setPlayerHealthUpdated();

        // FIXME 1.9 - Somewhat ugly way to clear the Boss Info stuff when teleporting FROM The End
        if (worldServerSrc.provider instanceof WorldProviderEnd)
        {
            DragonFightManager manager = ((WorldProviderEnd)worldServerSrc.provider).getDragonFightManager();

            if (manager != null)
            {
                try
                {
                    BossInfoServer bossInfo = ReflectionHelper.getPrivateValue(DragonFightManager.class, manager, "field_186109_c", "bossInfo");
                    if (bossInfo != null)
                    {
                        bossInfo.removePlayer(player);
                    }
                }
                catch (UnableToAccessFieldException e)
                {
                    EnderUtilities.logger.warn("TeleportEntity.transferPlayerToDimension: Failed to get DragonFightManager#bossInfo");
                }
            }
        }

        for (PotionEffect potioneffect : player.getActivePotionEffects())
        {
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }

        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, dimSrc, dimDst);

        return player;
    }
}
