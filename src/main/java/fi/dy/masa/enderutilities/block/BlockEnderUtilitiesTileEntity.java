package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.machine.EnumMachine;
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities implements ITileEntityProvider
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum MACHINE_TYPE = PropertyEnum.create("machinetype", EnumMachine.class);

    //public static final PropertyEnum FURNACE_STATE = PropertyEnum.create("furnacestate", EnumFurnaceState.class);

    public int blockIndex;

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
    }

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness, Material material)
    {
        super(index, name, hardness, material);
        this.blockIndex = index;
        this.setDefaultState(this.blockState.getBaseState().withProperty(MACHINE_TYPE, Machine.getDefaultState(this.blockIndex)).withProperty(FACING, EnumFacing.NORTH));
        Machine.setBlockHardness(this, this.blockIndex);
        Machine.setBlockHarvestLevels(this, this.blockIndex);
    }

    @Override
    public int damageDropped(IBlockState iBlockState)
    {
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(iBlockState));
        if (machine != null)
        {
            //System.out.println("damageDropped(), machine: " + machine); // FIXME debug
            return machine.damageDropped();
        }

        return this.getMetaFromState(iBlockState);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {MACHINE_TYPE, FACING});
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        //System.out.println("getStateFromMeta(), meta: " + meta); // FIXME debug
        return this.getDefaultState().withProperty(MACHINE_TYPE, EnumMachine.getMachineType(this.blockIndex, meta));
    }

    @Override
    public int getMetaFromState(IBlockState iBlockState)
    {
        //System.out.println("getMetaFromState(), iBlockState: " + iBlockState); // FIXME debug
        return ((EnumMachine)iBlockState.getValue(MACHINE_TYPE)).getMetadata();
    }

    @Override
    public IBlockState getActualState(IBlockState iBlockState, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderUtilities)
        {
            EnumFacing enumFacing = EnumFacing.getFront(((TileEntityEnderUtilities)te).getRotation());

            if (enumFacing.getAxis() == EnumFacing.Axis.Y)
            {
                enumFacing = EnumFacing.NORTH;
            }

            iBlockState = iBlockState.withProperty(FACING, enumFacing);
        }

        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(iBlockState));
        if (machine != null)
        {
            //System.out.println("getActualState(), machine: " + machine); // FIXME debug
            return machine.getActualState(iBlockState, worldIn, pos);
        }

        return iBlockState;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState iBlockState)
    {
        //System.out.println("createTileEntity(), iBlockState: " + iBlockState); // FIXME debug
        return this.createNewTileEntity(world, this.getMetaFromState(iBlockState));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        //System.out.println("createNewTileEntity(), meta: " + meta); // FIXME debug
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            //System.out.println("createNewTileEntity(), machine: " + machine); // FIXME debug
            return machine.createNewTileEntity();
        }

        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState iBlockState, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te == null || (te instanceof TileEntityEnderUtilities) == false)
        {
            return;
        }

        TileEntityEnderUtilities teeu = (TileEntityEnderUtilities)te;
        NBTTagCompound nbt = stack.getTagCompound();

        // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
        if (nbt != null && nbt.hasKey("TileEntityData", Constants.NBT.TAG_COMPOUND) == true)
        {
            teeu.readFromNBTCustom(nbt.getCompoundTag("TileEntityData"));
        }
        else
        {
            if (placer instanceof EntityPlayer)
            {
                teeu.setOwner((EntityPlayer)placer);
            }

            if (teeu instanceof TileEntityEnderUtilitiesInventory && stack.hasDisplayName())
            {
                ((TileEntityEnderUtilitiesInventory)teeu).setInventoryName(stack.getDisplayName());
            }
        }

        // Update the rotation
        teeu.setRotation(placer.getHorizontalFacing().getOpposite().getIndex());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState iBlockState, EntityPlayer player, EnumFacing face, float offsetX, float offsetY, float offsetZ)
    {
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, pos, face, world);
        if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
        {
            return false;
        }

        // TODO: Maybe this should be moved into the Machine class?
        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te == null || te instanceof TileEntityEnderUtilities == false)
            {
                return false;
            }

            Machine machine = Machine.getMachine(this.blockIndex, world.getBlockState(pos).getBlock().getMetaFromState(iBlockState));
            if (machine != null && machine.isTileEntityValid(te) == true)
            {
                player.openGui(EnderUtilities.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockState(pos).getBlock().getMetaFromState(iBlockState));
        if (machine != null)
        {
            machine.breakBlock(world, pos, iBlockState);
        }

        super.breakBlock(world, pos, iBlockState);   // world.removeTileEntity(x, y, z);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState iBlockState = world.getBlockState(pos);
        Block block = iBlockState.getBlock();
        if (block != this)
        {
            return block.getLightValue(world, pos);
        }

        Machine machine = Machine.getMachine(this.blockIndex, block.getMetaFromState(iBlockState));
        if (machine != null)
        {
            return machine.getLightValue(world, pos, iBlockState);
        }

        return super.getLightValue(world, pos);
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        Machine.getSubBlocks(this.blockIndex, this, item, tab, list);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, BlockPos pos, IBlockState iBlockState, Random rand)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockState(pos).getBlock().getMetaFromState(iBlockState));
        if (machine != null)
        {
            machine.randomDisplayTick(world, pos, iBlockState, rand);
        }
    }
}
