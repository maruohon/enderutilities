package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import fi.dy.masa.enderutilities.tileentity.ITieredStorage;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;

public class MachineStorage extends Machine
{
    public MachineStorage(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof ITieredStorage)
            {
                // FIXME add properties for the type/tier
                IBlockState iBlockState = worldIn.getBlockState(pos);
                int meta = iBlockState.getBlock().getMetaFromState(iBlockState);
                int tier = 0;

                // Templated Chests
                if (meta <= 2)
                {
                    tier = meta;
                }
                // Handy Chests
                else if (meta <= 5)
                {
                    tier = meta - 3;
                }

                ((ITieredStorage)te).setStorageTier(tier);
            }
        }
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        super.onBlockClicked(worldIn, pos, playerIn);

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityHandyChest)
            {
                ((TileEntityHandyChest)te).onLeftClickBlock(playerIn);
            }
        }
    }
}
