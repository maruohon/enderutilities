package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

public class MachineToolWorkstation extends Machine
{
    public MachineToolWorkstation(EnumMachine machineType, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(machineType, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public boolean breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            // The tool ItemStack
            BlockEnderUtilitiesInventory.dropItemStacks(world, pos, teeui.getStackInSlot(0), -1, false);

            // Note, we don't drop the module ItemStacks (the ones storing the modules stored in the tools),
            // because they are always stored in the tool, and dropping the modules would duplicate them.

            // The module storage ItemStacks
            for (int i = TileEntityToolWorkstation.SLOT_MODULE_STORAGE_START; i < teeui.getSizeInventory(); ++i)
            {
                BlockEnderUtilitiesInventory.dropItemStacks(world, pos, teeui.getStackInSlot(i), -1, false);
            }
        }

        return true; // We want this to override any other (custom/mod) block breaking methods
    }
}
