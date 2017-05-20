package fi.dy.masa.enderutilities.block;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.event.RenderEventHandler;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class BlockPortalPanel extends BlockEnderUtilitiesInventory
{
    public static final AxisAlignedBB PANEL_BOUNDS_BASE  = new AxisAlignedBB(    1.0D,    0.0D,    1.0D,    0.0D,    1.0D,   0.75D);
    public static final AxisAlignedBB PANEL_BOUNDS_SOUTH = new AxisAlignedBB(    0.0D,    0.0D,    0.0D,    1.0D,    1.0D, 0.3125D);
    public static final AxisAlignedBB PANEL_BOUNDS_NORTH = new AxisAlignedBB(    0.0D,    0.0D, 0.6875D,    1.0D,    1.0D,    1.0D);
    public static final AxisAlignedBB PANEL_BOUNDS_WEST  = new AxisAlignedBB( 0.6875D,    0.0D,    0.0D,    1.0D,    1.0D,    1.0D);
    public static final AxisAlignedBB PANEL_BOUNDS_EAST  = new AxisAlignedBB(    0.0D,    0.0D,    0.0D, 0.3125D,    1.0D,    1.0D);
    public static final AxisAlignedBB PANEL_BOUNDS_UP    = new AxisAlignedBB(    0.0D,    0.0D,    0.0D,    1.0D, 0.3125D,    1.0D);
    public static final AxisAlignedBB PANEL_BOUNDS_DOWN  = new AxisAlignedBB(    0.0D, 0.6875D,    0.0D,    1.0D,    1.0D,    1.0D);

    public static final float BTN_X  = 15.5f / 16f;
    public static final float BTN_Y1 =   12f / 16f;
    public static final float BTN_Y2 =    1f / 16f;
    public static final float BTN_W  =    3f / 16f;
    public static final float BTN_D  =    4f / 16f;
    public static final float BTN_ZS =   12f / 16f;
    public static final float BTN_ZE =   11f / 16f;

    public static final AxisAlignedBB BUTTON_1 = new AxisAlignedBB(BTN_X - 0 * BTN_D, BTN_Y1, BTN_ZS, BTN_X - 0 * BTN_D - BTN_W, BTN_Y1 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_2 = new AxisAlignedBB(BTN_X - 1 * BTN_D, BTN_Y1, BTN_ZS, BTN_X - 1 * BTN_D - BTN_W, BTN_Y1 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_3 = new AxisAlignedBB(BTN_X - 2 * BTN_D, BTN_Y1, BTN_ZS, BTN_X - 2 * BTN_D - BTN_W, BTN_Y1 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_4 = new AxisAlignedBB(BTN_X - 3 * BTN_D, BTN_Y1, BTN_ZS, BTN_X - 3 * BTN_D - BTN_W, BTN_Y1 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_5 = new AxisAlignedBB(BTN_X - 0 * BTN_D, BTN_Y2, BTN_ZS, BTN_X - 0 * BTN_D - BTN_W, BTN_Y2 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_6 = new AxisAlignedBB(BTN_X - 1 * BTN_D, BTN_Y2, BTN_ZS, BTN_X - 1 * BTN_D - BTN_W, BTN_Y2 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_7 = new AxisAlignedBB(BTN_X - 2 * BTN_D, BTN_Y2, BTN_ZS, BTN_X - 2 * BTN_D - BTN_W, BTN_Y2 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_8 = new AxisAlignedBB(BTN_X - 3 * BTN_D, BTN_Y2, BTN_ZS, BTN_X - 3 * BTN_D - BTN_W, BTN_Y2 + BTN_W, BTN_ZE);
    public static final AxisAlignedBB BUTTON_M = new AxisAlignedBB(4f / 16f, 5.5f / 16f, BTN_ZS, 12f / 16f, 10.5f / 16f, 10.5f / 16f);

    private final Map<Integer, AxisAlignedBB> hilightBoxMap = new HashMap<Integer, AxisAlignedBB>();

    public BlockPortalPanel(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.propFacing = FACING;
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING));
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { this.blockName };
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 0x7));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityPortalPanel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == false)
        {
            // Returns the "id" of the pointed element of this block the player is currently looking at.
            // The target selection buttons are ids 0..7, the middle button is 8 and the base of the panel is 9.
            Integer id = this.getPointedElementId(world, pos, state.getValue(FACING), player);

            if (id != null && id >= 0 && id <= 8)
            {
                TileEntityPortalPanel te = getTileEntitySafely(world, pos, TileEntityPortalPanel.class);

                if (te != null)
                {
                    if (id == 8)
                    {
                        te.tryTogglePortal();
                    }
                    else
                    {
                        te.setActiveTargetId(id);
                        world.notifyBlockUpdate(pos, state, state, 3);
                    }

                    world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.MASTER, 0.5f, 1.0f);
                }

                return true;
            }
        }

        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand)
                .withProperty(FACING, facing);
    }

    @Override
    protected EnumFacing getPlacementFacing(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        // Retain the facing from onBlockPlaced
        return state.getValue(FACING);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        state = state.getActualState(blockAccess, pos);

        switch (state.getValue(FACING))
        {
            case EAST:
                return PANEL_BOUNDS_EAST;
            case WEST:
                return PANEL_BOUNDS_WEST;
            case NORTH:
                return PANEL_BOUNDS_NORTH;
            case SOUTH:
                return PANEL_BOUNDS_SOUTH;
            case UP:
                return PANEL_BOUNDS_UP;
            default:
                return PANEL_BOUNDS_DOWN;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        AxisAlignedBB bb = RenderEventHandler.getInstance().getPointedHilightBox(this);

        if (bb != null)
        {
            return bb;
        }

        return state.getBoundingBox(worldIn, pos).offset(pos);
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

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, AxisAlignedBB> getHilightBoxMap()
    {
        return this.hilightBoxMap;
    }

    @Override
    public void updateBlockHilightBoxes(World world, BlockPos pos, EnumFacing facing)
    {
        Map<Integer, AxisAlignedBB> boxMap = this.getHilightBoxMap();
        boxMap.clear();
        Vec3d reference = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // The button AABBs are defined in the NORTH orientation
        boxMap.put(0, PositionUtils.rotateBoxAroundPoint(BUTTON_1.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(1, PositionUtils.rotateBoxAroundPoint(BUTTON_2.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(2, PositionUtils.rotateBoxAroundPoint(BUTTON_3.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(3, PositionUtils.rotateBoxAroundPoint(BUTTON_4.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(4, PositionUtils.rotateBoxAroundPoint(BUTTON_5.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(5, PositionUtils.rotateBoxAroundPoint(BUTTON_6.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(6, PositionUtils.rotateBoxAroundPoint(BUTTON_7.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(7, PositionUtils.rotateBoxAroundPoint(BUTTON_8.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(8, PositionUtils.rotateBoxAroundPoint(BUTTON_M.offset(pos), reference, EnumFacing.NORTH, facing));
        boxMap.put(9, PositionUtils.rotateBoxAroundPoint(PANEL_BOUNDS_BASE.offset(pos), reference, EnumFacing.NORTH, facing));
    }
}
