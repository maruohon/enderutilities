package fi.dy.masa.enderutilities.block.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;

/**
 * Machine specific data, such as icons.
 * The basic structure of this class has been inspired by MineFactoryReloaded, credits powercrystals, skyboy + others.
 */
public class Machine
{
    protected static final Map<EnumMachine, Machine> MACHINES = new HashMap<EnumMachine, Machine>();
    protected static Machine enderFurnace = new MachineEnderFurnace(EnumMachine.ENDER_FURNACE, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE, TileEntityEnderFurnace.class, "pickaxe", 1, 6.0f);
    protected static Machine toolWorkstation = new MachineToolWorkstation(EnumMachine.TOOL_WORKSTATION, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION, TileEntityToolWorkstation.class, "pickaxe", 1, 6.0f);
    protected static Machine enderInfuser = new MachineEnderInfuser(EnumMachine.ENDER_INFUSER, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER, TileEntityEnderInfuser.class, "pickaxe", 1, 6.0f);

    public static Machine energyBridgeTransmitter = new MachineEnergyBridge(EnumMachine.ENERGY_BRIDGE_TRANSMITTER, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_TRANSMITTER, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);
    public static Machine energyBridgeReceiver = new MachineEnergyBridge(EnumMachine.ENERGY_BRIDGE_RECEIVER, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RECEIVER, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);
    public static Machine energyBridgeResonator = new MachineEnergyBridge(EnumMachine.ENERGY_BRIDGE_RESONATOR, ReferenceNames.NAME_TILE_ENERGY_BRIDGE_RESONATOR, TileEntityEnergyBridge.class, "pickaxe", 1, 6.0f);

    protected EnumMachine machineType;
    protected String blockName;
    protected Class<? extends TileEntityEnderUtilities> tileEntityClass;
    protected String toolClass;
    protected int harvestLevel;
    protected float blockHardness;

    @SideOnly(Side.CLIENT)
    public String texture_names[];
    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel models[];

    public Machine(EnumMachine machineType, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        this.machineType = machineType;
        this.blockName = name;
        this.tileEntityClass = TEClass;
        this.toolClass = tool;
        this.harvestLevel = harvestLevel;
        this.blockHardness = hardness;
        MACHINES.put(machineType, this);
    }

    public String getBlockName()
    {
        return this.blockName;
    }

    public static Machine getMachine(EnumMachine machineType)
    {
        return MACHINES.get(machineType);
    }

    public static Machine getMachine(int blockIndex, int meta)
    {
        return MACHINES.get(EnumMachine.getMachineType(blockIndex, meta));
    }

    public static EnumMachine getDefaultState(int blockIndex)
    {
        return EnumMachine.getMachineType(blockIndex, 0);
    }

    public IBlockState getActualState(IBlockState iBlockState, IBlockAccess worldIn, BlockPos pos)
    {
        return iBlockState;
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
     * The replacement/equivalent of Block.onBlockPlacedBy() for customized per-machine block placing behavior.
     */
    public void onBlockPlacedBy(World world, BlockPos pos, EntityLivingBase livingBase, ItemStack stack)
    {
    }

    /**
     * The replacement/equivalent of Block.onBlockAdded() for customized per-machine block adding behavior.
     */
    public void onBlockAdded(World world, BlockPos pos, IBlockState iBlockState)
    {
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

    /**
     * The replacement/equivalent of Block.onBlockActivated() for customized per-machine block activation behavior.
     */
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer player, EnumFacing face, float offsetX, float offsetY, float offsetZ)
    {
        PlayerInteractEvent e = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, pos, face, world);
        if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
        {
            return false;
        }

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te == null || te instanceof TileEntityEnderUtilities == false)
            {
                return false;
            }

            if (this.isTileEntityValid(te) == true)
            {
                player.openGui(EnderUtilities.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return true;
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

    @SideOnly(Side.CLIENT)
    public static void registerTextures(int blockIndex, TextureMap textureMap)
    {
        for (int meta = 0; meta < 16; ++meta)
        {
            Machine machine = getMachine(blockIndex, meta);
            if (machine != null)
            {
                machine.registerTextures(textureMap);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(int blockIndex, IRegistry modelRegistry, TextureMap textures, Map<ResourceLocation, ModelBlock> models)
    {
        for (int meta = 0; meta < 16; ++meta)
        {
            Machine machine = getMachine(blockIndex, meta);
            if (machine != null)
            {
                machine.registerModels(modelRegistry, textures, models);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel getModel(IBlockState iBlockState)
    {
        Map<String, String> map = this.getTextureMapping(iBlockState);

        String modelName = Reference.MOD_ID + ":block/" + this.blockName;
        ModelBlock modelBlock = EnderUtilitiesModelBlock.createNewModelBlockForTextures(EnderUtilitiesModelRegistry.modelBlockBaseBlocks, modelName, map, EnderUtilitiesModelRegistry.models, false);
        if (modelBlock != null)
        {
            //EnderUtilities.logger.info("Machine.getModel(): baking... " + modelName + ":");
            //EnderUtilitiesModelBlock.printModelBlock(modelBlock);
            ModelRotation rotation = ModelRotation.X0_Y0;

            // When we retrieve the model for the ItemBlocks, the IBlockState will be null!
            if (iBlockState != null)
            {
                EnumFacing facing = (EnumFacing)iBlockState.getValue(BlockEnderUtilities.FACING);
                switch(facing)
                {
                    case NORTH: rotation = ModelRotation.X0_Y0; break;
                    case EAST: rotation =  ModelRotation.X0_Y90; break;
                    case SOUTH: rotation = ModelRotation.X0_Y180; break;
                    case WEST: rotation =  ModelRotation.X0_Y270; break;
                    default:
                }
            }

            IFlexibleBakedModel model = EnderUtilitiesModelFactory.instance.bakeModel(modelBlock, rotation, false);
            //EnderUtilitiesModelFactory.printModelData(modelName, model);
            return model;
        }

        //EnderUtilities.logger.info("Machine.getModel(): modelBlock == null; return baseBlockModel");
        return EnderUtilitiesModelRegistry.baseBlockModel;
    }

    /**
     * Register the textures for this block/machine. You are required to register at least three textures
     * for the front, top and sides, in that order, unless you also override getTextureMapping().
     * @param textureMap
     */
    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        int len = this.texture_names.length;

        for (int i = 0; i < len; ++i)
        {
            textureMap.registerSprite(new ResourceLocation(ReferenceTextures.getTileTextureName(this.texture_names[i])));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, TextureMap textures, Map<ResourceLocation, ModelBlock> models)
    {
    }

    @SideOnly(Side.CLIENT)
    public Map<String, String> getTextureMapping(IBlockState iBlockState)
    {
        Map<String, String> textureMapping = Maps.newHashMap();
        textureMapping.put("front",   ReferenceTextures.getTileTextureName(this.texture_names[0]));
        textureMapping.put("top",     ReferenceTextures.getTileTextureName(this.texture_names[1]));
        textureMapping.put("bottom",  ReferenceTextures.getTileTextureName(this.texture_names[1]));
        textureMapping.put("side",    ReferenceTextures.getTileTextureName(this.texture_names[2]));

        return textureMapping;
    }
}
