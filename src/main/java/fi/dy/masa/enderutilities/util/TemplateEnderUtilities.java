package fi.dy.masa.enderutilities.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TemplateEnderUtilities
{
    public static ChiselsAndBitsHandler chiselsAndBitsHandler = new ChiselsAndBitsHandler();
    protected final List<TemplateEnderUtilities.TemplateBlockInfo> blocks = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
    protected final List<TemplateEnderUtilities.TemplateEntityInfo> entities = Lists.<TemplateEnderUtilities.TemplateEntityInfo>newArrayList();
    protected PlacementSettings placement = new PlacementSettings().setIgnoreEntities(true);
    protected BlockPos size = BlockPos.ORIGIN;
    protected String author = "?";
    protected ReplaceMode replaceMode = ReplaceMode.NOTHING;
    protected boolean dropOldBlocks;

    public TemplateEnderUtilities()
    {
    }

    public TemplateEnderUtilities(PlacementSettings placement, ReplaceMode replaceMode)
    {
        this.placement = placement;
        this.replaceMode = replaceMode;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public BlockPos getTemplateSize()
    {
        return this.size;
    }

    public void setPlacementSettings(PlacementSettings placement)
    {
        this.placement = placement;
    }

    public PlacementSettings getPlacementSettings()
    {
        return this.placement;
    }

    public void setReplaceMode(ReplaceMode mode)
    {
        this.replaceMode = mode;
    }

    public List<TemplateBlockInfo> getBlockList()
    {
        return this.blocks;
    }

    public void setDropOldBlocks(boolean drop)
    {
        this.dropOldBlocks = drop;
    }

    public void addBlocksToWorld(World world, BlockPos posStart)
    {
        for (int i = 0; i < this.blocks.size(); i++)
        {
            this.placeBlockAtIndex(world, i, posStart);
        }

        //this.notifyBlocks(world, posStart);
        this.addEntitiesToWorld(world, posStart);
    }

    public void placeBlockAtIndex(World world, int index, BlockPos posStart)
    {
        if (index < this.blocks.size())
        {
            TemplateEnderUtilities.TemplateBlockInfo blockInfo = this.blocks.get(index);

            BlockPos pos = transformedBlockPos(this.placement, blockInfo.pos).add(posStart);
            IBlockState stateNew = blockInfo.blockState;

            if (this.replaceMode == ReplaceMode.EVERYTHING ||
                (this.replaceMode == ReplaceMode.WITH_NON_AIR && stateNew.getMaterial() != Material.AIR) || world.isAirBlock(pos))
            {
                Mirror mirror = this.placement.getMirror();
                Rotation rotation = this.placement.getRotation();
                stateNew = stateNew.withMirror(mirror).withRotation(rotation);

                boolean success = false;

                if (this.dropOldBlocks)
                {
                    world.destroyBlock(pos, true);
                }
                /*
                else
                {
                    // Replace the block temporarily so that the old TE gets cleared for sure
                    BlockUtils.setBlockToAirWithoutSpillingContents(world, pos, 4);
                }
                */

                // Place the new block
                success = world.setBlockState(pos, stateNew, 2);

                if (success && blockInfo.tileEntityData != null)
                {
                    TileUtils.createAndAddTileEntity(world, pos, blockInfo.tileEntityData, rotation, mirror);
                }
            }
        }
    }

    public void notifyBlocks(World world, BlockPos posStart)
    {
        for (TemplateEnderUtilities.TemplateBlockInfo blockInfo : this.blocks)
        {
            BlockPos pos = transformedBlockPos(this.placement, blockInfo.pos).add(posStart);
            IBlockState state = world.getBlockState(pos);

            if (blockInfo.tileEntityData != null)
            {
                TileEntity te = world.getTileEntity(pos);

                if (te != null)
                {
                    te.markDirty();
                }

                world.notifyBlockUpdate(pos, state, state, 7);
            }
            else
            {
                world.notifyNeighborsRespectDebug(pos, blockInfo.blockState.getBlock(), false);
            }
        }
    }

    public void addEntitiesToWorld(World world, BlockPos posStart)
    {
        if (this.placement.getIgnoreEntities())
        {
            return;
        }

        Mirror mirror = this.placement.getMirror();
        Rotation rotation = this.placement.getRotation();

        BlockPos posEnd = posStart.add(PositionUtils.getRelativeEndPositionFromAreaSize(this.size));
        BlockPos pos1 = PositionUtils.getMinCorner(posStart, posEnd);
        BlockPos pos2 = PositionUtils.getMaxCorner(posStart, posEnd).add(1, 1, 1);
        List<Entity> existingEntities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos1, pos2));

        for (TemplateEnderUtilities.TemplateEntityInfo entityInfo : this.entities)
        {
            BlockPos pos = transformedBlockPos(this.placement, entityInfo.blockPos).add(posStart);

            NBTTagCompound nbt = entityInfo.entityData;
            UUID uuidOriginal = nbt.getUniqueId("UUID");
            Vec3d vec3d = PositionUtils.transformedVec3d(entityInfo.pos, mirror, rotation);
            Vec3d vec3d1 = vec3d.add((double)posStart.getX(), (double)posStart.getY(), (double)posStart.getZ());
            NBTTagList tagList = new NBTTagList();
            tagList.appendTag(new NBTTagDouble(vec3d1.x));
            tagList.appendTag(new NBTTagDouble(vec3d1.y));
            tagList.appendTag(new NBTTagDouble(vec3d1.z));
            nbt.setTag("Pos", tagList);
            nbt.setUniqueId("UUID", UUID.randomUUID());
            Entity entity;

            try
            {
                entity = EntityList.createEntityFromNBT(nbt, world);
            }
            catch (Exception e)
            {
                entity = null;
            }

            if (entity != null)
            {
                if (entity instanceof EntityPainting)
                {
                    entity.getMirroredYaw(mirror);
                    entity.getRotatedYaw(rotation);
                    entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    entity.setLocationAndAngles(vec3d1.x, vec3d1.y, vec3d1.z, entity.rotationYaw, entity.rotationPitch);
                }
                else
                {
                    float f = entity.getMirroredYaw(mirror);
                    f = f + (entity.rotationYaw - entity.getRotatedYaw(rotation));
                    entity.setLocationAndAngles(vec3d1.x, vec3d1.y, vec3d1.z, f, entity.rotationPitch);
                    // FIXME should call updateFacingWithBoundingBox(EnumFacing) for EntityHanging, otherwise a world reload is needed
                    // for ItemFrames for example to visually update to the correct facing
                }

                // Use the original UUID if possible. If there is an entity with the same UUID within the pasted area,
                // then the old one will be killed. Otherwise if there is no entity currently in the world with
                // the same UUID, then the original UUID will be used.
                Entity existing = EntityUtils.findEntityByUUID(existingEntities, uuidOriginal);
                if (existing != null)
                {
                    world.removeEntityDangerously(existing);
                    entity.setUniqueId(uuidOriginal);
                }
                else if (world instanceof WorldServer && ((WorldServer) world).getEntityFromUuid(uuidOriginal) == null)
                {
                    entity.setUniqueId(uuidOriginal);
                }

                world.spawnEntity(entity);
            }
        }
    }

    public void takeBlocksFromWorld(World worldIn, BlockPos posStart, BlockPos posEndRelative, boolean takeEntities, boolean cbCrossWorld)
    {
        BlockPos endPos = posStart.add(posEndRelative);
        List<TemplateEnderUtilities.TemplateBlockInfo> list = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
        List<TemplateEnderUtilities.TemplateBlockInfo> list1 = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
        List<TemplateEnderUtilities.TemplateBlockInfo> list2 = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();

        this.size = PositionUtils.getAreaSizeFromRelativeEndPosition(posEndRelative);

        for (BlockPos.MutableBlockPos posMutable : BlockPos.getAllInBoxMutable(posStart, endPos))
        {
            BlockPos posRelative = posMutable.subtract(posStart);
            IBlockState state = worldIn.getBlockState(posMutable);

            TileEntity te = worldIn.getTileEntity(posMutable);

            if (te != null)
            {
                NBTTagCompound tag = new NBTTagCompound();

                if (cbCrossWorld)
                {
                    tag = chiselsAndBitsHandler.writeChiselsAndBitsTileToNBT(tag, te);
                }
                else
                {
                    tag = te.writeToNBT(tag);
                }

                tag.removeTag("x");
                tag.removeTag("y");
                tag.removeTag("z");

                list1.add(new TemplateEnderUtilities.TemplateBlockInfo(posRelative, state, tag));
            }
            else if (state.isFullBlock() == false && state.isFullCube() == false)
            {
                list2.add(new TemplateEnderUtilities.TemplateBlockInfo(posRelative, state, null));
            }
            else
            {
                list.add(new TemplateEnderUtilities.TemplateBlockInfo(posRelative, state, null));
            }
        }

        this.blocks.clear();
        this.blocks.addAll(list);
        this.blocks.addAll(list1);
        this.blocks.addAll(list2);

        if (takeEntities)
        {
            this.takeEntitiesFromWorld(worldIn, posStart, posEndRelative);
        }
        else
        {
            this.entities.clear();
        }
    }

    public void takeEntitiesFromWorld(World world, BlockPos posStart, BlockPos posEndRelative)
    {
        BlockPos posEnd = posStart.add(posEndRelative);
        BlockPos pos1 = PositionUtils.getMinCorner(posStart, posEnd);
        BlockPos pos2 = PositionUtils.getMaxCorner(posStart, posEnd).add(1, 1, 1);
        List<Entity> list = world.<Entity>getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos1, pos2), new Predicate<Entity>()
        {
            public boolean apply(Entity entity)
            {
                return (entity instanceof EntityPlayer) == false;
            }
        });

        this.entities.clear();

        for (Entity entity : list)
        {
            Vec3d vec3d = new Vec3d(entity.posX - posStart.getX(), entity.posY - posStart.getY(), entity.posZ - posStart.getZ());
            NBTTagCompound nbt = new NBTTagCompound();
            entity.writeToNBTOptional(nbt);
            BlockPos pos;

            if (entity instanceof EntityPainting)
            {
                pos = ((EntityPainting)entity).getHangingPosition().subtract(posStart);
            }
            else
            {
                pos = new BlockPos(vec3d);
            }

            this.entities.add(new TemplateEnderUtilities.TemplateEntityInfo(vec3d, pos, nbt));
        }
    }

    public void write(NBTTagCompound compound)
    {
        NBTTagList nbttaglist = new NBTTagList();
        // FIXME: Ugly but quick-to-access storage... needs to be replaced when the world format changes some day
        ResourceLocation idMap[] = new ResourceLocation[4096];
        List<Integer> ids = new ArrayList<Integer>();
        int stateId = 0;
        int blockId = 0;

        for (TemplateEnderUtilities.TemplateBlockInfo blockInfo : this.blocks)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("pos", NBTUtils.writeInts(new int[] {blockInfo.pos.getX(), blockInfo.pos.getY(), blockInfo.pos.getZ()}));
            stateId = Block.getStateId(blockInfo.blockState);
            nbt.setInteger("state", stateId);

            blockId = stateId & 0xFFF;
            if (idMap[blockId] == null)
            {
                idMap[blockId] = ForgeRegistries.BLOCKS.getKey(blockInfo.blockState.getBlock());
                ids.add(blockId);
            }

            if (blockInfo.tileEntityData != null)
            {
                nbt.setTag("nbt", blockInfo.tileEntityData);
            }

            nbttaglist.appendTag(nbt);
        }

        NBTTagList tagIdMap = new NBTTagList();
        for (int id : ids)
        {
            if (idMap[id] != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("name", idMap[id].toString());
                tag.setShort("id", (short)id);
                tagIdMap.appendTag(tag);
            }
        }
        compound.setTag("blockidmap", tagIdMap);

        NBTTagList nbttaglist1 = new NBTTagList();

        for (TemplateEnderUtilities.TemplateEntityInfo entityInfo : this.entities)
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setTag("pos", NBTUtils.writeDoubles(new double[] {entityInfo.pos.x, entityInfo.pos.y, entityInfo.pos.z}));
            nbttagcompound1.setTag("blockPos", NBTUtils.writeInts(new int[] {entityInfo.blockPos.getX(), entityInfo.blockPos.getY(), entityInfo.blockPos.getZ()}));

            if (entityInfo.entityData != null)
            {
                nbttagcompound1.setTag("nbt", entityInfo.entityData);
            }

            nbttaglist1.appendTag(nbttagcompound1);
        }

        compound.setTag("blocks", nbttaglist);
        compound.setTag("entities", nbttaglist1);
        compound.setTag("size", NBTUtils.writeInts(new int[] {this.size.getX(), this.size.getY(), this.size.getZ()}));
        compound.setInteger("version", 1);
        compound.setString("author", this.author);
    }

    public void read(NBTTagCompound compound)
    {
        this.blocks.clear();
        this.entities.clear();

        this.author = compound.getString("author");

        NBTTagList tagList = compound.getTagList("size", 3);
        this.size = new BlockPos(tagList.getIntAt(0), tagList.getIntAt(1), tagList.getIntAt(2));

        boolean hasIdMap = compound.hasKey("blockidmap", Constants.NBT.TAG_LIST);
        // FIXME: Ugly but quick-to-access storage... needs to be replaced when the world format changes some day
        Block blockMap[] = new Block[4096];

        if (hasIdMap)
        {
            NBTTagList tagIdMap = compound.getTagList("blockidmap", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagIdMap.tagCount(); i++)
            {
                NBTTagCompound tag = tagIdMap.getCompoundTagAt(i);
                short id = (short)(tag.getShort("id") & 0xFFF);
                blockMap[id] = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("name")));
            }
        }

        tagList = compound.getTagList("blocks", 10);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            NBTTagList tagListPos = tag.getTagList("pos", 3);
            BlockPos blockpos = new BlockPos(tagListPos.getIntAt(0), tagListPos.getIntAt(1), tagListPos.getIntAt(2));

            IBlockState iblockstate;
            int stateId = tag.getInteger("state");

            if (hasIdMap)
            {
                Block block = blockMap[stateId & 0xFFF];
                if (block != null)
                {
                    stateId &= 0xF000;
                    stateId |= Block.getIdFromBlock(block);
                }
            }

            iblockstate = Block.getStateById(stateId);
            NBTTagCompound tagTileEntityData;

            if (tag.hasKey("nbt"))
            {
                tagTileEntityData = tag.getCompoundTag("nbt");
            }
            else
            {
                tagTileEntityData = null;
            }

            this.blocks.add(new TemplateEnderUtilities.TemplateBlockInfo(blockpos, iblockstate, tagTileEntityData));
        }

        tagList = compound.getTagList("entities", 10);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            NBTTagList tagListPos = tag.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(tagListPos.getDoubleAt(0), tagListPos.getDoubleAt(1), tagListPos.getDoubleAt(2));

            tagListPos = tag.getTagList("blockPos", 3);
            BlockPos blockpos1 = new BlockPos(tagListPos.getIntAt(0), tagListPos.getIntAt(1), tagListPos.getIntAt(2));

            if (tag.hasKey("nbt"))
            {
                NBTTagCompound tagEntityNBT = tag.getCompoundTag("nbt");
                this.entities.add(new TemplateEnderUtilities.TemplateEntityInfo(vec3d, blockpos1, tagEntityNBT));
            }
        }
    }

    public static BlockPos transformedBlockPos(PlacementSettings placement, BlockPos pos)
    {
        return PositionUtils.getTransformedBlockPos(pos, placement.getMirror(), placement.getRotation());
    }

    public static class TemplateBlockInfo
    {
        public final BlockPos pos;
        public final IBlockState blockState;
        public final NBTTagCompound tileEntityData;

        TemplateBlockInfo(BlockPos posIn, IBlockState stateIn, NBTTagCompound tileEntityNBT)
        {
            this.pos = posIn;
            this.blockState = stateIn;
            this.tileEntityData = tileEntityNBT;
        }
    }

    public static class TemplateEntityInfo
    {
        public final Vec3d pos;
        public final BlockPos blockPos;
        public final NBTTagCompound entityData;

        TemplateEntityInfo(Vec3d vecIn, BlockPos posIn, NBTTagCompound entityNBT)
        {
            this.pos = vecIn;
            this.blockPos = posIn;
            this.entityData = entityNBT;
        }
    }

    public static class ChiselsAndBitsHandler
    {
        public boolean isChiselsAndBitsTile(TileEntity te)
        {
            return false;
        }

        public NBTTagCompound writeChiselsAndBitsTileToNBT(NBTTagCompound tag, TileEntity te)
        {
            return te.writeToNBT(tag);
        }
    }

    public enum ReplaceMode
    {
        NOTHING,
        EVERYTHING,
        WITH_NON_AIR;
    }
}
