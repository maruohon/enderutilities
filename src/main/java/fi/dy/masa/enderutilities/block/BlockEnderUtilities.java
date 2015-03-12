package fi.dy.masa.enderutilities.block;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class BlockEnderUtilities extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public int blockIndex;

    @SideOnly(Side.CLIENT)
    public String texture_names[];
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite textures[];
    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel models[];

    public BlockEnderUtilities(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
    }

    public BlockEnderUtilities(int index, String name, float hardness, Material material)
    {
        super(material);
        this.blockIndex = index;
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setStepSound(soundTypeStone);
        this.setHardness(hardness);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    @Override
    public int damageDropped(IBlockState iBlockState)
    {
        return this.getMetaFromState(iBlockState);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING});
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState iBlockState)
    {
        //super.onBlockAdded(world, x, y, z);
        //this.func_149930_e(world, x, y, z);
        this.onNeighborBlockChange(world, pos, iBlockState, this);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return true;
    }

    @Override
    public boolean isFullCube()
    {
        return true;
    }

    // Render using a BakedModel
    @Override
    public int getRenderType()
    {
        return 3;
    }

    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        int len = this.texture_names.length;

        for (int i = 0; i < len; ++i)
        {
            String name = ReferenceTextures.getTileTextureName(this.texture_names[i]);
            textureMap.registerSprite(new ResourceLocation(name));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, TextureMap textures, Map<ResourceLocation, ModelBlock> models)
    {
    }

    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel getModel(IBlockState state)
    {
        return this.models[state.getBlock().getMetaFromState(state)];
    }
}
