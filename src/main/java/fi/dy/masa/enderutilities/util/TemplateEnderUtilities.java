package fi.dy.masa.enderutilities.util;

import java.util.List;
import java.util.UUID;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TemplateEnderUtilities
{
    protected final List<TemplateEnderUtilities.TemplateBlockInfo> blocks = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
    protected final List<TemplateEnderUtilities.TemplateEntityInfo> entities = Lists.<TemplateEnderUtilities.TemplateEntityInfo>newArrayList();
    protected PlacementSettings placement;
    protected BlockPos size = BlockPos.ORIGIN;
    protected String author = "?";
    protected boolean replaceExisting;

    public TemplateEnderUtilities()
    {
    }

    public TemplateEnderUtilities(PlacementSettings placement, boolean replaceExisting)
    {
        this.placement = placement;
        this.replaceExisting = replaceExisting;
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

    public void setReplaceExistingBlocks(boolean replace)
    {
        this.replaceExisting = replace;
    }

    public List<TemplateBlockInfo> getBlockList()
    {
        return this.blocks;
    }

    public void addBlocksToWorld(World world, BlockPos posStart)
    {
        for (int i = 0; i < this.blocks.size(); i++)
        {
            this.placeBlockAtIndex(world, i, posStart);
        }

        this.notifyBlocks(world, posStart);
        this.addEntitiesToWorld(world, posStart);
    }

    public void placeBlockAtIndex(World world, int index, BlockPos posStart)
    {
        if (index < this.blocks.size())
        {
            TemplateEnderUtilities.TemplateBlockInfo blockInfo = this.blocks.get(index);

            BlockPos pos = transformedBlockPos(this.placement, blockInfo.pos).add(posStart);
            //System.out.printf("placing, i: %d orig pos: %s tr pos: %s\n", index, blockInfo.pos, pos);

            if (this.replaceExisting == true || world.isAirBlock(pos) == true)
            {
                IBlockState state = blockInfo.blockState.withMirror(this.placement.getMirror());
                state = state.withRotation(this.placement.getRotation());

                if (blockInfo.tileEntityData != null)
                {
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof IInventory)
                    {
                        ((IInventory)te).clear();
                    }

                    //world.setBlockState(pos, Blocks.BARRIER.getDefaultState(), 4);
                }

                if (world.setBlockState(pos, state, 2) == true && blockInfo.tileEntityData != null)
                {
                    TileEntity te = world.getTileEntity(pos);

                    if (te != null)
                    {
                        blockInfo.tileEntityData.setInteger("x", pos.getX());
                        blockInfo.tileEntityData.setInteger("y", pos.getY());
                        blockInfo.tileEntityData.setInteger("z", pos.getZ());
                        te.readFromNBT(blockInfo.tileEntityData);
                    }
                }
            }
        }
    }

    public void notifyBlocks(World world, BlockPos posStart)
    {
        for (TemplateEnderUtilities.TemplateBlockInfo blockInfo : this.blocks)
        {
            BlockPos pos = transformedBlockPos(this.placement, blockInfo.pos).add(posStart);

            if (blockInfo.tileEntityData != null)
            {
                TileEntity te = world.getTileEntity(pos);

                if (te != null)
                {
                    te.markDirty();
                }

                //world.notifyBlockUpdate(pos, blockInfo.blockState, blockInfo.blockState, 7);
            }
            /*else
            {
                world.notifyNeighborsRespectDebug(pos, blockInfo.blockState.getBlock());
            }*/
        }
    }

    public void addEntitiesToWorld(World world, BlockPos posStart)
    {
        if (this.placement.getIgnoreEntities() == true)
        {
            return;
        }

        Mirror mirror = this.placement.getMirror();
        Rotation rotation = this.placement.getRotation();
        int x1 = posStart.getX();
        int y1 = posStart.getY();
        int z1 = posStart.getZ();
        int x2 = this.size.getX();
        int y2 = this.size.getY();
        int z2 = this.size.getZ();
        AxisAlignedBB bb = new AxisAlignedBB(x1, y1, z1, x1 + x2, y1 + y2, z1 + z2);
        List<Entity> existingEntities = world.getEntitiesWithinAABBExcludingEntity(null, bb);

        for (TemplateEnderUtilities.TemplateEntityInfo entityInfo : this.entities)
        {
            BlockPos pos = transformedBlockPos(this.placement, entityInfo.blockPos).add(posStart);

            NBTTagCompound nbt = entityInfo.entityData;
            UUID uuidOriginal = nbt.getUniqueId("UUID");
            Vec3d vec3d = PositionUtils.transformedVec3d(entityInfo.pos, mirror, rotation);
            Vec3d vec3d1 = vec3d.addVector((double)posStart.getX(), (double)posStart.getY(), (double)posStart.getZ());
            NBTTagList tagList = new NBTTagList();
            tagList.appendTag(new NBTTagDouble(vec3d1.xCoord));
            tagList.appendTag(new NBTTagDouble(vec3d1.yCoord));
            tagList.appendTag(new NBTTagDouble(vec3d1.zCoord));
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
                    entity.setLocationAndAngles(vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, entity.rotationYaw, entity.rotationPitch);
                }
                else
                {
                    float f = entity.getMirroredYaw(mirror);
                    f = f + (entity.rotationYaw - entity.getRotatedYaw(rotation));
                    entity.setLocationAndAngles(vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, f, entity.rotationPitch);
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

                world.spawnEntityInWorld(entity);
            }
        }
    }

    public void takeBlocksFromWorld(World worldIn, BlockPos startPos, BlockPos endPosRelative, boolean takeEntities)
    {
        BlockPos endPos = startPos.add(endPosRelative);
        List<TemplateEnderUtilities.TemplateBlockInfo> list = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
        List<TemplateEnderUtilities.TemplateBlockInfo> list1 = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();
        List<TemplateEnderUtilities.TemplateBlockInfo> list2 = Lists.<TemplateEnderUtilities.TemplateBlockInfo>newArrayList();

        this.size = PositionUtils.getAreaSizeFromRelativeEndPosition(endPosRelative);

        for (BlockPos.MutableBlockPos posMutable : BlockPos.getAllInBoxMutable(startPos, endPos))
        {
            BlockPos posRelative = posMutable.subtract(startPos);
            IBlockState state = worldIn.getBlockState(posMutable);

            TileEntity te = worldIn.getTileEntity(posMutable);

            if (te != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                te.writeToNBT(tag);

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
            this.takeEntitiesFromWorld(worldIn, startPos, endPos.add(1, 1, 1));
        }
        else
        {
            this.entities.clear();
        }
    }

    public void takeEntitiesFromWorld(World world, BlockPos startPos, BlockPos endPos)
    {
        List<Entity> list = world.<Entity>getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(startPos, endPos), new Predicate<Entity>()
        {
            public boolean apply(Entity entity)
            {
                return (entity instanceof EntityPlayer) == false;
            }
        });

        this.entities.clear();

        for (Entity entity : list)
        {
            Vec3d vec3d = new Vec3d(entity.posX - startPos.getX(), entity.posY - startPos.getY(), entity.posZ - startPos.getZ());
            NBTTagCompound nbt = new NBTTagCompound();
            entity.writeToNBTOptional(nbt);
            BlockPos pos;

            if (entity instanceof EntityPainting)
            {
                pos = ((EntityPainting)entity).getHangingPosition().subtract(startPos);
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

        for (TemplateEnderUtilities.TemplateBlockInfo blockInfo : this.blocks)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("pos", NBTUtils.writeInts(new int[] {blockInfo.pos.getX(), blockInfo.pos.getY(), blockInfo.pos.getZ()}));
            nbt.setInteger("state", Block.getStateId(blockInfo.blockState));

            if (blockInfo.tileEntityData != null)
            {
                nbt.setTag("nbt", blockInfo.tileEntityData);
            }

            nbttaglist.appendTag(nbt);
        }

        NBTTagList nbttaglist1 = new NBTTagList();

        for (TemplateEnderUtilities.TemplateEntityInfo entityInfo : this.entities)
        {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setTag("pos", NBTUtils.writeDoubles(new double[] {entityInfo.pos.xCoord, entityInfo.pos.yCoord, entityInfo.pos.zCoord}));
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

        tagList = compound.getTagList("blocks", 10);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            NBTTagList tagListPos = tag.getTagList("pos", 3);
            BlockPos blockpos = new BlockPos(tagListPos.getIntAt(0), tagListPos.getIntAt(1), tagListPos.getIntAt(2));

            int stateId = tag.getInteger("state");
            IBlockState iblockstate = Block.getStateById(stateId);
            NBTTagCompound tagTileEntityData;

            if (tag.hasKey("nbt") == true)
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
}
