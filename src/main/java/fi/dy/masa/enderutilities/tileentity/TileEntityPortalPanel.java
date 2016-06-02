package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesPortal;
import fi.dy.masa.enderutilities.event.tasks.TaskPositionDebug;
import fi.dy.masa.enderutilities.event.tasks.TaskScheduler;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiPortalPanel;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerPortalPanel;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private byte activeTargetId;
    private byte portalTargetId;
    private boolean active;
    private String displayName;
    private int[] colors = new int[9];

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.inventoryWrapper);
    }

    public int getActiveTargetId()
    {
        return this.activeTargetId;
    }

    private TargetData getActiveTarget()
    {
        int slot = this.getActiveTargetId();
        ItemStack stack = this.itemHandlerBase.getStackInSlot(slot);

        if (stack != null)
        {
            return TargetData.getTargetFromItem(stack);
        }

        return null;
    }

    public void setActiveTargetId(int target)
    {
        this.activeTargetId = (byte)MathHelper.clamp_int(target, 0, 7);
    }

    public int getActiveColor()
    {
        return this.getColorFromItems(8);
    }

    private int getColorFromItems(int target)
    {
        // The large button in the center will take the color of the active target
        if (target == 8)
        {
            target = this.activeTargetId;
        }

        if (target >= 0 && target < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(target + 8);

            if (stack != null && stack.getItem() == Items.DYE)
            {
                return EnumDyeColor.byDyeDamage(stack.getMetadata()).getMapColor().colorValue;
            }
        }

        return 0xFFFFFF;
    }

    public int getColor(int target)
    {
        target = MathHelper.clamp_int(target, 0, 8);
        return this.colors[target];
    }

    private String getActiveName()
    {
        if (this.activeTargetId >= 0 && this.activeTargetId < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(this.activeTargetId);

            if (stack != null && stack.hasDisplayName())
            {
                return stack.getDisplayName();
            }
        }

        return null;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setActiveTargetId(nbt.getByte("SelectedTarget"));
        this.portalTargetId = nbt.getByte("PortalTarget");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("SelectedTarget", this.activeTargetId);
        nbt.setByte("PortalTarget", this.portalTargetId);
        nbt.setBoolean("Active", this.active);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("s", this.activeTargetId);
        String name = this.getActiveName();
        if (name != null)
        {
            nbt.setString("n", name);
        }

        for (int i = 0; i < 9; i++)
        {
            nbt.setInteger("c" + i, this.getColorFromItems(i));
        }

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.activeTargetId = nbt.getByte("s");
        this.displayName = nbt.getString("n");

        for (int i = 0; i < 9; i++)
        {
            this.colors[i] = nbt.getInteger("c" + i);

            if (this.colors[i] == 0)
            {
                this.colors[i] = 0xFFFFFF;
            }
        }

        super.onDataPacket(net, packet);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        super.inventoryChanged(inventoryId, slot);

        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0 && element >= 0 && element < 8)
        {
            this.setActiveTargetId(element);

            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
        }
    }

    private class ItemHandlerWrapper extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapper(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return false;
            }

            if (slot < 8)
            {
                return stack.getItem() == EnderUtilitiesItems.linkCrystal &&
                        ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
            }

            return stack.getItem() == Items.DYE;
        }
    }

    @Override
    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }

    public void tryTogglePortal()
    {
        if (this.active == false)
        {
            this.tryActivatePortal();
        }
        else if (this.activeTargetId != this.portalTargetId)
        {
            this.tryUpdatePortal();
        }
        else
        {
            this.tryDisablePortal();
        }
    }

    private boolean tryActivatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());
        boolean success = false;
        TargetData destination = this.getActiveTarget();

        if (destination == null || world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        //System.out.println("plop - activate");
        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal, destination, this.getActiveColor());
        portalFormer.setLimits(100, 100, 200);
        portalFormer.analyzePortalFrame();
        portalFormer.validatePortalAreas();
        //success = portalFormer.formPortals();

        List<BlockPos> list = portalFormer.getVisited();
        IBlockState state = Blocks.EMERALD_BLOCK.getDefaultState();
        //List<BlockPos> list = portalFormer.getBranches();
        //IBlockState state = Blocks.GOLD_BLOCK.getDefaultState();
        //List<BlockPos> list = portalFormer.getCorners();
        //IBlockState state = Blocks.DIAMOND_BLOCK.getDefaultState();

        TaskPositionDebug task = new TaskPositionDebug(world, list, state, 1, true, false, EnumParticleTypes.VILLAGER_ANGRY);
        TaskScheduler.getInstance().addTask(task, 2);

        if (success)
        {
            this.active = true;
            this.portalTargetId = this.activeTargetId;
            world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.MASTER, 0.5f, 1.0f);
        }

        return success;
    }

    private void tryDisablePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posFrame = this.getPos().offset(this.getFacing().getOpposite());
        boolean success = false;

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal, this.getActiveTarget(), this.getActiveColor());
        portalFormer.analyzePortalFrame();
        success = portalFormer.destroyPortals();

        if (success)
        {
            this.active = false;
            world.playSound(null, this.getPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.MASTER, 0.5f, 1.0f);
        }
    }

    private void tryUpdatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posFrame = this.getPos().offset(this.getFacing().getOpposite());
        boolean success = false;

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame, blockPortal, this.getActiveTarget(), this.getActiveColor());
        portalFormer.setLimits(100, 100, 200); // TODO update values
        portalFormer.analyzePortalFrame();
        success = portalFormer.destroyPortals();
        portalFormer.validatePortalAreas();
        success &= portalFormer.formPortals();

        if (success)
        {
            this.active = true;
            this.portalTargetId = this.activeTargetId;
        }
    }

    public static class PortalFormer
    {
        public static final EnumFacing[] ADJACENT_SIDES_ZY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
        public static final EnumFacing[] ADJACENT_SIDES_XY = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST };
        public static final EnumFacing[] ADJACENT_SIDES_XZ = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };
        private final World world;
        private final TargetData target;
        private final int portalColor;
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
        private boolean analyzed;
        private boolean validated;
        private boolean formed;

        public PortalFormer(World world, BlockPos startPos, Block frameBlock, Block portalBlock, TargetData target, int portalColor)
        {
            this.world = world;
            this.target = target;
            this.portalColor = portalColor;
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
            this.frameCheckLimit = 400;
            this.frameLoopCheckLimit = 800;
            this.portalAreaCheckLimit = 4000;
        }

        public List<BlockPos> getVisited() { return this.visited; }
        public List<BlockPos> getBranches() { return this.branches; }
        public List<BlockPos> getCorners() { return this.corners; }

        public void setLimits(int frameCheckLimit, int frameLoopCheckLimit, int portalAreaCheckLimit)
        {
            this.frameCheckLimit = frameCheckLimit;
            this.frameLoopCheckLimit = frameLoopCheckLimit;
            this.portalAreaCheckLimit = portalAreaCheckLimit;
        }

        /**
         * Analyzes the portal frame structure and marks all the corner locations.
         * Call this method first.
         */
        public void analyzePortalFrame()
        {
            if (this.analyzed)
            {
                return;
            }

            this.visited.clear();
            this.branches.clear();
            this.corners.clear();

            int counter = 0;
            int branchIndex = 0;
            BlockPos pos = this.startPos;
            EnumFacing side = null;

            while (counter < this.frameCheckLimit)
            {
                // FIXME one loop will do
                while (counter < this.frameCheckLimit)
                {
                    side = this.checkFramePositionIgnoringSide(pos, null);
                    counter++;

                    if (side == null)
                    {
                        break;
                    }

                    pos = pos.offset(side);
                }

                if (branchIndex < this.branches.size())
                {
                    pos = this.branches.get(branchIndex);
                    branchIndex++;
                }
            }

            this.analyzed = true;
        }

        /**
         * Validates all the corner locations by trying to find an enclosing frame loop.
         * Call this after analyzePortalFrame().
         */
        public void validatePortalAreas()
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

                if (this.checkForCorner(pos, true) == true)
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

                    //System.out.printf("validating corner %s - corner valid; axis: %s side: %s\n", pos, axis, side);
                    if (this.walkFrameLoop(pos, axis, side, this.frameLoopCheckLimit) == false)
                    {
                        System.out.printf("validating area for corner %s - area invalid; axis: %s side: %s\n", pos, axis, side);
                        iter.remove();
                    }
                    else
                    {
                        System.out.printf("validating area for corner %s - area valid; axis: %s side: %s\n", pos, axis, side);
                    }
                }
                else
                {
                    //System.out.printf("validating corner %s - corner invalid\n", pos);
                    iter.remove();
                }
            }

            this.validated = true;
        }

        /**
         * Forms/creates the actual portal blocks into the validated areas.
         * Call this as the final step after validatePortalAreas().
         */
        public boolean formPortals()
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
            int formCount = 0;

            for (BlockPos pos : this.corners)
            {
                this.branches.clear();
                this.visited.clear();

                this.portalAxis = this.getPortalAxisFromCorner(pos);
                //System.out.printf("corner: %s - axis: %s\n", pos, this.portalAxis);
                if (this.portalAxis == null)
                {
                    continue;
                }

                counter = 0;
                int branchIndex = 0;
                posTmp = pos;

                // FIXME: One loop should do
                while (counter < this.portalAreaCheckLimit)
                {
                    while (counter < this.portalAreaCheckLimit)
                    {
                        valid = this.checkForValidPortalPosition(posTmp, ignoreSide);
                        counter++;

                        // This position invalidates this portal area
                        if (valid == false)
                        {
                            //System.out.printf("inner - not valid\n");
                            break;
                        }

                        // Valid position, but no more positions adjacent to it to go to
                        if (this.nextSide == null)
                        {
                            //System.out.printf("inner - nextSide = null\n");
                            break;
                        }

                        posTmp = posTmp.offset(this.nextSide);
                    }

                    // This position invalidates this portal area
                    if (valid == false)
                    {
                        //System.out.printf("outer - not valid\n");
                        break;
                    }

                    if (branchIndex < this.branches.size())
                    {
                        //System.out.printf("outer - new branch\n");
                        posTmp = this.branches.get(branchIndex);
                        branchIndex++;
                    }
                    else
                    {
                        break;
                    }
                }

                //System.out.printf("valid %s counter: %d\n", valid, counter);
                if (valid == true && counter < this.portalAreaCheckLimit)
                {
                    EnumFacing facing = this.portalAxis == EnumFacing.Axis.X ? EnumFacing.EAST :
                                        this.portalAxis == EnumFacing.Axis.Z ? EnumFacing.NORTH : EnumFacing.UP;
                    IBlockState state = EnderUtilitiesBlocks.blockPortal.getDefaultState().withProperty(BlockEnderUtilitiesPortal.FACING, facing);

                    for (BlockPos posPortal : this.visited)
                    {
                        this.world.setBlockState(posPortal, state, 2);

                        TileEntity te = this.world.getTileEntity(posPortal);
                        if (te instanceof TileEntityPortal)
                        {
                            ((TileEntityPortal) te).setDestination(this.target);
                            ((TileEntityPortal) te).setColor(this.portalColor);
                        }

                        formCount++;
                    }

                    //System.out.printf("valid: true, made portal - counter: %d formCount: %d\n", counter, formCount);
                    success = true;
                }
                /*else
                {
                    System.out.printf("valid: %s counter: %d formCount: %d\n", valid, counter, formCount);
                }*/
            }

            this.formed = true;
            return success;
        }

        /**
         * Destroys all the portal corner blocks, which should cause them to update
         * and destroy any adjacent portal blocks automatically.
         */
        public boolean destroyPortals()
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
            //this.visited.clear();
            // FIXME: This is just for debugging
            if (this.visited.contains(pos) == false)
            {
                this.visited.add(pos);
            }

            int counter = 0;
            int turns = 0;
            int tries = 0;
            IBlockState state;
            Block block;
            BlockPos startPos = pos;
            BlockPos posLast = startPos;
            //EnumFacing firstTrySide = axis == Axis.X || axis == Axis.Z ? EnumFacing.DOWN : EnumFacing.EAST;
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
                        // FIXME: This is just for debugging
                        if (this.visited.contains(pos) == false)
                        {
                            this.visited.add(pos);
                        }

                        //System.out.printf("frame loop, AIR @ %s, dir: %s\n", pos, moveDirection);
                        posLast = pos;

                        // The firstTrySide is facing into the adjacent portal frame when traveling
                        // along a straight frame. Thus we need to rotate it once to keep going straight.
                        // If we need to rotate it more than once, then we have hit a "right hand corner".
                        if (tries > 1)
                        {
                            //System.out.printf("frame loop, AIR @ %s, dir: %s, tries: %d -> turns++\n", pos, moveDirection, tries);
                            turns++;
                        }
                        // If we didn't have to rotate the firstTrySide at all, then we hit a "left hand turn"
                        // ie. traveled through an outer bend.
                        else if (tries == 0)
                        {
                            //System.out.printf("frame loop, AIR @ %s, dir: %s, tries: %d -> turns-- old firstTrySide: %s", pos, moveDirection, tries, firstTrySide);
                            turns--;

                            //System.out.printf(" new firstTrySide: %s\n", firstTrySide);
                        }

                        // Set the firstTrySide one rotation back from the side that we successfully moved to
                        // so that we can go around possible outer bends.
                        firstTrySide = moveDirection.rotateAround(axis).getOpposite();

                        break;
                    }
                    // Found a portal frame block, try the next adjacent side...
                    else if (block == this.blockFrame)
                    {
                        //System.out.printf("frame loop, frame @ %s, dirOld: %s dirNew: %s\n", pos, moveDirection, moveDirection.rotateAround(axis));
                        moveDirection = moveDirection.rotateAround(axis);
                    }
                    // Found a non-air, non-portal-frame block -> invalid area.
                    else
                    {
                        //System.out.printf("frame loop, non-air @ %s\n", pos);
                        return false;
                    }
                }

                counter++;

                // If we can return to the starting position hugging the portal frame,
                // then this is a valid portal frame loop.
                // Note that it is only valid if it forms an inside area, thus the turns check.
                if (pos.equals(startPos) && counter > 0)
                {
                    //System.out.printf("frame loop, back to start - valid: %s - counter: %d tries: %d turns: %d pos: %s\n", (turns > 0), counter, tries, turns, pos);
                    return turns > 0;
                }
            }

            //System.out.printf("frame loop, invalid - counter: %d tries: %d turns: %d pos: %s\n", counter, tries, turns, pos);
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

                    if (block == this.blockPortal || block.isAir(state, this.world, pos))
                    {
                        this.checkForCorner(pos, false);
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

            EnumFacing[] sides = this.getSides(this.portalAxis);

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
            for (EnumFacing side : this.getSides(axis))
            {
                if (this.world.getBlockState(pos.offset(side)).getBlock() == this.blockFrame)
                {
                    return side;
                }
            }

            return null;
        }

        private EnumFacing[] getSides(EnumFacing.Axis axis)
        {
            if (axis == EnumFacing.Axis.X)
            {
                return ADJACENT_SIDES_ZY;
            }

            return axis == EnumFacing.Axis.Z ? ADJACENT_SIDES_XY : ADJACENT_SIDES_XZ;
        }
    }

    public static enum SegmentResult
    {
        UNPROCESSED,
        IN_PROGRESS,
        DEAD_END,
        INTERSECTION,
        LOOP,
        LOOP_DIAGONALS,
        PATH,
        PATH_DIAGONALS;
    }
}
