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
    public Block block;
    public int blockMeta;
    public int itemMeta;
    public ResourceLocation resource;

    public BlockInfo(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        this.block = state.getBlock();
        this.blockMeta = this.block.getMetaFromState(state);
        this.itemMeta = 0;
        this.resource = ForgeRegistries.BLOCKS.getKey(this.block);

        @SuppressWarnings("deprecation")
        ItemStack stack = this.block.getItem(world, pos, state);
        if (stack != null)
        {
            this.itemMeta = stack.getMetadata();
        }
    }

    public BlockInfo(Block block, int blockMeta, int itemMeta)
    {
        this.block = block;
        this.resource = ForgeRegistries.BLOCKS.getKey(block);
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
    }

    public BlockInfo(ResourceLocation resource, int blockMeta, int itemMeta)
    {
        this.block = ForgeRegistries.BLOCKS.getValue(resource);
        this.resource = resource;
        this.blockMeta = blockMeta;
        this.itemMeta = itemMeta;
    }

    @Override
    public String toString()
    {
        return String.format("BlockInfo: {block rl: %s, blockMeta: %d, itemMeta: %d}", this.resource, this.blockMeta, this.itemMeta);
    }
}
