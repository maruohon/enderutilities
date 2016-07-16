package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockInfo
{
    public final IBlockState blockState;
    public final IBlockState blockStateActual;
    public final Block block;
    public final int blockMeta;
    public final int itemMeta;
    public final ResourceLocation resource;

    public BlockInfo(IBlockState state, IBlockState stateActual, Block block, int meta, int itemMeta)
    {
        this.blockState = state;
        this.blockStateActual = stateActual;
        this.block = block;
        this.blockMeta = meta;
        this.resource = ForgeRegistries.BLOCKS.getKey(this.block);
        this.itemMeta = itemMeta;
    }

    public BlockInfo(ResourceLocation resource, int blockMeta, int itemMeta)
    {
        this(ForgeRegistries.BLOCKS.getValue(resource), blockMeta, itemMeta);
    }

    @SuppressWarnings("deprecation")
    public BlockInfo(Block block, int blockMeta, int itemMeta)
    {
        this.block = block;
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
        this.blockState = this.block.getStateFromMeta(this.blockMeta);
        this.blockStateActual = this.blockState;
        this.resource = ForgeRegistries.BLOCKS.getKey(block);
    }

    public static BlockInfo getBlockInfo(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        IBlockState stateActual = state.getActualState(world, pos);
        Block block = state.getBlock();
        @SuppressWarnings("deprecation")
        ItemStack stack = block.getItem(world, pos, state);
        return new BlockInfo(state, stateActual, block, block.getMetaFromState(state), stack != null ? stack.getMetadata() : 0);
    }

    @Override
    public String toString()
    {
        return String.format("BlockInfo: {ResourceLocation: %s, blockMeta: %d, itemMeta: %d}", this.resource, this.blockMeta, this.itemMeta);
    }
}
