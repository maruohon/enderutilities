package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

            for (int i1 = 0; i1 < teeui.getSizeInventory(); ++i1)
            {
                ItemStack itemstack = teeui.getStackInSlot(i1);

                if (itemstack != null)
                {
                    float f = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.stackSize > 0)
                    {
                        int j1 = world.rand.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize)
                        {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

                        if (itemstack.hasTagCompound())
                        {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);
                        world.spawnEntityInWorld(entityitem);
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
