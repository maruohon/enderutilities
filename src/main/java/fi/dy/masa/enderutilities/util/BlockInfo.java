package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class BlockInfo
{
    public Block block;
    public ResourceLocation resource;
    public int meta;

    public BlockInfo (Block block, int meta)
    {
        this.block = block;
        this.resource = Block.blockRegistry.getNameForObject(block);
        this.meta = meta;
    }

    public BlockInfo(ResourceLocation resource, int meta)
    {
        this.block = Block.blockRegistry.getObject(resource);
        this.resource = resource;
        this.meta = meta;
    }
}
