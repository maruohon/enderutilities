package fi.dy.masa.enderutilities.proxy;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
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
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelBarrelBaked;
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelCamouflageBlock;
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelInserterBaked;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TESRBarrel;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.config.ConfigReader;
import fi.dy.masa.enderutilities.effects.Effects;
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

public class ClientProxy extends CommonProxy
{
    private ModFixs dataFixer = null;

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
        Effects.playPositionedSoundOnClient(soundId, pitch, volume, repeat, stop, x, y, z);
    }

    @Override
    public void registerColorHandlers()
    {
        if (EnderUtilitiesBlocks.ELEVATOR.isEnabled())
        {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        return tintIndex == 1 ? MapColor.func_193558_a(state.getValue(BlockElevator.COLOR)).colorValue : 0xFFFFFF;
                    }
                }, EnderUtilitiesBlocks.ELEVATOR);

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    new IItemColor()
                    {
                        @Override
                        public int getColorFromItemstack(ItemStack stack, int tintIndex)
                        {
                            return tintIndex == 1 ? MapColor.func_193558_a(EnumDyeColor.byMetadata(stack.getMetadata())).colorValue : 0xFFFFFF;
                        }
                    }, Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR));
        }

        if (EnderUtilitiesBlocks.ELEVATOR_SLAB.isEnabled())
        {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                    new IBlockColor()
                    {
                        @Override
                        public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                        {
                            return tintIndex == 1 ? MapColor.func_193558_a(state.getValue(BlockElevator.COLOR)).colorValue : 0xFFFFFF;
                        }
                    }, EnderUtilitiesBlocks.ELEVATOR_SLAB);

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    new IItemColor()
                    {
                        @Override
                        public int getColorFromItemstack(ItemStack stack, int tintIndex)
                        {
                            return tintIndex == 1 ? MapColor.func_193558_a(EnumDyeColor.byMetadata(stack.getMetadata())).colorValue : 0xFFFFFF;
                        }
                    }, Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR_SLAB));
        }

        if (EnderUtilitiesBlocks.ELEVATOR_LAYER.isEnabled())
        {

            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                    new IBlockColor()
                    {
                        @Override
                        public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                        {
                            return tintIndex == 1 ? MapColor.func_193558_a(state.getValue(BlockElevator.COLOR)).colorValue : 0xFFFFFF;
                        }
                    }, EnderUtilitiesBlocks.ELEVATOR_LAYER);


            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    new IItemColor()
                    {
                        @Override
                        public int getColorFromItemstack(ItemStack stack, int tintIndex)
                        {
                            return tintIndex == 1 ? MapColor.func_193558_a(EnumDyeColor.byMetadata(stack.getMetadata())).colorValue : 0xFFFFFF;
                        }
                    }, Item.getItemFromBlock(EnderUtilitiesBlocks.ELEVATOR_LAYER));
        }

        if (EnderUtilitiesBlocks.PORTAL.isEnabled())
        {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        // ParticleDigging#init() passes a null BlockPos for running/digging particles... wtf
                        if (tintIndex == 1 && pos != null)
                        {
                            TileEntity te = worldIn.getTileEntity(pos);
                            if (te instanceof TileEntityPortal)
                            {
                                return ((TileEntityPortal) te).getColor();
                            }
                        }
                        return 0xA010F0;
                    }
                }, EnderUtilitiesBlocks.PORTAL);
        }

        if (EnderUtilitiesBlocks.PORTAL_PANEL.isEnabled())
        {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        // ParticleDigging#init() passes a null BlockPos for running/digging particles... wtf
                        if (tintIndex >= 0 && tintIndex <= 8 && pos != null)
                        {
                            TileEntity te = worldIn.getTileEntity(pos);
                            if (te instanceof TileEntityPortalPanel)
                            {
                                return ((TileEntityPortalPanel) te).getColor(tintIndex);
                            }
                        }
                        return 0xFFFFFF;
                    }
                }, EnderUtilitiesBlocks.PORTAL_PANEL);
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
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class,
                new IRenderFactory<EntityEnderArrow>() {
                    @Override public Render<? super EntityEnderArrow> createRenderFor (RenderManager manager) {
                        return new RenderEnderArrow<EntityEnderArrow>(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class,
                new IRenderFactory<EntityEnderPearlReusable>() {
                    @Override public Render<? super EntityEnderPearlReusable> createRenderFor (RenderManager manager) {
                        return new RenderEntityEnderPearl(manager, EnderUtilitiesItems.ENDER_PEARL_REUSABLE);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class,
                new IRenderFactory<EntityEndermanFighter>() {
                    @Override public Render<? super EntityEndermanFighter> createRenderFor (RenderManager manager) {
                        return new RenderEndermanFighter(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityChair.class,
                new IRenderFactory<EntityChair>() {
                    @Override public Render<? super EntityChair> createRenderFor (RenderManager manager) {
                        return new RenderChair(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingBlockEU.class,
                new IRenderFactory<EntityFallingBlockEU>() {
                    @Override public Render<? super EntityFallingBlockEU> createRenderFor (RenderManager manager) {
                        return new RenderFallingBlockEU(manager);
                    }
                });

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

    @Override
    public void registerModels()
    {
        this.registerBlockModels();
        this.registerItemBlockModels();
        this.registerAllItemModels();
    }

    private void registerAllItemModels()
    {
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_CAPACITOR);
        this.registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PART);
        this.registerItemModelWithVariants(EnderUtilitiesItems.LINK_CRYSTAL);

        this.registerItemModel(EnderUtilitiesItems.BUILDERS_WAND);
        this.registerItemModel(EnderUtilitiesItems.CHAIR_WAND);
        this.registerItemModel(EnderUtilitiesItems.DOLLY);
        this.registerItemModel(EnderUtilitiesItems.ENDER_ARROW);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_BAG);
        this.registerItemModel(EnderUtilitiesItems.ENDER_BOW);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_BUCKET);
        this.registerItemModel(EnderUtilitiesItems.ENDER_LASSO);
        this.registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PEARL_REUSABLE);
        this.registerItemModelWithVariants(EnderUtilitiesItems.ENDER_PORTER);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_SWORD);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.ENDER_TOOL);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.HANDY_BAG);
        this.registerItemModelWithVariants(EnderUtilitiesItems.ICE_MELTER);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.INVENTORY_SWAPPER);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.LIVING_MANIPULATOR);
        this.registerItemModel(EnderUtilitiesItems.MOB_HARNESS);
        this.registerItemModelWithNamePrefix(EnderUtilitiesItems.NULLIFIER, 0, "item_");
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.PICKUP_MANAGER);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.QUICK_STACKER);
        this.registerItemModel(EnderUtilitiesItems.PORTAL_SCALER);
        this.registerItemModel(EnderUtilitiesItems.RULER);
        this.registerItemModelWithVariants(EnderUtilitiesItems.SYRINGE);
        this.registerItemModelWithNameSuffix(EnderUtilitiesItems.VOID_PICKAXE, 0, "_normal");

        ModelLoaderRegistry.registerLoader(ModelEnderBucket.LoaderEnderBucket.instance);
        ModelLoaderRegistry.registerLoader(ModelEnderTools.LoaderEnderTools.instance);
        ModelLoaderRegistry.registerLoader(new ModelNullifierBaked.ModelLoaderNullifier());
    }

    private void registerItemModel(ItemEnderUtilities item)
    {
        this.registerItemModel(item, 0);
    }

    private void registerItemModel(ItemEnderUtilities item, int meta)
    {
        if (item.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private void registerItemModelWithNameSuffix(ItemEnderUtilities item, int meta, String nameSuffix)
    {
        if (item.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName() + nameSuffix, "inventory"));
        }
    }

    private void registerItemModelWithNamePrefix(ItemEnderUtilities item, int meta, String namePrefix)
    {
        if (item.isEnabled())
        {
            ResourceLocation rl = item.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(item, meta,
                    new ModelResourceLocation(rl.getResourceDomain() + ":" + namePrefix + rl.getResourcePath(), "inventory"));
        }
    }

    private void registerItemModelWithVariants(ItemEnderUtilities item)
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

    private void registerItemModelWithVariantsAndMeshDefinition(ItemEnderUtilities item)
    {
        if (item.isEnabled())
        {
            ModelLoader.registerItemVariants(item, item.getItemVariants());
            ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
        }
    }

    private void registerBlockModels()
    {
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.BARREL, new ModelBarrelBaked.StateMapper());
        ModelLoaderRegistry.registerLoader(new ModelBarrelBaked.ModelLoaderBarrel());

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.INSERTER, new ModelInserterBaked.StateMapper());
        ModelLoaderRegistry.registerLoader(new ModelInserterBaked.ModelLoaderInserter());

        ModelLoaderRegistry.registerLoader(new ModelCamouflageBlock.ModelLoaderCamouflageBlocks());
    }

    private void registerItemBlockModels()
    {
        this.registerItemBlockModel(EnderUtilitiesBlocks.ASU, 0, "tier=1");
        this.registerItemBlockModel(EnderUtilitiesBlocks.BARREL, 0, "creative=false");

        // The Elevators don't have getSubBlocks() overridden, to cut down on JEI item list clutter.
        // And thus registerAllItemBlockModels() can't be used for them.
        for (int i = 0; i < 16; i++)
        {
            this.registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR, i, "inventory");
            this.registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR_SLAB, i, "inventory");
            this.registerItemBlockModel(EnderUtilitiesBlocks.ELEVATOR_LAYER, i, "inventory");
        }

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR,       (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR_SLAB,  (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.ELEVATOR_LAYER, (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());

        this.registerAllItemBlockModels(EnderUtilitiesBlocks.ENERGY_BRIDGE, "active=false,facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.MACHINE_1, "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.STORAGE_0, "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.MSU, "creative=false,type=", "");

        this.registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 0, "advanced=false,facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 1, "advanced=true,facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 0, "half=bottom,type=normal");
        this.registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 1, "half=bottom,type=cracked");
        this.registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 0, "type=normal");
        this.registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 1, "type=filtered");
        this.registerItemBlockModel(EnderUtilitiesBlocks.MOLECULAR_EXCITER, 0, "facing=north,powered=false");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 0, "inverted=false,powered=false");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 1, "inverted=true,powered=true");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PORTAL_FRAME, 0, "inventory");
        this.registerItemBlockModel(EnderUtilitiesBlocks.ENDER_FURNACE, 0, "facing=north,mode=off");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PORTAL_PANEL, 0, "facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.SOUND_BLOCK, 0, "inventory");
    }

    private void registerItemBlockModel(BlockEnderUtilities blockIn, int meta, String fullVariant)
    {
        if (blockIn.isEnabled())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockIn), meta,
                new ModelResourceLocation(blockIn.getRegistryName(), fullVariant));
        }
    }

    private void registerAllItemBlockModels(BlockEnderUtilities blockIn, String variantPre, String variantPost)
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
