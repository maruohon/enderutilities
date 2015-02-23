package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesInventory extends BlockEnderUtilitiesTileEntity
{
    public BlockEnderUtilitiesInventory(int index, String name, float hardness)
    {
        super(index, name, hardness);
    }

    public BlockEnderUtilitiesInventory(int index, String name, float hardness, Material material)
    {
        super(index, name, hardness, material);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        Machine machine = Machine.getMachine(this.blockIndex, this.getMetaFromState(iBlockState));
        if (machine != null)
        {
            if (machine.breakBlock(world, pos, iBlockState) == true)
            {
                world.removeTileEntity(pos);
                return;
            }
        }

        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            for (int i = 0; i < teeui.getSizeInventory(); ++i)
            {
                dropItemStacks(world, pos, teeui.getStackInSlot(i), -1, false);
            }
        }

        world.removeTileEntity(pos);
    }

    public static void dropItemStacks(World world, BlockPos pos, ItemStack stack, int amount, boolean dropFullStacks)
    {
        if (stack == null)
        {
            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        double xr = world.rand.nextFloat() * -0.5d + 0.75d + x;
        double yr = world.rand.nextFloat() * -0.5d + 0.75d + y;
        double zr = world.rand.nextFloat() * -0.5d + 0.75d + z;
        double motionScale = 0.04d;

        if (amount < 0)
        {
            amount = stack.stackSize;
        }

        int max = stack.getMaxStackSize();
        if (max <= 0)
        {
            EnderUtilities.logger.error("BlockEnderUtilitiesInventory.dropItemStack(): Max size of ItemStack to drop was <= 0");
            return;
        }

        int num = max;

        while (amount > 0)
        {
            if (dropFullStacks == false)
            {
                num = Math.min(world.rand.nextInt(23) + 10, max);
            }

            num = Math.min(num, amount);
            ItemStack dropStack = stack.copy();
            dropStack.stackSize = num;
            amount -= num;

            EntityItem entityItem = new EntityItem(world, xr, yr, zr, dropStack);
            entityItem.motionX = world.rand.nextGaussian() * motionScale;
            entityItem.motionY = world.rand.nextGaussian() * motionScale + 0.3d;
            entityItem.motionZ = world.rand.nextGaussian() * motionScale;

            world.spawnEntityInWorld(entityItem);
        }
    }

    @Override
    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if ((te instanceof IInventory) == false)
        {
            return 0;
        }

        return Container.calcRedstoneFromInventory((IInventory)te);
    }
}
