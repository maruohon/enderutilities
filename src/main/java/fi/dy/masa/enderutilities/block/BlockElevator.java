package fi.dy.masa.enderutilities.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderElevator;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockElevator extends BlockEnderUtilitiesTileEntity
{
    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);

    public BlockElevator(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(COLOR, EnumDyeColor.WHITE)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { COLOR, FACING });
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        String[] names = new String[EnumDyeColor.values().length];

        int i = 0;
        for (EnumDyeColor color : EnumDyeColor.values())
        {
            names[i++] = ReferenceNames.NAME_TILE_ENTITY_ENDER_ELEVATOR + "_" + color.getName();
        }

        return names;
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        return new TileEntityEnderElevator();
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(COLOR).getMetadata();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    /*@Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }*/

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(playerIn, ItemDye.class);

        if (stack != null)
        {
            EnumDyeColor stackColor = EnumDyeColor.byDyeDamage(stack.getMetadata());

            if (state.getValue(COLOR) != stackColor)
            {
                if (worldIn.isRemote == false)
                {
                    worldIn.setBlockState(pos, state.withProperty(COLOR, stackColor), 3);
                    stack.stackSize--;
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);

        if (worldIn.isRemote == true)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderElevator)
        {
            ((TileEntityEnderElevator)te).onNeighborBlockChange(worldIn, pos, state, neighborBlock);
        }
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (EnumDyeColor color : EnumDyeColor.values())
        {
            list.add(new ItemStack(item, 1, color.getMetadata()));
        }
    }
}
