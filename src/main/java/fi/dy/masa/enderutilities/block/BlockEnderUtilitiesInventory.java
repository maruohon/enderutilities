package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            for (int i = 0; i < teeui.getSizeInventory(); ++i)
            {
                ItemStack stack = teeui.getStackInSlot(i);

                if (stack != null)
                {
                    double xr = world.rand.nextFloat() * -0.5d + 0.75d + x;
                    double yr = world.rand.nextFloat() * -0.5d + 0.75d + y;
                    double zr = world.rand.nextFloat() * -0.5d + 0.75d + z;

                    while (stack.stackSize > 0)
                    {
                        int num = world.rand.nextInt(21) + 10;
                        if (num > stack.stackSize)
                        {
                            num = stack.stackSize;
                        }

                        ItemStack dropStack = stack.copy();
                        dropStack.stackSize = num;
                        stack.stackSize -= num;
                        EntityItem entityItem = new EntityItem(world, xr, yr, zr, dropStack);

                        double motionScale = 0.04d;
                        entityItem.motionX = world.rand.nextGaussian() * motionScale;
                        entityItem.motionY = world.rand.nextGaussian() * motionScale + 0.3d;
                        entityItem.motionZ = world.rand.nextGaussian() * motionScale;

                        world.spawnEntityInWorld(entityItem);
                    }
                }
            }

            //world.func_147453_f(x, y, z, block); // this gets called in World.removeTileEntity()
        }
    
        super.breakBlock(world, x, y, z, block, meta);
        //world.removeTileEntity(x, y, z);
    }

    // If this returns true, then comparators facing away from this block will use the value from
    // getComparatorInputOverride instead of the actual redstone signal strength.
    @Override
    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    // If hasComparatorInputOverride returns true, the return value from this is used instead of the redstone signal
    // strength when this block inputs to a comparator.
    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IInventory == false)
        {
            return 0;
        }

        return Container.calcRedstoneFromInventory((IInventory)te);
    }
}
