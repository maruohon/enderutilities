package fi.dy.masa.enderutilities.util.teleport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TeleportEntityNetherPortal
{
    /** The axis along which the destination portal aligns. Is either EAST or SOUTH. */
    public EnumFacing portalAxis;
    /** The side of the portal the player gets placed to */
    public EnumFacing teleportSide;
    public int portalPosX;
    public int portalPosY;
    public int portalPosZ;
    public double entityPosX;
    public double entityPosY;
    public double entityPosZ;

    public TeleportEntityNetherPortal()
    {
        this.portalPosX = 0;
        this.portalPosY = 0;
        this.portalPosZ = 0;
        this.entityPosX = 0.0d;
        this.entityPosY = 0.0d;
        this.entityPosZ = 0.0d;
        this.portalAxis = EnumFacing.NORTH;
        this.teleportSide = EnumFacing.EAST;
    }

    /**
     * Teleports the entity to the given dimension (0 or -1). Finds an existing portal or creates a new portal.
     * @param entity
     * @param dimension Destination dimension (0 = Overworld or -1 = Nether)
     * @param idealX The requested ideal destination coordinates
     * @param idealY The requested ideal destination coordinates
     * @param idealZ The requested ideal destination coordinates
     * @param portalSearchRadius
     * @param placeInsidePortal true to place the entity inside the portal blocks, false to place the entity one block infront of the portal blocks
     * @return the instance of the teleported entity, or null in case of failure
     */
    public Entity travelToDimension(Entity entity, int dimension, double idealX, double idealY, double idealZ, int portalSearchRadius, boolean placeInsidePortal)
    {
        WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dimension);

        if (this.searchForExistingPortal(worldServer, idealX, idealY, idealZ, portalSearchRadius) == false)
        {
            double origX = entity.posX;
            double origY = entity.posY;
            double origZ = entity.posZ;
            entity.posX = idealX;
            entity.posY = idealY;
            entity.posZ = idealZ;
            worldServer.getDefaultTeleporter().makePortal(entity);
            entity.posX = origX;
            entity.posY = origY;
            entity.posZ = origZ;

            // Failed to create or find a portal. This shouldn't happen, but better to be sure.
            // The vanilla method tries to create a portal inside a 16 block radius of the player's position.
            if (this.searchForExistingPortal(worldServer, idealX, idealY, idealZ, 20) == false)
            {
                return null;
            }
        }

        this.getTeleportPosition(worldServer, placeInsidePortal);

        return TeleportEntity.teleportEntity(entity, this.entityPosX, this.entityPosY, this.entityPosZ, dimension, true, true);
    }

    /**
     * Searches for an existing Nether Portal inside the given radius around the ideal position.
     * If a valid portal is found, the coordinates of it are stored.
     * @param world
     * @param idealX
     * @param idealY
     * @param idealZ
     * @param searchRadius
     * @return true if an existing portal is found
     */
    public boolean searchForExistingPortal(World world, double idealX, double idealY, double idealZ, int searchRadius)
    {
        int x;
        int y;
        int z;
        double distance = -1.0D;

        for (x = (int)idealX - searchRadius; x <= idealX + searchRadius; ++x)
        {
            double dx = (double)x + 0.5D - idealX;

            for (z = (int)idealZ - searchRadius; z <= idealZ + searchRadius; ++z)
            {
                double dz = (double)z + 0.5D - idealZ;

                for (y = world.getActualHeight() - 1; y >= 0; --y)
                {
                    if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.portal)
                    {
                        while (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == Blocks.portal)
                        {
                            --y;
                        }

                        double dy = (double)y + 0.5D - idealY;
                        double tdist = dx * dx + dy * dy + dz * dz;

                        if (distance < 0.0D || tdist < distance)
                        {
                            distance = tdist;
                            this.portalPosX = x;
                            this.portalPosY = y;
                            this.portalPosZ = z;
                        }
                    }
                }
            }
        }

        // Portal block found
        if (distance >= 0.0D)
        {
            this.getPortalOrientation(world);
        }

        return distance >= 0.0D;
    }

    public void getPortalOrientation(World world)
    {
        BlockPos pos = new BlockPos(this.portalPosX, this.portalPosY, this.portalPosZ);
        if (world.getBlockState(pos.west()).getBlock() == Blocks.portal || world.getBlockState(pos.east()).getBlock() == Blocks.portal)
        {
            this.portalAxis = EnumFacing.EAST;
        }
        else if (world.getBlockState(pos.north()).getBlock() == Blocks.portal || world.getBlockState(pos.south()).getBlock() == Blocks.portal)
        {
            this.portalAxis = EnumFacing.SOUTH;
        }
    }

    public void getTeleportPosition(World world, boolean placeInsidePortal)
    {
        if (placeInsidePortal == true)
        {
            this.entityPosX = this.portalPosX + 0.5d;
            this.entityPosY = this.portalPosY + 0.5d;
            this.entityPosZ = this.portalPosZ + 0.5d;
        }
        else
        {
            EnumFacing dirSide = this.portalAxis.rotateY();
            EnumFacing dirPortal = this.portalAxis;

            // Get the axis where there are more portal blocks (if only 2 wide portal)
            BlockPos posTmp = new BlockPos(this.portalPosX + dirPortal.getFrontOffsetX(),
                                           this.portalPosY,
                                           this.portalPosZ + dirPortal.getFrontOffsetZ());
            if (world.getBlockState(posTmp).getBlock() != Blocks.portal)
            {
                dirPortal = dirPortal.getOpposite();
            }

            List<BlockPos> list = new ArrayList<BlockPos>();
            int xPos = this.portalPosX + dirSide.getFrontOffsetX();
            int zPos = this.portalPosZ + dirSide.getFrontOffsetZ();
            int xNeg = this.portalPosX - dirSide.getFrontOffsetX();
            int zNeg = this.portalPosZ - dirSide.getFrontOffsetZ();
            list.add(new BlockPos(xPos, this.portalPosY - 1, zPos));
            list.add(new BlockPos(xPos, this.portalPosY - 2, zPos));
            list.add(new BlockPos(xPos + dirPortal.getFrontOffsetX(), this.portalPosY - 1, zPos + dirPortal.getFrontOffsetZ()));
            list.add(new BlockPos(xPos + dirPortal.getFrontOffsetX(), this.portalPosY - 2, zPos + dirPortal.getFrontOffsetZ()));
            list.add(new BlockPos(xNeg, this.portalPosY - 1, zNeg));
            list.add(new BlockPos(xNeg, this.portalPosY - 2, zNeg));
            list.add(new BlockPos(xNeg + dirPortal.getFrontOffsetX(), this.portalPosY - 1, zNeg + dirPortal.getFrontOffsetZ()));
            list.add(new BlockPos(xNeg + dirPortal.getFrontOffsetX(), this.portalPosY - 2, zNeg + dirPortal.getFrontOffsetZ()));

            // Try to find a suitable position on either side of the portal
            for (BlockPos pos : list)
            {
                if (World.doesBlockHaveSolidTopSurface(world, pos) == true
                    && world.isAirBlock(pos.offset(EnumFacing.UP, 1)) && world.isAirBlock(pos.offset(EnumFacing.UP, 2)))
                {
                    this.entityPosX = pos.getX() + 0.5d;
                    this.entityPosY = pos.getY() + 1.5d;
                    this.entityPosZ = pos.getZ() + 0.5d;
                    return;
                }
            }

            // No suitable positions found, try to add a solid block to teleport to
            for (BlockPos pos : list)
            {
                if (world.isAirBlock(pos) == true
                    && world.isAirBlock(pos.offset(EnumFacing.UP, 1)) && world.isAirBlock(pos.offset(EnumFacing.UP, 2)))
                {
                    world.setBlockState(pos, Blocks.stone.getDefaultState(), 3);
                    this.entityPosX = pos.getX() + 0.5d;
                    this.entityPosY = pos.getY() + 1.5d;
                    this.entityPosZ = pos.getZ() + 0.5d;
                    return;
                }
            }

            // No suitable positions found on either side of the portal, what should we do here??
            // Let's just stick the player to wherever he ends up on the side of the portal for now...
            this.entityPosX = this.portalPosX + dirSide.getFrontOffsetX() + 0.5d;
            this.entityPosY = this.portalPosY + 0.5d;
            this.entityPosZ = this.portalPosZ + dirSide.getFrontOffsetZ() + 0.5d;
        }
    }
}
