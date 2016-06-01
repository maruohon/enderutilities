package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class BlockPortalPanel extends BlockEnderUtilitiesInventory
{
    protected static final AxisAlignedBB PANEL_BOUNDS_SOUTH = new AxisAlignedBB(  0.0D,  0.0D,  0.0D,  1.0D,  1.0D, 0.25D);
    protected static final AxisAlignedBB PANEL_BOUNDS_NORTH = new AxisAlignedBB(  0.0D,  0.0D, 0.75D,  1.0D,  1.0D,  1.0D);
    protected static final AxisAlignedBB PANEL_BOUNDS_WEST  = new AxisAlignedBB( 0.75D,  0.0D,  0.0D,  1.0D,  1.0D,  1.0D);
    protected static final AxisAlignedBB PANEL_BOUNDS_EAST  = new AxisAlignedBB(  0.0D,  0.0D,  0.0D, 0.25D,  1.0D,  1.0D);
    protected static final AxisAlignedBB PANEL_BOUNDS_UP    = new AxisAlignedBB(  0.0D,  0.0D,  0.0D,  1.0D, 0.25D,  1.0D);
    protected static final AxisAlignedBB PANEL_BOUNDS_DOWN  = new AxisAlignedBB(  0.0D, 0.75D,  0.0D,  1.0D,  1.0D,  1.0D);

    public BlockPortalPanel(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, BlockEnderUtilities.DEFAULT_FACING));
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { ReferenceNames.NAME_TILE_PORTAL_PANEL };
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        return new TileEntityPortalPanel();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote == false && side == state.getActualState(worldIn, pos).getValue(FACING))
        {
            int id = this.getTargetId(hitX, hitY, hitZ, side);

            if (id >= 0)
            {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityPortalPanel)
                {
                    if (id == 8)
                    {
                        ((TileEntityPortalPanel) te).tryTogglePortal();
                    }
                    else
                    {
                        ((TileEntityPortalPanel) te).setActiveTargetId(id);
                        worldIn.notifyBlockUpdate(pos, state, state, 3);
                    }

                    worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.MASTER, 0.5f, 1.0f);
                }

                return true;
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, facing);
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

    private int getTargetId(float x, float y, float z, EnumFacing side)
    {
        //System.out.printf("x: %.3f, y: %.3f, z: %.3f side: %s\n", x, y, z, side);

        float tmp = 0f;

        switch (side)
        {
            case UP:
                x = 1f - x;
                y = z;
                break;
            case DOWN:
                x = 1f - x;
                y = 1f - z;
                break;
            case WEST:
                tmp = z;
                z = x;
                x = tmp;
                break;
            case EAST:
                tmp = 1f - z;
                z = 1f - x;
                x = tmp;
                break;
            case NORTH:
                x = 1f - x;
                z = 1f - z;
                break;
            case SOUTH:
        }

        if (this.isPointInsideRegion(x, y,  1f / 32f, 12f / 16f,  7f / 32f, 15f / 16f)) { return 0; }
        if (this.isPointInsideRegion(x, y,  9f / 32f, 12f / 16f, 15f / 32f, 15f / 16f)) { return 1; }
        if (this.isPointInsideRegion(x, y, 17f / 32f, 12f / 16f, 23f / 32f, 15f / 16f)) { return 2; }
        if (this.isPointInsideRegion(x, y, 25f / 32f, 12f / 16f, 31f / 32f, 15f / 16f)) { return 3; }

        if (this.isPointInsideRegion(x, y,  1f / 32f,  1f / 16f,  7f / 32f,  4f / 16f)) { return 4; }
        if (this.isPointInsideRegion(x, y,  9f / 32f,  1f / 16f, 15f / 32f,  4f / 16f)) { return 5; }
        if (this.isPointInsideRegion(x, y, 17f / 32f,  1f / 16f, 23f / 32f,  4f / 16f)) { return 6; }
        if (this.isPointInsideRegion(x, y, 25f / 32f,  1f / 16f, 31f / 32f,  4f / 16f)) { return 7; }

        if (this.isPointInsideRegion(x, y,  4f / 16f, 11f / 32f, 12f / 16f, 21f / 32f)) { return 8; }

        return -1;
    }

    private boolean isPointInsideRegion(float x, float y, float minX, float minY, float maxX, float maxY)
    {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
