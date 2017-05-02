package fi.dy.masa.enderutilities.tileentity;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.effects.Sounds;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityMolecularExciter extends TileEntityEnderUtilities
{
    private boolean redstoneLast;

    public TileEntityMolecularExciter()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_MOLECULAR_EXCITER);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean redstone = this.isPowered();

        if (worldIn.isRemote == false)
        {
            if (redstone)
            {
                this.scheduleBlockUpdate(1, true);
            }
        }
        else
        {
            if (redstone != this.redstoneLast)
            {
                this.getWorld().markBlockRangeForRenderUpdate(this.getPos(), this.getPos());
            }

            this.redstoneLast = redstone;
        }
    }

    @Override
    public void onScheduledBlockUpdate(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this.isPowered())
        {
            this.convertBlockToFallingBlockEntity(worldIn, this.getPos().offset(this.getFacing()));
        }
    }

    private void convertBlockToFallingBlockEntity(World worldIn, BlockPos pos)
    {
        if (worldIn.getWorldBorder().contains(pos) && worldIn.isAirBlock(pos) == false &&
            worldIn.getBlockState(pos).getBlockHardness(worldIn, pos) >= 0F)
        {
            worldIn.spawnEntity(EntityFallingBlockEU.convertBlockToEntity(worldIn, pos));
            worldIn.playSound(null, pos, Sounds.MOLECULAR_EXCITER, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    @Override
    public boolean isPowered()
    {
        return this.getWorld().isBlockPowered(this.getPos());
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }
}
