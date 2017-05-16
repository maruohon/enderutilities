package fi.dy.masa.enderutilities.util;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.effects.Sounds;
import fi.dy.masa.enderutilities.entity.ai.EntityAIDummyBlockerTask;
import fi.dy.masa.enderutilities.registry.BlackLists;
import fi.dy.masa.enderutilities.util.MethodHandleUtils.UnableToFindMethodHandleException;

public class EntityUtils
{
    //public static final byte YAW_TO_DIRECTION[] = {3, 4, 2, 5};
    private static MethodHandle methodHandle_Entity_copyDataFromOld;
    private static MethodHandle methodHandle_EntityLiving_canDespawn;

    static
    {
        try
        {
            methodHandle_Entity_copyDataFromOld = MethodHandleUtils.getMethodHandleVirtual(
                    Entity.class, new String[] { "func_180432_n", "copyDataFromOld" }, Entity.class);
            methodHandle_EntityLiving_canDespawn = MethodHandleUtils.getMethodHandleVirtual(
                    EntityLiving.class, new String[] { "func_70692_ba", "canDespawn" });
        }
        catch (UnableToFindMethodHandleException e)
        {
            EnderUtilities.logger.error("EntityUtils: Failed to get a MethodHandle for Entity#copyDataFromOld() or for EntityLiving#canDespawn()", e);
        }
    }

    public static RayTraceResult getRayTraceFromPlayer(World world, EntityPlayer player, boolean useLiquids)
    {
        Vec3d vec3d = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        float f2 = MathHelper.cos(player.rotationYaw * -0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(player.rotationYaw * -0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(player.rotationPitch * -0.017453292F);
        double f5 = MathHelper.sin(player.rotationPitch * -0.017453292F);
        double f6 = f3 * f4;
        double f7 = f2 * f4;
        double reach = 5.0D;

        if (player instanceof EntityPlayerMP)
        {
            reach = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
        }

        Vec3d vec3d1 = vec3d.addVector(f6 * reach, f5 * reach, f7 * reach);

        return world.rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
    }

    public static Vec3d getEyesVec(Entity entity)
    {
        return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
    }

    /**
     * Returns the index of the BB in the given list that the given entity is currently looking at.
     * @return the list index of the pointed box, or -1 of no hit was detected
     */
    public static int getPointedBox(Entity entity, double reach, List<AxisAlignedBB> boxes, float partialTicks)
    {
        Vec3d eyesVec = entity.getPositionEyes(partialTicks);
        Vec3d lookVec = entity.getLook(partialTicks);

        return getPointedBox(eyesVec, lookVec, reach, boxes);
    }

    /**
     * Returns the index of the BB in the given list that the given vectors are currently pointing at.
     * @return the list index of the pointed box, or -1 of no hit was detected
     */
    public static int getPointedBox(Vec3d eyesVec, Vec3d lookVec, double reach, List<AxisAlignedBB> boxes)
    {
        Vec3d lookEndVec = eyesVec.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);
        //AxisAlignedBB box = null;
        //Vec3d hitVec = null;
        double distance = reach;
        int index = -1;

        for (int i = 0; i < boxes.size(); ++i)
        {
            AxisAlignedBB bb = boxes.get(i);
            RayTraceResult rayTrace = bb.calculateIntercept(eyesVec, lookEndVec);

            if (bb.isVecInside(eyesVec))
            {
                if (distance >= 0.0D)
                {
                    //box = bb;
                    //hitVec = rayTrace == null ? eyesVec : rayTrace.hitVec;
                    distance = 0.0D;
                    index = i;
                }
            }
            else if (rayTrace != null)
            {
                double distanceTmp = eyesVec.distanceTo(rayTrace.hitVec);

                if (distanceTmp < distance)
                {
                    //box = bb;
                    //hitVec = rayTrace.hitVec;
                    distance = distanceTmp;
                    index = i;
                }
            }
        }

        return index;
    }

    /**
     * Returns the index of the BB in the given list that the given entity is currently looking at.
     * @return the list index of the pointed box, or null of no hit was detected
     */
    public static <T> T getPointedBox(Entity entity, double reach, Map<T, AxisAlignedBB> boxes, float partialTicks)
    {
        Vec3d eyesVec = entity.getPositionEyes(partialTicks);
        Vec3d lookVec = entity.getLook(partialTicks);

        return getPointedBox(eyesVec, lookVec, reach, boxes);
    }

    /**
     * Returns the index of the BB in the given list that the given vectors are currently pointing at.
     * @return the list index of the pointed box, or null of no hit was detected
     */
    @Nullable
    public static <T> T getPointedBox(Vec3d eyesVec, Vec3d lookVec, double reach, Map<T, AxisAlignedBB> boxMap)
    {
        Vec3d lookEndVec = eyesVec.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);
        double distance = reach;
        T key = null;

        for (Map.Entry<T, AxisAlignedBB> entry : boxMap.entrySet())
        {
            AxisAlignedBB bb = entry.getValue();
            RayTraceResult rayTrace = bb.calculateIntercept(eyesVec, lookEndVec);

            if (bb.isVecInside(eyesVec))
            {
                if (distance >= 0.0D)
                {
                    distance = 0.0D;
                    key = entry.getKey();
                }
            }
            else if (rayTrace != null)
            {
                double distanceTmp = eyesVec.distanceTo(rayTrace.hitVec);

                if (distanceTmp < distance)
                {
                    distance = distanceTmp;
                    key = entry.getKey();
                }
            }
        }

        return key;
    }

