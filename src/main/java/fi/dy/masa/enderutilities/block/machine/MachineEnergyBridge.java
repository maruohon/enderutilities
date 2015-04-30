package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
    }

    @Override
    public boolean breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        return false;
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
