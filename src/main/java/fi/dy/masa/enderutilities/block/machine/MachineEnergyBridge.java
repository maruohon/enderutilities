package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, livingBase, stack);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(world, x, y, z);
            }
        }
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
    {
        super.onBlockPreDestroy(world, x, y, z, oldMeta);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(world, x, y, z, oldMeta);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z, Block block, int meta)
    {
        return 12;
    }
}
