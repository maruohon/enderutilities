package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class MachineToolWorkstation extends Machine
{
    public MachineToolWorkstation(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public boolean breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            // The tool ItemStack
            BlockEnderUtilitiesInventory.dropItemStacks(world, x, y, z, teeui.getStackInSlot(0), -1, false);

            // Note, we don't drop the module ItemStacks (the ones storing the modules stored in the tools),
            // because they are always stored in the tool, and dropping the modules would duplicate them.

            // The module storage ItemStacks
            for (int i = TileEntityToolWorkstation.SLOT_MODULE_STORAGE_START; i < teeui.getSizeInventory(); ++i)
            {
                BlockEnderUtilitiesInventory.dropItemStacks(world, x, y, z, teeui.getStackInSlot(i), -1, false);
            }
        }

        return true; // We want this to override any other (custom/mod) block breaking methods
    }
}
