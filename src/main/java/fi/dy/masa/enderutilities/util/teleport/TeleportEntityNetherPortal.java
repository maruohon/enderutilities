package fi.dy.masa.enderutilities.util.teleport;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import fi.dy.masa.enderutilities.util.BlockPosEU;

public class TeleportEntityNetherPortal
{
    /** The axis along which the destination portal aligns. Is either EAST or SOUTH. */
    public ForgeDirection portalAxis;
    /** The side of the portal the player gets placed to */
    public ForgeDirection teleportSide;
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
        this.portalAxis = ForgeDirection.UNKNOWN;
        this.teleportSide = ForgeDirection.UNKNOWN;
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
                    if (world.getBlock(x, y, z) == Blocks.portal)
                    {
                        while (world.getBlock(x, y - 1, z) == Blocks.portal)
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
        if (world.getBlock(this.portalPosX - 1, this.portalPosY, this.portalPosZ) == Blocks.portal || world.getBlock(this.portalPosX + 1, this.portalPosY, this.portalPosZ) == Blocks.portal)
        {
            this.portalAxis = ForgeDirection.EAST;
        }
        else if (world.getBlock(this.portalPosX, this.portalPosY, this.portalPosZ - 1) == Blocks.portal || world.getBlock(this.portalPosX, this.portalPosY, this.portalPosZ + 1) == Blocks.portal)
        {
            this.portalAxis = ForgeDirection.SOUTH;
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
            ForgeDirection dirSide = this.portalAxis.getRotation(ForgeDirection.UP);
            ForgeDirection dirPortal = this.portalAxis;

            // Get the axis where there are more portal blocks (if only 2 wide portal)
            if (world.getBlock(this.portalPosX + dirPortal.offsetX, this.portalPosY, this.portalPosZ + dirPortal.offsetZ) != Blocks.portal)
            {
                dirPortal = dirPortal.getOpposite();
            }

            List<BlockPosEU> list = new ArrayList<BlockPosEU>(8);
            list.add(new BlockPosEU(this.portalPosX + dirSide.offsetX, this.portalPosY - 1, this.portalPosZ + dirSide.offsetZ));
            list.add(new BlockPosEU(this.portalPosX + dirSide.offsetX, this.portalPosY - 2, this.portalPosZ + dirSide.offsetZ));
            list.add(new BlockPosEU(this.portalPosX + dirSide.offsetX + dirPortal.offsetX, this.portalPosY - 1, this.portalPosZ + dirSide.offsetZ + dirPortal.offsetZ));
            list.add(new BlockPosEU(this.portalPosX + dirSide.offsetX + dirPortal.offsetX, this.portalPosY - 2, this.portalPosZ + dirSide.offsetZ + dirPortal.offsetZ));
            list.add(new BlockPosEU(this.portalPosX - dirSide.offsetX, this.portalPosY - 1, this.portalPosZ - dirSide.offsetZ));
            list.add(new BlockPosEU(this.portalPosX - dirSide.offsetX, this.portalPosY - 2, this.portalPosZ - dirSide.offsetZ));
            list.add(new BlockPosEU(this.portalPosX - dirSide.offsetX + dirPortal.offsetX, this.portalPosY - 1, this.portalPosZ - dirSide.offsetZ + dirPortal.offsetZ));
            list.add(new BlockPosEU(this.portalPosX - dirSide.offsetX + dirPortal.offsetX, this.portalPosY - 2, this.portalPosZ - dirSide.offsetZ + dirPortal.offsetZ));

            // Try to find a suitable position on either side of the portal
            for (BlockPosEU pos : list)
            {
                if (World.doesBlockHaveSolidTopSurface(world, pos.posX, pos.posY, pos.posZ) == true
                    && world.isAirBlock(pos.posX, pos.posY + 1, pos.posZ) && world.isAirBlock(pos.posX, pos.posY + 2, pos.posZ))
                {
                    this.entityPosX = pos.posX + 0.5d;
                    this.entityPosY = pos.posY + 1.5d;
                    this.entityPosZ = pos.posZ + 0.5d;
                    return;
                }
            }

            // No suitable positions found, try to add a solid block to teleport to
            for (BlockPosEU pos : list)
            {
                if (world.isAirBlock(pos.posX, pos.posY, pos.posZ) == true
                    && world.isAirBlock(pos.posX, pos.posY + 1, pos.posZ) && world.isAirBlock(pos.posX, pos.posY + 2, pos.posZ))
                {
                    world.setBlock(pos.posX, pos.posY, pos.posZ, Blocks.cobblestone, 0, 3);
                    this.entityPosX = pos.posX + 0.5d;
                    this.entityPosY = pos.posY + 1.5d;
                    this.entityPosZ = pos.posZ + 0.5d;
                    return;
                }
            }

            // No suitable positions found on either side of the portal, what should we do here??
            // Let's just stick the player to wherever he ends up on the side of the portal for now...
            this.entityPosX = this.portalPosX + dirSide.offsetX + 0.5d;
            this.entityPosY = this.portalPosY + 0.5d;
            this.entityPosZ = this.portalPosZ + dirSide.offsetZ + 0.5d;
        }
    }
}
