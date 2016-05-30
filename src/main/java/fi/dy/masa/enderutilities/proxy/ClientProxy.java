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
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityEnderPearl;
import fi.dy.masa.enderutilities.client.renderer.model.ItemMeshDefinitionWrapper;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderBucket;
import fi.dy.masa.enderutilities.client.renderer.model.ModelEnderTools;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.GuiEventHandler;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.event.RenderEventHandler;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.ConfigReader;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.Keybindings;
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
    public void registerColorHandlers()
    {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
            new IBlockColor()
            {
                @Override
                public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                {
                    if (tintIndex == 1)
                    {
                        return state.getValue(BlockElevator.COLOR).getMapColor().colorValue;
                    }

                    return 0xFFFFFF;
                }
            }, EnderUtilitiesBlocks.blockElevator);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        if (tintIndex == 1)
                        {
                            TileEntity te = worldIn.getTileEntity(pos);
                            if (te instanceof TileEntityPortal)
                            {
                                return ((TileEntityPortal) te).getColor().getMapColor().colorValue;
                            }
                        }

                        return 0xA010F0;
                    }
                }, EnderUtilitiesBlocks.blockPortal);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                new IBlockColor()
                {
                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex)
                    {
                        if (tintIndex >= 1 && tintIndex <= 8)
                        {
                            TileEntity te = worldIn.getTileEntity(pos);
                            if (te instanceof TileEntityPortalPanel)
                            {
                                return ((TileEntityPortalPanel) te).getColor(tintIndex - 1);
                            }
                            //return EnumDyeColor.byMetadata(tintIndex).getMapColor().colorValue;
                        }

                        return 0xFFFFFF;
                    }
                }, EnderUtilitiesBlocks.blockPortalPanel);

        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new IItemColor()
                {
                    @Override
                    public int getColorFromItemstack(ItemStack stack, int tintIndex)
                    {
                        if (tintIndex == 1)
                        {
                            return EnumDyeColor.byMetadata(stack.getMetadata()).getMapColor().colorValue;
                        }

                        return 0xFFFFFF;
                    }
                }, Item.getItemFromBlock(EnderUtilitiesBlocks.blockElevator));
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
        Keybindings.keyToggleMode = new KeyBinding(ReferenceKeys.KEYBIND_NAME_TOGGLE_MODE,
                                                   ReferenceKeys.DEFAULT_KEYBIND_TOGGLE_MODE,
                                                   ReferenceKeys.KEYBIND_CATEGORY_ENDERUTILITIES);

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
        this.registerItemBlockModels();
        this.registerAllItemModels();
    }

    public void registerAllItemModels()
    {
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderCapacitor);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPart);
        this.registerItemModelWithVariants(EnderUtilitiesItems.linkCrystal);

        this.registerItemModel(EnderUtilitiesItems.buildersWand);
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
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.inventorySwapper);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.livingManipulator);
        this.registerItemModel(EnderUtilitiesItems.mobHarness);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.pickupManager);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.quickStacker);
        this.registerItemModel(EnderUtilitiesItems.portalScaler);
        this.registerItemModel(EnderUtilitiesItems.ruler);

        ModelLoaderRegistry.registerLoader(ModelEnderBucket.LoaderEnderBucket.instance);
        ModelLoaderRegistry.registerLoader(ModelEnderTools.LoaderEnderTools.instance);
    }

    private void registerItemModel(ItemEnderUtilities item)
    {
        this.registerItemModel(item, 0);
    }

    private void registerItemModel(ItemEnderUtilities item, int meta)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private void registerItemModelWithVariants(ItemEnderUtilities item)
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

    private void registerItemModelWithVariantsAndMeshDefinition(ItemEnderUtilities item)
    {
        ModelLoader.registerItemVariants(item, item.getItemVariants());
        ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
    }

    /*private void registerSmartItemModelWrapper(ItemEnderUtilities item)
    {
        ModelLoader.registerItemVariants(item, item.getItemVariants());
        ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
    }*/

    private void registerItemBlockModels()
    {
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockElevator);
        ModelLoader.setCustomStateMapper(EnderUtilitiesBlocks.blockElevator, (new StateMap.Builder()).ignore(BlockElevator.COLOR, BlockElevator.FACING).build());

        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockEnergyBridge, "active=false,facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockMachine_1,    "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockStorage_0,    "facing=north,type=", "");

        this.registerItemBlockModel(EnderUtilitiesBlocks.blockFrame,        0, "inventory");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockMachine_0,    0, "facing=north,mode=off");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockPortal,       0, "facing=north");
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockPortalPanel,  0, "facing=north");
    }

    private void registerItemBlockModel(BlockEnderUtilities blockIn, int meta, String fullVariant)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockIn), meta,
                new ModelResourceLocation(blockIn.getRegistryName(), fullVariant));
    }

    private void registerAllItemBlockModels(BlockEnderUtilities blockIn, String variantPre, String variantPost)
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

    private void registerAllItemBlockModels(BlockEnderUtilities blockIn)
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
