package fi.dy.masa.enderutilities.tileentity;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityMolecularExciter extends TileEntityEnderUtilities
{
    public TileEntityMolecularExciter()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MOLECULAR_EXCITER);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        if (worldIn.isRemote)
        {
            return;
        }

        if (this.getWorld().isBlockPowered(this.getPos()))
        {
            this.scheduleBlockUpdate(1, true);
        }
    }

    @Override
    public void onScheduledBlockUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        this.convertBlockToFallingBlockEntity(worldIn, this.getPos().offset(this.getFacing()));
    }

    private void convertBlockToFallingBlockEntity(World worldIn, BlockPos pos)
    {
        if (worldIn.isAirBlock(pos) == false)
        {
            worldIn.spawnEntity(EntityFallingBlockEU.convertBlockToEntity(worldIn, pos));

            /*EntityFallingBlock falling = new EntityFallingBlock(worldIn, posFront.getX() + 0.5D, posFront.getY(), posFront.getZ() + 0.5D, stateFront);
            worldIn.restoringBlockSnapshots = true;

            try
            {
                BlockUtils.setBlockToAirWithBreakSound(worldIn, posFront);
            }
            catch (Exception e) {}

            worldIn.restoringBlockSnapshots = false;*/
        }
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }
}
