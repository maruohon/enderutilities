package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.MathHelper;
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
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities implements ITileEntityProvider
{
    public static final byte YAW_TO_DIRECTION[] = {2, 5, 3, 4};
    public int blockIndex;

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
    }

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness, Material material)
    {
        super(index, name, hardness, material);
        this.blockIndex = index;
        Machine.setBlockHardness(this, this.blockIndex);
        Machine.setBlockHarvestLevels(this, this.blockIndex);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.createNewTileEntity();
        }

        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState iBlockState, EntityLivingBase livingBase, ItemStack stack)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te == null || (te instanceof TileEntityEnderUtilities) == false)
        {
            return;
        }

        int yaw = MathHelper.floor_double((double)(livingBase.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3;

        TileEntityEnderUtilities teeu = (TileEntityEnderUtilities)te;

        NBTTagCompound nbt = stack.getTagCompound();

        // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
        if (nbt != null && nbt.hasKey("TileEntityData", Constants.NBT.TAG_COMPOUND) == true)
        {
            teeu.readFromNBTCustom(nbt.getCompoundTag("TileEntityData"));

            // Update the rotation
            if (yaw < YAW_TO_DIRECTION.length)
            {
                teeu.setRotation(YAW_TO_DIRECTION[yaw]);
            }
        }
        else
        {
            /*
            if (livingBase.rotationPitch > 45.0f)
            {
                rot = (rot << 4) | 1;
            }
            else if (livingBase.rotationPitch < -45.0f)
            {
                rot = rot << 4;
            }
            else
            {
            */
                // {DOWN, UP, NORTH, SOUTH, WEST, EAST}
                /*switch (yaw)
                {
                    case 0: yaw = 2; break;
                    case 1: yaw = 5; break;
                    case 2: yaw = 3; break;
                    case 3: yaw = 4; break;
                    default:
                }*/
            //}

            if (yaw < YAW_TO_DIRECTION.length)
            {
                teeu.setRotation(YAW_TO_DIRECTION[yaw]);
            }

            if (livingBase instanceof EntityPlayer)
            {
                teeu.setOwner((EntityPlayer)livingBase);
            }

            if (teeu instanceof TileEntityEnderUtilitiesInventory && stack.hasDisplayName())
            {
                ((TileEntityEnderUtilitiesInventory)teeu).setInventoryName(stack.getDisplayName());
            }
        }
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
