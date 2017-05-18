package fi.dy.masa.enderutilities.proxy;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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
import fi.dy.masa.enderutilities.client.renderer.model.block.ModelElevator;
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
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.Keybindings;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;

public class ClientProxy extends CommonProxy
{
    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case CLIENT:
                return FMLClientHandler.instance().getClientPlayerEntity();
            case SERVER:
                return ctx.getServerHandler().playerEntity;
            default:
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public void playSound(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z)
    {
        Effects.playPositionedSoundOnClient(soundId, pitch, volume, repeat, stop, x, y, z);
    }

    @Override
    public void registerColorHandlers()
    {
        if (EnderUtilitiesBlocks.blockElevator.isEnabled())
        {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        return tintIndex == 1 ? state.getValue(BlockElevator.COLOR).getMapColor().colorValue : 0xFFFFFF;
                    }
                }, EnderUtilitiesBlocks.blockElevator);

            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                    new IBlockColor()
                    {
                        @Override
                        public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                        {
                            return tintIndex == 1 ? state.getValue(BlockElevator.COLOR).getMapColor().colorValue : 0xFFFFFF;
                        }
                    }, EnderUtilitiesBlocks.blockElevatorSlab);

            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                    new IBlockColor()
                    {
                        @Override
                        public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                        {
                            return tintIndex == 1 ? state.getValue(BlockElevator.COLOR).getMapColor().colorValue : 0xFFFFFF;
                        }
                    }, EnderUtilitiesBlocks.blockElevatorLayer);

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new IItemColor()
                {
                    @Override
                    public int getColorFromItemstack(ItemStack stack, int tintIndex)
                    {
                        return tintIndex == 1 ? EnumDyeColor.byMetadata(stack.getMetadata()).getMapColor().colorValue : 0xFFFFFF;
                    }
                }, Item.getItemFromBlock(EnderUtilitiesBlocks.blockElevator));

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    new IItemColor()
                    {
                        @Override
                        public int getColorFromItemstack(ItemStack stack, int tintIndex)
                        {
                            return tintIndex == 1 ? EnumDyeColor.byMetadata(stack.getMetadata()).getMapColor().colorValue : 0xFFFFFF;
                        }
                    }, Item.getItemFromBlock(EnderUtilitiesBlocks.blockElevatorSlab));

            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                    new IItemColor()
                    {
                        @Override
                        public int getColorFromItemstack(ItemStack stack, int tintIndex)
                        {
                            return tintIndex == 1 ? EnumDyeColor.byMetadata(stack.getMetadata()).getMapColor().colorValue : 0xFFFFFF;
                        }
                    }, Item.getItemFromBlock(EnderUtilitiesBlocks.blockElevatorLayer));
        }

        if (EnderUtilitiesBlocks.blockPortal.isEnabled())
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
                }, EnderUtilitiesBlocks.blockPortal);
        }

        if (EnderUtilitiesBlocks.blockPortalPanel.isEnabled())
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
                }, EnderUtilitiesBlocks.blockPortalPanel);
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
                        return new RenderEntityEnderPearl(manager, EnderUtilitiesItems.enderPearlReusable);
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
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderCapacitor);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPart);
        this.registerItemModelWithVariants(EnderUtilitiesItems.linkCrystal);

        this.registerItemModel(EnderUtilitiesItems.buildersWand);
        this.registerItemModel(EnderUtilitiesItems.chairWand);
        this.registerItemModel(EnderUtilitiesItems.DOLLY);
        this.registerItemModel(EnderUtilitiesItems.enderArrow);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBag);
        this.registerItemModel(EnderUtilitiesItems.enderBow);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBucket);
        this.registerItemModel(EnderUtilitiesItems.enderLasso);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPearlReusable);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPorter);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderSword);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderTool);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.handyBag);
        this.registerItemModelWithVariants(EnderUtilitiesItems.iceMelter);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.inventorySwapper);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.livingManipulator);
        this.registerItemModel(EnderUtilitiesItems.mobHarness);
        this.registerItemModelWithNamePrefix(EnderUtilitiesItems.NULLIFIER, 0, "item_");
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.pickupManager);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.quickStacker);
        this.registerItemModel(EnderUtilitiesItems.portalScaler);
        this.registerItemModel(EnderUtilitiesItems.ruler);
        this.registerItemModelWithVariants(EnderUtilitiesItems.syringe);
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
            List<ItemStack> items = new ArrayList<ItemStack>();
            item.getSubItems(item, item.getCreativeTab(), items);

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

        ModelLoaderRegistry.registerLoader(new ModelElevator.ModelLoaderElevator());

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.INSERTER, new ModelInserterBaked.StateMapper());
        ModelLoaderRegistry.registerLoader(new ModelInserterBaked.ModelLoaderInserter());
    }

    private void registerItemBlockModels()
    {
        this.registerItemBlockModel(EnderUtilitiesBlocks.ASU, 0, "tier=1");
        this.registerItemBlockModel(EnderUtilitiesBlocks.BARREL, 0, "creative=false");

        // The Elevators don't have getSubBlocks() overridden, to cut down on JEI item list clutter.
        // And thus registerAllItemBlockModels() can't be used for them.
        for (int i = 0; i < 16; i++)
        {
            this.registerItemBlockModel(EnderUtilitiesBlocks.blockElevator, i, "inventory");
            this.registerItemBlockModel(EnderUtilitiesBlocks.blockElevatorSlab, i, "inventory");
            this.registerItemBlockModel(EnderUtilitiesBlocks.blockElevatorLayer, i, "inventory");
        }

        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.blockElevator,      (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.blockElevatorSlab,  (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.blockElevatorLayer, (new StateMap.Builder()).ignore(BlockElevator.COLOR).build());

        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockEnergyBridge, "active=false,facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockMachine_1,    "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockStorage_0,    "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.MSU,               "type=", "");

        this.registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 0, "advanced=false,facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.DRAWBRIDGE, 1, "advanced=true,facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 0, "half=bottom,type=normal");
        this.registerItemBlockModel(EnderUtilitiesBlocks.FLOOR, 1, "half=bottom,type=cracked");
        this.registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 0, "type=normal");
        this.registerItemBlockModel(EnderUtilitiesBlocks.INSERTER, 1, "type=filtered");
        this.registerItemBlockModel(EnderUtilitiesBlocks.MOLECULAR_EXCITER, 0, "facing=north,powered=false");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 0, "inverted=false,powered=false");
        this.registerItemBlockModel(EnderUtilitiesBlocks.PHASING, 1, "inverted=true,powered=true");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockPortalFrame,  0, "inventory");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockMachine_0,    0, "facing=north,mode=off");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockPortalPanel,  0, "facing=north");
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
            List<ItemStack> stacks = new ArrayList<ItemStack>();
            blockIn.getSubBlocks(Item.getItemFromBlock(blockIn), blockIn.getCreativeTabToDisplayOn(), stacks);
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
