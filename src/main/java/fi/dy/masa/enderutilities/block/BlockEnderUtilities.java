package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockEnderUtilities extends Block
{
    public int blockIndex;

    public BlockEnderUtilities(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
        this.setStepSound(soundTypeStone);
    }

    public BlockEnderUtilities(int index, String name, float hardness, Material material)
    {
        super(material);
        this.setHardness(hardness);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setBlockName(name);
        this.blockIndex = index;
        Machine.setBlockHardness(this, this.blockIndex);
        Machine.setBlockHarvestLevels(this, this.blockIndex);
    }

    @Override
    public int damageDropped(int meta)
    {
        return meta;
    }

    // Called whenever the block is added into the world. Args: world, x, y, z
    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        //super.onBlockAdded(world, x, y, z);
        //this.func_149930_e(world, x, y, z);
        this.onNeighborBlockChange(world, x, y, z, this);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            machine.breakBlock(world, x, y, z, block, meta);
        }

        super.breakBlock(world, x, y, z, block, meta);   // world.removeTileEntity(x, y, z);
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        Machine.getSubBlocks(this.blockIndex, this, item, tab, list);
    }

    // A randomly called display update to be able to add particles or other items for display
    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            machine.randomDisplayTick(world, x, y, z, rand);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.getIcon(side);
        }

        return this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityEnderUtilities)
        {
            Machine machine = Machine.getMachine(this.blockIndex, blockAccess.getBlockMetadata(x, y, z));
            if (machine != null)
            {
                return machine.getIcon((TileEntityEnderUtilities)te, side);
            }
        }

        return this.getIcon(side, blockAccess.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        Machine.registerIcons(this.blockIndex, iconRegister);
    }
}
