package fi.dy.masa.enderutilities.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class EntityChair extends Entity
{
    private static final DataParameter<Float> WIDTH  = EntityDataManager.<Float>createKey(EntityChair.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityChair.class, DataSerializers.FLOAT);

    public EntityChair(World worldIn)
    {
        super(worldIn);

        this.setSize(0.5f, 0.75f);
        this.setNoGravity(true);
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit()
    {
        this.getDataManager().register(WIDTH,  Float.valueOf(0f));
        this.getDataManager().register(HEIGHT, Float.valueOf(0f));
    }

    @Override
    protected void setSize(float width, float height)
    {
        this.setWidth(width);
        this.setHeight(height);

        this.width = width;
        this.height = height;
        double r = this.width / 2D;
        this.setEntityBoundingBox(new AxisAlignedBB(
                this.posX - r, this.posY, this.posZ - r,
                this.posX + r, this.posY + this.height, this.posZ + r));
    }

    public float getWidth()
    {
        return this.getDataManager().get(WIDTH).floatValue();
    }

    public float getHeight()
    {
        return this.getDataManager().get(HEIGHT).floatValue();
    }

    public void setWidth(float width)
    {
        this.width = width;
        this.getDataManager().set(WIDTH,  Float.valueOf(this.width));
        this.getDataManager().setDirty(WIDTH);
    }

    public void setHeight(float height)
    {
        this.height = height;
        this.getDataManager().set(HEIGHT, Float.valueOf(this.height));
        this.getDataManager().setDirty(HEIGHT);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (WIDTH.equals(key) || HEIGHT.equals(key))
        {
            this.setSize(this.getWidth(), this.getHeight());
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag)
    {
        if (tag.hasKey("Width", Constants.NBT.TAG_FLOAT) && tag.hasKey("Height", Constants.NBT.TAG_FLOAT))
        {
            this.setSize(tag.getFloat("Width"), tag.getFloat("Height"));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag)
    {
        tag.setFloat("Width", this.width);
        tag.setFloat("Height", this.height);
    }

    @Override
    public boolean canBeAttackedWithItem()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        // Needed for the mc.objectMouseOver ray trace to accept this entity
        return true;
    }

    @Override
    public boolean hitByEntity(Entity entityIn)
    {
        return super.hitByEntity(entityIn);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger)
    {
        return this.getPassengers().size() < 4;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn)
    {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, ItemStack stack, EnumHand hand)
    {
        if (player.getEntityWorld().isRemote == false && this.getPassengers().contains(player) == false)
        {
            player.startRiding(this);
            return true;
        }

        return false;
    }
}
