package fi.dy.masa.enderutilities.block.machine;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import gnu.trove.map.hash.TIntObjectHashMap;

/*
 * Machine specific data, such as icons.
 * The basic structure of this class has been inspired by MineFactoryReloaded, credits powercrystals, skyboy + others.
 */
public class Machine
{
    protected static TIntObjectHashMap<Machine> machines = new TIntObjectHashMap<Machine>();

    public static Machine enderFurnace = new MachineEnderFurnace(0, 0, ReferenceBlocksItems.NAME_TILEENTITY_ENDER_FURNACE, TileEntityEnderFurnace.class, "pickaxe", 1, 6.0f);
    public static Machine toolWorkstation = new Machine(0, 1, ReferenceBlocksItems.NAME_TILEENTITY_TOOL_WORKSTATION, TileEntityToolWorkstation.class, "pickaxe", 1, 6.0f);

    protected int blockIndex;
    protected int blockMeta;
    protected int machineIndex;
    protected String blockName;
    protected Class<? extends TileEntityEnderUtilities> tileEntityClass;
    protected String toolClass;
    protected int harvestLevel;
    protected float blockHardness;
    protected IIcon[] icons;

    public Machine(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        this.blockIndex = index;
        this.blockMeta = meta;
        this.machineIndex = (index << 4) | (meta & 0x0F);
        this.blockName = name;
        this.tileEntityClass = TEClass;
        this.toolClass = tool;
        this.harvestLevel = harvestLevel;
        this.blockHardness = hardness;
        machines.put(this.machineIndex, this);
    }

    public String getBlockName()
    {
        return this.blockName;
    }

    public static Machine getMachine(int blockIndex, int meta)
    {
        return machines.get((blockIndex << 4) | (meta & 0x0F));
    }

    public TileEntityEnderUtilities createNewTileEntity()
    {
        try
        {
            TileEntityEnderUtilities te = this.tileEntityClass.newInstance();
            return te;
        }
        catch (IllegalAccessException e)
        {
            EnderUtilities.logger.fatal("Unable to create instance of TileEntity from %s (IllegalAccessException)", this.tileEntityClass.getName());
            return null;
        }
        catch (InstantiationException e)
        {
            EnderUtilities.logger.fatal("Unable to create instance of TileEntity from %s (InstantiationException)", this.tileEntityClass.getName());
            return null;
        }
    }

    public static String[] getNames(int blockIndex)
    {
        String[] names = new String[16];
        Machine m;
        for (int meta = 0; meta < 16; ++meta)
        {
            m = getMachine(blockIndex, meta);
            if (m != null)
            {
                names[meta] = m.blockName;
            }
        }

        return names;
    }

    public static Block setBlockHardness(Block block, int blockIndex)
    {
        Machine m;
        for (int meta = 0; meta < 16; ++meta)
        {
            if (machines.containsKey(blockIndex << 4 | meta) == true)
            {
                m = machines.get((blockIndex << 4) | (meta & 0x0F));
                if (m != null)
                {
                    block.setHardness(m.blockHardness);
                    break; // Since hardness is not meta sensitive, we set the hardness to the first one found for this block id
                }
            }
        }
        return block;
    }

    public static Block setBlockHarvestLevels(Block block, int blockIndex)
    {
        // Wood: 0; Gold: 0; Stone: 1; Iron: 2; Diamond: 3
        Machine m;
        for (int meta = 0; meta < 16; ++meta)
        {
            if (machines.containsKey(blockIndex << 4 | meta) == true)
            {
                m = machines.get((blockIndex << 4) | (meta & 0x0F));
                if (m != null)
                {
                    block.setHarvestLevel(m.toolClass, m.harvestLevel, meta);
                }
            }
        }
        return block;
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
    }

    @SideOnly(Side.CLIENT)
    public static void getSubBlocks(int blockIndex, Block block, Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 16; ++meta)
        {
            if (machines.containsKey(blockIndex << 4 | meta) == true && machines.get(blockIndex << 4 | meta) != null)
            {
                list.add(new ItemStack(block, 1, meta));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side)
    {
        // These are for the rendering in ItemBlock form in inventories etc.

        if (side == 0 || side == 1)
        {
            return this.icons[1]; // top
        }
        if (side == 3)
        {
            return this.icons[2]; // front
        }

        return this.icons[4]; // side (left)
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(TileEntityEnderUtilities te, int side)
    {
        // FIXME we should get the proper side (left, right, back) textures based on the TE rotation and the side argument
        if (side == 0 || side == 1)
        {
            return this.icons[side];
        }

        if (te != null && side == te.getRotation())
        {
            return this.icons[2]; // front
        }

        return this.icons[4];
    }

    @SideOnly(Side.CLIENT)
    protected void registerIcons(IIconRegister iconRegister)
    {
        this.icons = new IIcon[6];
        this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".bottom");
        this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top");
        this.icons[2] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front");
        this.icons[3] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back");
        this.icons[4] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".left");
        this.icons[5] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".right");
    }

    @SideOnly(Side.CLIENT)
    public static void registerIcons(int blockIndex, IIconRegister iconRegister)
    {
        Machine m;
        for (int meta = 0; meta < 16; ++meta)
        {
            if (machines.containsKey(blockIndex << 4 | meta) == true)
            {
                m = machines.get(blockIndex << 4 | meta);
                if (m != null)
                {
                    m.registerIcons(iconRegister);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
    }
}
