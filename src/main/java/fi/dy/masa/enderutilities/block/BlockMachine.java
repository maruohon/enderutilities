package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderElevator;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockMachine extends BlockEnderUtilitiesInventory
{
    public static final PropertyEnum<BlockMachine.EnumMachineType> TYPE =
            PropertyEnum.<BlockMachine.EnumMachineType>create("type", BlockMachine.EnumMachineType.class);

    public BlockMachine(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(TYPE, BlockMachine.EnumMachineType.ENDER_INFUSER)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TYPE, FACING });
    }

    @Override
    public String[] getUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER,
                ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION,
                ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION,
                ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED,
                ReferenceNames.NAME_TILE_ENTITY_ENDER_ELEVATOR
        };
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        EnumMachineType type = state.getValue(TYPE);
        switch (type)
        {
            case CREATION_STATION: return new TileEntityCreationStation();
            case ENDER_INFUSER: return new TileEntityEnderInfuser();
            case QUICK_STACKER: return new TileEntityQuickStackerAdvanced();
            case TOOL_WORKSTATION: return new TileEntityToolWorkstation();
            case ENDERELEVATOR: return new TileEntityEnderElevator();
        }

        return new TileEntityEnderInfuser();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (state.getValue(TYPE) == EnumMachineType.ENDERELEVATOR)
        {
            ItemStack stack = EntityUtils.getHeldItemOfType(playerIn, ItemDye.class);

            if (worldIn.isRemote == false && stack != null)
            {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityEnderElevator)
                {
                    EnumDyeColor stackColor = EnumDyeColor.byDyeDamage(stack.getMetadata());

                    if (((TileEntityEnderElevator)te).getColor() != stackColor)
                    {
                        ((TileEntityEnderElevator)te).setColor(stackColor);
                        worldIn.notifyBlockUpdate(pos, state, state, 3);
                        stack.stackSize--;
                        return true;
                    }
                }
            }

            return false;
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEnderUtilities)
            {
                ((TileEntityEnderUtilities)te).onLeftClickBlock(playerIn);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityCreationStation)
        {
            // Drop the items from the furnace inventories
            IItemHandler inv = ((TileEntityCreationStation)te).getFurnaceInventory();

            for (int i = 0; i < inv.getSlots(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                {
                    EntityUtils.dropItemStacksInWorld(worldIn, pos, stack, -1, true);
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        if (state.getValue(TYPE) == EnumMachineType.ENDERELEVATOR)
        {
            return false;
        }

        return true;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if (state.getValue(TYPE) == EnumMachineType.CREATION_STATION)
        {
            return 10;
        }

        return super.getLightValue(state, worldIn, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, EnumMachineType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityEnderUtilities)
        {
            EnumFacing facing = EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation());

            if (facing.getAxis().isHorizontal() == true)
            {
                state = state.withProperty(FACING, facing);
            }
        }

        return state;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            if (((TileEntityEnderFurnace)te).isBurningLast == true)
            {
                Effects.spawnParticlesAround(worldIn, EnumParticleTypes.PORTAL, pos, 2, rand);
            }
        }
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < EnumMachineType.values().length; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumMachineType implements IStringSerializable
    {
        ENDER_INFUSER(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER),
        TOOL_WORKSTATION(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION),
        CREATION_STATION(ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION),
        QUICK_STACKER(ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED),
        ENDERELEVATOR(ReferenceNames.NAME_TILE_ENTITY_ENDER_ELEVATOR);

        private final String name;

        private EnumMachineType(String name)
        {
            this.name = name;
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
            return this.ordinal();
        }

        public static EnumMachineType fromMeta(int meta)
        {
            return meta < values().length ? values()[meta] : ENDER_INFUSER;
        }
    }
}
