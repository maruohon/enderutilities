package fi.dy.masa.enderutilities.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.setup.Registry;

public class EntityUtils
{
    //public static final byte YAW_TO_DIRECTION[] = {3, 4, 2, 5};

    public static enum LeftRight
    {
        LEFT,
        RIGHT;

        public LeftRight opposite()
        {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    public static EnumFacing getLookingDirection(Entity entity)
    {
        if (entity.rotationPitch < -45)
        {
            return EnumFacing.UP;
        }

        if (entity.rotationPitch > 45)
        {
            return EnumFacing.DOWN;
        }

        return getHorizontalLookingDirection(entity);
    }

    public static EnumFacing getHorizontalLookingDirection(Entity entity)
    {
        //float yaw = (entity.rotationYaw % 360.0f + 360.0f) % 360.0f;
        //System.out.printf("axis: " + ForgeDirection.getOrientation(YAW_TO_DIRECTION[MathHelper.floor_double((entity.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3]) + "\n");
        //return ForgeDirection.getOrientation(YAW_TO_DIRECTION[MathHelper.floor_double((entity.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3]);
        return EnumFacing.fromAngle(entity.rotationYaw);
    }

    public static EnumFacing getVerticalLookingDirection(Entity entity)
    {
        return entity.rotationPitch > 0 ? EnumFacing.DOWN : EnumFacing.UP;
    }

    public static EnumFacing getClosestLookingDirection(Entity entity)
    {
        //float yaw = (entity.rotationYaw % 360.0f + 360.0f) % 360.0f;
        //int yawAxis = MathHelper.floor_double((double)(entity.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3;
        //float yawAxisMiddle = yawAxis * 360.0f / 4.0f;

        //System.out.printf("yaw: %.1f yawAxis: %d yawAxisMiddle: %.1f pitch: %.1f", yaw, yawAxis, yawAxisMiddle, entity.rotationPitch);
        //if (entity.rotationPitch > (Math.abs(yawAxisMiddle - yaw) + 45.0f))
        if (entity.rotationPitch > 60.0f)
        {
            //System.out.printf(" axis: " + ForgeDirection.DOWN + "\n");
            return EnumFacing.DOWN;
        }
        //else if (-entity.rotationPitch > (Math.abs(yawAxisMiddle - yaw) + 45.0f))
        else if (-entity.rotationPitch > 60.0f)
        {
            //System.out.printf(" axis: " + ForgeDirection.UP + "\n");
            return EnumFacing.UP;
        }

        //System.out.printf(" axis: " + ForgeDirection.getOrientation(YAW_TO_DIRECTION[yawAxis]) + "\n");
        //return ForgeDirection.getOrientation(YAW_TO_DIRECTION[yawAxis]);
        return getHorizontalLookingDirection(entity);
    }

    public static EnumFacing getClosesLookingDirectionPlanarized(Entity entity, boolean usePitch)
    {
        if (usePitch == true)
        {
            EnumFacing facing = getClosestLookingDirection(entity);

            if (facing == EnumFacing.UP)
            {
                facing = EnumFacing.NORTH;
            }
            else if (facing == EnumFacing.DOWN)
            {
                facing = EnumFacing.SOUTH;
            }

            return facing;
        }

        return getHorizontalLookingDirection(entity);
    }

    public static EnumFacing getClosestLookingDirectionNotOnAxis(Entity entity, EnumFacing notOnAxis)
    {
        EnumFacing facing = getClosestLookingDirection(entity);
        if (facing == notOnAxis || facing.getOpposite() == notOnAxis)
        {
            if (notOnAxis == EnumFacing.UP || notOnAxis == EnumFacing.DOWN)
            {
                facing = getHorizontalLookingDirection(entity);
            }
            else
            {
                facing = getVerticalLookingDirection(entity);
            }
        }

        return facing;
    }

    /**
     * Return whether the entity is looking to the left or to the right of the given axis.
     * The axis is the one coming towards the entity from the source location.
     */
    public static LeftRight getLookLeftRight(Entity entity, EnumFacing axis)
    {
        float yaw = (entity.rotationYaw % 360.0f + 360.0f) % 360.0f;
        LeftRight result;

        // South = 0 degrees
        switch(axis)
        {
            case NORTH:
                result = yaw <= 180.0f ? LeftRight.RIGHT : LeftRight.LEFT;
                break;
            case SOUTH:
                result = yaw > 180.0f ? LeftRight.RIGHT : LeftRight.LEFT;
                break;
            case WEST:
                result = (yaw >= 90.0f && yaw <= 270.0f) ? LeftRight.LEFT : LeftRight.RIGHT;
                break;
            case EAST:
                result = (yaw < 90.0f || yaw > 270.0f) ? LeftRight.LEFT : LeftRight.RIGHT;
                break;
            default:
                result = LeftRight.LEFT;
        }

        //System.out.printf("yaw: %.1f axis: %s look: %s\n", yaw, axis.toString(), result.toString());
        return result;
    }

    /**
     * Get the (non-optimized) transformations to transform a plane defined by the
     * vectors p1Up and p1Right to match the other plane defined by p2Up and p2Right.
     */
    public static List<EnumFacing> getTransformationsToMatchPlanes(EnumFacing p1Up, EnumFacing p1Right, EnumFacing p2Up, EnumFacing p2Right)
    {
        List<EnumFacing> list = new ArrayList<EnumFacing>();
        EnumFacing tmp1 = p1Up;
        EnumFacing rot = p1Up;

        // First get the rotations to match p1Up to p2Up
        if (p2Up == p1Right)
        {
            rot = BlockPosEU.getRotation(p1Up, p1Right.getOpposite());
            //System.out.printf("TR right - p1Up: %s p2Up: %s p1Right: %s p2Right: %s rot: %s\n", p1Up, p2Up, p1Right, p2Right, rot);
            list.add(rot);
            p1Right = BlockPosEU.getRotation(p1Right, rot);
            p1Up = p2Up;
        }
        else if (p2Up == p1Right.getOpposite())
        {
            rot = BlockPosEU.getRotation(p1Up, p1Right);
            //System.out.printf("TR left - p1Up: %s p2Up: %s p1Right: %s p2Right: %s rot: %s\n", p1Up, p2Up, p1Right, p2Right, rot);
            list.add(rot);
            p1Right = BlockPosEU.getRotation(p1Right, rot);
            p1Up = p2Up;
        }
        else
        {
            for (int i = 0; i < 4; i++)
            {
                if (tmp1 == p2Up)
                {
                    break;
                }

                //System.out.printf("TR loop 1 - p1Right %s ", p1Right);
                tmp1 = BlockPosEU.getRotation(tmp1, p1Right);
                list.add(p1Right);
            }
        }

        //System.out.printf("\np1Right: %s p2Right: %s\n", p1Right, p2Right);
        p1Up = tmp1;
        tmp1 = p1Right;

        // Then get the rotations to match p1Right to p2Right, rotating around p2Up
        for (int i = 0; i < 4; i++)
        {
            if (tmp1 == p2Right)
            {
                break;
            }

            //System.out.printf("TR loop 2: %s ", p2Up);
            tmp1 = BlockPosEU.getRotation(tmp1, p2Up);
            list.add(p2Up);
        }

        //System.out.printf("\n");
        return list;
    }

    public static <T extends Entity> T findEntityByUUID(List<T> list, UUID uuid)
    {
        if (uuid == null)
        {
            return null;
        }

        for (T entity : list)
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
        /*Entity ent;

        for (ent = entity; ent.isRiding() == true; ent = ent.getRidingEntity())
        {
        }

        return ent;*/
        return entity.getLowestRidingEntity();
    }

    public static Entity getTopEntity(Entity entity)
    {
        Entity ent;

        for (ent = entity; ent.isBeingRidden() == true; ent = ent.getPassengers().get(0))
        {
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

        for (ent = entity; ent != null; ent = ent.getRidingEntity())
        {
            if (ent instanceof EntityPlayer)
            {
                return true;
            }
        }

        for (ent = entity; ent.isBeingRidden() == true; ent = ent.getPassengers().get(0))
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

        for (ent = entityInStack; ent != null; ent = ent.getRidingEntity())
        {
            if (ent == entity)
            {
                return true;
            }
        }

        for (ent = entityInStack; ent.isBeingRidden() == true; ent = ent.getPassengers().get(0))
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

        for (ent = entity; ent != null; ent = ent.getRidingEntity())
        {
            if (blacklist.contains(ent.getClass().getSimpleName()) == true)
            {
                return true;
            }
        }

        for (ent = entity; ent.isBeingRidden() == true; ent = ent.getPassengers().get(0))
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
        if (entity != null && entity.isBeingRidden() == true)
        {
            entity.getPassengers().get(0).dismountRidingEntity();
            return true;
        }

        return false;
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
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        int mX = MathHelper.floor_double(bb.minX);
        int mY = MathHelper.floor_double(bb.minY);
        int mZ = MathHelper.floor_double(bb.minZ);

        for (int y2 = mY; y2 < bb.maxY; y2++)
        {
            for (int x2 = mX; x2 < bb.maxX; x2++)
            {
                for (int z2 = mZ; z2 < bb.maxZ; z2++)
                {
                    if (world.getBlockState(new BlockPos(x2, y2, z2)).getBlock() == block)
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
            boolean canDespawn = ((living instanceof EntityMob) && living.isNonBoss() == true) ||
                                  (living instanceof EntityWaterMob) ||
                                  ((living instanceof EntityTameable) && ((EntityTameable)living).isTamed() == false);

            if (canDespawn == false)
            {
                Method method = ReflectionHelper.findMethod(EntityLiving.class, living, new String[] {"canDespawn", "func_70692_ba", "C"});
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
                living.enablePersistence();
                // FIXME 1.9: add back after there is a sound registry
                //living.worldObj.playSoundAtEntity(living, Reference.MOD_ID + ":jailer", 0.8f, 1.2f);

                return true;
            }
        }

        return false;
    }

    public static boolean spawnEnderCrystal(World world, BlockPos pos)
    {
        // Only allow the activation to happen in The End
        if (world == null || world.provider == null)
        {
            return false;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // The item must be right clicked on an obsidian block on top of the obsidian pillars
        if (world.provider.getDimension() == 1 && world.getBlockState(pos).getBlock() == Blocks.obsidian)
        {
            double r = 1.0d;
            // Check that there aren't already Ender Crystals nearby
            List<EntityEnderCrystal> entities = world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r));
            if (entities.isEmpty() == false)
            {
                return false;
            }

            // Check that we have a pillar of obsidian below the bedrock block (at least 3x3 wide and 6 tall)
            for (int by = y - 5; by <= y; ++by)
            {
                for (int bx = x - 1; bx <= x + 1; ++bx)
                {
                    for (int bz = z - 1; bz <= z + 1; ++bz)
                    {
                        if (world.getBlockState(new BlockPos(bx, by, bz)).getBlock() != Blocks.obsidian)
                        {
                            return false;
                        }
                    }
                }
            }

            // Everything ok, create an explosion and then spawn a new Ender Crystal
            world.createExplosion(null, x + 0.5d, y + 1.0d, z + 0.5d, 10.0f, true);
            EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
            entityendercrystal.setLocationAndAngles(x + 0.5d, (double)y + 1.0d, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
            world.spawnEntityInWorld(entityendercrystal);

            return true;
        }
        // Allow spawning decorative Ender Crystals in other dimensions.
        // They won't be valid for Ender Charge, and spawning them doesn't create an explosion or have block requirements.
        else if (world.provider.getDimension() != 1)
        {
            EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
            entityendercrystal.setLocationAndAngles(x + 0.5d, y + 2.0d, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
            world.spawnEntityInWorld(entityendercrystal);
            return true;
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

    /**
     * Drops/spawns EntityItems to the world from the provided ItemStack stack.
     * The number of items dropped is dictated by the parameter amount.
     * If amountOverride > 0, then stack is only the ItemStack template and amountOverride is the number of items that will be dropped.
     * (Thus amountOverride can also be larger than stack.stackSize.)
     * If amountOverride <= 0, then stack.stackSize is used for the amount to be dropped.
     * @param worldIn
     * @param pos
     * @param stack The template ItemStack of the dropped items.
     * @param amountOverride Amount of items to drop. If amountOverride is > 0, stack is only a template. If <= 0, stack.stackSize is used.
     * @param dropFullStacks If false, then the stackSize of the the spawned EntityItems is randomized between 10..32
     */
    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks)
    {
        if (stack == null)
        {
            return;
        }

        double xr = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getX();
        double yr = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getY();
        double zr = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getZ();
        double motionScale = 0.04d;

        int amount = stack.stackSize;
        int max = stack.getMaxStackSize();
        int num = max;

        if (amountOverride > 0)
        {
            amount = amountOverride;
        }

        while (amount > 0)
        {
            if (dropFullStacks == false)
            {
                num = Math.min(worldIn.rand.nextInt(23) + 10, max);
            }

            num = Math.min(num, amount);
            ItemStack dropStack = stack.copy();
            dropStack.stackSize = num;
            amount -= num;

            EntityItem entityItem = new EntityItem(worldIn, xr, yr, zr, dropStack);
            entityItem.motionX = worldIn.rand.nextGaussian() * motionScale;
            entityItem.motionY = worldIn.rand.nextGaussian() * motionScale + 0.3d;
            entityItem.motionZ = worldIn.rand.nextGaussian() * motionScale;

            worldIn.spawnEntityInWorld(entityItem);
        }
    }
}
