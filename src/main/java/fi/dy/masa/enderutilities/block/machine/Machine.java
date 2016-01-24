package fi.dy.masa.enderutilities.block.machine;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityTemplatedChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Machine specific data, such as icons.
 * The basic structure of this class has been inspired by MineFactoryReloaded, credits powercrystals, skyboy + others.
 */
public class Machine
{
    protected static TIntObjectHashMap<Machine> machines = new TIntObjectHashMap<Machine>();

    public static Machine enderFurnace = new MachineEnderFurnace(0, 0, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE, TileEntityEnderFurnace.class, "pickaxe", 1, 6.0f);
    public static Machine toolWorkstation = new Machine(0, 1, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION, TileEntityToolWorkstation.class, "pickaxe", 1, 6.0f);
    public static Machine enderInfuser = new Machine(0, 2, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER, TileEntityEnderInfuser.class, "pickaxe", 1, 6.0f);
    public static Machine creationStation = new MachineCreationStation(0, 3, ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION, TileEntityCreationStation.class, "pickaxe", 1, 10.0f);

    public static Machine energyBridgeTransmitter = new MachineEnergyBridge(1, 0, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);
    public static Machine energyBridgeReceiver = new MachineEnergyBridge(1, 1, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);
    public static Machine energyBridgeResonator = new MachineEnergyBridge(1, 2, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);

    public static Machine templatedChest0 = new MachineStorage(2, 0, ReferenceNames.NAME_TILE_ENTITY_TEMPLATED_CHEST + ".0", TileEntityTemplatedChest.class, "pickaxe", 1, 16.0f);
    public static Machine templatedChest1 = new MachineStorage(2, 1, ReferenceNames.NAME_TILE_ENTITY_TEMPLATED_CHEST + ".1", TileEntityTemplatedChest.class, "pickaxe", 1, 16.0f);
    public static Machine templatedChest2 = new MachineStorage(2, 2, ReferenceNames.NAME_TILE_ENTITY_TEMPLATED_CHEST + ".2", TileEntityTemplatedChest.class, "pickaxe", 1, 16.0f);

    public static Machine handyChest0 = new MachineStorage(2, 3, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + ".0", TileEntityHandyChest.class, "pickaxe", 1, 16.0f);
    public static Machine handyChest1 = new MachineStorage(2, 4, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + ".1", TileEntityHandyChest.class, "pickaxe", 1, 16.0f);
    public static Machine handyChest2 = new MachineStorage(2, 5, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + ".2", TileEntityHandyChest.class, "pickaxe", 1, 16.0f);

    protected int blockIndex;
    protected int blockMeta;
    protected int machineIndex;
    protected String blockName;
    protected Class<? extends TileEntityEnderUtilities> tileEntityClass;
    protected String toolClass;
    protected int harvestLevel;
    protected float blockHardness;

    @SideOnly(Side.CLIENT)
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

    public TileEntity createTileEntity()
    {
        try
        {
            return this.tileEntityClass.newInstance();
        }
        catch (IllegalAccessException e)
        {
            EnderUtilities.logger.fatal(String.format("Unable to create instance of TileEntity from %s (IllegalAccessException)", this.tileEntityClass.getName()));
            return null;
        }
        catch (InstantiationException e)
        {
            EnderUtilities.logger.fatal(String.format("Unable to create instance of TileEntity from %s (InstantiationException)", this.tileEntityClass.getName()));
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
        return te != null && te.getClass() == this.tileEntityClass;
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
            Machine m = getMachine(blockIndex, meta);
            if (m != null)
            {
                names[meta] = m.blockName;
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
            Machine m = getMachine(blockIndex, meta);
            if (m != null)
            {
                block.setHardness(m.blockHardness);
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
            Machine m = getMachine(blockIndex, meta);
            if (m != null)
            {
                block.setHarvestLevel(m.toolClass, m.harvestLevel, meta);
            }
        }

        return block;
    }

    /**
     * The replacement/equivalent of Block.onBlockPlacedBy() for customized per-machine block placing behavior.
     */
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
    }

    /**
     * The replacement/equivalent of Block.onBlockAdded() for customized per-machine block adding behavior.
     */
    public void onBlockAdded(World world, int x, int y, int z)
    {
    }

    /**
     * The replacement/equivalent of Block.onBlockPreDestroy() for customized per-machine pre-block-breaking behavior.
     */
    public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
    {
    }

    /**
     * The replacement/equivalent of Block.breakBlock() for customized per-machine block breaking behavior.
     * Return true if custom behavior should override the default BlockEnderUtilities*.breakBlock().
     * Note that the vanilla Block.breakBlock() (or equivalent) will still get called! (To deal with the TE removal etc.)
     */
    public boolean breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        return false;
    }

    /**
     * The replacement/equivalent of Block.onBlockActivated() for customized per-machine block activation behavior.
     */
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
    {
        PlayerInteractEvent e = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side, world);
        if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
        {
            return false;
        }

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te == null || te instanceof TileEntityEnderUtilities == false)
            {
                return false;
            }

            if (this.isTileEntityValid(te) == true)
            {
                player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, world, x, y, z);
            }
        }

        return true;
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z, Block block, int meta)
    {
        return block.getLightValue();
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
    public IIcon getIcon(int side, int meta)
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
    public IIcon getIcon(TileEntityEnderUtilities te, int side, int meta)
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
        for (int meta = 0; meta < 16; ++meta)
        {
            Machine m = getMachine(blockIndex, meta);
            if (m != null)
            {
                m.registerIcons(iconRegister);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
    }
}
