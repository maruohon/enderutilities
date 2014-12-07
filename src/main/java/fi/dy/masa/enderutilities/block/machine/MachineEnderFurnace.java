package fi.dy.masa.enderutilities.block.machine;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineEnderFurnace extends Machine
{
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconFrontOnSlow;
    @SideOnly(Side.CLIENT)
    private IIcon iconFrontOnFast;
    @SideOnly(Side.CLIENT)
    private IIcon iconFrontOnNofuel;

    public MachineEnderFurnace(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;

            // Drop the items from the output buffer
            if (teef.getOutputBufferAmount() > 0 && teef.getOutputBufferStack() != null)
            {
                int amount = teef.getOutputBufferAmount();
                ItemStack stack = teef.getOutputBufferStack();

                double xr = world.rand.nextFloat() * -0.5d + 0.75d + x;
                double yr = world.rand.nextFloat() * -0.5d + 0.75d + y;
                double zr = world.rand.nextFloat() * -0.5d + 0.75d + z;

                int num = 0;
                int max = stack.getMaxStackSize();

                while (amount > 0)
                {
                    num = Math.min(amount, max);
                    ItemStack dropStack = stack.copy();
                    dropStack.stackSize = num;
                    amount -= num;
                    EntityItem entityItem = new EntityItem(world, xr, yr, zr, dropStack);

                    double motionScale = 0.04d;
                    entityItem.motionX = world.rand.nextGaussian() * motionScale;
                    entityItem.motionY = world.rand.nextGaussian() * motionScale + 0.3d;
                    entityItem.motionZ = world.rand.nextGaussian() * motionScale;

                    world.spawnEntityInWorld(entityItem);
                }
            }
        }
    
        super.breakBlock(world, x, y, z, block, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side)
    {
        // These are for the rendering in ItemBlock form in inventories etc.
        if (side == 0 || side == 1)
        {
            return this.iconTop;
        }
        if (side == 3)
        {
            return this.iconFront;
        }

        return this.iconSide;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(TileEntityEnderUtilities te, int side)
    {
        if (side == 0 || side == 1)
        {
            return this.iconTop;
        }

        if (te != null && te instanceof TileEntityEnderFurnace && side == ((TileEntityEnderFurnace)te).getRotation())
        {
            if (((TileEntityEnderFurnace)te).isActive == false)
            {
                return this.iconFront;
            }

            if (((TileEntityEnderFurnace)te).usingFuel == true)
            {
                if (((TileEntityEnderFurnace)te).operatingMode == 1)
                {
                    return this.iconFrontOnFast;
                }
                return this.iconFrontOnSlow;
            }
            return this.iconFrontOnNofuel;
        }

        return this.iconSide;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void registerIcons(IIconRegister iconRegister)
    {
        this.iconSide           = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".side");
        this.iconTop            = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".top");
        this.iconFront          = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".front.off");
        this.iconFrontOnSlow    = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".front.on.slow");
        this.iconFrontOnFast    = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".front.on.fast");
        this.iconFrontOnNofuel  = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceBlocksItems.NAME_ITEM_ENDER_FURNACE) + ".front.on.nofuel");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            if (((TileEntityEnderFurnace)te).isActive == true)
            {
                Particles.spawnParticlesAround(world, "portal", x, y, z, 2, rand);
            }
        }
    }
}
