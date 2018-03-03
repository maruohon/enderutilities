package fi.dy.masa.enderutilities.proxy;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderChair;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityEnderPearl;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderFallingBlockEU;
import fi.dy.masa.enderutilities.client.renderer.model.ItemMeshDefinitionWrapper;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderBucket;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderTools;
import fi.dy.masa.enderutilities.client.renderer.model.ModelNullifierBaked;
import fi.dy.masa.enderutilities.client.renderer.model.block.BakedModelBarrel;
import fi.dy.masa.enderutilities.client.renderer.model.block.BakedModelInserter;
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelCamouflageBlock;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TESRBarrel;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.config.ConfigReader;
import fi.dy.masa.enderutilities.entity.EntityChair;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;
import fi.dy.masa.enderutilities.event.GuiEventHandler;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.event.RenderEventHandler;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.Keybindings;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    private ModFixs dataFixer = null;

    @Override
    public String format(String key, Object... args)
    {
        return I18n.format(key, args);
    }

    @Override
    public EntityPlayer getClientPlayer()
    {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }

    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case CLIENT:
                return FMLClientHandler.instance().getClientPlayerEntity();
            case SERVER:
                return ctx.getServerHandler().player;
            default:
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public ModFixs getDataFixer()
    {
        // On a server, the DataFixer gets created for and is stored inside MinecraftServer,
        // but in single player the DataFixer is stored in the client Minecraft class
        // over world reloads.
        if (this.dataFixer == null)
        {
            this.dataFixer = FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, EnderUtilities.DATA_FIXER_VERSION);
        }

        return this.dataFixer;
    }

    @Override
    public void playSound(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z)
    {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        SoundEvent sound = SoundEvent.REGISTRY.getObjectById(soundId);

        if (sound != null)
        {
            if (stop)
            {
                soundHandler.stop(sound.getRegistryName().toString(), null);
            }
            else
            {
                PositionedSoundRecord positionedSound = new PositionedSoundRecord(sound.getSoundName(),
                        SoundCategory.RECORDS, volume, pitch, repeat, 0, AttenuationType.LINEAR, x, y, z);
                soundHandler.playSound(positionedSound);
            }
        }
    }

    @SubscribeEvent
    public void registerBlockColorHandlers(ColorHandlerEvent.Block event)
    {
        BlockColors colors = event.getBlockColors();

        if (EnderUtilitiesBlocks.ELEVATOR.isEnabled())
        {
            colors.registerBlockColorHandler(new BlockColorHandlerDyes(1, 0xFFFFFF, BlockElevator.COLOR), EnderUtilitiesBlocks.ELEVATOR);
        }

        if (EnderUtilitiesBlocks.ELEVATOR_SLAB.isEnabled())
        {
            colors.registerBlockColorHandler(new BlockColorHandlerDyes(1, 0xFFFFFF, BlockElevator.COLOR), EnderUtilitiesBlocks.ELEVATOR_SLAB);
        }

        if (EnderUtilitiesBlocks.ELEVATOR_LAYER.isEnabled())
        {
            colors.registerBlockColorHandler(new BlockColorHandlerDyes(1, 0xFFFFFF, BlockElevator.COLOR), EnderUtilitiesBlocks.ELEVATOR_LAYER);
        }

        if (EnderUtilitiesBlocks.PORTAL.isEnabled())
        {
            colors.registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        // ParticleDigging#init() passes a null BlockPos for running/digging particles... wtf
                        if (tintIndex == 1 && pos != null)
                        {
                            TileEntityPortal te = BlockEnderUtilities.getTileEntitySafely(worldIn, pos, TileEntityPortal.class);

                            if (te != null)
                            {
                                return te.getColor();
                            }
                        }
                        return 0xA010F0;
                    }
                }, EnderUtilitiesBlocks.PORTAL);
        }

        if (EnderUtilitiesBlocks.PORTAL_PANEL.isEnabled())
        {
            colors.registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        // ParticleDigging#init() passes a null BlockPos for running/digging particles... wtf
                        if (tintIndex >= 0 && tintIndex <= 8 && pos != null)
                        {
                            TileEntityPortalPanel te = BlockEnderUtilities.getTileEntitySafely(worldIn, pos, TileEntityPortalPanel.class);

                            if (te != null)
                            {
                                return te.getColor(tintIndex);
                            }
                        }
                        return 0xFFFFFF;
                    }
                }, EnderUtilitiesBlocks.PORTAL_PANEL);
        }
    }

    @SubscribeEvent
    public void registerItemColorHandlers(ColorHandlerEvent.Item event)
    {
        ItemColors colors = event.getItemColors();

        if (EnderUtilitiesBlocks.ELEVATOR.isEnabled())
        {
            colors.registerItemColorHandler(new ItemColorHandlerDyes(1, 0xFFFFFF), Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR));
        }

        if (EnderUtilitiesBlocks.ELEVATOR_SLAB.isEnabled())
        {
            colors.registerItemColorHandler(new ItemColorHandlerDyes(1, 0xFFFFFF), Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR_SLAB));
        }

        if (EnderUtilitiesBlocks.ELEVATOR_LAYER.isEnabled())
        {
            colors.registerItemColorHandler(new ItemColorHandlerDyes(1, 0xFFFFFF), Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR_LAYER));
        }
    }

    private static class BlockColorHandlerDyes implements IBlockColor
    {
        private final int targetTintIndex;
        private final int defaultColor;
        private final PropertyEnum<EnumDyeColor> prop;

        private BlockColorHandlerDyes(int targetTintIndex, int defaultColor, PropertyEnum<EnumDyeColor> prop)
        {
            this.targetTintIndex = targetTintIndex;
            this.defaultColor = defaultColor;
            this.prop = prop;
        }

        @Override
        public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
        {
            return tintIndex == this.targetTintIndex ? MapColor.getBlockColor(state.getValue(this.prop)).colorValue : this.defaultColor;
        }
    }

    private static class ItemColorHandlerDyes implements IItemColor
    {
        private final int targetTintIndex;
        private final int defaultColor;

        private ItemColorHandlerDyes(int targetTintIndex, int defaultColor)
        {
            this.targetTintIndex = targetTintIndex;
            this.defaultColor = defaultColor;
        }

        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex)
        {
            return tintIndex == this.targetTintIndex ? MapColor.getBlockColor(EnumDyeColor.byMetadata(stack.getMetadata())).colorValue : this.defaultColor;
        }
    }

    @Override
    public void registerEventHandlers()
    {
        super.registerEventHandlers();

        MinecraftForge.EVENT_BUS.register(new ConfigReader());
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        MinecraftForge.EVENT_BUS.register(new InputEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
    }

    @Override
    public void registerKeyBindings()
    {
        Keybindings.keyToggleMode = new KeyBinding(HotKeys.KEYBIND_NAME_TOGGLE_MODE,
                                                   HotKeys.DEFAULT_KEYBIND_TOGGLE_MODE,
                                                   HotKeys.KEYBIND_CATEGORY_ENDERUTILITIES);

        ClientRegistry.registerKeyBinding(Keybindings.keyToggleMode);
    }

    @Override
    public void registerRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, manager -> { return new RenderEnderArrow<EntityEnderArrow>(manager); });
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, manager -> { return new RenderEntityEnderPearl(manager, EnderUtilitiesItems.ENDER_PEARL_REUSABLE); });
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class, manager -> { return new RenderEndermanFighter(manager); });
        RenderingRegistry.registerEntityRenderingHandler(EntityChair.class, manager -> { return new RenderChair(manager); });
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingBlockEU.class, manager -> { return new RenderFallingBlockEU(manager); });

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TESRBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyBridge.class, new TileEntityRendererEnergyBridge());
    }

    @Override
    public boolean isShiftKeyDown()
    {
        return GuiScreen.isShiftKeyDown();
    }

    @Override
    public boolean isControlKeyDown()
    {
        return GuiScreen.isCtrlKeyDown();
    }

    @Override
    public boolean isAltKeyDown()
    {
        return GuiScreen.isAltKeyDown();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        registerBlockModels();
        registerItemBlockModels();
        registerAllItemModels();
    }

    private static void registerAllItemModels()
    {
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_CAPACITOR);
        registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PART);
        registerItemModelWithVariants(EnderUtilitiesItems.LINK_CRYSTAL);

        registerItemModel(EnderUtilitiesItems.BUILDERS_WAND);
        registerItemModel(EnderUtilitiesItems.CHAIR_WAND);
        registerItemModel(EnderUtilitiesItems.DOLLY);
        registerItemModel(EnderUtilitiesItems.ENDER_ARROW);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_BAG);
        registerItemModel(EnderUtilitiesItems.ENDER_BOW);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_BUCKET);
        registerItemModel(EnderUtilitiesItems.ENDER_LASSO);
        registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PEARL_REUSABLE);
        registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PORTER);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_SWORD);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_TOOL);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.HANDY_BAG);
        registerItemModelWithVariants(EnderUtilitiesItems.ICE_MELTER);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.INVENTORY_SWAPPER);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.LIVING_MANIPULATOR);
        registerItemModel(EnderUtilitiesItems.MOB_HARNESS);
        registerItemModelWithNamePrefix(EnderUtilitiesItems.NULLIFIER, 0, "item_");
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.PICKUP_MANAGER);
        registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.QUICK_STACKER);
        registerItemModel(EnderUtilitiesItems.PORTAL_SCALER);
        registerItemModel(EnderUtilitiesItems.RULER);
        registerItemModelWithVariants(EnderUtilitiesItems.SYRINGE);
        registerItemModelWithNameSuffix(EnderUtilitiesItems.VOID_PICKAXE, 0, "_normal");

        ModelLoaderRegistry.registerLoader(ModelEnderBucket.LoaderEnderBucket.instance);
        ModelLoaderRegistry.registerLoader(ModelEnderTools.LoaderEnderTools.instance);
        ModelLoaderRegistry.registerLoader(new ModelNullifierBaked.ModelLoaderNullifier());
    }

    private static void registerItemModel(ItemEnderUtilities item)
    {
        registerItemModel(item, 0);
    }

    private static void registerItemModel(ItemEnderUtilities item, int meta)
    {
        if (item.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private static void registerItemModelWithNameSuffix(ItemEnderUtilities item, int meta, String nameSuffix)
    {
        if (item.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName() + nameSuffix, "inventory"));
        }
    }

    private static void registerItemModelWithNamePrefix(ItemEnderUtilities item, int meta, String namePrefix)
    {
        if (item.isEnabled())
        {
            ResourceLocation rl = item.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(item, meta,
                    new ModelResourceLocation(rl.getResourceDomain() + ":" + namePrefix + rl.getResourcePath(), "inventory"));
        }
    }

    private static void registerItemModelWithVariants(ItemEnderUtilities item)
    {
        if (item.isEnabled())
        {
            ResourceLocation[] variants = item.getItemVariants();
            NonNullList<ItemStack> items = NonNullList.create();
            item.getSubItems(item.getCreativeTab(), items);

            int i = 0;
            for (ItemStack stack : items)
            {
                ModelResourceLocation mrl = (variants[i] instanceof ModelResourceLocation) ?
                                            (ModelResourceLocation)variants[i] : new ModelResourceLocation(variants[i], "inventory");
                ModelLoader.setCustomModelResourceLocation(stack.getItem(), stack.getMetadata(), mrl);
                i++;
            }
        }
    }

    private static void registerItemModelWithVariantsAndMeshDefinition(ItemEnderUtilities item)
    {
        if (item.isEnabled())
        {
            ModelLoader.registerItemVariants(item, item.getItemVariants());
            ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
        }
    }

    private static void registerBlockModels()
    {
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.BARREL, new BakedModelBarrel.StateMapper());
        ModelLoaderRegistry.registerLoader(new BakedModelBarrel.ModelLoaderBarrel());

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.INSERTER, new BakedModelInserter.StateMapper());
        ModelLoaderRegistry.registerLoader(new BakedModelInserter.ModelLoaderInserter());

        ModelLoaderRegistry.registerLoader(new ModelCamouflageBlock.ModelLoaderCamouflageBlocks());

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR,       (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR_SLAB,  (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR_LAYER, (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
    }

    private static void registerItemBlockModels()
    {
        registerItemBlockModel(EnderUtilitiesBlocks.ASU, 0, "tier=1");
        registerItemBlockModel(EnderUtilitiesBlocks.BARREL, 0, "creative=false");

        // The Elevators don't have getSubBlocks() overridden, to cut down on JEI item list clutter.
        // And thus registerAllItemBlockModels() can't be used for them.
        for (int i = 0; i < 16; i++)
        {
            registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR, i, "inventory");
            registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR_SLAB, i, "inventory");
            registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR_LAYER, i, "inventory");
        }

        registerAllItemBlockModels(EnderUtilitiesBlocks.ENERGY_BRIDGE, "active=false,facing=north,type=", "");
        registerAllItemBlockModels(EnderUtilitiesBlocks.MACHINE_1, "facing=north,type=", "");
        registerAllItemBlockModels(EnderUtilitiesBlocks.STORAGE_0, "facing=north,type=", "");
        registerAllItemBlockModels(EnderUtilitiesBlocks.MSU, "creative=false,type=", "");

        registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 0, "advanced=false,facing=north");
        registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 1, "advanced=true,facing=north");
        registerItemBlockModel(EnderUtilitiesBlocks.ENDER_FURNACE, 0, "facing=north,mode=off");
        registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 0, "half=bottom,type=normal");
        registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 1, "half=bottom,type=cracked");
        registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 0, "type=normal");
        registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 1, "type=filtered");
        registerItemBlockModel(EnderUtilitiesBlocks.MOLECULAR_EXCITER, 0, "facing=north,powered=false");
        registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 0, "inverted=false,powered=false");
        registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 1, "inverted=true,powered=true");
        registerItemBlockModel(EnderUtilitiesBlocks.PORTAL_FRAME, 0, "inventory");
        registerItemBlockModel(EnderUtilitiesBlocks.PORTAL_PANEL, 0, "facing=north");
        registerItemBlockModel(EnderUtilitiesBlocks.SOUND_BLOCK, 0, "inventory");
    }

    private static void registerItemBlockModel(BlockEnderUtilities blockIn, int meta, String fullVariant)
    {
        if (blockIn.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockIn), meta,
                new ModelResourceLocation(blockIn.getRegistryName(), fullVariant));
        }
    }

    private static void registerAllItemBlockModels(BlockEnderUtilities blockIn, String variantPre, String variantPost)
    {
        if (blockIn.isEnabled())
        {
            NonNullList<ItemStack> stacks = NonNullList.create();
            blockIn.getSubBlocks(blockIn.getCreativeTabToDisplayOn(), stacks);
            String[] names = blockIn.getUnlocalizedNames();

            for (ItemStack stack : stacks)
            {
                Item item = stack.getItem();
                int meta = stack.getMetadata();
                ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName(), variantPre + names[meta] + variantPost);
                ModelLoader.setCustomModelResourceLocation(item, meta, mrl);
            }
        }
    }

    /*
    private void registerAllItemBlockModels(BlockEnderUtilities blockIn)
    {
        if (blockIn.isEnabled())
        {
            List<ItemStack> stacks = new ArrayList<ItemStack>();
            blockIn.getSubBlocks(Item.getItemFromBlock(blockIn), blockIn.getCreativeTabToDisplayOn(), stacks);

            for (ItemStack stack : stacks)
            {
                Item item = stack.getItem();
                ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName(), "inventory");
                ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), mrl);
            }
        }
    }
    */
}
