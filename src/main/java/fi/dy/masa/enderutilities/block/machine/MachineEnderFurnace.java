package fi.dy.masa.enderutilities.block.machine;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
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
    public boolean breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te != null && te instanceof TileEntityEnderFurnace)
        {
            // Drop the items from the output buffer
            TileEntityEnderFurnace teef = (TileEntityEnderFurnace)te;
            BlockEnderUtilitiesInventory.dropItemStacks(world, x, y, z, teef.getOutputBufferStack(), teef.getOutputBufferAmount(), true);
        }
    
        // We want the default BlockEnderUtilitiesInventory.breakBlock() to deal with the generic inventory
        return false;
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
        this.iconSide           = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".side");
        this.iconTop            = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".top");
        this.iconFront          = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".front.off");
        this.iconFrontOnSlow    = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".front.on.slow");
        this.iconFrontOnFast    = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".front.on.fast");
        this.iconFrontOnNofuel  = iconRegister.registerIcon(ReferenceTextures.getTileName(ReferenceNames.NAME_ITEM_ENDER_FURNACE) + ".front.on.nofuel");
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
