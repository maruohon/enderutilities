package fi.dy.masa.enderutilities.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.event.RenderEventHandler;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityInserter;

public class BlockInserter extends BlockEnderUtilitiesInventory
{
    protected static final AxisAlignedBB BOUNDS_MAIN_NS = new AxisAlignedBB(0.25, 0.25,  0.0, 0.75, 0.75,  1.0);
    protected static final AxisAlignedBB BOUNDS_MAIN_WE = new AxisAlignedBB( 0.0, 0.25, 0.25,  1.0, 0.75, 0.75);
    protected static final AxisAlignedBB BOUNDS_MAIN_DU = new AxisAlignedBB(0.25,  0.0, 0.25, 0.75,  1.0, 0.75);

    protected static final AxisAlignedBB BOUNDS_SIDE_DOWN  = new AxisAlignedBB(0.25,  0.0, 0.25, 0.75, 0.25, 0.75);
    protected static final AxisAlignedBB BOUNDS_SIDE_UP    = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75,  1.0, 0.75);
    protected static final AxisAlignedBB BOUNDS_SIDE_NORTH = new AxisAlignedBB(0.25, 0.25,  0.0, 0.75, 0.75, 0.25);
    protected static final AxisAlignedBB BOUNDS_SIDE_SOUTH = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75,  1.0);
    protected static final AxisAlignedBB BOUNDS_SIDE_WEST  = new AxisAlignedBB( 0.0, 0.25, 0.25, 0.25, 0.75, 0.75);
    protected static final AxisAlignedBB BOUNDS_SIDE_EAST  = new AxisAlignedBB(0.75, 0.25, 0.25,  1.0, 0.75, 0.75);

    public static final PropertyEnum<InserterType> TYPE = PropertyEnum.<InserterType>create("type", InserterType.class);
    public static final PropertyEnum<Connection> CONN_UP    = PropertyEnum.<Connection>create("up", Connection.class);
    public static final PropertyEnum<Connection> CONN_DOWN  = PropertyEnum.<Connection>create("down", Connection.class);
    public static final PropertyEnum<Connection> CONN_NORTH = PropertyEnum.<Connection>create("north", Connection.class);
    public static final PropertyEnum<Connection> CONN_SOUTH = PropertyEnum.<Connection>create("south", Connection.class);
    public static final PropertyEnum<Connection> CONN_WEST  = PropertyEnum.<Connection>create("west", Connection.class);
    public static final PropertyEnum<Connection> CONN_EAST  = PropertyEnum.<Connection>create("east", Connection.class);

    public static final List<PropertyEnum<Connection>> CONNECTIONS = new ArrayList<PropertyEnum<Connection>>();
    private static final AxisAlignedBB[] SIDE_BOUNDS_BY_FACING = new AxisAlignedBB[] { BOUNDS_SIDE_DOWN, BOUNDS_SIDE_UP, BOUNDS_SIDE_NORTH, BOUNDS_SIDE_SOUTH, BOUNDS_SIDE_WEST, BOUNDS_SIDE_EAST };

    private final Map<Pair<Part, EnumFacing>, AxisAlignedBB> hilightBoxMap = new HashMap<Pair<Part, EnumFacing>, AxisAlignedBB>();

    public BlockInserter(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.propFacing = FACING;
        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(TYPE, InserterType.NORMAL)
                .withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING)
                .withProperty(CONN_UP,    Connection.NONE)
                .withProperty(CONN_DOWN,  Connection.NONE)
                .withProperty(CONN_NORTH, Connection.NONE)
                .withProperty(CONN_SOUTH, Connection.NONE)
                .withProperty(CONN_WEST,  Connection.NONE)
                .withProperty(CONN_EAST,  Connection.NONE));

        CONNECTIONS.add(CONN_DOWN);
        CONNECTIONS.add(CONN_UP);
        CONNECTIONS.add(CONN_NORTH);
        CONNECTIONS.add(CONN_SOUTH);
        CONNECTIONS.add(CONN_WEST);
        CONNECTIONS.add(CONN_EAST);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TYPE, FACING, CONN_UP, CONN_DOWN, CONN_NORTH, CONN_SOUTH, CONN_WEST, CONN_EAST});
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_INSERTER + "_normal",
                ReferenceNames.NAME_TILE_INSERTER + "_filtered"
        };
    }

    @Override
    protected String[] generateTooltipNames()
    {
        return this.generateUnlocalizedNames();
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityInserter();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        super.breakBlock(world, pos, state); // TODO unnecessary override?
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

        if (te != null)
        {
            te.setFacing(BlockPistonBase.getFacingFromEntity(pos, placer).getOpposite());
            te.setIsFiltered(state.getValue(TYPE) == InserterType.FILTERED);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() && heldItem == null)
        {
            if (world.isRemote == false)
            {
                TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

                if (te != null)
                {
                    Pair<Part, EnumFacing> key = this.getPointedElementId(world, pos, state.getActualState(world, pos).getValue(FACING), player);

                    // Targeting one of the side outputs
                    if (key != null && key.getLeft() == Part.SIDE)
                    {
                        side = key.getRight();
                    }

                    if (side == te.getFacing() || side == te.getFacing().getOpposite())
                    {
                        te.setFacing(te.getFacing().getOpposite());
                    }
                    else
                    {
                        te.toggleOutputSide(side);
                    }
                }
            }

            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        if (world instanceof World && ((World) world).isRemote == false)
        {
            TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

            if (te != null)
            {
                te.onNeighborTileChange(world, pos, neighbor);
            }
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random)
    {
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (world.isRemote == false)
        {
            TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

            if (te != null)
            {
                te.onScheduledBlockUpdate(world, pos, state, rand);
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, InserterType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

        if (te != null)
        {
            List<EnumFacing> validSides = te.getValidOutputSides();
            List<EnumFacing> invalidSides = new ArrayList<EnumFacing>(te.getEnabledOutputSides());
            invalidSides.removeAll(validSides);

            state = state.withProperty(FACING, te.getFacing());

            for (EnumFacing side : validSides)
            {
                state = state.withProperty(CONNECTIONS.get(side.getIndex()), Connection.VALID);
            }

            for (EnumFacing side : invalidSides)
            {
                state = state.withProperty(CONNECTIONS.get(side.getIndex()), Connection.INVALID);
            }
        }

        return state;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        state = this.getActualState(state, blockAccess, pos);
        EnumFacing facing = state.getValue(FACING);

        switch (facing)
        {
            case NORTH:
            case SOUTH:
                return BOUNDS_MAIN_NS;
            case WEST:
            case EAST:
                return BOUNDS_MAIN_WE;
            case DOWN:
            case UP:
            default:
                return BOUNDS_MAIN_DU;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity)
    {
        super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity);

        TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);

        if (te != null)
        {
            for (EnumFacing side : te.getEnabledOutputSides())
            {
                switch (side)
                {
                    case DOWN:  addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_DOWN); break;
                    case UP:    addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_UP); break;
                    case NORTH: addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_NORTH); break;
                    case SOUTH: addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_SOUTH); break;
                    case WEST:  addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_WEST); break;
                    case EAST:  addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDS_SIDE_EAST); break;
                }
            }
        }
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end)
    {
        this.updateBlockHilightBoxes(world, pos, state.getActualState(world, pos).getValue(FACING));
        List<RayTraceResult> list = new ArrayList<RayTraceResult>();

        for (AxisAlignedBB bb : this.getHilightBoxMap().values())
        {
            RayTraceResult trace = bb.calculateIntercept(start, end);

            if (trace != null)
            {
                list.add(new RayTraceResult(trace.hitVec, trace.sideHit, pos));
            }
        }

        RayTraceResult trace = null;
        // Closest to start, by being furthest from the end point
        double closest = 0.0D;

        for (RayTraceResult traceTmp : list)
        {
            if (traceTmp != null)
            {
                double dist = traceTmp.hitVec.squareDistanceTo(end);

                if (dist > closest)
                {
                    trace = traceTmp;
                    closest = dist;
                }
            }
        }

        return trace;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    {
        AxisAlignedBB bb = RenderEventHandler.getInstance().getPointedHilightBox(this);

        if (bb != null)
        {
            return bb;
        }

        return state.getBoundingBox(world, pos).offset(pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Pair<Part, EnumFacing>, AxisAlignedBB> getHilightBoxMap()
    {
        return this.hilightBoxMap;
    }

    @Override
    public void updateBlockHilightBoxes(World world, BlockPos pos, EnumFacing facing)
    {
        TileEntityInserter te = getTileEntitySafely(world, pos, TileEntityInserter.class);
        Map<Pair<Part, EnumFacing>, AxisAlignedBB> boxMap = this.getHilightBoxMap();
        boxMap.clear();

        if (te != null)
        {
            for (EnumFacing side : te.getEnabledOutputSides())
            {
                boxMap.put(Pair.of(Part.SIDE, side), SIDE_BOUNDS_BY_FACING[side.getIndex()].offset(pos));
            }
        }

        switch (facing)
        {
            case DOWN:
            case UP:
                boxMap.put(Pair.of(Part.MAIN, EnumFacing.DOWN), BOUNDS_MAIN_DU.offset(pos));
                break;
            case NORTH:
            case SOUTH:
                boxMap.put(Pair.of(Part.MAIN, EnumFacing.NORTH), BOUNDS_MAIN_NS.offset(pos));
                break;
            case WEST:
            case EAST:
                boxMap.put(Pair.of(Part.MAIN, EnumFacing.WEST), BOUNDS_MAIN_WE.offset(pos));
                break;
        }
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < InserterType.values().length; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public enum InserterType implements IStringSerializable
    {
        NORMAL      (0, "normal"),
        FILTERED    (1, "filtered");

        private final int meta;
        private final String name;

        private InserterType(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        public static InserterType fromMeta(int meta)
        {
            return values()[meta % values().length];
        }
    }

    public enum Connection implements IStringSerializable
    {
        NONE    ("none"),
        VALID   ("valid"),
        INVALID ( "invalid");

        private final String name;

        private Connection(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return this.name;
        }
    }

    private enum Part
    {
        MAIN,
        SIDE
    }
}
