package fi.dy.masa.enderutilities.block.base;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.block.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockEnderUtilities extends Block
{
    public static final EnumFacing DEFAULT_FACING = EnumFacing.NORTH;
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    public static final PropertyDirection FACING_H = BlockHorizontal.FACING;

    protected String blockName;
    protected String[] unlocalizedNames;
    protected String[] tooltipNames;
    protected boolean enabled = true;
    public PropertyDirection propFacing = FACING_H;

    public BlockEnderUtilities(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(material);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setHarvestLevel("pickaxe", harvestLevel);
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setSoundType(SoundType.STONE);
        this.blockName = name;
        this.unlocalizedNames = this.generateUnlocalizedNames();
        this.tooltipNames = this.generateTooltipNames();
    }

    public String getBlockName()
    {
        return this.blockName;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    protected String[] generateUnlocalizedNames()
    {
        return new String[] { this.blockName };
    }

    /**
     * Generate the names used to look up tooltips for the ItemBlocks.
     * To use a common tooltip for all variants of the block, return an array with exactly one entry in it.
     * @return
     */
    protected String[] generateTooltipNames()
    {
        return this.generateUnlocalizedNames();
    }

    public String[] getUnlocalizedNames()
    {
        return this.unlocalizedNames;
    }

    public String[] getTooltipNames()
    {
        return this.tooltipNames;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public BlockEnderUtilities setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public ItemBlock createItemBlock()
    {
        return new ItemBlockEnderUtilities(this);
    }

    public void setPlacementProperties(World world, BlockPos pos, @Nonnull ItemStack stack, @Nonnull NBTTagCompound tag)
    {
        // Only apply the properties if the stack doesn't have NBT data
        // (in case of a BlockEntityTag from an already placed and configured block)
        if (stack.getTagCompound() == null)
        {
            TileEntityEnderUtilities te = getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

            if (te != null)
            {
                te.setPlacementProperties(world, pos, stack, tag);
            }
        }
    }

    public void scheduleBlockUpdate(World world, BlockPos pos, IBlockState state, int delay, boolean force)
    {
        if (force || world.isUpdateScheduled(pos, state.getBlock()) == false)
        {
            world.scheduleUpdate(pos, state.getBlock(), delay);
        }
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        IBlockState state = world.getBlockState(pos).withRotation(Rotation.CLOCKWISE_90);
        world.setBlockState(pos, state, 3);
        return true;
    }

    public <T> Map<T, AxisAlignedBB> getHilightBoxMap()
    {
        return Collections.emptyMap();
    }

    /**
     * Returns the "id" or "key" of the pointed element's bounding box the player is currently looking at.
     * Invalid hits (ie. misses) return null.
     */
    public <T> T getPointedElementId(World world, BlockPos pos, EnumFacing side, Entity entity)
    {
        this.updateBlockHilightBoxes(world, pos, side);
        return EntityUtils.getPointedBox(EntityUtils.getEyesVec(entity), entity.getLookVec(), 6d, this.getHilightBoxMap());
    }

    public void updateBlockHilightBoxes(World world, BlockPos pos, EnumFacing facing)
    {
    }

    /**
     * Returns the tile of the specified class, returns null if it is the wrong type or does not exist.
     * Avoids creating new tile entities when using a ChunkCache (off the main thread).
     * see {@link BlockFlowerPot#getActualState(IBlockState, IBlockAccess, BlockPos)}
     */
    @Nullable
    public static <T extends TileEntity> T getTileEntitySafely(IBlockAccess world, BlockPos pos, Class<T> tileClass)
    {
        TileEntity te;

        if (world instanceof ChunkCache)
        {
            ChunkCache chunkCache = (ChunkCache) world;
            te = chunkCache.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        }
        else
        {
            te = world.getTileEntity(pos);
        }

        if (tileClass.isInstance(te))
        {
            return tileClass.cast(te);
        }
        else
        {
            return null;
        }
    }
}
