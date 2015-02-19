package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.machine.Machine;

public class ItemBlockMachine extends ItemBlockEnderUtilities
{
    public ItemBlockMachine(Block block)
    {
        super(block);

        if (block instanceof BlockEnderUtilitiesTileEntity)
        {
            this.setNames(Machine.getNames(((BlockEnderUtilitiesTileEntity)block).getBlockIndex()));
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }
}
