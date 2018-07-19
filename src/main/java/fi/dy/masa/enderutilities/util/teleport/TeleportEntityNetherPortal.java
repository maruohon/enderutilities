package fi.dy.masa.enderutilities.util.teleport;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TeleportEntityNetherPortal
{
    /** The axis along which the destination portal aligns. Is either EAST or SOUTH. */
    private EnumFacing portalAxis;
    private BlockPos portalPos;

    public TeleportEntityNetherPortal()
    {
        this.portalAxis = EnumFacing.NORTH;
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
    public Entity travelToDimension(Entity entity, int dimension, BlockPos idealPos, int portalSearchRadius, boolean placeInsidePortal)
    {
        WorldServer worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);

        if (worldServer == null)
        {
            return null;
        }

        if (this.searchForExistingPortal(worldServer, idealPos, portalSearchRadius) == false)
        {
            double origX = entity.posX;
            double origY = entity.posY;
            double origZ = entity.posZ;
            entity.posX = idealPos.getX() + 0.5d;
            entity.posY = idealPos.getY() + 0.5d;
            entity.posZ = idealPos.getZ() + 0.5d;
            worldServer.getDefaultTeleporter().makePortal(entity);
            entity.posX = origX;
            entity.posY = origY;
            entity.posZ = origZ;

            // Failed to create or find a portal. This shouldn't happen, but better to be sure.
            // The vanilla method tries to create a portal inside a 16 block radius of the player's position.
            if (this.searchForExistingPortal(worldServer, idealPos, 20) == false)
            {
                return null;
            }
        }

        BlockPos pos = this.findTeleportPosition(worldServer, placeInsidePortal);

        return TeleportEntity.teleportEntity(entity, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, dimension, true, true);
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
    public boolean searchForExistingPortal(World world, BlockPos idealPos, int searchRadius)
    {
        BlockPos foundPos = null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        double distance = -1.0D;

        for (int x = idealPos.getX() - searchRadius; x <= idealPos.getX() + searchRadius; ++x)
        {
            double dx = (double)x + 0.5D - idealPos.getX();

            for (int z = (int)idealPos.getZ() - searchRadius; z <= idealPos.getZ() + searchRadius; ++z)
            {
                double dz = (double)z + 0.5D - idealPos.getZ();

                for (int y = world.getActualHeight() - 1; y >= 0; --y)
                {
                    pos.setPos(x, y, z);

                    if (world.getBlockState(pos).getBlock() == Blocks.PORTAL)
                    {
                        pos.setY(pos.getY() - 1);

                        while (world.getBlockState(pos).getBlock() == Blocks.PORTAL)
                        {
                            pos.setY(pos.getY() - 1);
                        }

                        double dy = pos.getY() + 1 - idealPos.getY() + 0.5D;
                        double distTemp = dx * dx + dy * dy + dz * dz;

                        if (distance < 0.0D || distTemp < distance)
                        {
                            distance = distTemp;
                            foundPos = new BlockPos(x, pos.getY() + 1, z);
                        }
                    }
                }
            }
        }

        // Portal block found
        if (distance >= 0.0D)
        {
            this.portalPos = foundPos;
            this.portalAxis = this.getPortalOrientation(world, foundPos);
        }

        return distance >= 0.0D;
    }

    protected EnumFacing getPortalOrientation(World world, BlockPos portalPos)
    {
        if (world.getBlockState(portalPos.west()).getBlock() == Blocks.PORTAL ||
            world.getBlockState(portalPos.east()).getBlock() == Blocks.PORTAL)
        {
            return EnumFacing.EAST;
        }
        else if (world.getBlockState(portalPos.north()).getBlock() == Blocks.PORTAL ||
                 world.getBlockState(portalPos.south()).getBlock() == Blocks.PORTAL)
        {
            return EnumFacing.SOUTH;
        }

        return EnumFacing.NORTH;
    }

    protected BlockPos findTeleportPosition(World world, boolean placeInsidePortal)
    {
        if (placeInsidePortal)
        {
            return this.portalPos;
        }
        else
        {
            EnumFacing dirSide = this.portalAxis.rotateY();
            EnumFacing dirPortal = this.portalAxis;

            // Get the axis where there are more portal blocks (if only 2 wide portal)
            BlockPos posTmp = this.portalPos.add(dirPortal.getXOffset(), 0, dirPortal.getZOffset());
            if (world.getBlockState(posTmp).getBlock() != Blocks.PORTAL)
            {
                dirPortal = dirPortal.getOpposite();
            }

            List<BlockPos> list = new ArrayList<BlockPos>();
            int xOff = dirSide.getXOffset();
            int zOff = dirSide.getZOffset();
            list.add(this.portalPos.add(xOff, -1, zOff));
            list.add(this.portalPos.add(xOff, -2, zOff));
            list.add(this.portalPos.add(xOff + dirPortal.getXOffset(), -1, zOff + dirPortal.getZOffset()));
            list.add(this.portalPos.add(xOff + dirPortal.getXOffset(), -2, zOff + dirPortal.getZOffset()));
            list.add(this.portalPos.add(-xOff, -1, -zOff));
            list.add(this.portalPos.add(-xOff, -2, -zOff));
            list.add(this.portalPos.add(-xOff + dirPortal.getXOffset(), -1, -zOff + dirPortal.getZOffset()));
            list.add(this.portalPos.add(-xOff + dirPortal.getXOffset(), -2, -zOff + dirPortal.getZOffset()));

            // Try to find a suitable position on either side of the portal
            for (BlockPos pos : list)
            {
                if (world.isSideSolid(pos, EnumFacing.UP) &&
                    world.isAirBlock(pos.offset(EnumFacing.UP, 1)) &&
                    world.isAirBlock(pos.offset(EnumFacing.UP, 2)))
                {
                    return pos.up();
                }
            }

            // No suitable positions found, try to add a solid block to teleport to
            for (BlockPos pos : list)
            {
                if (world.isAirBlock(pos) &&
                    world.isAirBlock(pos.offset(EnumFacing.UP, 1)) &&
                    world.isAirBlock(pos.offset(EnumFacing.UP, 2)))
                {
                    world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3);
                    return pos.up();
                }
            }

            // No suitable positions found on either side of the portal, what should we do here??
            // Let's just stick the player to wherever he ends up on the side of the portal for now...
            return this.portalPos.add(dirSide.getXOffset(), 0, dirSide.getZOffset());
        }
    }
}
