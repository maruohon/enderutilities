package fi.dy.masa.enderutilities.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class BlockFloor extends BlockEnderUtilities
{
    public static final AxisAlignedBB BOUNDS_BOTTOM = new AxisAlignedBB( 0.0,    0.0,  0.0, 1.0, 0.1875, 1.0);
    public static final AxisAlignedBB BOUNDS_TOP    = new AxisAlignedBB( 0.0, 0.8125,  0.0, 1.0,    1.0, 1.0);

    public static final PropertyEnum<EnumHalf> HALF = PropertyEnum.<EnumHalf>create("half", EnumHalf.class);
    public static final PropertyEnum<FloorType> TYPE = PropertyEnum.<FloorType>create("type", FloorType.class);

    public BlockFloor(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(HALF, EnumHalf.TOP)
                .withProperty(TYPE, FloorType.NORMAL));
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { HALF, TYPE });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_FLOOR,
                ReferenceNames.NAME_TILE_FLOOR + "_cracked"
        };
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
                .withProperty(HALF, EnumHalf.fromMeta(meta))
                .withProperty(TYPE, FloorType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta() | state.getValue(HALF).getMeta();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);

        if (facing != EnumFacing.UP && hitY > 0.5f)
        {
            state = state.withProperty(HALF, EnumHalf.TOP);
        }

        return state;
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
    {
        return state.getValue(HALF).equals(EnumHalf.BOTTOM) ? BOUNDS_BOTTOM : BOUNDS_TOP;
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
        if (state.getValue(TYPE) == FloorType.CRACKED)
        {
            if ((entityIn instanceof EntityItem) == false && (entityIn instanceof EntityXPOrb) == false)
            {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
            }
        }
        else
        {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < FloorType.values().length; i++)
        {
            list.add(new ItemStack(this, 1, FloorType.values()[i].getMeta()));
        }
    }

    public static enum EnumHalf implements IStringSerializable
    {
        BOTTOM  (0, "bottom"),
        TOP     (8, "top");

        private final String name;
        private final int meta;

        private EnumHalf(int meta, String name)
        {
            this.name = name;
            this.meta = meta;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static EnumHalf fromMeta(int meta)
        {
            return values()[(meta & 0x8) >>> 3];
        }
    }

    public static enum FloorType implements IStringSerializable
    {
        NORMAL  (0, "normal"),
        CRACKED (1, "cracked");

        private final String name;
        private final int meta;

        private FloorType(int meta, String name)
        {
            this.name = name;
            this.meta = meta;
        }

        public String toString()
        {
            return this.name;
        }

        public String getName()
        {
            return this.name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static FloorType fromMeta(int meta)
        {
            return values()[(meta & 0x3)];
        }
    }
}
