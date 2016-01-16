package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;

public class BlockInfo
{
    public Block block;
    public String blockName;
    public int meta;

    public BlockInfo (Block block, int meta)
    {
        this.block = block;
        this.blockName = Block.blockRegistry.getNameForObject(block);
        this.meta = meta;
    }

    public BlockInfo(String blockName, int meta)
    {
        this.block = (Block)Block.blockRegistry.getObject(blockName);
        this.blockName = blockName;
        this.meta = meta;
    }
}
