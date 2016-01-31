package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities
{
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
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World worldIn, IBlockState state)
    {
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(state));
        if (machine != null)
        {
            return machine.createTileEntity();
        }

        return null;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity te = worldIn.getTileEntity(pos);
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

        // FIXME add the 24-way rotation stuff
        EnumFacing facing = placer.getHorizontalFacing();
        teeu.setRotation(facing.getIndex());

        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(worldIn.getBlockState(pos)));
        if (machine != null)
        {
            machine.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(worldIn.getBlockState(pos)));
        if (machine != null)
        {
            machine.onBlockAdded(worldIn, pos, state);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(worldIn.getBlockState(pos)));
        if (machine != null)
        {
            return machine.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        super.onBlockClicked(worldIn, pos, playerIn);

        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(worldIn.getBlockState(pos)));
        if (machine != null)
        {
            machine.onBlockClicked(worldIn, pos, playerIn);
        }
    }

    // FIXME
    /*@Override
    public void onBlockPreDestroy(World world, BlockPos pos, int oldMeta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(world.getBlockState(pos)));
        if (machine != null)
        {
            machine.onBlockPreDestroy(world, pos, oldMeta);
        }
    }*/

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        int meta = this.getMetaFromState(worldIn.getBlockState(pos));
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            machine.breakBlock(worldIn, pos, state);
        }

        super.breakBlock(worldIn, pos, state); // world.removeTileEntity(pos);
    }

    @Override
    public int getLightValue(IBlockAccess worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();
        if (block != this)
        {
            return block.getLightValue(worldIn, pos);
        }

        IBlockState state = worldIn.getBlockState(pos);
        int meta = this.getMetaFromState(state);
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.getLightValue(worldIn, pos, state);
        }

        return super.getLightValue(worldIn, pos);
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        Machine.getSubBlocks(this.blockIndex, this, item, tab, list);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        int meta = this.getMetaFromState(worldIn.getBlockState(pos));
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            machine.randomDisplayTick(worldIn, pos, state, rand);
        }
    }
}
