package fi.dy.masa.enderutilities.tileentity;

import java.util.List;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockPosDistance;
import fi.dy.masa.enderutilities.util.PositionUtils;

public class TileEntityEnderElevator extends TileEntityEnderUtilities
{
    public static Predicate<TileEntity> isMatchingElevator(final EnumDyeColor color)
    {
        return new Predicate<TileEntity> ()
        {
            public boolean apply(TileEntity te)
            {
                return te instanceof TileEntityEnderElevator && ((TileEntityEnderElevator)te).getColor() == color;
            }
        };
    }

    private boolean redstoneState;
    private EnumDyeColor color;

    public TileEntityEnderElevator()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_ELEVATOR);
        this.color = EnumDyeColor.WHITE;
    }

    public EnumDyeColor getColor()
    {
        return this.color;
    }

    public void setColor(EnumDyeColor color)
    {
        this.color = color;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.redstoneState = nbt.getBoolean("Redstone");
        this.color = EnumDyeColor.byMetadata(nbt.getByte("Color"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("Redstone", this.redstoneState);
        nbt.setByte("Color", (byte)this.color.getMetadata());
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("c", (byte)this.color.getMetadata());

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);

        NBTTagCompound nbt = packet.getNbtCompound();
        this.color = EnumDyeColor.byMetadata(nbt.getByte("c"));
    }

    public void onNeighbourChange()
    {
        boolean redstone = this.getWorld().isBlockPowered(this.getPos());

        if (redstone != this.redstoneState && redstone == true)
        {
            this.activateByRedstone(this.getWorld().isBlockIndirectlyGettingPowered(this.getPos()) >= 8);
        }
    }

    public void activateByRedstone(boolean goingUp)
    {
        List<Entity> entities = this.getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().up()));

        for (Entity entity : entities)
        {
            this.activateForEntity(entity, goingUp);
        }
    }

    public void activateForEntity(Entity entity, boolean goingUp)
    {
        BlockPos targetPos = this.getMoveVector(goingUp);

        if (targetPos != null)
        {
            entity.setPositionAndUpdate(entity.posX + targetPos.getX(), entity.posY + targetPos.getY(), entity.posZ + targetPos.getZ());
            this.getWorld().playSound(null, entity.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.3f, 1.8f);
        }
    }

    private BlockPos getMoveVector(boolean goingUp)
    {
        BlockPos center = goingUp ? this.getPos().up() : this.getPos().down();
        int rangeHorizontal = 8;
        int rangeVerticalUp = goingUp ? 64 : 0;
        int rangeVerticalDown = goingUp ? 0 : 64;

        List<BlockPosDistance> elevators = PositionUtils.getTileEntityPositions(this.getWorld(), center,
                rangeHorizontal, rangeVerticalUp, rangeVerticalDown, isMatchingElevator(this.color));

        if (elevators.size() > 0)
        {
            return elevators.get(0).pos.subtract(this.getPos());
        }

        return null;
    }
}
