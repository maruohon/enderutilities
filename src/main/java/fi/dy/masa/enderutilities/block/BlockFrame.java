package fi.dy.masa.enderutilities.block;

import net.minecraft.block.material.Material;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class BlockFrame extends BlockEnderUtilities
{
    public BlockFrame(String name, float hardness,float resistance,  int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] { ReferenceNames.NAME_TILE_FRAME };
    }
}
