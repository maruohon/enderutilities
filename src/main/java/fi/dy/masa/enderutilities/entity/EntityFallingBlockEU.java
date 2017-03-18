package fi.dy.masa.enderutilities.entity;

import java.util.List;
import javax.annotation.Nullable;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class EntityFallingBlockEU extends Entity
{
    private IBlockState blockState;
    private int fallTime;
    private boolean shouldDropItem = true;
    private boolean dropAsItemOnPlacementFail;
    private boolean canSetAsBlock = true;
    private boolean hurtEntities;
    private boolean canBePushed;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0F;
    private NBTTagCompound tileEntityData;
    protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.<BlockPos>createKey(EntityFallingBlockEU.class, DataSerializers.BLOCK_POS);
    protected static final DataParameter<Optional<IBlockState>> BLOCK_STATE = EntityDataManager.<Optional<IBlockState>>createKey(EntityFallingBlockEU.class, DataSerializers.OPTIONAL_BLOCK_STATE);

    public EntityFallingBlockEU(World worldIn)
    {
        super(worldIn);

        this.dropAsItemOnPlacementFail = Configs.fallingBlockDropsAsItemOnPlacementFail;
    }

    public static EntityFallingBlockEU convertBlockToEntity(World worldIn, BlockPos pos)
    {
        EntityFallingBlockEU entity = new EntityFallingBlockEU(worldIn);

        //entity.blockState = worldIn.getBlockState(pos).getActualState(worldIn, pos);
        entity.setBlockState(worldIn.getBlockState(pos).getActualState(worldIn, pos));
        entity.setOrigin(new BlockPos(pos));

        TileEntity te = worldIn.getTileEntity(pos);

        if (te != null)
        {
            entity.tileEntityData = te.writeToNBT(new NBTTagCompound());
        }

        entity.preventEntitySpawning = true;
        entity.setCanBePushed(true);
        entity.setSize(0.98F, 0.98F);
        entity.setPosition(pos.getX() + 0.5D, pos.getY() + (1.0F - entity.height) / 2.0F, pos.getZ() + 0.5D);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        entity.prevPosX = entity.posX;
        entity.prevPosY = entity.posY;
        entity.prevPosZ = entity.posZ;

        worldIn.restoringBlockSnapshots = true;
        worldIn.setBlockToAir(pos);
        worldIn.restoringBlockSnapshots = false;

        return entity;
    }

    @Override
    protected void entityInit()
    {
        this.getDataManager().register(ORIGIN, BlockPos.ORIGIN);
        this.getDataManager().register(BLOCK_STATE, Optional.<IBlockState>absent());
    }

    private void setOrigin(BlockPos pos)
    {
        this.getDataManager().set(ORIGIN, pos);
    }

    public void setCanBePushed(boolean canBePushed)
    {
        this.canBePushed = canBePushed;
    }

    private void setBlockState(IBlockState state)
    {
        this.blockState = state;
        this.getDataManager().set(BLOCK_STATE, Optional.of(state));
    }

    @Nullable
    public IBlockState getBlockState()
    {
        return this.getDataManager().get(BLOCK_STATE).orNull();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (BLOCK_STATE.equals(key))
        {
            this.blockState = this.getBlockState();
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return this.isDead == false;
    }

    @Override
    public boolean canBePushed()
    {
        // Returning true here allows the entity to be picked up by a Minecart
        return this.canBePushed;
    }

    @Override
    public void onUpdate()
    {
        if (this.blockState == null)
        {
            return;
        }

        if (this.blockState.getMaterial() == Material.AIR)
        {
            this.setDead();
            return;
        }

        BlockPos pos = new BlockPos(this);
        World world = this.getEntityWorld();
        Block block = this.blockState.getBlock();

        /*if (this.fallTime++ == 0)
        {
            if (world.getBlockState(pos).getBlock() == block)
            {
                world.setBlockToAir(pos);
            }
            else if (world.isRemote == false)
            {
                this.setDead();
                return;
            }
        }*/

        this.updateMovement();

        if (world.isRemote)
        {
            return;
        }

        pos = new BlockPos(this);

        if (this.onGround)
        {
            IBlockState iblockstate = world.getBlockState(pos);
            BlockPos posBelow = new BlockPos(this.posX, this.posY - 0.01D, this.posZ);

            if (world.isAirBlock(posBelow) && BlockFalling.canFallThrough(world.getBlockState(posBelow)))
            {
                this.onGround = false;
                return;
            }

            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;

            if (iblockstate.getBlock() != Blocks.PISTON_EXTENSION)
            {
                if (this.canSetAsBlock)
                {
                    if (world.canBlockBePlaced(block, pos, true, EnumFacing.UP, null, null) &&
                        BlockFalling.canFallThrough(this.world.getBlockState(pos.down())) == false &&
                        this.world.setBlockState(pos, this.blockState, 3))
                    {
                        if (block instanceof BlockFalling)
                        {
                            ((BlockFalling) block).onEndFalling(world, pos);
                        }

                        if (this.tileEntityData != null && this.blockState.getBlock().hasTileEntity(this.blockState))
                        {
                            TileEntity te = world.getTileEntity(pos);

                            if (te != null)
                            {
                                // Re-creating the TE from NBT and then calling World#setTileEntity() causes
                                // TileEntity#validate() and TileEntity#onLoad() to get called for the TE
                                // from Chunk#addTileEntity(), which should hopefully be more mod
                                // friendly than just doing te.readFromNBT(tag).
                                te = TileEntity.create(world, this.tileEntityData);

                                if (te != null)
                                {
                                    te.setPos(pos);
                                    world.setTileEntity(pos, te);
                                    te.markDirty();
                                }
                            }
                        }

                        this.setDead();
                    }
                    else if (this.dropAsItemOnPlacementFail)
                    {
                        if (this.shouldDropItem && world.getGameRules().getBoolean("doEntityDrops"))
                        {
                            ItemStack stack = new ItemStack(block, 1, block.damageDropped(this.blockState));

                            if (this.tileEntityData != null)
                            {
                                NBTUtils.getRootCompoundTag(stack, true).setTag("BlockEntityTag", this.tileEntityData);
                            }

                            this.entityDropItem(stack, 0.0F);
                        }

                        this.setDead();
                    }
                }
            }
        }
        else if (pos.getY() < -64)
        {
            this.setDead();
        }
    }

    private void updateMovement()
    {
        if (this.hasNoGravity() == false)
        {
            this.motionY -= 0.04D;
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.move(this.motionX, this.motionY, this.motionZ);

        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;
    }

    public void fall(float distance, float damageMultiplier)
    {
        Block block = this.blockState.getBlock();

        if (this.hurtEntities)
        {
            int i = MathHelper.ceil(distance - 1.0F);

            if (i > 0)
            {
                boolean isAnvil = block == Blocks.ANVIL;
                List<Entity> list = Lists.newArrayList(this.getEntityWorld().getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
                DamageSource damageSource = isAnvil ? DamageSource.anvil : DamageSource.fallingBlock;

                for (Entity entity : list)
                {
                    entity.attackEntityFrom(damageSource, Math.min(MathHelper.floor(i * this.fallHurtAmount), this.fallHurtMax));
                }

                if (isAnvil && this.rand.nextFloat() < 0.05D + i * 0.05D)
                {
                    int j = this.blockState.getValue(BlockAnvil.DAMAGE).intValue() + 1;

                    if (j > 2)
                    {
                        this.canSetAsBlock = false;
                    }
                    else
                    {
                        this.blockState = this.blockState.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(j));
                    }
                }
            }
        }
    }

    @Override
    public void dismountRidingEntity()
    {
        Entity entity = this.getRidingEntity();

        super.dismountRidingEntity();

        // Try to find an air block beside the track to dismount to
        if (entity instanceof EntityMinecart)
        {
            BlockPos pos = new BlockPos(this);

            for (BlockPos posTmp : PositionUtils.getAdjacentPositions(pos, EnumFacing.UP, true))
            {
                if (this.getEntityWorld().isAirBlock(posTmp))
                {
                    this.setPosition(posTmp.getX() + 0.5D, this.posY, posTmp.getZ() + 0.5D);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        Block block = null;
        int meta = nbt.getByte("Data") & 0xF;

        if (nbt.hasKey("Block", Constants.NBT.TAG_STRING))
        {
            block = Block.getBlockFromName(nbt.getString("Block"));
            this.blockState = block.getStateFromMeta(meta);
        }

        this.fallTime = nbt.getInteger("Time");

        if (nbt.hasKey("HurtEntities", Constants.NBT.TAG_BYTE))
        {
            this.hurtEntities = nbt.getBoolean("HurtEntities");
            this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
            this.fallHurtMax = nbt.getInteger("FallHurtMax");
        }
        else if (block == Blocks.ANVIL)
        {
            this.hurtEntities = true;
        }

        if (nbt.hasKey("DropItem", 99))
        {
            this.shouldDropItem = nbt.getBoolean("DropItem");
        }

        if (nbt.hasKey("TileEntityData", 10))
        {
            this.tileEntityData = nbt.getCompoundTag("TileEntityData");
        }

        if (block == null || block.getDefaultState().getMaterial() == Material.AIR)
        {
            this.blockState = Blocks.SAND.getDefaultState();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        Block block = this.blockState != null ? this.blockState.getBlock() : Blocks.AIR;
        ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
        compound.setString("Block", rl == null ? "" : rl.toString());
        compound.setByte("Data", this.blockState != null ? (byte) block.getMetaFromState(this.blockState) : 0);
        compound.setInteger("Time", this.fallTime);
        compound.setBoolean("DropItem", this.shouldDropItem);
        compound.setBoolean("HurtEntities", this.hurtEntities);
        compound.setFloat("FallHurtAmount", this.fallHurtAmount);
        compound.setInteger("FallHurtMax", this.fallHurtMax);

        if (this.tileEntityData != null)
        {
            compound.setTag("TileEntityData", this.tileEntityData);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin()
    {
        return this.getDataManager().get(ORIGIN);
    }
}
