package fi.dy.masa.enderutilities.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.Registry;

public class EntityUtils
{
    public static final byte YAW_TO_DIRECTION[] = {2, 5, 3, 4};

    public static ForgeDirection getLookingDirection(Entity entity)
    {
        if (entity.rotationPitch < -45)
        {
            return ForgeDirection.UP;
        }

        if (entity.rotationPitch > 45)
        {
            return ForgeDirection.DOWN;
        }

        return ForgeDirection.getOrientation(YAW_TO_DIRECTION[MathHelper.floor_double((double)(entity.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3]);
    }

    public static EntityPlayer findPlayerByUUID(UUID uuid)
    {
        if (uuid == null)
        {
            return null;
        }

        MinecraftServer mcs = MinecraftServer.getServer();
        if (mcs == null)
        {
            return null;
        }

        List<EntityPlayer> playerList = mcs.getConfigurationManager().playerEntityList;

        for (EntityPlayer player : playerList)
        {
            if (player.getUniqueID().equals(uuid) == true)
            {
                return player;
            }
        }

        return null;
    }

    public static Entity findEntityByUUID(List<Entity> list, UUID uuid)
    {
        if (uuid == null)
        {
            return null;
        }

        for (Entity entity : list)
        {
            if (entity.getUniqueID().equals(uuid) == true)
            {
                return entity;
            }
        }

        return null;
    }

    public static Entity getBottomEntity(Entity entity)
    {
        Entity ent;

        for (ent = entity; ent != null; ent = ent.ridingEntity)
        {
            if (ent.ridingEntity == null)
            {
                break;
            }
        }

        return ent;
    }

    public static Entity getTopEntity(Entity entity)
    {
        Entity ent;

        for (ent = entity; ent != null; ent = ent.riddenByEntity)
        {
            if (ent.riddenByEntity == null)
            {
                break;
            }
        }

        return ent;
    }

    /**
     * Check if there are any players in this entity 'stack' (pile of mounted entities)
     * @param entity
     * @return
     */
    public static boolean doesEntityStackHavePlayers(Entity entity)
    {
        Entity ent;

        for (ent = entity; ent != null; ent = ent.ridingEntity)
        {
            if (ent instanceof EntityPlayer)
            {
                return true;
            }
        }

        for (ent = entity.riddenByEntity; ent != null; ent = ent.riddenByEntity)
        {
            if (ent instanceof EntityPlayer)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the given entity is contained in the 'stack' that the second argument is part of
     * @param entity
     * @param entityInStack
     * @return
     */
    public static boolean doesEntityStackContainEntity(Entity entity, Entity entityInStack)
    {
        Entity ent;

        for (ent = entityInStack; ent != null; ent = ent.ridingEntity)
        {
            if (ent == entity)
            {
                return true;
            }
        }

        for (ent = entityInStack.riddenByEntity; ent != null; ent = ent.riddenByEntity)
        {
            if (ent == entity)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if there are any blacklisted entities in this entity 'stack' (pile of mounted entities)
     * @param entity
     * @return
     */
    public static boolean doesEntityStackHaveBlacklistedEntities(Entity entity)
    {
        List<String> blacklist = Registry.getTeleportBlacklist();
        Entity ent;

        for (ent = entity; ent != null; ent = ent.ridingEntity)
        {
            if (blacklist.contains(ent.getClass().getSimpleName()) == true)
            {
                return true;
            }
        }

        for (ent = entity.riddenByEntity; ent != null; ent = ent.riddenByEntity)
        {
            if (blacklist.contains(ent.getClass().getSimpleName()) == true)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean unmountRider(Entity entity)
    {
        if (entity != null && entity.riddenByEntity != null)
        {
            entity.riddenByEntity.mountEntity(null);
            return true;
        }

        return false;
    }

    public static boolean unmountRidden(Entity entity)
    {
        if (entity != null && entity.ridingEntity != null)
        {
            entity.mountEntity(null);
            return true;
        }

        return false;
    }

    /**
     * Unmounts the riding entity from the passed in entity.
     * Does not call Entity.mountEntity(null) but rather just sets he references to null.
     * @param entity
     */
    public static void unmountRiderSimple(Entity entity)
    {
        if (entity.riddenByEntity != null)
        {
            entity.riddenByEntity.ridingEntity = null;
        }

        entity.riddenByEntity = null;
    }

    /**
     * Returns true if the given entity's bounding box is inside the block space of a block
     * of the given type.
     * @param world
     * @param entity
     * @param block
     * @return true if the entity is inside the given block type
     */
    public static boolean isEntityCollidingWithBlockSpace(World world, Entity entity, Block block)
    {
        AxisAlignedBB bb = entity.boundingBox;
        int mX = MathHelper.floor_double(bb.minX);
        int mY = MathHelper.floor_double(bb.minY);
        int mZ = MathHelper.floor_double(bb.minZ);

        for (int y2 = mY; y2 < bb.maxY; y2++)
        {
            for (int x2 = mX; x2 < bb.maxX; x2++)
            {
                for (int z2 = mZ; z2 < bb.maxZ; z2++)
                {
                    if (world.getBlock(x2, y2, z2) == block)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds the persistenceRequired flag to entities, if they need it in order to not despawn.
     * The checks are probably at most accurate for vanilla entities.
     * @param livingBase
     * @return
     */
    public static boolean applyMobPersistence(EntityLiving living)
    {
        if (living.isNoDespawnRequired() == false)
        {
            boolean canDespawn = ((living instanceof EntityMob) && (living instanceof IBossDisplayData) == false) || (living instanceof EntityWaterMob) || ((living instanceof EntityTameable) && ((EntityTameable)living).isTamed() == false);

            if (canDespawn == false)
            {
                Method method = ReflectionHelper.findMethod(EntityLiving.class, living, new String[] {"canDespawn", "v", "func_70692_ba"});
                try
                {
                    Object o = method.invoke(living);
                    if (o instanceof Boolean)
                    {
                        canDespawn = ((Boolean)o).booleanValue();
                    }
                }
                catch (UnableToFindMethodException e)
                {
                    EnderUtilities.logger.error("Error while trying reflect EntityLiving.canDespawn() (UnableToFindMethodException)");
                    e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                    EnderUtilities.logger.error("Error while trying reflect EntityLiving.canDespawn() (InvocationTargetException)");
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    EnderUtilities.logger.error("Error while trying reflect EntityLiving.canDespawn() (IllegalAccessException)");
                    e.printStackTrace();
                }
            }

            if (canDespawn == true)
            {
                // Sets the persistenceRequired boolean
                living.func_110163_bv();
                living.worldObj.playSoundAtEntity(living, Reference.MOD_ID + ":jailer", 1.0f, 1.2f);

                return true;
            }
        }

        return false;
    }

    public static boolean spawnEnderCrystal(World world, int x, int y, int z)
    {
        // Only allow the activation to happen in The End
        if (world != null && world.provider != null)
        {
            // The item must be right clicked on the Bedrock block on top of the obsidian pillars
            if (world.provider.dimensionId == 1 && world.getBlock(x, y, z) == Blocks.bedrock)
            {
                // Check that there aren't already Ender Crystals nearby
                List<Entity> entities = world.getEntitiesWithinAABB(EntityEnderCrystal.class, AxisAlignedBB.getBoundingBox(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2));
                if (entities.isEmpty() == false)
                {
                    return false;
                }

                // Check that we have a pillar of obsidian below the bedrock block (at least 3x3 wide and 6 tall)
                for (int by = y - 6; by < y; ++by)
                {
                    for (int bx = x - 1; bx <= x + 1; ++bx)
                    {
                        for (int bz = z - 1; bz <= z + 1; ++bz)
                        {
                            if (world.getBlock(bx, by, bz) != Blocks.obsidian)
                            {
                                return false;
                            }
                        }
                    }
                }

                // Everything ok, create an explosion and then spawn a new Ender Crystal
                world.createExplosion(null, x + 0.5d, y + 1.0d, z + 0.5d, 10.0f, true);
                EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
                entityendercrystal.setLocationAndAngles(x + 0.5d, (double)y, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
                world.spawnEntityInWorld(entityendercrystal);

                return true;
            }
            // Allow spawning decorative Ender Crystals in other dimensions.
            // They won't be valid for Ender Charge, and spawning them doesn't create an explosion or have block requirements.
            else if (world.provider.dimensionId != 1)
            {
                EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
                entityendercrystal.setLocationAndAngles(x + 0.5d, y + 1.0d, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
                world.spawnEntityInWorld(entityendercrystal);
            }
        }

        return false;
    }

    /**
     * Adds the AI task 'task' to 'entity' after all the existing tasks of types in 'afterTasks' 
     * @param living
     * @param task
     * @param afterTasks
     * @return
     */
    public static boolean addAITaskAfterTasks(EntityLiving living, EntityAIBase task, boolean replaceMatching, Class<? extends EntityAIBase>[] afterTasks)
    {
        if (living == null)
        {
            return false;
        }

        int priority = -1;
        Iterator<EntityAITaskEntry> taskEntryIter = living.tasks.taskEntries.iterator();

        while (taskEntryIter.hasNext() == true)
        {
            EntityAITaskEntry taskEntry = taskEntryIter.next();

            // If this entity already has the same AI task
            if (taskEntry.action.getClass().equals(task.getClass()))
            {
                // Replace the old matching task with the new instance
                if (replaceMatching == true)
                {
                    int p = taskEntry.priority;
                    living.tasks.removeTask(taskEntry.action);
                    living.tasks.addTask(p, task);
                }

                return true;
            }

            for (Class<? extends EntityAIBase> clazz : afterTasks)
            {
                if (priority <= taskEntry.priority && clazz.equals(taskEntry.action.getClass()))
                {
                    priority = taskEntry.priority + 1;
                }
            }
        }

        // Didn't find any matching AI tasks, insert ours as the highest priority task
        if (priority == -1)
        {
            priority = 0;
        }

        living.tasks.addTask(priority, task);

        return true;
    }
}
