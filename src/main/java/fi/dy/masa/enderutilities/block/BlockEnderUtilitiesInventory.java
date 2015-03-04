package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            if (machine.breakBlock(world, x, y, z, block, meta) == true)
            {
                world.removeTileEntity(x, y, z);
                return;
            }
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            for (int i = 0; i < teeui.getSizeInventory(); ++i)
            {
                dropItemStacks(world, x, y, z, teeui.getStackInSlot(i), -1, false);
            }
        }

        world.removeTileEntity(x, y, z);
    }

    /**
     * Drops/spawns EntityItems to the world from the provided ItemStack stack.
     * The number of items dropped is dictated by the parameter amount.
     * If amount is >= 0, then stack is only the ItemStack template; amount can also be larger than stack.stackSize.
     * However, if amount is < 0, then stack.stackSize is used.
     * @param world
     * @param pos
     * @param stack The template ItemStack of the dropped items.
     * @param amount Amount of items to spawn; if >= 0, stack is only a template. If negative, stack.stackSize is used.
     * @param dropFullStacks If false, then the stackSize of the the spawned EntityItems is randomized between 10..32
     */
    public static void dropItemStacks(World world, int x, int y, int z, ItemStack stack, int amount, boolean dropFullStacks)
    {
        if (stack == null)
        {
            return;
        }

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
    public int getComparatorInputOverride(World world, int x, int y, int z, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if ((te instanceof IInventory) == false)
        {
            return 0;
        }

        return Container.calcRedstoneFromInventory((IInventory)te);
    }
}
