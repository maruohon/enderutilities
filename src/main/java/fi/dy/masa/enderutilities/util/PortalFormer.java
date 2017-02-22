package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class PortalFormer
{
    private final World world;
    private TargetData target;
    private OwnerData owner;
    private int portalColor;
    /*private final Set<BlockPos> visited;
    private final Set<BlockPos> branches;
    private final Set<BlockPos> corners;*/
    private final List<BlockPos> visited;
    private final List<BlockPos> branches;
    private final List<BlockPos> corners;
    private final Block blockFrame;
    private final Block blockPortal;
    private final BlockPos startPos;
    private BlockPos lastPos;
    private EnumFacing nextSide;
    private EnumFacing.Axis portalAxis;
    private int frameCheckLimit;
    private int frameLoopCheckLimit;
    private int portalAreaCheckLimit;
    private int portalsFound;
    private boolean analyzed;
    private boolean validated;
    private boolean formed;

    public PortalFormer(World world, BlockPos startPos, Block frameBlock, Block portalBlock)
    {
        this.world = world;
        this.portalColor = 8339378;
        /*this.visited = new HashSet<BlockPos>();
        this.branches = new HashSet<BlockPos>();
        this.corners = new HashSet<BlockPos>();*/
        this.visited = new ArrayList<BlockPos>();
        this.branches = new ArrayList<BlockPos>();
        this.corners = new ArrayList<BlockPos>();
        this.blockFrame = frameBlock;
        this.blockPortal = portalBlock;
        this.startPos = startPos;
        this.lastPos = startPos;
        this.frameCheckLimit = Configs.portalFrameCheckLimit;
        this.frameLoopCheckLimit = Configs.portalLoopCheckLimit;
        this.portalAreaCheckLimit = Configs.portalAreaCheckLimit;
    }

    /*public List<BlockPos> getVisited() { return this.visited; }
    public List<BlockPos> getBranches() { return this.branches; }
    public List<BlockPos> getCorners() { return this.corners; }*/

    public void setPortalData(TargetData target, OwnerData owner, int color)
    {
        this.target = target;
        this.owner = owner;
        this.portalColor = color;
    }

    public void setLimits(int frameCheckLimit, int frameLoopCheckLimit, int portalAreaCheckLimit)
    {
        this.frameCheckLimit = frameCheckLimit;
        this.frameLoopCheckLimit = frameLoopCheckLimit;
        this.portalAreaCheckLimit = portalAreaCheckLimit;
    }

    public boolean getPortalState()
    {
        return this.portalsFound > 0;
    }

    public boolean togglePortalState(boolean recreate)
    {
        this.analyzePortal();

        if (this.getPortalState())
        {
            this.destroyPortals();

            if (recreate == false)
            {
                return true;
            }
        }

        this.validatePortalAreas();
        return this.formPortals();
    }

    /**
     * Analyzes the portal frame structure and marks all the corner locations.
     * Call this method first.
     */
    public void analyzePortal()
    {
        if (this.analyzed)
        {
            return;
        }

        this.visited.clear();
        this.branches.clear();
        this.corners.clear();
        this.portalsFound = 0;

        if (this.world.getBlockState(this.startPos).getBlock() != this.blockFrame)
        {
            this.analyzed = true;
            return;
        }

        int counter = 0;
        int branchIndex = 0;
        BlockPos pos = this.startPos;
        EnumFacing side = null;

        while (counter < this.frameCheckLimit)
        {
            side = this.checkFramePositionIgnoringSide(pos, null);
            counter++;

            if (side == null)
            {
                if (branchIndex < this.branches.size())
                {
                    pos = this.branches.get(branchIndex);
                    branchIndex++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                pos = pos.offset(side);
            }
        }

        this.analyzed = true;
    }

    /**
     * Validates all the corner locations by trying to find an enclosing frame loop.
     * Call this after analyzePortalFrame().
     */
    private void validatePortalAreas()
    {
        if (this.validated)
        {
            return;
        }

        this.visited.clear();
        Iterator<BlockPos> iter = this.corners.iterator();

        while (iter.hasNext())
        {
            BlockPos pos = iter.next();

            if (this.checkForCorner(pos, true))
            {
                // TODO: Check all axes, if multiple are valid for this corner position
                EnumFacing.Axis axis = this.getPortalAxisFromCorner(pos);

                if (axis == null)
                {
                    EnderUtilities.logger.warn("null axis in PortalFormer#validateCorners()");
                    break;
                }

                EnumFacing side = this.getSideWithFrame(pos, axis);
                if (side == null)
                {
                    EnderUtilities.logger.warn("Didn't find an adjacent portal frame in PortalFormer#validateCorners()");
                    break;
                }

                if (this.walkFrameLoop(pos, axis, side, this.frameLoopCheckLimit) == false)
                {
                    iter.remove();
                }
            }
            else
            {
                iter.remove();
            }
        }

        this.validated = true;
    }

    /**
     * Forms/creates the actual portal blocks into the validated areas.
     * Call this as the final step after validatePortalAreas().
     */
    private boolean formPortals()
    {
        if (this.formed)
        {
            return false;
        }

        EnumFacing ignoreSide = null;
        boolean valid = false;
        boolean success = false;
        BlockPos posTmp;
        int counter = 0;

        for (BlockPos pos : this.corners)
        {
            this.branches.clear();
            this.visited.clear();

            this.portalAxis = this.getPortalAxisFromCorner(pos);
            if (this.portalAxis == null)
            {
                continue;
            }

            counter = 0;
            int branchIndex = 0;
            posTmp = pos;

            while (counter < this.portalAreaCheckLimit)
            {
                valid = this.checkForValidPortalPosition(posTmp, ignoreSide);
                counter++;

                // This position invalidates this portal area
                if (valid == false)
                {
                    break;
                }

                // Valid position, but no more positions adjacent to it to go to
                if (this.nextSide == null)
                {
                    // Try to go to saved "branch positions" if there are some left
                    if (branchIndex < this.branches.size())
                    {
                        posTmp = this.branches.get(branchIndex);
                        branchIndex++;
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    posTmp = posTmp.offset(this.nextSide);
                }
            }

            if (valid && counter < this.portalAreaCheckLimit)
            {
                EnumFacing facing = this.portalAxis == EnumFacing.Axis.X ? EnumFacing.EAST :
                                    this.portalAxis == EnumFacing.Axis.Z ? EnumFacing.NORTH : EnumFacing.UP;
                BlockEnderUtilities block = EnderUtilitiesBlocks.blockPortal;
                IBlockState state = block.getDefaultState().withProperty(block.propFacing, facing);

                for (BlockPos posPortal : this.visited)
                {
                    this.world.setBlockState(posPortal, state, 2);

                    TileEntity te = this.world.getTileEntity(posPortal);
                    if (te instanceof TileEntityPortal)
                    {
                        ((TileEntityPortal) te).setDestination(this.target);
                        ((TileEntityPortal) te).setOwner(this.owner);
                        ((TileEntityPortal) te).setColor(this.portalColor);
                    }
                }

                success = true;
            }
        }

        this.formed = true;
        return success;
    }

    /**
     * Destroys all the portal corner blocks, which should cause them to update
     * and destroy any adjacent portal blocks automatically.
     */
    private boolean destroyPortals()
    {
        boolean success = false;

        for (BlockPos pos : this.corners)
        {
            if (this.world.getBlockState(pos).getBlock() == this.blockPortal)
            {
                this.world.setBlockToAir(pos);
                success = true;
            }
        }

        return success;
    }

    private boolean walkFrameLoop(BlockPos pos, EnumFacing.Axis axis, EnumFacing frameSide, int distanceLimit)
    {
        int counter = 0;
        int turns = 0;
        int tries = 0;
        IBlockState state;
        Block block;
        BlockPos startPos = pos;
        BlockPos posLast = startPos;
        EnumFacing firstTrySide = frameSide;
        EnumFacing moveDirection = frameSide;

        while (counter < distanceLimit)
        {
            moveDirection = firstTrySide;

            for (tries = 0; tries < 4; tries++)
            {
                pos = posLast.offset(moveDirection);
                state = this.world.getBlockState(pos);
                block = state.getBlock();

                if (block.isAir(state, this.world, pos))
                {
                    posLast = pos;

                    // The firstTrySide is facing into the adjacent portal frame when traveling
                    // along a straight frame. Thus we need to rotate it once to keep going straight.
                    // If we need to rotate it more than once, then we have hit a "right hand corner".
                    if (tries > 1)
                    {
                        turns++;
                    }
                    // If we didn't have to rotate the firstTrySide at all, then we hit a "left hand turn"
                    // ie. traveled through an outer bend.
                    else if (tries == 0)
                    {
                        turns--;
                    }

                    // Set the firstTrySide one rotation back from the side that we successfully moved to
                    // so that we can go around possible outer bends.
                    firstTrySide = moveDirection.rotateAround(axis).getOpposite();

                    break;
                }
                // Found a portal frame block, try the next adjacent side...
                else if (block == this.blockFrame)
                {
                    moveDirection = moveDirection.rotateAround(axis);
                }
                // Found a non-air, non-portal-frame block -> invalid area.
                else
                {
                    return false;
                }
            }

            // If we can return to the starting position hugging the portal frame,
            // then this is a valid portal frame loop.
            // Note that it is only valid if it forms an inside area, thus the turns check.
            // the tries == 4 && counter == 0 check is for a 1x1 area special case
            if ((tries == 4 && counter == 0) || pos.equals(startPos))
            {
                return turns >= 0;
            }

            counter++;
        }

        return false;
    }

    private EnumFacing checkFramePositionIgnoringSide(BlockPos posIn, EnumFacing ignoreSide)
    {
        BlockPos pos = posIn;
        IBlockState state;
        Block block;
        EnumFacing continueTo = null;
        int frames = 0;

        if (this.visited.contains(posIn))
        {
            return null;
        }

        for (EnumFacing side : EnumFacing.values())
        {
            if (side != ignoreSide)
            {
                pos = posIn.offset(side);

                if (pos.equals(this.lastPos))
                {
                    continue;
                }

                state = this.world.getBlockState(pos);
                block = state.getBlock();

                if (block.isAir(state, this.world, pos) || block == this.blockPortal)
                {
                    this.checkForCorner(pos, false);

                    // This is to fix single block portals not getting found via the checkForCorner() method.
                    // The actual portal block count will be skewed, but it isn't used for anything other than a "> 0" check anyway.
                    if (block == this.blockPortal)
                    {
                        this.portalsFound++;
                    }
                }
                else if (block == this.blockFrame)
                {
                    if (this.visited.contains(pos) == false)
                    {
                        if (frames == 0)
                        {
                            continueTo = side;
                        }
                        else
                        {
                            this.branches.add(pos);
                        }
                    }
                    frames++;
                }
            }
        }

        this.visited.add(posIn);
        this.lastPos = posIn;

        return continueTo;
    }

    private boolean checkForCorner(BlockPos posIn, boolean checkIsAir)
    {
        if (checkIsAir && this.world.isAirBlock(posIn) == false)
        {
            return false;
        }

        if (this.corners.contains(posIn))
        {
            return true;
        }

        Block block;
        int adjacents = 0;
        EnumFacing frames[] = new EnumFacing[6];

        for (EnumFacing side : EnumFacing.values())
        {
            block = this.world.getBlockState(posIn.offset(side)).getBlock();

            if (block == this.blockFrame)
            {
                frames[adjacents] = side;
                adjacents++;
            }
            else if (block == this.blockPortal)
            {
                this.portalsFound++;
            }
        }

        if (adjacents >= 3 || (adjacents == 2 && frames[0] != frames[1].getOpposite()))
        {
            this.corners.add(posIn);
            return true;
        }

        return false;
    }

    private boolean checkForValidPortalPosition(BlockPos posIn, EnumFacing ignoreSide)
    {
        BlockPos pos = posIn;
        IBlockState state;
        Block block;
        EnumFacing continueTo = null;
        int adjacent = 0;

        if (this.visited.contains(posIn))
        {
            return true;
        }

        EnumFacing[] sides = PositionUtils.getSidesForAxis(this.portalAxis);

        for (EnumFacing side : sides)
        {
            if (side != ignoreSide)
            {
                pos = posIn.offset(side);

                if (pos.equals(this.lastPos))
                {
                    continue;
                }

                state = this.world.getBlockState(pos);
                block = state.getBlock();

                if (block.isAir(state, this.world, pos))
                {
                    if (this.visited.contains(pos) == false)
                    {
                        if (adjacent == 0)
                        {
                            continueTo = side;
                        }
                        else
                        {
                            this.branches.add(pos);
                        }
                        adjacent++;
                    }
                }
                else if (block != this.blockFrame)
                {
                    return false;
                }
            }
        }

        this.visited.add(posIn);
        this.lastPos = posIn;

        this.nextSide = continueTo;

        return true;
    }

    private EnumFacing.Axis getPortalAxisFromCorner(BlockPos posIn)
    {
        Block block;
        int numAdjacents = 0;
        EnumFacing adjacents[] = new EnumFacing[6];

        for (EnumFacing side : EnumFacing.values())
        {
            if (numAdjacents > 0 && adjacents[numAdjacents - 1] == side.getOpposite())
            {
                continue;
            }

            block = this.world.getBlockState(posIn.offset(side)).getBlock();

            if (block == this.blockFrame)
            {
                adjacents[numAdjacents] = side;
                numAdjacents++;
            }
        }

        if (numAdjacents >= 3 || (numAdjacents == 2 && adjacents[0] != adjacents[1].getOpposite()))
        {
            if (adjacents[0] == EnumFacing.DOWN || adjacents[0] == EnumFacing.UP)
            {
                if (adjacents[1] == EnumFacing.NORTH || adjacents[1] == EnumFacing.SOUTH)
                {
                    return EnumFacing.Axis.X;
                }

                return EnumFacing.Axis.Z;
            }

            return EnumFacing.Axis.Y;
        }

        return null;
    }

    private EnumFacing getSideWithFrame(BlockPos pos, EnumFacing.Axis axis)
    {
        EnumFacing[] sides = PositionUtils.getSidesForAxis(axis);

        for (EnumFacing side : sides)
        {
            if (this.world.getBlockState(pos.offset(side)).getBlock() == this.blockFrame)
            {
                return side;
            }
        }

        return null;
    }
}
