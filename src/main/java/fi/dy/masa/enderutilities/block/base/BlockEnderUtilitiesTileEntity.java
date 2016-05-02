package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities
{
    public static final PropertyDirection FACING = BlockProperties.FACING;

    public BlockEnderUtilitiesTileEntity(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
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
        if (nbt != null && nbt.hasKey("BlockEntityData", Constants.NBT.TAG_COMPOUND) == true)
        {
            teeu.readFromNBTCustom(nbt.getCompoundTag("BlockEntityData"));
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
        EnumFacing facing = placer.getHorizontalFacing().getOpposite();
        teeu.setRotation(facing.getIndex());
    }

    public boolean isTileEntityValid(TileEntity te)
    {
        return te != null && te.isInvalid() == false;
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
}
