package fi.dy.masa.enderutilities.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.ITieredStorage;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityJSU;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;

public class BlockStorage extends BlockEnderUtilitiesInventory
{
    protected static final AxisAlignedBB SINGLE_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    public static final PropertyEnum<BlockStorage.EnumStorageType> TYPE =
            PropertyEnum.<BlockStorage.EnumStorageType>create("type", BlockStorage.EnumStorageType.class);

    public BlockStorage(String name, float hardness, float resistance, int harvestLevel, Material material)
    {
        super(name, hardness, resistance, harvestLevel, material);

        this.setDefaultState(this.getBlockState().getBaseState()
                .withProperty(TYPE, BlockStorage.EnumStorageType.MEMORY_CHEST_0)
                .withProperty(FACING_H, BlockEnderUtilities.DEFAULT_FACING));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { TYPE, FACING_H });
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        if (state.getValue(TYPE) == EnumStorageType.JSU)
        {
            return FULL_BLOCK_AABB;
        }

        return SINGLE_CHEST_AABB;
    }

    @Override
    protected String[] generateUnlocalizedNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_0",
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_1",
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST + "_2",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_0",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_1",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_2",
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST + "_3",
                ReferenceNames.NAME_TILE_ENTITY_JSU
        };
    }

    @Override
    protected String[] generateTooltipNames()
    {
        return new String[] {
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST,
                ReferenceNames.NAME_TILE_ENTITY_JSU
        };
    }

    @Override
    protected TileEntityEnderUtilities createTileEntityInstance(World worldIn, IBlockState state)
    {
        switch (state.getValue(TYPE))
        {
            case MEMORY_CHEST_0:    return new TileEntityMemoryChest();
            case MEMORY_CHEST_1:    return new TileEntityMemoryChest();
            case MEMORY_CHEST_2:    return new TileEntityMemoryChest();
            case HANDY_CHEST_0:     return new TileEntityHandyChest();
            case HANDY_CHEST_1:     return new TileEntityHandyChest();
            case HANDY_CHEST_2:     return new TileEntityHandyChest();
            case HANDY_CHEST_3:     return new TileEntityHandyChest();
            case JSU:               return new TileEntityJSU();
        }

        return new TileEntityMemoryChest();
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        if (world.isRemote == false)
        {
            TileEntity te = getTileEntitySafely(world, pos, TileEntity.class);

            if (te instanceof ITieredStorage)
            {
                ((ITieredStorage) te).setStorageTier(state.getValue(TYPE).getTier());
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == false && player.capabilities.isCreativeMode == false)
        {
            switch (state.getValue(TYPE))
            {
                case MEMORY_CHEST_0:
                case MEMORY_CHEST_1:
                case MEMORY_CHEST_2:
                    TileEntityMemoryChest te = getTileEntitySafely(world, pos, TileEntityMemoryChest.class);

                    if (te != null && te instanceof TileEntityMemoryChest)
                    {
                        // If a Memory Chest has been set to Private mode, then the chest itself shall be unbreakable
                        if (te.isUseableByPlayer(player) == false)
                        {
                            player.sendMessage(new TextComponentTranslation("enderutilities.chat.message.private.owned.by", te.getOwnerName()));
                            return false;
                        }
                    }
                    break;
                default:
            }
        }

        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    @Deprecated
    public float getBlockHardness(IBlockState state, World world, BlockPos pos)
    {
        switch (state.getValue(TYPE))
        {
            case HANDY_CHEST_0:
            case HANDY_CHEST_1:
            case HANDY_CHEST_2:
            case HANDY_CHEST_3:
                TileEntityHandyChest tehc = getTileEntitySafely(world, pos, TileEntityHandyChest.class);

                if (tehc != null)
                {
                    // If a Handy Chest has any locked memory card slots, then the chest itself shall be unbreakable
                    if (tehc.getLockMask() != 0)
                    {
                        return -1f;
                    }
                }

                break;

            case MEMORY_CHEST_0:
            case MEMORY_CHEST_1:
            case MEMORY_CHEST_2:
                TileEntityMemoryChest temc = getTileEntitySafely(world, pos, TileEntityMemoryChest.class);

                if (temc != null)
                {
                    // If a Memory Chest has been set to Private mode, then the chest itself shall be unbreakable
                    if (temc.isPublic() == false)
                    {
                        return -1f;
                    }
                }

                break;

            default:
        }

        return super.getBlockHardness(state, world, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, EnumStorageType.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE).getMeta();
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
    {
        for (int meta = 0; meta < EnumStorageType.values().length; meta++)
        {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    public static enum EnumStorageType implements IStringSerializable
    {
        MEMORY_CHEST_0 (0, 0, ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST),
        MEMORY_CHEST_1 (1, 1, ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST),
        MEMORY_CHEST_2 (2, 2, ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST),
        HANDY_CHEST_0 (3, 0, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST),
        HANDY_CHEST_1 (4, 1, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST),
        HANDY_CHEST_2 (5, 2, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST),
        HANDY_CHEST_3 (6, 3, ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST),
        JSU           (7, -1, ReferenceNames.NAME_TILE_ENTITY_JSU);

        private final int tier;
        private final String nameBase;
        private final int meta;

        private EnumStorageType(int meta, int tier, String nameBase)
        {
            this.meta = meta;
            this.tier = tier;
            this.nameBase = nameBase;
        }

        public String toString()
        {
            if (this.tier < 0)
            {
                return this.nameBase;
            }

            return this.nameBase + "_" + this.tier;
        }

        @Override
        public String getName()
        {
            return this.toString();
        }

        public int getTier()
        {
            return this.tier;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public static EnumStorageType fromMeta(int meta)
        {
            return values()[meta % values().length];
        }
    }
}
