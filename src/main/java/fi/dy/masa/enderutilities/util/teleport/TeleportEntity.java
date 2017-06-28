package fi.dy.masa.enderutilities.util.teleport;

import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.common.ForgeHooks;
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
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TeleportEntity
{
    private static boolean teleportInProgress;

    public static boolean isTeleportInProgress()
    {
        return teleportInProgress;
    }

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
        World world = entity.getEntityWorld();

        if (canTeleportEntity(entity) == false || world.isRemote)
        {
            return false;
        }

        double deltaYaw = 0.0d;
        double deltaPitch = 0.0d;
        double x = 0.0d;
        double y = 0.0d;
        double z = 0.0d;
        maxDist = maxDist - (world.rand.nextFloat() * maxDist / 2.0d);

        // Try to find a free spot (non-colliding with blocks)
        for (int i = 0; i < 20; i++)
        {
            deltaYaw = world.rand.nextFloat() * 2d * Math.PI;
            //deltaPitch = ((90.0d - (Math.random() * 180.0d)) / 180.0d) * Math.PI; // free range on the y-direction
            deltaPitch = world.rand.nextFloat() * 0.5d * Math.PI; // only from the same level upwards
            x = entity.posX;
            y = entity.posY;
            z = entity.posZ;
            x += Math.cos(deltaPitch) * Math.cos(deltaYaw) * maxDist;
            z += Math.cos(deltaPitch) * Math.sin(deltaYaw) * maxDist;
            y += Math.sin(deltaPitch) * maxDist;

            if (entity.getEntityBoundingBox() != null && world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty())
            {
                // Sound and particles on the original location
                addTeleportSoundsAndParticles(world, entity.posX, entity.posY, entity.posZ);

                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

                // Sound and particles on the new, destination location.
                addTeleportSoundsAndParticles(world, x, y, z);
                return true;
            }
        }

        return false;
    }

    public static boolean teleportEntityWithProjectile(Entity entity, Entity projectile, RayTraceResult rayTraceResult, float teleportDamage, boolean allowMounts, boolean allowRiders)
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

        Entity entNew = teleportEntity(entity, pos.posX, pos.posY, pos.posZ, projectile.getEntityWorld().provider.getDimension(), allowMounts, allowRiders);

        if (entNew != null)
        {
            if (teleportDamage != 0.0f)
            {
                // Inflict fall damage to the bottom most entity
                Entity bottom = EntityUtils.getBottomEntity(entNew);

                if (bottom instanceof EntityLivingBase)
                {
                    bottom.attackEntityFrom(DamageSource.FALL, teleportDamage);
                }
            }

            entNew.fallDistance = 0.0f;
            return true;
        }

        return false;
    }

    public static Entity teleportEntityUsingModularItem(Entity entity, ItemStack stack, boolean allowMounts, boolean allowRiders)
    {
        return teleportEntityUsingItem(entity, UtilItemModular.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL), allowMounts, allowRiders);
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

        PositionUtils.adjustTargetPosition(target, entity);

        if (target.hasRotation)
        {
            entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, target.yaw, target.pitch);
        }

        return teleportEntity(entity, target.dPosX, target.dPosY, target.dPosZ, target.dimension, allowMounts, allowRiders);
    }

    public static Entity teleportEntity(Entity entityIn, double x, double y, double z, int dimDst, boolean allowMounts, boolean allowRiders)
    {
        if (entityIn == null || entityIn.getEntityWorld() == null || entityIn.getEntityWorld().isRemote) { return null; }
        if (allowMounts == false && entityIn.isRiding()) { return null; }
        if (allowRiders == false && entityIn.isBeingRidden()) { return null; }
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

        if (ridden)
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

        if (ridden)
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

        if (isOriginal)
        {
            teleported = EntityUtils.findEntityFromStackByUUID(teleported, uuidOriginal);
        }

        return teleported;
    }

    private static Entity teleportEntity(Entity entity, double x, double y, double z, int dimDst, boolean forceRecreate)
    {
        if (entity == null || entity.isEntityAlive() == false || canTeleportEntity(entity) == false || entity.getEntityWorld().isRemote)
        {
            return null;
        }

        // Post the event and check if the teleport should be allowed
        if (entity instanceof EntityLivingBase)
        {
            EnderTeleportEvent event = new EnderTeleportEvent((EntityLivingBase) entity, x, y, z, 0.0f);

            if (MinecraftForge.EVENT_BUS.post(event))
            {
                return null;
            }
        }

        // Sound and particles on the original location
        addTeleportSoundsAndParticles(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ);

        if (entity instanceof EntityLiving)
        {
            ((EntityLiving) entity).setMoveForward(0.0f);
            ((EntityLiving) entity).getNavigator().clearPathEntity();
        }

        if (entity.getEntityWorld().provider.getDimension() != dimDst)
        {
            entity = teleportEntityToDimension(entity, x, y, z, dimDst);
        }
        else
        {
            entity = teleportEntityInsideSameDimension(entity, x, y, z);
        }

        if (entity != null)
        {
            // Final position
            addTeleportSoundsAndParticles(entity.getEntityWorld(), x, y, z);
        }

        return entity;
    }

    private static Entity teleportEntityInsideSameDimension(Entity entity, double x, double y, double z)
    {
        // Load the chunk first
        entity.getEntityWorld().getChunkFromChunkCoords((int) Math.floor(x / 16D), (int) Math.floor(z / 16D));

        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        entity.setPositionAndUpdate(x, y, z);
        return entity;
    }

    private static Entity teleportEntityToDimension(Entity entity, double x, double y, double z, int dimDst)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer worldDst = server.getWorld(dimDst);

        teleportInProgress = true;

        if (worldDst == null || ForgeHooks.onTravelToDimension(entity, dimDst) == false)
        {
            teleportInProgress = false;
            return null;
        }

        teleportInProgress = false;

        // Load the chunk first
        int chunkX = (int) Math.floor(x / 16D);
        int chunkZ = (int) Math.floor(z / 16D);
        ChunkLoading.getInstance().loadChunkForcedWithModTicket(dimDst, chunkX, chunkZ, 10);

        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            World worldOld = player.getEntityWorld();
            DummyTeleporter teleporter = new DummyTeleporter(worldDst);

            player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
            server.getPlayerList().transferPlayerToDimension(player, dimDst, teleporter);

            // See PlayerList#transferEntityToWorld()
            if (worldOld.provider.getDimension() == 1)
            {
                worldDst.spawnEntity(player);
            }

            // Teleporting FROM The End, remove the boss bar that would otherwise get stuck on
            if (worldOld.provider instanceof WorldProviderEnd)
            {
                removeDragonBossBarHack(player, (WorldProviderEnd) worldOld.provider);
            }

            player.setPositionAndUpdate(x, y, z);
            worldDst.updateEntityWithOptionalForce(player, false);
            player.addExperienceLevel(0);
            player.setPlayerHealthUpdated();
            // TODO update food level?
        }
        else
        {
            WorldServer worldSrc = (WorldServer) entity.getEntityWorld();

            // FIXME ugly special case to prevent the chest minecart etc from duping items
            if (entity instanceof EntityMinecartContainer)
            {
                ((EntityMinecartContainer) entity).setDropItemsWhenDead(false);
            }

            worldSrc.removeEntity(entity);
            entity.isDead = false;
            worldSrc.updateEntityWithOptionalForce(entity, false);

            Entity entityNew = EntityList.newEntity(entity.getClass(), worldDst);

            if (entityNew != null)
            {
                EntityUtils.copyDataFromOld(entityNew, entity);
                entityNew.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);

                boolean flag = entityNew.forceSpawn;
                entityNew.forceSpawn = true;
                worldDst.spawnEntity(entityNew);
                entityNew.forceSpawn = flag;

                worldDst.updateEntityWithOptionalForce(entityNew, false);
                entity.isDead = true;

                worldSrc.resetUpdateEntityTick();
                worldDst.resetUpdateEntityTick();
            }

            entity = entityNew;
        }

        return entity;
    }

    private static void removeDragonBossBarHack(EntityPlayerMP player, WorldProviderEnd provider)
    {
        // Somewhat ugly way to clear the Boss Info stuff when teleporting FROM The End
        DragonFightManager manager = provider.getDragonFightManager();

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
                EnderUtilities.logger.warn("TP: Failed to get DragonFightManager#bossInfo");
            }
        }
    }

    private static class DummyTeleporter extends Teleporter
    {
        public DummyTeleporter(WorldServer world)
        {
            super(world);
        }

        @Override
        public boolean makePortal(Entity entityIn)
        {
            return true;
        }

        @Override
        public boolean placeInExistingPortal(Entity entityIn, float rotationYaw)
        {
            return true;
        }

        @Override
        public void removeStalePortalLocations(long worldTime)
        {
            // NO-OP
        }

        @Override
        public void placeInPortal(Entity entityIn, float rotationYaw)
        {
        }
    }
}