    public static boolean isHoldingItem(EntityLivingBase entity, Item item)
    {
        return getHeldItemOfType(entity, item).isEmpty() == false;
    }

    public static ItemStack getHeldItemOfType(EntityLivingBase entity, Item item)
    {
        ItemStack stack = entity.getHeldItemMainhand();

        if (stack.isEmpty() == false && stack.getItem() == item)
        {
            return stack;
        }

        stack = entity.getHeldItemOffhand();

        if (stack.isEmpty() == false && stack.getItem() == item)
        {
            return stack;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isHoldingItemOfType(EntityLivingBase entity, Class<?> clazz)
    {
        return getHeldItemOfType(entity, clazz).isEmpty() == false;
    }

    public static ItemStack getHeldItemOfType(EntityLivingBase entity, Class<?> clazz)
    {
        ItemStack stack = entity.getHeldItemMainhand();

        if (stack.isEmpty() == false)
        {
            if (clazz.isAssignableFrom(stack.getItem().getClass()))
            {
                return stack;
            }
        }

        stack = entity.getHeldItemOffhand();

        if (stack.isEmpty() == false)
        {
            if (clazz.isAssignableFrom(stack.getItem().getClass()))
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
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
        if (usePitch)
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
            if (entity.getUniqueID().equals(uuid))
            {
                return entity;
            }
        }

        return null;
    }

    public static Entity findEntityFromStackByUUID(Entity entityInStack, UUID uuid)
    {
        return findEntityFromStackByUUID(entityInStack, uuid, true);
    }

    private static Entity findEntityFromStackByUUID(Entity entityInStack, UUID uuid, boolean startFromBottom)
    {
        // TODO useless check?
        if (entityInStack == null)
        {
            return null;
        }

        if (uuid.equals(entityInStack.getUniqueID()))
        {
            return entityInStack;
        }

        if (startFromBottom)
        {
            entityInStack = getBottomEntity(entityInStack);
        }

        if (entityInStack.isBeingRidden())
        {
            List<Entity> passengers = entityInStack.getPassengers();

            for (Entity passenger : passengers)
            {
                if (uuid.equals(passenger.getUniqueID()))
                {
                    return passenger;
                }

                entityInStack = findEntityFromStackByUUID(passenger, uuid, false);

                if (entityInStack != null)
                {
                    return entityInStack;
                }
            }
        }

        return null;
    }

    public static Entity getBottomEntity(Entity entity)
    {
        return entity.getLowestRidingEntity();
    }

    /**
     * Gets the top-most riding entity. Note: Always gets the first rider (ie. get(0))!
     */
    public static Entity getTopEntity(Entity entity)
    {
        while (entity.isBeingRidden())
        {
            entity = entity.getPassengers().get(0);
        }

        return entity;
    }

    /**
     * Check if the Entity <b>rider</b> is among the riders of the other Entity <b>target</b>.
     * This check is done recursively to all the riders of <b>target</b>.
     */
    public static boolean isEntityRiddenBy(Entity target, Entity rider)
    {
        if (target == null || rider == null)
        {
            return false;
        }

        if (target.isBeingRidden())
        {
            List<Entity> passengers = target.getPassengers();

            for (Entity passenger : passengers)
            {
                if (passenger.equals(rider) || isEntityRiddenBy(passenger, rider))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if there are any players in this entity 'stack' (pile of mounted entities)
     * @param entity
     * @return
     */
    public static boolean doesEntityStackHavePlayers(Entity entity)
    {
        if (entity == null)
        {
            return false;
        }

        return doesEntityStackHavePlayers(entity, true);
    }

    private static boolean doesEntityStackHavePlayers(Entity entity, boolean startFromBottom)
    {
        if (entity instanceof EntityPlayer)
        {
            return true;
        }

        if (startFromBottom)
        {
            entity = getBottomEntity(entity);
        }

        if (entity.isBeingRidden())
        {
            List<Entity> passengers = entity.getPassengers();

            for (Entity passenger : passengers)
            {
                if (passenger instanceof EntityPlayer || doesEntityStackHavePlayers(passenger, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Recursively gets a list of all the entities in this "tower of mobs"
     */
    public static List<Entity> getAllEntitiesInStack(Entity entity)
    {
        List<Entity> entities = new ArrayList<Entity>();

        getAllEntitiesInStack(entity, entities, true);

        return entities;
    }

    private static void getAllEntitiesInStack(Entity entity, List<Entity> entities, boolean startFromBottom)
    {
        if (startFromBottom)
        {
            entity = getBottomEntity(entity);
        }

        entities.add(entity);

        if (entity.isBeingRidden())
        {
            for (Entity passenger : entity.getPassengers())
            {
                getAllEntitiesInStack(passenger, entities, false);
            }
        }
    }

    /**
     * Check if the given Entity <b>entity</b> is among the 'stack' of entities
     * that the second argument <b>entityInStack</b> is part of.
     * @param entity
     * @param entityInStack
     * @return
     */
    public static boolean doesEntityStackContainEntity(Entity entity, Entity entityInStack)
    {
        if (entity == null || entityInStack == null)
        {
            return false;
        }

        return doesEntityStackContainEntity(entity, entityInStack, true);
    }

    private static boolean doesEntityStackContainEntity(Entity entity, Entity entityInStack, boolean startFromBottom)
    {
        if (startFromBottom)
        {
            entityInStack = getBottomEntity(entityInStack);
        }

        if (entity == entityInStack)
        {
            return true;
        }

        if (entityInStack.isBeingRidden())
        {
            List<Entity> passengers = entityInStack.getPassengers();

            for (Entity passenger : passengers)
            {
                if (passenger == entity || doesEntityStackContainEntity(entity, passenger, false))
                {
                    return true;
                }
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
        return doesEntityStackHaveBlacklistedEntities(entity, true);
    }

    private static boolean doesEntityStackHaveBlacklistedEntities(Entity entity, boolean startFromBottom)
    {
        if (BlackLists.isEntityBlacklistedForTeleport(entity))
        {
            return true;
        }

        if (startFromBottom)
        {
            entity = getBottomEntity(entity);
        }

        if (entity.isBeingRidden())
        {
            List<Entity> passengers = entity.getPassengers();

            for (Entity passenger : passengers)
            {
                if (BlackLists.isEntityBlacklistedForTeleport(passenger) ||
                    doesEntityStackHaveBlacklistedEntities(passenger, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean unmountFirstRider(Entity entity)
    {
        if (entity != null && entity.isBeingRidden())
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
        return getPositionOfBlockEntityIsCollidingWith(world, entity, block) != null;
    }

    public static BlockPos getPositionOfBlockEntityIsCollidingWith(World world, Entity entity, Block block)
    {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        int minX = MathHelper.floor(bb.minX);
        int minY = MathHelper.floor(bb.minY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxX = MathHelper.floor(bb.maxX);
        int maxY = MathHelper.floor(bb.maxY);
        int maxZ = MathHelper.floor(bb.maxZ);

        for (int y2 = minY; y2 <= maxY; y2++)
        {
            for (int x2 = minX; x2 <= maxX; x2++)
            {
                for (int z2 = minZ; z2 <= maxZ; z2++)
                {
                    BlockPos pos = new BlockPos(x2, y2, z2);

                    if (world.getBlockState(pos).getBlock() == block)
                    {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    public static void copyDataFromOld(Entity newEntity, Entity oldEntity)
    {
        try
        {
            methodHandle_Entity_copyDataFromOld.invokeExact(newEntity, oldEntity);
        }
        catch (Throwable e)
        {
            EnderUtilities.logger.error("Error while trying invoke Entity#copyDataFromOld()", e);
        }
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
            boolean canDespawn = ((living instanceof EntityMob) && living.isNonBoss()) ||
                                  (living instanceof EntityWaterMob) ||
                                  ((living instanceof EntityTameable) && ((EntityTameable)living).isTamed() == false);

            if (canDespawn == false)
            {
                try
                {
                    canDespawn = (boolean) methodHandle_EntityLiving_canDespawn.invokeExact(living);
                }
                catch (Throwable t)
                {
                    EnderUtilities.logger.warn("Error while trying to invoke EntityLiving.canDespawn() on entity '{}' via a MethodHandle", living, t);
                }
            }

            if (canDespawn)
            {
                // Sets the persistenceRequired boolean
                living.enablePersistence();
                living.getEntityWorld().playSound(null, living.getPosition(), Sounds.JAILER, SoundCategory.MASTER, 0.8f, 1.2f);

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
        if (world.provider.getDimension() == 1 && world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
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
                        if (world.getBlockState(new BlockPos(bx, by, bz)).getBlock() != Blocks.OBSIDIAN)
                        {
                            return false;
                        }
                    }
                }
            }

            // Everything ok, create an explosion and then spawn a new Ender Crystal
            world.createExplosion(null, x + 0.5d, y + 1.0d, z + 0.5d, 3.0f, true);
            EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
            entityendercrystal.setLocationAndAngles(x + 0.5d, (double)y + 1.0d, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
            world.spawnEntity(entityendercrystal);

            return true;
        }
        // Allow spawning decorative Ender Crystals in other dimensions.
        // They won't be valid for Ender Charge, and spawning them doesn't create an explosion or have block requirements.
        else if (world.provider.getDimension() != 1)
        {
            EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
            entityendercrystal.setLocationAndAngles(x + 0.5d, y + 2.0d, z + 0.5d, world.rand.nextFloat() * 360.0f, 0.0f);
            world.spawnEntity(entityendercrystal);
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
    public static <T extends EntityAIBase> boolean addAITaskAfterTasks(EntityLiving living, EntityAIBase task, boolean replaceMatching, Class<T>[] afterTasks)
    {
        int priority = -1;
        Iterator<EntityAITaskEntry> taskEntryIter = living.tasks.taskEntries.iterator();

        while (taskEntryIter.hasNext())
        {
            EntityAITaskEntry taskEntry = taskEntryIter.next();
            //System.out.printf("addAITaskAfterTasks() - start - task: %s\n", taskEntry.action);

            // If this entity already has the same AI task
            if (taskEntry.action.getClass() == task.getClass())
            {
                // Replace the old matching task with the new instance
                if (replaceMatching)
                {
                    //System.out.printf("addAITaskAfterTasks() - task already present - replacing %s with %s\n", taskEntry.action, task);
                    int p = taskEntry.priority;
                    living.tasks.removeTask(taskEntry.action);
                    living.tasks.addTask(p, task);
                }
                //else System.out.printf("addAITaskAfterTasks() - task already present: %s (not replacing)\n", task);

                return true;
            }

            for (Class<T> clazz : afterTasks)
            {
                if (priority <= taskEntry.priority && taskEntry.action.getClass() == clazz)
                {
                    priority = taskEntry.priority + 1;
                }
            }
        }

        // Didn't find any matching AI tasks, insert ours as the highest priority task
        if (priority == -1)
        {
            //System.out.printf("addAITaskAfterTasks() - no matches for afterTasks\n");
            priority = 0;
        }

        //System.out.printf("addAITaskAfterTasks() - priority: %d\n", priority);

        living.tasks.addTask(priority, task);

        return true;
    }

    /**
     * Removes all AI tasks from the given EntityAITasks object
     * @param tasks the EntityAITasks object to remove the tasks from
     * @return true if at least some tasks were removed
     */
    public static boolean removeAllAITasks(EntityAITasks tasks)
    {
        List<EntityAITaskEntry> taskList = new ArrayList<EntityAITaskEntry>(tasks.taskEntries);

        for (EntityAITaskEntry taskEntry : taskList)
        {
            tasks.removeTask(taskEntry.action);
        }

        return taskList.isEmpty() == false;
    }

    /**
     * Adds a dummy AI task with the given priority and mutex bits, to block other AI tasks.
     * Also resets all other tasks in the same EntityAITasks object.
     * @param living
     * @param tasks
     * @param priority
     * @param mutexBits
     * @return true if the blocker task didn't exist yet
     */
    public static boolean addDummyAIBlockerTask(EntityLiving living, EntityAITasks tasks, int priority, int mutexBits)
    {
        List<EntityAITaskEntry> taskList = new ArrayList<EntityAITaskEntry>(tasks.taskEntries);
        boolean hadTask = false;

        // Removing and re-adding the tasks will remove them from the executing tasks
        for (EntityAITaskEntry taskEntry : taskList)
        {
            //taskEntry.action.resetTask();
            tasks.removeTask(taskEntry.action);

            if (taskEntry.action instanceof EntityAIDummyBlockerTask)
            {
                hadTask = true;
            }
        }

        tasks.addTask(priority, new EntityAIDummyBlockerTask(living, mutexBits));

        // The tickrate is hard coded to 3 at the moment, so this should get our dummy task into the executing tasks set
        tasks.onUpdateTasks();
        tasks.onUpdateTasks();
        tasks.onUpdateTasks();

        for (EntityAITaskEntry taskEntry : taskList)
        {
            tasks.addTask(taskEntry.priority, taskEntry.action);

            // The EntityAIFindEntityNearestPlayer task by default has mutex bits as 0,
            // so our dummy task wouldn't block it without this.
            if (taskEntry.action instanceof EntityAIFindEntityNearestPlayer)
            {
                taskEntry.action.setMutexBits(taskEntry.action.getMutexBits() | 0x80);
            }
        }

        return hadTask == false;
    }

    /**
     * Removes the (last found) dummy blocker AI task, if any
     * @param tasks
     */
    public static void removeDummyAIBlockerTask(EntityAITasks tasks)
    {
        EntityAIBase task = null;

        for (EntityAITaskEntry taskEntry : tasks.taskEntries)
        {
            if (taskEntry.action instanceof EntityAIDummyBlockerTask)
            {
                task = taskEntry.action;
            }

            // Restore the default mutex bits.
            // TODO: If modded mob tasks use this bit, then we should store the original value so we can restore it.
            if (taskEntry.action instanceof EntityAIFindEntityNearestPlayer)
            {
                taskEntry.action.setMutexBits(taskEntry.action.getMutexBits() & 0x7F);
            }
        }

        if (task != null)
        {
            tasks.removeTask(task);
        }
    }

    /**
     * Creates a new, identical instance of the given entity, by writing it to NBT
     * and then constructing a new entity based on that NBT data.
     * @param entityOld the original entity
     * @return the new entity, or null if the operation failed
     */
    @Nullable
    public static Entity recreateEntityViaNBT(Entity entityOld)
    {
        NBTTagCompound tag = new NBTTagCompound();
        Entity entityNew = null;

        if (entityOld.writeToNBTOptional(tag))
        {
            entityNew = EntityList.createEntityFromNBT(tag, entityOld.getEntityWorld());
        }

        return entityNew;
    }

    /**
     * Sets the held item, without playing the equip sound.
     * @param player
     * @param hand
     * @param stack
     */
    public static void setHeldItemWithoutEquipSound(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (hand == EnumHand.MAIN_HAND)
        {
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);
        }
        else if (hand == EnumHand.OFF_HAND)
        {
            player.inventory.offHandInventory.set(0, stack);
        }
    }

    /**
     * Drops/spawns EntityItems to the world from the provided ItemStack stack.
     * The number of items dropped is dictated by the parameter amountOverride.
     * If amountOverride > 0, then stack is only the ItemStack template and amountOverride is the number of items that will be dropped.
     * (Thus amountOverride can also be larger than stack.stackSize.)
     * If amountOverride <= 0, then stack.stackSize is used for the amount to be dropped.
     * @param worldIn
     * @param pos
     * @param stack The template ItemStack of the dropped items.
     * @param amountOverride Amount of items to drop. If amountOverride is > 0, stack is only a template. If <= 0, stack.stackSize is used.
     * @param dropFullStacks If false, then the stackSize of the the spawned EntityItems is randomized between 10..32
     * @param randomMotion If true, then a small amount on random motion is applied to the spawned entities
     */
    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks)
    {
        dropItemStacksInWorld(worldIn, pos, stack, amountOverride, dropFullStacks, true);
    }

    /**
     * Drops/spawns EntityItems to the world from the provided ItemStack stack.
     * The number of items dropped is dictated by the parameter amountOverride.
     * If amountOverride > 0, then stack is only the ItemStack template and amountOverride is the number of items that will be dropped.
     * (Thus amountOverride can also be larger than stack.stackSize.)
     * If amountOverride <= 0, then stack.stackSize is used for the amount to be dropped.
     * @param worldIn
     * @param pos
     * @param stack The template ItemStack of the dropped items.
     * @param amountOverride Amount of items to drop. If amountOverride is > 0, stack is only a template. If <= 0, stack.stackSize is used.
     * @param dropFullStacks If false, then the stackSize of the the spawned EntityItems is randomized between 10..32
     * @param randomMotion If true, then a small amount on random motion is applied to the spawned entities
     */
    public static void dropItemStacksInWorld(World worldIn, BlockPos pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
    {
        double x = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getX();
        double y = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getY();
        double z = worldIn.rand.nextFloat() * -0.5d + 0.75d + pos.getZ();

        dropItemStacksInWorld(worldIn, new Vec3d(x, y, z), stack, amountOverride, dropFullStacks, randomMotion);
    }

    /**
     * Drops/spawns EntityItems to the world from the provided ItemStack stack.
     * The number of items dropped is dictated by the parameter amountOverride.
     * If amountOverride > 0, then stack is only the ItemStack template and amountOverride is the number of items that will be dropped.
     * (Thus amountOverride can also be larger than stack.stackSize.)
     * If amountOverride <= 0, then stack.stackSize is used for the amount to be dropped.
     * @param worldIn
     * @param pos The exact position where the EntityItems will be spawned
     * @param stack The template ItemStack of the dropped items.
     * @param amountOverride Amount of items to drop. If amountOverride is > 0, stack is only a template. If <= 0, stack.stackSize is used.
     * @param dropFullStacks If false, then the stackSize of the the spawned EntityItems is randomized between 10..32
     * @param randomMotion If true, then a small amount on random motion is applied to the spawned entities
     */
    public static void dropItemStacksInWorld(World worldIn, Vec3d pos, ItemStack stack, int amountOverride, boolean dropFullStacks, boolean randomMotion)
    {
        int amount = stack.getCount();
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
            amount -= num;

            ItemStack dropStack = stack.copy();
            dropStack.setCount(num);

            EntityItem entityItem = new EntityItem(worldIn, pos.xCoord, pos.yCoord, pos.zCoord, dropStack);

            if (randomMotion)
            {
                double motionScale = 0.04d;
                entityItem.motionX = worldIn.rand.nextGaussian() * motionScale;
                entityItem.motionY = worldIn.rand.nextGaussian() * motionScale + 0.3d;
                entityItem.motionZ = worldIn.rand.nextGaussian() * motionScale;
            }
            else
            {
                entityItem.motionX = 0d;
                entityItem.motionY = 0d;
                entityItem.motionZ = 0d;
            }

            worldIn.spawnEntity(entityItem);
        }
    }

    public static enum LeftRight
    {
        LEFT,
        RIGHT;

        public LeftRight opposite()
        {
            return this == LEFT ? RIGHT : LEFT;
        }
    }
}
