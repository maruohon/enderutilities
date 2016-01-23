package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, livingBase, stack);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ITieredStorage)
            {
                int meta = world.getBlockMetadata(x, y, z);
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
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        super.onBlockClicked(world, x, y, z, player);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityHandyChest)
            {
                ((TileEntityHandyChest)te).onLeftClickBlock(player);
            }
        }
    }
}
