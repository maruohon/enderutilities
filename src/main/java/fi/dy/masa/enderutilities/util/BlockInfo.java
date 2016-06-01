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
    public final Block block;
    public final int blockMeta;
    public final int itemMeta;
    public final ResourceLocation resource;

    public BlockInfo(World world, BlockPos pos)
    {
        this.blockState = world.getBlockState(pos).getActualState(world, pos);
        this.block = this.blockState.getBlock();
        this.blockMeta = this.block.getMetaFromState(this.blockState);
        this.resource = ForgeRegistries.BLOCKS.getKey(this.block);

        @SuppressWarnings("deprecation")
        ItemStack stack = this.block.getItem(world, pos, this.blockState);
        this.itemMeta = stack != null ? stack.getMetadata() : 0;
    }

    public BlockInfo(ResourceLocation resource, int blockMeta, int itemMeta)
    {
        this(ForgeRegistries.BLOCKS.getValue(resource), blockMeta, itemMeta);
    }

    public BlockInfo(Block block, int blockMeta, int itemMeta)
    {
        this.block = block;
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
        this.blockState = this.block.getStateFromMeta(this.blockMeta);
        this.resource = ForgeRegistries.BLOCKS.getKey(block);
    }

    @Override
    public String toString()
    {
        return String.format("BlockInfo: {ResourceLocation: %s, blockMeta: %d, itemMeta: %d}", this.resource, this.blockMeta, this.itemMeta);
    }
}
