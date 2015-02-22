package fi.dy.masa.enderutilities.block.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

/**
 * Machine specific data, such as icons.
 * The basic structure of this class has been inspired by MineFactoryReloaded, credits powercrystals, skyboy + others.
 */
public class Machine
{
    public static final PropertyEnum MACHINE_TYPE = PropertyEnum.create("machinetype", EnumMachine.class);

    protected static final Map<EnumMachine, Machine> machines = new HashMap<EnumMachine, Machine>();
    protected static Machine enderFurnace = new MachineEnderFurnace(EnumMachine.ENDER_FURNACE, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE, TileEntityEnderFurnace.class, "pickaxe", 1, 6.0f);
    protected static Machine toolWorkstation = new MachineToolWorkstation(EnumMachine.TOOL_WORKSTATION, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION, TileEntityToolWorkstation.class, "pickaxe", 1, 6.0f);
    protected static Machine enderInfuser = new Machine(EnumMachine.ENDER_INFUSER, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER, TileEntityEnderInfuser.class, "pickaxe", 1, 6.0f);

    protected EnumMachine machineType;
    protected String blockName;
    protected Class<? extends TileEntityEnderUtilities> tileEntityClass;
    protected String toolClass;
    protected int harvestLevel;
    protected float blockHardness;

    public Machine(EnumMachine machineType, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        this.machineType = machineType;
        this.blockName = name;
        this.tileEntityClass = TEClass;
        this.toolClass = tool;
        this.harvestLevel = harvestLevel;
        this.blockHardness = hardness;
        machines.put(machineType, this);
    }

    public String getBlockName()
    {
        return this.blockName;
    }

    public static Machine getMachine(EnumMachine machineType)
    {
        return machines.get(machineType);
    }

    public static Machine getMachine(int blockIndex, int meta)
    {
        return machines.get(EnumMachine.getMachineType(blockIndex, meta));
    }

    public static EnumMachine getDefaultState(int blockIndex)
    {
        return EnumMachine.getMachineType(blockIndex, 0);
    }

    public static BlockState createBlockState(Block block, int index)
    {
        return new BlockState(block, new IProperty[] {MACHINE_TYPE});
    }

    public int damageDropped()
    {
        return this.machineType.getMetadata();
    }

    public TileEntity createNewTileEntity()
    {
        try
        {
            return this.tileEntityClass.newInstance();
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

    /**
     * Checks if class of the given TileEntity instance matches the one of this machine.
     * @param te The TileEntity to be validated
     * @return true if the given TileEntity is not null and matches the class of this machine's TE
     */
    public boolean isTileEntityValid(TileEntity te)
    {
        if (te != null && te.getClass() == this.tileEntityClass)
        {
            return true;
        }

        return false;
    }

    /**
     * Returns all the block names for the given block index
     * @param blockIndex
     * @return String[] of defined block names
     */
    public static String[] getNames(int blockIndex)
    {
        String[] names = new String[16];

        for (int meta = 0; meta < 16; ++meta)
        {
            Machine machine = getMachine(blockIndex, meta);
            if (machine != null)
            {
                names[meta] = machine.blockName;
            }
            else
            {
                names[meta] = "null";
            }
        }

        return names;
    }

    public static Block setBlockHardness(Block block, int blockIndex)
    {
        for (int meta = 0; meta < 16; ++meta)
        {
            Machine machine = getMachine(blockIndex, meta);
            if (machine != null)
            {
                block.setHardness(machine.blockHardness);
                break; // Since hardness is not meta sensitive, we set the hardness to the first one found for this block id
            }
        }

        return block;
    }

    public static Block setBlockHarvestLevels(Block block, int blockIndex)
    {
        // Wood: 0; Gold: 0; Stone: 1; Iron: 2; Diamond: 3
        for (int meta = 0; meta < 16; ++meta)
        {
            Machine machine = getMachine(blockIndex, meta);
            if (machine != null)
            {
                block.setHarvestLevel(machine.toolClass, machine.harvestLevel, block.getStateFromMeta(meta));
            }
        }

        return block;
    }

    /**
     * The replacement/equivalent of Block.breakBlock() for customized per-machine block breaking behavior.
     * Return true if custom behavior should override the default BlockEnderUtilities*.breakBlock().
     * Note that the vanilla Block.breakBlock() (or equivalent) will still get called! (To deal with the TE removal etc.)
     */
    public boolean breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        return false;
    }

    public int getLightValue(IBlockAccess world, BlockPos pos, IBlockState iBlockState)
    {
        return world.getBlockState(pos).getBlock().getLightValue();
    }

    @SideOnly(Side.CLIENT)
    public static void getSubBlocks(int blockIndex, Block block, Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < 16; ++meta)
        {
            if (getMachine(blockIndex, meta) != null)
            {
                list.add(new ItemStack(block, 1, meta));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState iBlockState, Random rand)
    {
    }
}
