package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
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
import fi.dy.masa.enderutilities.block.BlockPortal;
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
        IBlockState statePortal = blockPortal.getDefaultState().withProperty(BlockPortal.FACING, EnumFacing.NORTH);
        TargetData destination = this.getActiveTarget();
        int color = this.getColorFromItems(8); // The active color

        if (destination == null || world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        System.out.println("plop - activate");
        /*for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);

            if (world.isAirBlock(pos))
            {
                world.setBlockState(pos, statePortal, 2);
                TileEntity te = world.getTileEntity(pos);

                if (te instanceof TileEntityPortal)
                {
                    ((TileEntityPortal) te).setDestination(destination);
                    ((TileEntityPortal) te).setColor(color);
                    success = true;
                }
            }
        }*/

        PortalFormer portalFormer = new PortalFormer(world, posFrame, blockFrame);
        portalFormer.analyzePortal();
        portalFormer.formPortals();

        //List<BlockPos> list = portalFormer.getVisited();
        //IBlockState state = Blocks.EMERALD_BLOCK.getDefaultState();
        //List<BlockPos> list = portalFormer.getBranches();
        //IBlockState state = Blocks.GOLD_BLOCK.getDefaultState();
        //List<BlockPos> list = portalFormer.getCorners();
        //IBlockState state = Blocks.DIAMOND_BLOCK.getDefaultState();

        //TaskPositionDebug task = new TaskPositionDebug(world, list, state, 1, true, false, EnumParticleTypes.VILLAGER_ANGRY);
        //TaskScheduler.getInstance().addTask(task, 5);

        if (success)
        {
            world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.MASTER, 0.5f, 1.0f);
            this.active = true;
            this.portalTargetId = this.activeTargetId;
        }

        return success;
    }

    private boolean tryDisablePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());
        boolean success = false;

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        System.out.println("plop - disable");
        //List<BlockPos> positions = this.getExistingPortalPositions(world, posFrame);
        //for (BlockPos pos : positions)
        //{
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);
            if (world.getBlockState(pos).getBlock() == blockPortal)
            {
                world.setBlockToAir(pos);
                success = true;
            }
        }

        if (success)
        {
            world.playSound(null, posPanel, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.MASTER, 0.5f, 1.0f);
        }

        this.active = false;

        return success;
    }

    private void tryUpdatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        TargetData destination = this.getActiveTarget();
        int color = this.getColorFromItems(8); // The active color

        System.out.println("plop - update");
        //List<BlockPos> positions = this.getExistingPortalPositions(world, posFrame);
        //for (BlockPos pos : positions)
        //{
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TileEntityPortal)
            {
                ((TileEntityPortal) te).setDestination(destination);
                ((TileEntityPortal) te).setColor(color);
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }

        this.active = true;
        this.portalTargetId = this.activeTargetId;
    }

    private List<BlockPos> getExistingPortalPositions(World world, BlockPos posFrame)
    {
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        List<BlockPos> positions = new ArrayList<BlockPos>();

        return positions;
    }

    private List<BlockPos> getPortalPositionsForCreation(World world, BlockPos posFrame)
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        List<BlockPos> positions = new ArrayList<BlockPos>();

        return positions;
    }

    public static class PortalFormer
    {
        public static final EnumFacing[] SIDES_X = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
        public static final EnumFacing[] SIDES_Z = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST, EnumFacing.WEST };
        public static final EnumFacing[] SIDES_Y = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };
        private final World world;
        /*private final Set<BlockPos> visited;
        private final Set<BlockPos> branches;
        private final Set<BlockPos> corners;*/
        private final List<BlockPos> visited;
        private final List<BlockPos> branches;
        private final List<BlockPos> corners;
        private final Block blockFrame;
        //private final Block blockInside;
        private final BlockPos startPos;
        private BlockPos lastPos;
        private EnumFacing nextSide;
        private EnumFacing.Axis portalAxis;

        public PortalFormer(World world, BlockPos startPos, Block frameBlock)
        {
            this.world = world;
            /*this.visited = new HashSet<BlockPos>();
            this.branches = new HashSet<BlockPos>();
            this.corners = new HashSet<BlockPos>();*/
            this.visited = new ArrayList<BlockPos>();
            this.branches = new ArrayList<BlockPos>();
            this.corners = new ArrayList<BlockPos>();
            this.blockFrame = frameBlock;
            //this.blockInside = insideBlock;
            this.startPos = startPos;
            this.lastPos = startPos;
        }

        public List<BlockPos> getVisited() { return this.visited; }
        public List<BlockPos> getBranches() { return this.branches; }
        public List<BlockPos> getCorners() { return this.corners; }

        public void analyzePortal()
        {
            this.visited.clear();
            this.branches.clear();
            this.corners.clear();

            int counter = 0;
            int branchIndex = 0;
            BlockPos pos = this.startPos;
            EnumFacing side = null;

            while (counter < 100)
            {
                while (counter < 100)
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
        }

        public void formPortals()
        {
            EnumFacing ignoreSide = null;
            boolean valid = false;
            BlockPos posTmp;
            int limit = 160;

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

                int counter = 0;
                int branchIndex = 0;
                posTmp = pos;

                // TODO: We should try to walk the frame boundary before trying to check/fill the area
                while (counter < limit)
                {
                    while (counter < limit)
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
                if (valid == true && counter < limit)
                {
                    EnumFacing facing = this.portalAxis == EnumFacing.Axis.X ? EnumFacing.EAST :
                                        this.portalAxis == EnumFacing.Axis.Z ? EnumFacing.NORTH : EnumFacing.UP;
                    IBlockState state = EnderUtilitiesBlocks.blockPortal.getDefaultState().withProperty(BlockPortal.FACING, facing);

                    for (BlockPos posPortal : this.visited)
                    {
                        //System.out.printf("setting at %s\n", posPortal);
                        this.world.setBlockState(posPortal, state, 2);
                    }

                    // FIXME debug return to keep the list from the first area for the debug task
                    //return;
                }
            }
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

                    if (block.isAir(state, this.world, pos))
                    {
                        this.checkForCorner(pos);
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

        private boolean checkForCorner(BlockPos posIn)
        {
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

        private EnumFacing[] getSides(EnumFacing.Axis axis)
        {
            if (axis == EnumFacing.Axis.X)
            {
                return SIDES_X;
            }

            return axis == EnumFacing.Axis.Z ? SIDES_Z : SIDES_Y;
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
